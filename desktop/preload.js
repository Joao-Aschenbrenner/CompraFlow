const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('compraflow', {
  api: {
    request: (method, endpoint, body) => ipcRenderer.invoke('api:request', { method, endpoint, body })
  },
  backend: {
    status: () => ipcRenderer.invoke('backend:status'),
    openSwagger: () => ipcRenderer.invoke('backend:open-swagger'),
    openLogs: () => ipcRenderer.invoke('backend:open-logs'),
    onStopped: (callback) => ipcRenderer.on('backend:stopped', (_event, payload) => callback(payload))
  },
  app: {
    showDataFolder: () => ipcRenderer.invoke('app:show-data-folder')
  }
});
