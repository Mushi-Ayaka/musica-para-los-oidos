// Servicio de Base de Datos, Copias de Seguridad y Sincronizacion Cloud (Fase 2 y 3)
// Proyecto: Musica para los Oidos

const DB_NAME = 'MusicaParaLosOidosDB';
const DB_VERSION = 2;

// Inicializar base de datos IndexedDB en el navegador
export function openAppDatabase() {
  return new Promise((resolve) => {
    if (typeof window === 'undefined' || !window.indexedDB) {
      resolve(null);
      return;
    }

    const request = window.indexedDB.open(DB_NAME, DB_VERSION);

    request.onupgradeneeded = (event) => {
      const db = event.target.result;

      // Tabla de pistas y catalogo indexado offline
      if (!db.objectStoreNames.contains('tracks')) {
        const store = db.createObjectStore('tracks', { keyPath: 'id' });
        store.createIndex('title', 'title', { unique: false });
        store.createIndex('artist', 'artist', { unique: false });
        store.createIndex('album', 'album', { unique: false });
        store.createIndex('folder', 'folder', { unique: false });
      }

      // Tabla de ubicaciones fisicas de archivos (Zero-copy)
      if (!db.objectStoreNames.contains('track_locations')) {
        const store = db.createObjectStore('track_locations', { keyPath: 'id', autoIncrement: true });
        store.createIndex('absolute_path', 'absolute_path', { unique: true });
      }

      // Tabla de listas de reproduccion
      if (!db.objectStoreNames.contains('playlists')) {
        db.createObjectStore('playlists', { keyPath: 'id' });
      }

      // Tabla de letras sincronizadas
      if (!db.objectStoreNames.contains('lyrics_tracking')) {
        db.createObjectStore('lyrics_tracking', { keyPath: 'fileName' });
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
  const rawSongs = appState.songs || [];

  // Exportar el catalogo de pistas indexadas con sus rutas relativas
  const indexedTracks = rawSongs.map((s) => ({
    id: s.id,
    title: s.title,
    artist: s.artist,
    album: s.album,
    duration: s.duration,
    folder: s.folder || '/Musica',
    genre: s.genre || 'Desconocido',
    mood: s.mood || '',
    cover: s.cover || '',
    plays: s.plays || 0,
    addedAt: s.addedAt || timestamp.split('T')[0],
    isFavorite: (appState.likedSongIds || []).includes(s.id)
  }));

  return {
    app: 'Musica para los Oidos',
    version: '3.0.0',
    schemaVersion: 2,
    createdAt: timestamp,
    summary: {
      totalTracks: indexedTracks.length,
      totalPlaylists: (appState.playlists || []).length,
      totalLikedSongs: (appState.likedSongIds || []).length,
      totalCustomPresets: (appState.customStylePresets || []).length,
      totalEqPresets: (appState.eqPresets || []).length,
      totalLrcFiles: Object.keys(appState.lrcFilesMap || {}).length,
      totalDownloadsRecorded: (appState.downloadHistory || []).length
    },
    data: {
      // 1. Catalogo de canciones offline indexadas
      tracks: indexedTracks,

      // 2. Mapa completo de archivos de letras .lrc
      lrcFilesMap: appState.lrcFilesMap || {},

      // 3. Listas de reproduccion y sus ordenes de pistas
      playlists: appState.playlists || [],

      // 4. Canciones favoritas (IDs y titulos para re-vinculacion en nuevo telefono)
      likedSongIds: appState.likedSongIds || [],
      favoriteTitles: indexedTracks.filter((t) => t.isFavorite).map((t) => ({ title: t.title, artist: t.artist })),

      // 5. Presets de ecualizador y audio
      eqPresets: appState.eqPresets || [],
      audioSettings: {
        volumePercent: appState.volumePercent || 100,
        outputProfile: appState.outputProfile || 'studio_monitor',
        playbackSpeed: appState.playbackSpeed || 1.0,
        eqEnabled: appState.eqEnabled ?? true,
        selectedEqPresetId: appState.selectedEqPresetId || 'flat'
      },

      // 6. Presets de estilo visual, tema y fondo de pantalla
      customStylePresets: appState.customStylePresets || [],
      themeVars: appState.themeVars || {},

      // 7. Directorios y filtros de la biblioteca
      monitoredFolders: appState.folders || [],
      trackSortOrder: appState.trackSortOrder || 'az',
      trackFolderFilter: appState.trackFolderFilter || 'all',

      // 8. Historial de descargas
      downloadHistory: appState.downloadHistory || [],

      // 9. Metadatos de sincronizacion
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
      summary: parsed.summary || null,
      payload: {
        tracks: Array.isArray(payload.tracks) ? payload.tracks : null,
        lrcFilesMap: payload.lrcFilesMap && typeof payload.lrcFilesMap === 'object' ? payload.lrcFilesMap : null,
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
        tracksCount: backupPayload?.summary?.totalTracks || 0
      };
      try {
        localStorage.setItem('mplo_gdrive_cloud_backup', JSON.stringify({
          payload: backupPayload,
          meta: driveRecord
        }));
      } catch (e) {
        console.warn('No se pudo guardar en almacenamiento local:', e);
      }
      resolve(driveRecord);
    }, 850);
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
            summary: parsed.payload?.summary || null,
            meta: parsed.meta
          });
        } else {
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
    }, 800);
  });
}
