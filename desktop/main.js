const { app, BrowserWindow, ipcMain, shell, dialog } = require('electron');
const { spawn } = require('node:child_process');
const fs = require('node:fs');
const net = require('node:net');
const path = require('node:path');

let mainWindow;
let backendProcess;
let backendBaseUrl;
let backendLogFile;
let isQuitting = false;

function getFreePort() {
  return new Promise((resolve, reject) => {
    const server = net.createServer();
    server.unref();
    server.on('error', reject);
    server.listen(0, '127.0.0.1', () => {
      const address = server.address();
      const port = address.port;
      server.close(() => resolve(port));
    });
  });
}

function getJavaExecutable() {
  if (app.isPackaged) return path.join(process.resourcesPath, 'jre', 'bin', 'java.exe');
  if (process.env.JAVA_HOME) {
    const exe = process.platform === 'win32' ? 'java.exe' : 'java';
    return path.join(process.env.JAVA_HOME, 'bin', exe);
  }
  return 'java';
}

function getBackendJar() {
  if (app.isPackaged) return path.join(process.resourcesPath, 'backend', 'compraflow.jar');
  return path.resolve(__dirname, '..', 'target', 'compraflow-1.0.0.jar');
}

function normalizeH2Path(filePath) { return filePath.replace(/\\/g, '/'); }

async function startBackend() {
  const port = await getFreePort();
  backendBaseUrl = `http://127.0.0.1:${port}`;
  const dataDir = path.join(app.getPath('userData'), 'data');
  const logDir = path.join(app.getPath('userData'), 'logs');
  fs.mkdirSync(dataDir, { recursive: true });
  fs.mkdirSync(logDir, { recursive: true });
  backendLogFile = path.join(logDir, 'compraflow-backend.log');
  const dbPath = normalizeH2Path(path.join(dataDir, 'compraflow'));
  const java = getJavaExecutable();
  const jar = getBackendJar();
  if (app.isPackaged && !fs.existsSync(java)) throw new Error(`Runtime Java não encontrado: ${java}`);
  if (!fs.existsSync(jar)) throw new Error(`Backend CompraFlow não encontrado: ${jar}`);

  const args = [
    '-jar', jar,
    `--server.port=${port}`,
    '--server.address=127.0.0.1',
    `--spring.datasource.url=jdbc:h2:file:${dbPath};MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE`,
    '--spring.jpa.hibernate.ddl-auto=update',
    '--spring.h2.console.enabled=false',
    `--logging.file.name=${normalizeH2Path(backendLogFile)}`
  ];

  backendProcess = spawn(java, args, { windowsHide: true, stdio: ['ignore', 'pipe', 'pipe'] });
  const appendLog = (chunk) => { try { fs.appendFileSync(backendLogFile, chunk); } catch (_) {} };
  backendProcess.stdout.on('data', appendLog);
  backendProcess.stderr.on('data', appendLog);
  backendProcess.on('exit', (code) => {
    backendProcess = undefined;
    if (!isQuitting && mainWindow && !mainWindow.isDestroyed()) mainWindow.webContents.send('backend:stopped', { code });
  });
  await waitForBackend();
}

async function waitForBackend() {
  const started = Date.now();
  let lastError;
  while (Date.now() - started < 45000) {
    try {
      const response = await fetch(`${backendBaseUrl}/api/fornecedores`);
      if (response.ok) return;
      lastError = new Error(`HTTP ${response.status}`);
    } catch (error) { lastError = error; }
    await new Promise((resolve) => setTimeout(resolve, 500));
  }
  throw new Error(`Backend não iniciou em 45s. ${lastError?.message || ''}`.trim());
}

function stopBackend() {
  if (!backendProcess) return;
  try { backendProcess.kill(); } catch (_) {}
  backendProcess = undefined;
}

