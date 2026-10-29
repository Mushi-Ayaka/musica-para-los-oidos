// Sample dataset for "Música para los Oídos"
export const SAMPLE_SONGS = [
  {
    id: 1,
    title: "Nebulosa Estelar",
    artist: "Synthwave Sunset",
    album: "Horizontes de Neón",
    duration: 215, // seconds
    genre: "Electrónica",
    mood: "Energía & Enfoque",
    cover: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
    audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
    color: "#8b5cf6",
    lyrics: [
      "Luces de neón se encienden al caer el sol...",
      "Un pulso eléctrico recorre la ciudad...",
      "Ondas sonoras viajan en la inmensidad...",
      "Música para los oídos que despiertan la verdad.",
      "Sigue el compás, siente la gravedad desvanecer.",
      "El eco del mañana comienza hoy."
    ]
  },
  {
    id: 2,
    title: "Brisa del Océano",
    artist: "Luna Acústica",
    album: "Madera y Sal",
    duration: 182,
    genre: "Acústico",
    mood: "Relax & Calma",
    cover: "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&auto=format&fit=crop&q=80",
    audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
    color: "#06b6d4",
    lyrics: [
      "El sonido de las olas susurra tu nombre...",
      "Cuerdas de nylon bajo el cielo abierto...",
      "La marea se lleva cualquier preocupación...",
      "Paz en cada acorde, un refugio en la canción."
    ]
  },
  {
    id: 3,
    title: "Latidos de Medianoche",
    artist: "Kaizen Beats",
    album: "Ciudad de Cristal",
    duration: 240,
    genre: "Lo-Fi",
    mood: "Estudio & Café",
    cover: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
    audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
    color: "#ec4899",
    lyrics: [
      "Taza caliente y la lluvia en el cristal...",
      "El vinilo gira suave, todo fluye natural...",
      "Ideas que toman forma en la noche sin final...",
      "Música para los oídos, medicina sensorial."
    ]
  },
  {
    id: 4,
    title: "Ecos en la Niebla",
    artist: "Aurora Sonora",
    album: "Frecuencias Puras",
    duration: 198,
    genre: "Relajación",
    mood: "Meditación",
    cover: "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=600&auto=format&fit=crop&q=80",
    audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
    color: "#10b981",
    lyrics: [
      "Respira hondo... suelta la tensión.",
      "Armonías flotantes envuelven tu atención.",
      "El bosque respira a tu alrededor.",
      "Silencio y notas en perfecta unión."
    ]
  },
  {
    id: 5,
    title: "Viaje Astral",
    artist: "Nova Quartet",
    album: "Cuentos Eléctricos",
    duration: 228,
    genre: "Electrónica",
    mood: "Viajes & Creatividad",
    cover: "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80",
    audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
    color: "#f59e0b",
    lyrics: [
      "Despegando hacia constelaciones lejanas...",
      "Sintetizadores que abren nuevas ventanas.",
      "Cada nota un pulso en el infinito.",
      "Todo lo que sientes se vuelve un rito."
    ]
  }
];

export const CATEGORIES = [
  { id: "all", label: "Todas" },
  { id: "Electrónica", label: "⚡ Electrónica" },
  { id: "Lo-Fi", label: "☕ Lo-Fi" },
  { id: "Acústico", label: "🎸 Acústico" },
  { id: "Relajación", label: "🌿 Relajación" },
  { id: "favorites", label: "❤️ Favoritas" },
];

export const APP_CONCEPTS = [
  {
    id: "aura",
    title: "Concepto 1: Aura Stream (Estilo Spotify / Apple Music)",
    badge: "Pop & Mainstream",
    description: "Diseño cinematográfico con portadas grandes, visualizador de ecualizador, listas algorítmicas, perfiles de artistas y letras sincronizadas.",
    color: "#a855f7",
    highlights: ["Reproductor flotante", "Letras en tiempo real", "Modo fiesta y cola"]
  },
  {
    id: "soundscape",
    title: "Concepto 2: Oasis Sonoro (Música Inmersiva & Bienestar)",
    badge: "Salud Mental & Enfoque",
    description: "Pensada para el nombre 'Música para los Oídos': mezcla de frecuencias binaurales, sonidos de lluvia/bosque combinados con música ambiental, temporizador de sueño y visuales hipnóticos.",
    color: "#06b6d4",
    highlights: ["Mezclador de ambientes", "Temporizador de apagado", "Modo respiración"]
  },
  {
    id: "indie",
    title: "Concepto 3: Escena Libre (Comunidad & Descubrimiento)",
    badge: "Artistas Independientes",
    description: "Espacio para compartir música original, subir canciones directamente, dejar comentarios con timestamp en la onda de sonido y dar propinas a creadores.",
    color: "#f43f5e",
    highlights: ["Comentarios en la onda", "Perfil de creador", "Radio comunitaria"]
  }
];
