// Servicio de Base de Datos, Copias de Seguridad y Sincronizacion Cloud (Fase 2 y 3)
// Proyecto: Musica para los Oidos

const DB_NAME = 'MusicaParaLosOidosDB';
const DB_VERSION = 1;

// Inicializar base de datos IndexedDB en el navegador
export function openAppDatabase() {
  return new Promise((resolve, reject) => {
    if (typeof window === 'undefined' || !window.indexedDB) {
      resolve(null);
      return;
    }

    const request = window.indexedDB.open(DB_NAME, DB_VERSION);

    request.onupgradeneeded = (event) => {
      const db = event.target.result;

      // Tabla de ubicaciones fisicas de archivos (Zero-copy)
      if (!db.objectStoreNames.contains('track_locations')) {
        const store = db.createObjectStore('track_locations', { keyPath: 'id', autoIncrement: true });
        store.createIndex('absolute_path', 'absolute_path', { unique: true });
        store.createIndex('folder_id', 'folder_id', { unique: false });
      }

      // Tabla de pistas y metadatos
      if (!db.objectStoreNames.contains('tracks')) {
        const store = db.createObjectStore('tracks', { keyPath: 'id', autoIncrement: true });
        store.createIndex('title', 'title', { unique: false });
        store.createIndex('artist', 'artist', { unique: false });
        store.createIndex('album', 'album', { unique: false });
      }

      // Tabla de listas de reproduccion
      if (!db.objectStoreNames.contains('playlists')) {
        db.createObjectStore('playlists', { keyPath: 'id' });
      }

      // Tabla de letras sincronizadas
      if (!db.objectStoreNames.contains('lyrics_tracking')) {
        const store = db.createObjectStore('lyrics_tracking', { keyPath: 'id', autoIncrement: true });
        store.createIndex('track_id', 'track_id', { unique: true });
      }

      // Historial de descargas
      if (!db.objectStoreNames.contains('download_history')) {
        db.createObjectStore('download_history', { keyPath: 'id' });
      }

      // Presets de ecualizador y estilos
      if (!db.objectStoreNames.contains('presets')) {
        db.createObjectStore('presets', { keyPath: 'id' });
      }

      // Configuraciones generales
      if (!db.objectStoreNames.contains('app_settings')) {
        db.createObjectStore('app_settings', { keyPath: 'key' });
      }
    };

    request.onsuccess = (event) => {
      resolve(event.target.result);
    };

    request.onerror = (event) => {
      console.warn('No se pudo inicializar IndexedDB:', event.target.error);
      resolve(null);
    };
  });
}

// Generar paquete completo de copia de seguridad (Fase 3)
export function createBackupPayload(appState) {
  const timestamp = new Date().toISOString();
  return {
    app: 'Musica para los Oidos',
    version: '3.0.0',
    schemaVersion: 2,
    createdAt: timestamp,
    data: {
      playlists: appState.playlists || [],
      likedSongIds: appState.likedSongIds || [],
      customStylePresets: appState.customStylePresets || [],
      eqPresets: appState.eqPresets || [],
      themeVars: appState.themeVars || {},
      audioSettings: {
        volumePercent: appState.volumePercent || 100,
        outputProfile: appState.outputProfile || 'studio_monitor',
        playbackSpeed: appState.playbackSpeed || 1.0,
        eqEnabled: appState.eqEnabled ?? true,
        selectedEqPresetId: appState.selectedEqPresetId || 'flat'
      },
      downloadHistory: appState.downloadHistory || [],
      monitoredFolders: appState.folders || [],
      trackSortOrder: appState.trackSortOrder || 'az',
      trackFolderFilter: appState.trackFolderFilter || 'all',
      cloudSyncMeta: {
        lastExportedAt: timestamp,
        provider: 'Google Drive',
        accountEmail: 'usuario.musica@gmail.com'
      }
    }
  };
}

