// Datos iniciales de canciones y librerias para Musica para los Oidos

export const INITIAL_SONGS = [
  {
    id: 1,
    title: "Nebulosa Estelar",
    artist: "Synthwave Sunset",
    album: "Horizontes de Neon",
    duration: 215,
    genre: "Electronica",
    mood: "Energia y Enfoque",
    cover: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
    audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
    folder: "/Musica/Electronica/2026",
    plays: 1420,
    addedAt: "2026-10-15",
    lyrics: [
      "Luces de neon se encienden al caer el sol...",
      "Un pulso electrico recorre la ciudad...",
      "Ondas sonoras viajan en la inmensidad...",
      "Musica para los oidos que despiertan la verdad.",
      "Sigue el compas, siente la gravedad desvanecer.",
      "El eco del manana comienza hoy."
    ]
  },
  {
    id: 2,
    title: "Brisa del Oceano",
    artist: "Luna Acustica",
    album: "Madera y Sal",
    duration: 182,
    genre: "Acustico",
    mood: "Calma y Relajacion",
    cover: "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&auto=format&fit=crop&q=80",
    audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
    folder: "/Musica/Acustico/EnVivo",
    plays: 890,
    addedAt: "2026-10-18",
    lyrics: [
      "El sonido de las olas susurra tu nombre...",
      "Cuerdas de nylon bajo el cielo abierto...",
      "La marea se lleva cualquier preocupacion...",
      "Paz en cada acorde, un refugio en la cancion."
    ]
  },
  {
    id: 3,
    title: "Latidos de Medianoche",
    artist: "Kaizen Beats",
    album: "Ciudad de Cristal",
    duration: 240,
    genre: "Lo-Fi",
    mood: "Estudio y Cafe",
    cover: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
    audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
    folder: "/Musica/LoFi_Beats",
    plays: 2310,
    addedAt: "2026-10-20",
    lyrics: [
      "Taza caliente y la lluvia en el cristal...",
      "El vinilo gira suave, todo fluye natural...",
      "Ideas que toman forma en la noche sin final...",
      "Musica para los oidos, medicina sensorial."
    ]
  },
  {
    id: 4,
    title: "Ecos en la Niebla",
    artist: "Aurora Sonora",
    album: "Frecuencias Puras",
    duration: 198,
    genre: "Relajacion",
    mood: "Meditacion",
    cover: "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=600&auto=format&fit=crop&q=80",
    audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
    folder: "/Musica/Ambiental",
    plays: 640,
    addedAt: "2026-10-22",
    lyrics: [
      "Respira hondo... suelta la tension.",
      "Armonias flotantes envuelven tu atencion.",
      "El bosque respira a tu alrededor.",
      "Silencio y notas en perfecta union."
    ]
  },
  {
    id: 5,
    title: "Viaje Astral",
    artist: "Nova Quartet",
    album: "Cuentos Electricos",
    duration: 228,
    genre: "Electronica",
    mood: "Viajes y Creatividad",
    cover: "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80",
    audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
    folder: "/Musica/Electronica/2026",
    plays: 1105,
    addedAt: "2026-10-25",
    lyrics: [
      "Despegando hacia constelaciones lejanas...",
      "Sintetizadores que abren nuevas ventanas.",
      "Cada nota un pulso en el infinito.",
      "Todo lo que sientes se vuelve un rito."
    ]
  },
  {
    id: 6,
    title: "Caminos de Cobre",
    artist: "Sol y Cuerdas",
    album: "Madera y Sal",
    duration: 205,
    genre: "Acustico",
    mood: "Atardecer",
    cover: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
    audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
    folder: "/Musica/Acustico/EnVivo",
    plays: 520,
    addedAt: "2026-10-26",
    lyrics: [
      "Rayos dorados entre los tejados...",
      "Un arpegio suave en los dias olvidados...",
      "Siente la madera vibrar en tus manos."
    ]
  }
];

export const INITIAL_PLAYLISTS = [
  {
    id: "pl-1",
    name: "Favoritos de Siempre",
    songIds: [1, 3],
    cover: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
    description: "Tus pistas mas escuchadas y guardadas"
  },
  {
    id: "pl-2",
    name: "Concentracion y Programacion",
    songIds: [3, 4, 1],
    cover: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
    description: "Frecuencias para trabajar y estudiar sin distracciones"
  },
  {
    id: "pl-3",
    name: "Entrenamiento y Ritmo",
    songIds: [1, 5],
    cover: "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=600&auto=format&fit=crop&q=80",
    description: "Ritmos rapidos y frecuencias con pegada"
  }
];

export const FOLDERS = [
  { path: "/Musica/Electronica/2026", trackCount: 2, sizeMb: 24.5 },
  { path: "/Musica/Acustico/EnVivo", trackCount: 2, sizeMb: 19.8 },
  { path: "/Musica/LoFi_Beats", trackCount: 1, sizeMb: 11.2 },
  { path: "/Musica/Ambiental", trackCount: 1, sizeMb: 9.4 }
];

