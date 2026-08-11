const fs = require('node:fs');
const path = require('node:path');

const required = [
  'main.js',
  'preload.js',
  path.join('renderer', 'index.html'),
  path.join('renderer', 'styles.css'),
  path.join('renderer', 'app.js')
];

for (const file of required) {
  const absolute = path.join(__dirname, file);
  if (!fs.existsSync(absolute)) {
    console.error(`Arquivo ausente: ${file}`);
    process.exit(1);
  }
}

const html = fs.readFileSync(path.join(__dirname, 'renderer', 'index.html'), 'utf8');
if (!html.includes('Content-Security-Policy') || !html.includes('app.js')) {
  console.error('Renderer sem CSP ou entrypoint esperado.');
  process.exit(1);
}

console.log('DESKTOP_STATIC_SMOKE_PASS');
