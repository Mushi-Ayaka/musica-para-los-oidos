// Enhanced mock library dataset for "Música para los Oídos"

export const INITIAL_SONGS = [
  {
    id: 1,
    title: "Nebulosa Estelar",
    artist: "Synthwave Sunset",
    album: "Horizontes de Neón",
    duration: 215,
    genre: "Electrónica",
    mood: "Energía & Enfoque",
    cover: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
    audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
    folder: "/Music/Electronic/2026",
    plays: 1420,
    addedAt: "2026-10-15",
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
    folder: "/Music/Acoustic/Live",
    plays: 890,
    addedAt: "2026-10-18",
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
    folder: "/Music/LoFi_Beats",
    plays: 2310,
    addedAt: "2026-10-20",
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
    folder: "/Music/Ambient_Focus",
    plays: 640,
    addedAt: "2026-10-22",
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
    folder: "/Music/Electronic/2026",
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
    artist: "Sol & Cuerdas",
    album: "Madera y Sal",
    duration: 205,
    genre: "Acústico",
    mood: "Atardecer",
    cover: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
    audioUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
    folder: "/Music/Acoustic/Live",
    plays: 520,
    addedAt: "2026-10-26",
    lyrics: [
      "Rayos dorados entre los tejados...",
      "Un arpegio suave en los días olvidados...",
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
    description: "Tus pistas más escuchadas y guardadas"
  },
  {
    id: "pl-2",
    name: "Noches de Código & Chill",
    songIds: [3, 4, 1],
    cover: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
    description: "Frecuencias perfectas para programar sin distracciones"
  },
  {
    id: "pl-3",
    name: "Energía & Gimnasio",
    songIds: [1, 5],
    cover: "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=600&auto=format&fit=crop&q=80",
    description: "Ritmos rápidos y bajos profundos para motivarte"
  }
];

export const FOLDERS = [
  { path: "/Music/Electronic/2026", trackCount: 2, sizeMb: 24.5 },
  { path: "/Music/Acoustic/Live", trackCount: 2, sizeMb: 19.8 },
  { path: "/Music/LoFi_Beats", trackCount: 1, sizeMb: 11.2 },
  { path: "/Music/Ambient_Focus", trackCount: 1, sizeMb: 9.4 }
];

export const THEME_PRESETS = [
  {
    id: "amoled",
    name: "Deep AMOLED",
    description: "Negro puro absoluto para pantallas OLED, acentos cian y violeta",
    vars: {
      bgMain: "#000000",
      bgSurface: "#09090b",
      bgElevated: "#121215",
      accentPrimary: "#06b6d4",
      accentSecondary: "#8b5cf6",
      accentGradient: "linear-gradient(135deg, #06b6d4 0%, #8b5cf6 100%)",
      textColor: "#ffffff",
      textMuted: "#a1a1aa",
      radius: "24px",
      cardStyle: "glass",
      fontFamily: "'Plus Jakarta Sans', sans-serif"
    }
  },
  {
    id: "cyberpunk",
    name: "Cyberpunk Neon",
    description: "Vibraciones futuristas con fucsia neón, cian eléctrico y resplandor",
    vars: {
      bgMain: "#080612",
      bgSurface: "#130d29",
      bgElevated: "#1e1442",
      accentPrimary: "#ec4899",
      accentSecondary: "#3b82f6",
      accentGradient: "linear-gradient(135deg, #ec4899 0%, #06b6d4 100%)",
      textColor: "#fdf4ff",
      textMuted: "#d8b4fe",
      radius: "16px",
      cardStyle: "glow",
      fontFamily: "'Outfit', sans-serif"
    }
  },
  {
    id: "retro-orange",
    name: "Sunset Vinyl",
    description: "Tonos cálidos ámbar y mandarina inspirados en tocadiscos vintage",
    vars: {
      bgMain: "#120e0a",
      bgSurface: "#1d1712",
      bgElevated: "#2a2119",
      accentPrimary: "#f97316",
      accentSecondary: "#eab308",
      accentGradient: "linear-gradient(135deg, #f97316 0%, #fbbf24 100%)",
      textColor: "#fffbeb",
      textMuted: "#d6d3d1",
      radius: "20px",
      cardStyle: "warm",
      fontFamily: "'Plus Jakarta Sans', sans-serif"
    }
  },
  {
    id: "emerald",
    name: "Emerald Forest",
    description: "Sensación natural zen, tonos menta, esmeralda y musgo relajante",
    vars: {
      bgMain: "#05110d",
      bgSurface: "#0c1f19",
      bgElevated: "#133027",
      accentPrimary: "#10b981",
      accentSecondary: "#14b8a6",
      accentGradient: "linear-gradient(135deg, #10b981 0%, #06b6d4 100%)",
      textColor: "#f0fdf4",
      textMuted: "#86efac",
      radius: "28px",
      cardStyle: "nature",
      fontFamily: "'Plus Jakarta Sans', sans-serif"
    }
  },
  {
    id: "monochrome",
    name: "Minimalist Mono",
    description: "Diseño minimalista editorial en escala de grises y blanco puro",
    vars: {
      bgMain: "#09090b",
      bgSurface: "#18181b",
      bgElevated: "#27272a",
      accentPrimary: "#f4f4f5",
      accentSecondary: "#a1a1aa",
      accentGradient: "linear-gradient(135deg, #ffffff 0%, #a1a1aa 100%)",
      textColor: "#fafafa",
      textMuted: "#71717a",
      radius: "12px",
      cardStyle: "flat",
      fontFamily: "system-ui, sans-serif"
    }
  }
];
