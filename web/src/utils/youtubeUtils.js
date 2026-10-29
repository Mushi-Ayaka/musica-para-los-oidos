// Utilidades para YouTube y Descargas
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
export function getYouTubeThumbnail(videoId, quality = 'hqdefault') {
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

// Catalogo de videos populares simulados para sugerencias y busquedas
export const TRENDING_SEARCH_RESULTS = [
  {
    id: 'dQw4w9WgXcQ',
    title: 'Never Gonna Give You Up',
    artist: 'Rick Astley',
    channel: 'Rick Astley Official',
    duration: '3:33',
    views: '1.5B vistas',
    year: '1987',
    thumbnail: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=320&h=180&fit=crop'
  },
  {
    id: 'fJ9rUzIMcZQ',
    title: 'Bohemian Rhapsody',
    artist: 'Queen',
    channel: 'Queen Official',
    duration: '5:59',
    views: '1.7B vistas',
    year: '1975',
    thumbnail: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=320&h=180&fit=crop'
  },
  {
    id: 'kXYiU_JCYtU',
    title: 'Numb',
    artist: 'Linkin Park',
    channel: 'Linkin Park',
    duration: '3:07',
    views: '2.2B vistas',
    year: '2003',
    thumbnail: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=320&h=180&fit=crop'
  },
  {
    id: 'JGwWNGJdvx8',
    title: 'Shape of You',
    artist: 'Ed Sheeran',
    channel: 'Ed Sheeran',
    duration: '4:23',
    views: '6.1B vistas',
    year: '2017',
    thumbnail: 'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=320&h=180&fit=crop'
  },
  {
    id: 'OPf0YbXqDm0',
    title: 'Uptown Funk',
    artist: 'Mark Ronson ft. Bruno Mars',
    channel: 'Mark Ronson',
    duration: '4:30',
    views: '5.1B vistas',
    year: '2014',
    thumbnail: 'https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=320&h=180&fit=crop'
  },
  {
    id: 'YQHsXMglC9A',
    title: 'Hello',
    artist: 'Adele',
    channel: 'Adele Official',
    duration: '6:07',
    views: '3.1B vistas',
    year: '2015',
    thumbnail: 'https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=320&h=180&fit=crop'
  }
];

// Generador de resultados de busqueda simulados basados en la consulta del usuario
export function searchYouTubeSimulated(query, isOnline = true) {
  if (!query || !query.trim()) return [];

  const q = query.trim().toLowerCase();

  // Filtrar en el catalogo existente si coincide
  const filtered = TRENDING_SEARCH_RESULTS.filter(
    (item) =>
      item.title.toLowerCase().includes(q) ||
      item.artist.toLowerCase().includes(q) ||
      item.channel.toLowerCase().includes(q)
  );

  if (filtered.length > 0) {
    return filtered;
  }

  // Si no coincide directamente, generar resultados dinamicos y coherentes
  const parsed = parseYouTubeTitle(query.trim());
  const thumbs = [
    'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=320&h=180&fit=crop',
    'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=320&h=180&fit=crop',
    'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=320&h=180&fit=crop',
    'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=320&h=180&fit=crop',
    'https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=320&h=180&fit=crop'
  ];

  return [
    {
      id: `sim1_${Date.now().toString(36)}`,
      title: parsed.title,
      artist: parsed.artist !== 'Artista desconocido' ? parsed.artist : query.trim(),
      channel: `${parsed.artist !== 'Artista desconocido' ? parsed.artist : query.trim()} Oficial`,
      duration: '3:45',
      views: '12.4M vistas',
      year: new Date().getFullYear().toString(),
      thumbnail: isOnline ? thumbs[0] : null
    },
    {
      id: `sim2_${Date.now().toString(36)}`,
      title: `${parsed.title} (Audio Oficial)`,
      artist: parsed.artist !== 'Artista desconocido' ? parsed.artist : query.trim(),
      channel: `${parsed.artist !== 'Artista desconocido' ? parsed.artist : query.trim()} - Topic`,
      duration: '3:48',
      views: '5.8M vistas',
      year: new Date().getFullYear().toString(),
      thumbnail: isOnline ? thumbs[1] : null
    },
    {
      id: `sim3_${Date.now().toString(36)}`,
      title: `${parsed.title} (Remix Studio)`,
      artist: parsed.artist !== 'Artista desconocido' ? parsed.artist : query.trim(),
      channel: 'Sound Lab Music',
      duration: '4:12',
      views: '2.1M vistas',
      year: new Date().getFullYear().toString(),
      thumbnail: isOnline ? thumbs[2] : null
    },
    {
      id: `sim4_${Date.now().toString(36)}`,
      title: `${parsed.title} (En Vivo / Live Acustico)`,
      artist: parsed.artist !== 'Artista desconocido' ? parsed.artist : query.trim(),
      channel: 'Live Sessions HD',
      duration: '4:35',
      views: '940K vistas',
      year: new Date().getFullYear().toString(),
      thumbnail: isOnline ? thumbs[3] : null
    }
  ];
}

// Simular peticion de metadatos (en la app real: yt-dlp o YouTube oEmbed)
export function fetchYouTubeMetaSimulated(videoId) {
  const simulatedTitles = {
    'dQw4w9WgXcQ': 'Rick Astley - Never Gonna Give You Up',
    'fJ9rUzIMcZQ': 'Queen - Bohemian Rhapsody',
    'kXYiU_JCYtU': 'Linkin Park - Numb',
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