async function apiRequest({ method = 'GET', endpoint, body }) {
  if (!backendBaseUrl) return { ok: false, status: 503, data: { detail: 'Backend ainda não está disponível.' } };
  try {
    const options = { method, headers: { Accept: 'application/json' } };
    if (body !== undefined) {
      options.headers['Content-Type'] = 'application/json';
      options.body = JSON.stringify(body);
    }
    const response = await fetch(`${backendBaseUrl}${endpoint}`, options);
    const text = await response.text();
    let data = null;
    if (text) {
      try { data = JSON.parse(text); } catch (_) { data = { detail: text }; }
    }
    return { ok: response.ok, status: response.status, data };
  } catch (error) {
    return { ok: false, status: 503, data: { title: 'Backend indisponível', detail: error.message } };
  }
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1420,
    height: 900,
    minWidth: 1080,
    minHeight: 680,
    show: false,
    backgroundColor: '#0b1020',
    title: 'CompraFlow',
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true
    }
  });
  mainWindow.setMenuBarVisibility(false);
  mainWindow.loadFile(path.join(__dirname, 'renderer', 'index.html'));
  mainWindow.once('ready-to-show', () => mainWindow.show());
  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    if (/^https?:\/\//i.test(url)) shell.openExternal(url);
    return { action: 'deny' };
  });
}

function safeReportFilename(value) {
  return String(value || 'solicitacao').replace(/[^a-zA-Z0-9._-]+/g, '-').replace(/^-+|-+$/g, '') || 'solicitacao';
}

async function createReportWindow(html) {
  if (typeof html !== 'string' || html.length > 2_000_000) throw new Error('Documento inválido para impressão.');
  if (/<script\b/i.test(html)) throw new Error('Documento recusado por segurança.');
  const dir = path.join(app.getPath('temp'), 'compraflow-reports');
  fs.mkdirSync(dir, { recursive: true });
  const file = path.join(dir, `report-${Date.now()}-${Math.random().toString(16).slice(2)}.html`);
  fs.writeFileSync(file, html, 'utf8');
  const win = new BrowserWindow({
    show: false,
    webPreferences: { contextIsolation: true, nodeIntegration: false, sandbox: true }
  });
  await win.loadFile(file);
  return { win, file };
}

async function printDocument({ html }) {
  const { win, file } = await createReportWindow(html);
  return new Promise((resolve) => {
    win.webContents.print({ silent: false, printBackground: true }, (success, failureReason) => {
      try { win.destroy(); } catch (_) {}
      try { fs.unlinkSync(file); } catch (_) {}
      resolve({ success, failureReason: failureReason || null });
    });
  });
}

async function savePdfDocument({ html, filename }) {
  const { win, file } = await createReportWindow(html);
  try {
    const suggested = path.join(app.getPath('documents'), `${safeReportFilename(filename)}.pdf`);
    const choice = await dialog.showSaveDialog(mainWindow, {
      title: 'Salvar solicitação em PDF',
      defaultPath: suggested,
      buttonLabel: 'Salvar PDF',
      filters: [{ name: 'Documento PDF', extensions: ['pdf'] }]
    });
    if (choice.canceled || !choice.filePath) return { saved: false, canceled: true };
    const data = await win.webContents.printToPDF({ printBackground: true, pageSize: 'A4' });
    fs.writeFileSync(choice.filePath, data);
    return { saved: true, path: choice.filePath };
  } finally {
    try { win.destroy(); } catch (_) {}
    try { fs.unlinkSync(file); } catch (_) {}
  }
}

ipcMain.handle('api:request', (_event, payload) => apiRequest(payload));
ipcMain.handle('backend:status', () => ({
  running: Boolean(backendProcess), baseUrl: backendBaseUrl, logFile: backendLogFile,
  packaged: app.isPackaged, version: app.getVersion()
}));
ipcMain.handle('backend:open-swagger', () => shell.openExternal(`${backendBaseUrl}/swagger-ui.html`));
ipcMain.handle('backend:open-logs', async () => {
  if (!backendLogFile || !fs.existsSync(backendLogFile)) return false;
  return shell.openPath(backendLogFile);
});
ipcMain.handle('app:show-data-folder', () => shell.openPath(app.getPath('userData')));
ipcMain.handle('document:print', (_event, payload) => printDocument(payload));
ipcMain.handle('document:save-pdf', (_event, payload) => savePdfDocument(payload));

app.whenReady().then(async () => {
  try {
    await startBackend();
    createWindow();
  } catch (error) {
    dialog.showErrorBox('CompraFlow não conseguiu iniciar', `${error.message}\n\nVerifique os logs em: ${backendLogFile || app.getPath('userData')}`);
    app.quit();
  }
});

app.on('window-all-closed', () => { if (process.platform !== 'darwin') app.quit(); });
app.on('before-quit', () => { isQuitting = true; stopBackend(); });
app.on('activate', () => { if (BrowserWindow.getAllWindows().length === 0 && backendBaseUrl) createWindow(); });
