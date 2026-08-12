const fs = require('node:fs');
const path = require('node:path');

const root = __dirname;
const app = fs.readFileSync(path.join(root, 'renderer', 'app.js'), 'utf8');
const preload = fs.readFileSync(path.join(root, 'preload.js'), 'utf8');
const main = fs.readFileSync(path.join(root, 'main.js'), 'utf8');
const index = fs.readFileSync(path.join(root, 'renderer', 'index.html'), 'utf8');

const requiredAppTokens = [
  'editRequestButton',
  'deleteRequestButton',
  'printRequestButton',
  'savePdfButton',
  "api('PUT', `/api/solicitacoes/${id}`",
  "api('DELETE', `/api/solicitacoes/${id}`",
  "request.status === 'RASCUNHO'",
  'window.compraflow.document.print',
  'window.compraflow.document.savePdf'
];

const requiredPreloadTokens = ['document:print', 'document:save-pdf'];
const requiredMainTokens = ["ipcMain.handle('document:print'", "ipcMain.handle('document:save-pdf'"];

for (const token of requiredAppTokens) {
  if (!app.includes(token)) throw new Error(`Contrato do renderer ausente: ${token}`);
}
for (const token of requiredPreloadTokens) {
  if (!preload.includes(token)) throw new Error(`Contrato do preload ausente: ${token}`);
}
for (const token of requiredMainTokens) {
  if (!main.includes(token)) throw new Error(`Contrato do main ausente: ${token}`);
}
if (index.includes('enhancements.js')) {
  throw new Error('index.html ainda depende de enhancements.js; os recursos devem estar integrados ao app.js principal.');
}

console.log('DESKTOP_CONTRACT_PASS');
