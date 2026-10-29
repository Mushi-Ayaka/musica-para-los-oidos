// Opciones de calidad para descarga
export const QUALITY_OPTIONS = [
  { id: 'best', label: 'Mejor calidad disponible', bitrate: null, format: 'opus/m4a' },
  { id: '320k', label: '320 kbps MP3 (Alta fidelidad)', bitrate: 320, format: 'mp3' },
  { id: '256k', label: '256 kbps MP3', bitrate: 256, format: 'mp3' },
  { id: '128k', label: '128 kbps MP3 (Estandar)', bitrate: 128, format: 'mp3' },
  { id: 'flac', label: 'FLAC (Sin perdida)', bitrate: null, format: 'flac' },
];

// Estados del proceso de descarga
export const DOWNLOAD_STATUS = {
  QUEUED: 'queued',
  FETCHING_META: 'fetching_meta',
  DOWNLOADING: 'downloading',
  COMPLETE: 'complete',
  ERROR: 'error',
  PAUSED: 'paused',
};