// Descargar archivo de respaldo en el dispositivo/computadora
export function triggerBackupDownload(backupPayload) {
  const jsonString = JSON.stringify(backupPayload, null, 2);
  const blob = new Blob([jsonString], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const now = new Date();
  const dateStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
  
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = `musica_para_los_oidos_backup_${dateStr}.json`;
  document.body.appendChild(anchor);
  anchor.click();
  document.body.removeChild(anchor);
  URL.revokeObjectURL(url);
}

// Validar y leer un archivo de copia de seguridad
export function parseAndValidateBackup(jsonString) {
  try {
    const parsed = JSON.parse(jsonString);
    if (!parsed || typeof parsed !== 'object') {
      return { success: false, error: 'El archivo no contiene un formato JSON valido.' };
    }

    if (parsed.app !== 'Musica para los Oidos' && !parsed.data) {
      return { success: false, error: 'El archivo no corresponde a una copia de seguridad valida de Musica para los Oidos.' };
    }

    const payload = parsed.data || parsed;
    return {
      success: true,
      payload: {
        playlists: Array.isArray(payload.playlists) ? payload.playlists : [],
        likedSongIds: Array.isArray(payload.likedSongIds) ? payload.likedSongIds : [],
        customStylePresets: Array.isArray(payload.customStylePresets) ? payload.customStylePresets : [],
        eqPresets: Array.isArray(payload.eqPresets) ? payload.eqPresets : [],
        themeVars: payload.themeVars && typeof payload.themeVars === 'object' ? payload.themeVars : null,
        audioSettings: payload.audioSettings || {},
        downloadHistory: Array.isArray(payload.downloadHistory) ? payload.downloadHistory : [],
        monitoredFolders: Array.isArray(payload.monitoredFolders) ? payload.monitoredFolders : [],
        trackSortOrder: payload.trackSortOrder || 'az',
        trackFolderFilter: payload.trackFolderFilter || 'all'
      },
      createdAt: parsed.createdAt || new Date().toISOString()
    };
  } catch (err) {
    return { success: false, error: `Error al procesar el archivo: ${err.message}` };
  }
}

// Simular subida a Google Drive en la nube
export function uploadToGoogleDriveSimulated(backupPayload) {
  return new Promise((resolve) => {
    setTimeout(() => {
      const driveRecord = {
        fileId: `gdrive_${Date.now()}`,
        fileName: 'musica_para_los_oidos_cloud_backup.json',
        sizeBytes: JSON.stringify(backupPayload).length,
        syncedAt: new Date().toISOString(),
        accountEmail: 'usuario.musica@gmail.com',
        storageUsedPct: 24
      };
      // Guardar en localStorage como persistencia en el navegador
      try {
        localStorage.setItem('mplo_gdrive_cloud_backup', JSON.stringify({
          payload: backupPayload,
          meta: driveRecord
        }));
      } catch (e) {
        console.warn('No se pudo guardar en almacenamiento local:', e);
      }
      resolve(driveRecord);
    }, 900);
  });
}

// Simular restauracion desde Google Drive en la nube
export function restoreFromGoogleDriveSimulated() {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      try {
        const raw = localStorage.getItem('mplo_gdrive_cloud_backup');
        if (raw) {
          const parsed = JSON.parse(raw);
          resolve({
            success: true,
            payload: parsed.payload?.data || parsed.payload,
            meta: parsed.meta
          });
        } else {
          // Si no habia una previa en localStorage, generar una copia de demostracion en la nube
          resolve({
            success: true,
            isDemo: true,
            meta: {
              fileId: 'gdrive_demo_cloud_01',
              fileName: 'musica_para_los_oidos_cloud_backup.json',
              syncedAt: new Date().toISOString(),
              accountEmail: 'usuario.musica@gmail.com'
            }
          });
        }
      } catch (err) {
        reject(err);
      }
    }, 850);
  });
}
