// Extraer ID del video de YouTube en distintos formatos de URL
export function extractYouTubeId(url) {
  if (!url) return null;
  const patterns = [
    /youtu\.be\/([A-Za-z0-9_-]{11})/,
    /youtube\.com\/watch\?v=([A-Za-z0-9_-]{11})/,
    /youtube\.com\/embed\/([A-Za-z0-9_-]{11})/,
    /youtube\.com\/shorts\/([A-Za-z0-9_-]{11})/,
  ];
  for (const pattern of patterns) {
    const match = url.match(pattern);
    if (match) return match[1];
  }
  if (/^[A-Za-z0-9_-]{11}$/.test(url.trim())) return url.trim();
  return null;
}

// Obtener URL de miniatura de YouTube
export function getYouTubeThumbnail(videoId, quality = 'maxresdefault') {
  return `https://img.youtube.com/vi/${videoId}/${quality}.jpg`;
}

// Parsear titulo de YouTube para extraer artista y titulo
export function parseYouTubeTitle(rawTitle) {
  if (!rawTitle) return { title: 'Pista desconocida', artist: 'Artista desconocido', album: '' };
  
  const cleaned = rawTitle
    .replace(/\(Official.*?\)/gi, '')
    .replace(/\[Official.*?\]/gi, '')
    .replace(/\(.*?Video.*?\)/gi, '')
    .replace(/\(.*?Audio.*?\)/gi, '')
    .replace(/\(.*?Lyric.*?\)/gi, '')
    .replace(/\(.*?HD.*?\)/gi, '')
    .replace(/\(.*?HQ.*?\)/gi, '')
    .trim();

  if (cleaned.includes(' - ')) {
    const parts = cleaned.split(' - ');
    return {
      artist: parts[0].trim(),
      title: parts.slice(1).join(' - ').trim(),
      album: ''
    };
  }
  return { title: cleaned, artist: 'Artista desconocido', album: '' };
}

// Simular peticion de metadatos (en la app real: yt-dlp o YouTube oEmbed)
export function fetchYouTubeMetaSimulated(videoId) {
  const simulatedTitles = {
    'dQw4w9WgXcQ': 'Rick Astley - Never Gonna Give You Up',
    'default': 'Artista Ejemplo - Titulo de Cancion Descargada'
  };
  const rawTitle = simulatedTitles[videoId] || simulatedTitles['default'];
  const meta = parseYouTubeTitle(rawTitle);
  return {
    ...meta,
    videoId,
    rawTitle,
    duration: 213,
    year: new Date().getFullYear().toString(),
    thumbnail: getYouTubeThumbnail(videoId)
  };
}
