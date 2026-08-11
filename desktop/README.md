# CompraFlow Desktop

Cliente Electron para executar o CompraFlow como aplicativo Windows autônomo.

## Arquitetura

```text
Renderer (HTML/CSS/JS)
      │ contextBridge / IPC
      ▼
Electron Main
      │ HTTP somente em 127.0.0.1
      ▼
Spring Boot
      ▼
H2 persistente no userData
```

O renderer não recebe `nodeIntegration`. Toda chamada à API passa pelo `preload.js` e pelo processo principal.

## Desenvolvimento local

```powershell
mvn clean package
cd desktop
npm install
npm start
```

Em desenvolvimento, o app usa `JAVA_HOME` quando disponível e procura o backend em `../target/compraflow-1.0.0.jar`.

## Release Windows

O workflow `.github/workflows/desktop-release.yml` executa os testes Maven, empacota o backend, baixa JRE 21 x64 do Eclipse Temurin, gera Installer NSIS e versão Portable e publica/atualiza a pre-release `v1.1.0-desktop`.

O usuário final não precisa instalar Java separadamente.