export const WALLPAPER_PRESETS = [
  {
    id: "none",
    name: "Sin imagen (Fondo Solido)",
    url: ""
  },
  {
    id: "galaxy",
    name: "Nebulosa Espacial",
    url: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop&q=80"
  },
  {
    id: "wood",
    name: "Madera y Acustica",
    url: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=1200&auto=format&fit=crop&q=80"
  },
  {
    id: "abstract",
    name: "Ondas Abstractas",
    url: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200&auto=format&fit=crop&q=80"
  },
  {
    id: "ocean",
    name: "Oceano Nocturno",
    url: "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200&auto=format&fit=crop&q=80"
  }
];

export const THEME_PRESETS = [
  {
    id: "amoled",
    name: "Deep AMOLED",
    description: "Negro puro absoluto para pantallas OLED, acentos cian y violeta",
    vars: {
      bgMain: "#000000",
      bgSurface: "#0a0a0c",
      bgElevated: "#131317",
      playerBg: "#0f0f14",
      playerAccent: "#06b6d4",
      accentPrimary: "#06b6d4",
      accentSecondary: "#8b5cf6",
      accentGradient: "linear-gradient(135deg, #06b6d4 0%, #8b5cf6 100%)",
      textColor: "#ffffff",
      textMuted: "#a1a1aa",
      radius: "20px",
      fontFamily: "'Plus Jakarta Sans', sans-serif",
      bgImage: "",
      bgBlur: 16,
      bgDim: 0.8
    }
  },
  {
    id: "cyberpunk",
    name: "Cyberpunk Neon",
    description: "Estilo futurista con magenta neon y cian electrico",
    vars: {
      bgMain: "#080612",
      bgSurface: "#130d29",
      bgElevated: "#1e1442",
      playerBg: "#170f33",
      playerAccent: "#ec4899",
      accentPrimary: "#ec4899",
      accentSecondary: "#3b82f6",
      accentGradient: "linear-gradient(135deg, #ec4899 0%, #06b6d4 100%)",
      textColor: "#fdf4ff",
      textMuted: "#d8b4fe",
      radius: "16px",
      fontFamily: "'Outfit', sans-serif",
      bgImage: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1200&auto=format&fit=crop&q=80",
      bgBlur: 24,
      bgDim: 0.85
    }
  },
  {
    id: "sunset",
    name: "Sunset Vinyl",
    description: "Tonos calidos ambar y mandarina inspirados en tocadiscos clasicos",
    vars: {
      bgMain: "#100d0a",
      bgSurface: "#1c1611",
      bgElevated: "#292018",
      playerBg: "#221912",
      playerAccent: "#f97316",
      accentPrimary: "#f97316",
      accentSecondary: "#eab308",
      accentGradient: "linear-gradient(135deg, #f97316 0%, #fbbf24 100%)",
      textColor: "#fffbeb",
      textMuted: "#d6d3d1",
      radius: "20px",
      fontFamily: "'Plus Jakarta Sans', sans-serif",
      bgImage: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=1200&auto=format&fit=crop&q=80",
      bgBlur: 20,
      bgDim: 0.88
    }
  },
  {
    id: "emerald",
    name: "Emerald Forest",
    description: "Sensacion natural y balanceada, tonos menta y esmeralda",
    vars: {
      bgMain: "#05110d",
      bgSurface: "#0c1f19",
      bgElevated: "#133027",
      playerBg: "#0f261f",
      playerAccent: "#10b981",
      accentPrimary: "#10b981",
      accentSecondary: "#14b8a6",
      accentGradient: "linear-gradient(135deg, #10b981 0%, #06b6d4 100%)",
      textColor: "#f0fdf4",
      textMuted: "#86efac",
      radius: "24px",
      fontFamily: "'Plus Jakarta Sans', sans-serif",
      bgImage: "",
      bgBlur: 16,
      bgDim: 0.82
    }
  },
  {
    id: "monochrome",
    name: "Minimalist Mono",
    description: "Diseno minimalista simetrico en escala de grises y blanco puro",
    vars: {
      bgMain: "#09090b",
      bgSurface: "#141418",
      bgElevated: "#202026",
      playerBg: "#17171d",
      playerAccent: "#fafafa",
      accentPrimary: "#f4f4f5",
      accentSecondary: "#a1a1aa",
      accentGradient: "linear-gradient(135deg, #ffffff 0%, #a1a1aa 100%)",
      textColor: "#fafafa",
      textMuted: "#71717a",
      radius: "14px",
      fontFamily: "system-ui, sans-serif",
      bgImage: "",
      bgBlur: 12,
      bgDim: 0.85
    }
  }
];
