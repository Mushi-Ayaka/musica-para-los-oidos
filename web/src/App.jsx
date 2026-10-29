import React, { useState, useRef, useEffect, useMemo } from 'react';
import {
  Play,
  Pause,
  SkipForward,
  SkipBack,
  Volume2,
  VolumeX,
  Heart,
  Music,
  Sliders,
  Palette,
  Folder,
  Disc,
  ListMusic,
  Search,
  Maximize2,
  ChevronDown,
  Sparkles,
  Plus,
  FileCode,
  Smartphone,
  Eye,
  Check,
  Zap,
  Trash2,
  Save,
  Headphones,
  SlidersHorizontal,
  Image as ImageIcon,
  ArrowUp,
  ArrowDown,
  X,
  GripVertical,
  FileText,
  Gauge,
  ScanLine,
  Upload,
  ImageOff,
  Download,
  Wifi,
  WifiOff,
  Clock,
  CheckCircle,
  AlertCircle,
  XCircle,
  RotateCcw,
  Edit3,
  Filter,
  ArrowDownAZ
} from 'lucide-react';

import {
  INITIAL_SONGS,
  INITIAL_PLAYLISTS,
  FOLDERS,
  WALLPAPER_PRESETS,
  THEME_PRESETS
} from './data/songs';
import {
  audioEngine,
  DEFAULT_EQ_PRESETS,
  OUTPUT_AUDIO_PROFILES
} from './utils/audioEqualizer';
import { synth } from './utils/audioSynthesizer';
import {
  parseLRC,
  getCurrentLrcIndex,
  findMatchingLyric,
  MOCK_DEVICE_LRC_FILES
} from './utils/lrcParser';
import { QUALITY_OPTIONS, DOWNLOAD_STATUS } from './data/downloader';
import {
  extractYouTubeId,
  getYouTubeThumbnail,
  fetchYouTubeMetaSimulated,
  parseYouTubeTitle,
  searchYouTubeSimulated,
  TRENDING_SEARCH_RESULTS
} from './utils/youtubeUtils';
import './App.css';

export default function App() {
  // LIBRERIA PRINCIPAL
  const [songs, setSongs] = useState(INITIAL_SONGS);
  const [playlists, setPlaylists] = useState(INITIAL_PLAYLISTS);
  const [folders, setFolders] = useState(FOLDERS);
  const [currentSong, setCurrentSong] = useState(INITIAL_SONGS[0]);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(INITIAL_SONGS[0].duration);
  const [likedSongIds, setLikedSongIds] = useState([1, 3]);

  // COLA / PLAYLIST EN REPRODUCCION
  const [activeQueue, setActiveQueue] = useState([...INITIAL_SONGS]);
  const [draggedQueueIndex, setDraggedQueueIndex] = useState(null);
  const [showAddTrackModal, setShowAddTrackModal] = useState(false);

  // DESLIZAMIENTO SUPERIOR DEL REPRODUCTOR (0: PORTADA | 1: LIRICA)
  const [playerTopTab, setPlayerTopTab] = useState(0);
  const [touchStartX, setTouchStartX] = useState(null);

  // SISTEMA DE LETRAS LRC / IRC / ICR
  const [lrcFilesMap, setLrcFilesMap] = useState(MOCK_DEVICE_LRC_FILES);
  const lrcFileInputRef = useRef(null);

  // IMAGEN DE FONDO PERSONALIZADA DESDE DISCO
  const wallpaperInputRef = useRef(null);

  // NAVEGACION DENTRO DEL TELEFONO
  const [phoneScreen, setPhoneScreen] = useState('home');
  const [isPlayerModalOpen, setIsPlayerModalOpen] = useState(false);
  const [libraryTab, setLibraryTab] = useState('tracks');
  const [activePlaylistId, setActivePlaylistId] = useState(null);
  const [selectedAlbumName, setSelectedAlbumName] = useState(null);
  const [newPlaylistName, setNewPlaylistName] = useState('');
  const [showNewPlaylistModal, setShowNewPlaylistModal] = useState(false);

  // FILTROS Y ORDENAMIENTO DE PISTAS (FASE 1.5)
  const [trackSortOrder, setTrackSortOrder] = useState('az');
  const [trackFolderFilter, setTrackFolderFilter] = useState('all');
  const [selectedAlphaLetter, setSelectedAlphaLetter] = useState(null);

  // CONFIGURACION DEL ESTUDIO / SIMULADOR
  const [deviceScale, setDeviceScale] = useState(0.92);
  const [isFlatDevice, setIsFlatDevice] = useState(false);
  const [sidebarTab, setSidebarTab] = useState('themes');

  // PERSONALIZACION DE LA INTERFAZ
  const [activePreset, setActivePreset] = useState(THEME_PRESETS[0].id);
  const [themeVars, setThemeVars] = useState(THEME_PRESETS[0].vars);
  const [customStylePresets, setCustomStylePresets] = useState([]);
  const [newStylePresetName, setNewStylePresetName] = useState('');

  // VELOCIDAD DE REPRODUCCION
  const [playbackSpeed, setPlaybackSpeed] = useState(1.0);

  // ECUALIZADOR Y SONIDO
  const [eqPresets, setEqPresets] = useState(DEFAULT_EQ_PRESETS);
  const [eqEnabled, setEqEnabled] = useState(true);
  const [selectedEqPresetId, setSelectedEqPresetId] = useState(DEFAULT_EQ_PRESETS[0].id);
  const [eqBands, setEqBands] = useState([...DEFAULT_EQ_PRESETS[0].gains]);
  const [bassBoost, setBassBoost] = useState(0);
  const [virtualizer, setVirtualizer] = useState(0);
  const [customPresetName, setCustomPresetName] = useState('');

  // VOLUMEN++ Y PERFIL DE SALIDA
  const [volumePercent, setVolumePercent] = useState(100);
  const [isMuted, setIsMuted] = useState(false);
  const [outputProfile, setOutputProfile] = useState('studio_monitor');

  // DESCARGADOR DE YOUTUBE (FASE 1)
  const [ytUrl, setYtUrl] = useState('');
  const [ytSearchQuery, setYtSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState(TRENDING_SEARCH_RESULTS);
  const [isSearching, setIsSearching] = useState(false);
  const [showUrlInput, setShowUrlInput] = useState(false);
  const [activeResultOptionsId, setActiveResultOptionsId] = useState(null);
  const [selectedQuality, setSelectedQuality] = useState('320k');
  const [includeLrc, setIncludeLrc] = useState(true);
  const [downloadQueue, setDownloadQueue] = useState([]);
  const [downloadHistory, setDownloadHistory] = useState([]);
  const [isOnline, setIsOnline] = useState(typeof navigator !== 'undefined' ? navigator.onLine : true);
  const [isAppVisible, setIsAppVisible] = useState(typeof document !== 'undefined' ? !document.hidden : true);
  const downloadTimersRef = useRef({});

  const audioRef = useRef(null);

  // Inicializar Web Audio Engine
  useEffect(() => {
    if (audioRef.current) {
      audioEngine.init(audioRef.current);
    }
  }, []);

  // Sincronizar velocidad de reproduccion
  useEffect(() => {
    if (audioRef.current) {
      audioRef.current.playbackRate = playbackSpeed;
    }
  }, [playbackSpeed]);

  // Sincronizar ecualizador
  useEffect(() => {
    if (eqEnabled) {
      audioEngine.setAllBands(eqBands);
      audioEngine.setBassBoost(bassBoost);
      audioEngine.setPan(virtualizer * 0.4);
    } else {
      audioEngine.setAllBands([0, 0, 0, 0, 0, 0, 0, 0, 0, 0]);
      audioEngine.setBassBoost(0);
      audioEngine.setPan(0);
    }
  }, [eqEnabled, eqBands, bassBoost, virtualizer]);

  // Sincronizar volumen++ hasta 200%
  useEffect(() => {
    const effectiveVol = isMuted ? 0 : volumePercent;
    audioEngine.setVolumePercent(effectiveVol);
  }, [volumePercent, isMuted]);

  // Sincronizar perfil de salida
  useEffect(() => {
    audioEngine.setOutputProfile(outputProfile);
  }, [outputProfile]);

  // Detectar conexion a internet
  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  // Detectar si la app esta visible (solo descargar si la app esta abierta)
  useEffect(() => {
    const handleVisibility = () => {
      const visible = !document.hidden;
      setIsAppVisible(visible);
    };
    document.addEventListener('visibilitychange', handleVisibility);
    return () => document.removeEventListener('visibilitychange', handleVisibility);
  }, []);

  // Pausar o reanudar descargas segun visibilidad de la aplicacion
  useEffect(() => {
    if (!isAppVisible) {
      setDownloadQueue(prev => prev.map(item =>
        item.status === DOWNLOAD_STATUS.DOWNLOADING
          ? { ...item, status: DOWNLOAD_STATUS.PAUSED }
          : item
      ));
    } else {
      setDownloadQueue(prev => prev.map(item =>
        item.status === DOWNLOAD_STATUS.PAUSED
          ? { ...item, status: DOWNLOAD_STATUS.DOWNLOADING }
          : item
      ));
    }
  }, [isAppVisible]);

  // Parsear letras sincronizadas admitiendo .lrc, .irc y .icr
  const activeParsedLyrics = useMemo(() => {
    const match = findMatchingLyric(currentSong.title, lrcFilesMap);
    if (match && match.content) {
      return parseLRC(match.content);
    }
    if (currentSong.lyrics && currentSong.lyrics.length > 0) {
      const step = (duration || 180) / currentSong.lyrics.length;
      return currentSong.lyrics.map((line, i) => ({
        time: i * step,
        text: line
      }));
    }
    return [];
  }, [currentSong, lrcFilesMap, duration]);

  const currentLrcLineIndex = useMemo(() => {
    return getCurrentLrcIndex(activeParsedLyrics, currentTime);
  }, [activeParsedLyrics, currentTime]);

  // Lista de letras para barra de navegacion alfabetica
  const alphabetLetters = useMemo(
    () => ['#', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'],
    []
  );

  // Obtener lista unica de carpetas disponibles en la coleccion
  const availableFolders = useMemo(() => {
    const set = new Set();
    songs.forEach((s) => {
      if (s.folder) set.add(s.folder);
    });
    return Array.from(set);
  }, [songs]);

  // Pistas filtradas y ordenadas dinamicamente (Fase 1.5)
  const filteredAndSortedTracks = useMemo(() => {
    let list = [...songs];

    // 1. Filtrar por carpeta seleccionada
    if (trackFolderFilter !== 'all') {
      list = list.filter((s) => s.folder === trackFolderFilter);
    }

    // 2. Ordenamiento (Alfabeticamente A-Z / Z-A / Mas nuevas / Mas antiguas)
    if (trackSortOrder === 'az') {
      list.sort((a, b) => (a.title || '').localeCompare(b.title || ''));
    } else if (trackSortOrder === 'za') {
      list.sort((a, b) => (b.title || '').localeCompare(a.title || ''));
    } else if (trackSortOrder === 'newest') {
      list.sort((a, b) => new Date(b.addedAt || '2020-01-01') - new Date(a.addedAt || '2020-01-01'));
    } else if (trackSortOrder === 'oldest') {
      list.sort((a, b) => new Date(a.addedAt || '2020-01-01') - new Date(b.addedAt || '2020-01-01'));
    }

    // 3. Filtrar por letra del alfabeto si hay una seleccionada
    if (selectedAlphaLetter) {
      list = list.filter((s) => {
        const first = (s.title || '').trim()[0]?.toUpperCase();
        if (selectedAlphaLetter === '#') {
          return !/[A-Z]/i.test(first);
        }
        return first === selectedAlphaLetter;
      });
    }

    return list;
  }, [songs, trackFolderFilter, trackSortOrder, selectedAlphaLetter]);

  // Auto-importar o vincular archivo de letra
  const handleAutoImportLyric = () => {
    const match = findMatchingLyric(currentSong.title, lrcFilesMap);
    if (match) {
      alert(`Archivo de letra vinculado: ${match.fileName}`);
    } else {
      const newFileName = `${currentSong.title}.lrc`;
      const generatedLrc = `[00:01.00] Letra sincronizada para ${currentSong.title}\n[00:07.00] Musica para los oidos\n[00:15.00] Frecuencia y ritmo en armonia sensorial\n[00:25.00] Melodia que fluye en cada compas`;
      setLrcFilesMap((prev) => ({
        ...prev,
        [newFileName]: generatedLrc
      }));
      alert(`Se creo y vinculo el archivo sincronizado: ${newFileName}`);
    }
  };

  // FUNCIONES DEL DESCARGADOR DE YOUTUBE (FASE 1)
  const simulateDownloadProgress = (itemId) => {
    let progress = 0;
    const timer = setInterval(() => {
      if (document.hidden) return;

      progress += Math.random() * 8 + 4;
      if (progress >= 100) {
        progress = 100;
        clearInterval(timer);
        delete downloadTimersRef.current[itemId];

        setDownloadQueue((prev) => {
          const item = prev.find((i) => i.id === itemId);
          if (item) {
            const completedItem = {
              ...item,
              progress: 100,
              status: DOWNLOAD_STATUS.COMPLETE,
              completedAt: new Date()
            };
            setDownloadHistory((hist) => [completedItem, ...hist]);
            if (item.includeLrc) {
              const lrcFileName = `${item.meta.title}.lrc`;
              const lrcContent = `[00:01.00] Letra sincronizada para ${item.meta.title}\n[00:10.00] Musica para los Oidos - YouTube Downloader\n[00:20.00] Artista: ${item.meta.artist}\n[00:30.00] Formato: ${item.quality}`;
              setLrcFilesMap((prevMap) => ({ ...prevMap, [lrcFileName]: lrcContent }));
            }
          }
          return prev.filter((i) => i.id !== itemId);
        });
      } else {
        setDownloadQueue((prev) =>
          prev.map((i) =>
            i.id === itemId
              ? { ...i, progress: Math.round(progress), status: DOWNLOAD_STATUS.DOWNLOADING }
              : i
          )
        );
      }
    }, 220);
    downloadTimersRef.current[itemId] = timer;
  };

  const handleSearchYouTube = (e) => {
    if (e && e.preventDefault) e.preventDefault();
    if (!ytSearchQuery.trim()) {
      setSearchResults(TRENDING_SEARCH_RESULTS);
      return;
    }
    setIsSearching(true);
    setTimeout(() => {
      const results = searchYouTubeSimulated(ytSearchQuery, isOnline);
      setSearchResults(results);
      setIsSearching(false);
    }, 300);
  };

  const handleDownloadFromResult = (result, qualityOverride = null, lrcOverride = null) => {
    const qualityToUse = qualityOverride || selectedQuality;
    const lrcToUse = lrcOverride !== null ? lrcOverride : includeLrc;

    const newItem = {
      id: `dl-${Date.now()}`,
      videoId: result.id,
      url: `https://youtu.be/${result.id}`,
      quality: qualityToUse,
      includeLrc: lrcToUse,
      meta: {
        title: result.title,
        artist: result.artist,
        album: 'YouTube Music',
        year: result.year || new Date().getFullYear().toString()
      },
      thumbnail: isOnline ? (result.thumbnail || getYouTubeThumbnail(result.id)) : null,
      progress: 0,
      status: DOWNLOAD_STATUS.QUEUED,
      addedAt: new Date()
    };

    setDownloadQueue((prev) => [...prev, newItem]);
    setActiveResultOptionsId(null);

    setTimeout(() => {
      setDownloadQueue((prev) =>
        prev.map((i) => (i.id === newItem.id ? { ...i, status: DOWNLOAD_STATUS.FETCHING_META } : i))
      );
      setTimeout(() => {
        simulateDownloadProgress(newItem.id);
      }, 1000);
    }, 500);
  };

  const handleAddToQueue = () => {
    const urlToProcess = ytUrl.trim();
    const queryToProcess = ytSearchQuery.trim();
    if (!urlToProcess && !queryToProcess) return;

    const extractedId = extractYouTubeId(urlToProcess);
    const videoId = extractedId || `yt-${Date.now()}`;
    const hasRealId = !!extractedId;

    let meta;
    if (hasRealId) {
      meta = fetchYouTubeMetaSimulated(videoId);
    } else {
      const query = queryToProcess || urlToProcess;
      meta = parseYouTubeTitle(query);
      meta.videoId = videoId;
      meta.thumbnail = null;
    }

    const newItem = {
      id: `dl-${Date.now()}`,
      videoId,
      url: urlToProcess || `busqueda:${queryToProcess}`,
      quality: selectedQuality,
      includeLrc,
      meta: {
        title: meta.title,
        artist: meta.artist,
        album: meta.album || 'YouTube Music',
        year: meta.year || new Date().getFullYear().toString()
      },
      // Solo buscar y asignar portada si hay conexion a internet
      thumbnail: isOnline && hasRealId ? getYouTubeThumbnail(videoId) : null,
      progress: 0,
      status: DOWNLOAD_STATUS.QUEUED,
      addedAt: new Date()
    };

    setDownloadQueue((prev) => [...prev, newItem]);
    setYtUrl('');
    setYtSearchQuery('');

    // Iniciar el flujo de descarga simulado
    setTimeout(() => {
      setDownloadQueue((prev) =>
        prev.map((i) => (i.id === newItem.id ? { ...i, status: DOWNLOAD_STATUS.FETCHING_META } : i))
      );
      setTimeout(() => {
        simulateDownloadProgress(newItem.id);
      }, 1100);
    }, 600);
  };

  const handleCancelDownload = (itemId) => {
    if (downloadTimersRef.current[itemId]) {
      clearInterval(downloadTimersRef.current[itemId]);
      delete downloadTimersRef.current[itemId];
    }
    setDownloadQueue((prev) => prev.filter((i) => i.id !== itemId));
  };

  const handleDeleteHistory = (itemId) => {
    setDownloadHistory((prev) => prev.filter((i) => i.id !== itemId));
  };

  const handleAddHistoryToLibrary = (item) => {
    const newSong = {
      id: Date.now(),
      title: item.meta.title,
      artist: item.meta.artist,
      album: item.meta.album || 'YouTube Music',
      cover: item.thumbnail || 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=120&h=120&fit=crop',
      duration: 213,
      audioUrl: '',
      folder: '/Musica/Descargas_YouTube',
      addedAt: new Date().toISOString().split('T')[0],
      lyrics: []
    };
    setSongs((prev) => [newSong, ...prev]);
    alert(`"${item.meta.title}" agregada a la libreria`);
  };

  const handleUpdateDownloadMeta = (itemId, field, value) => {
    setDownloadQueue((prev) =>
      prev.map((i) => (i.id === itemId ? { ...i, meta: { ...i.meta, [field]: value } } : i))
    );
  };

  const getStatusLabel = (status) => {
    switch (status) {
      case DOWNLOAD_STATUS.QUEUED:
        return 'En cola';
      case DOWNLOAD_STATUS.FETCHING_META:
        return 'Obteniendo datos';
      case DOWNLOAD_STATUS.DOWNLOADING:
        return 'Descargando';
      case DOWNLOAD_STATUS.COMPLETE:
        return 'Completado';
      case DOWNLOAD_STATUS.ERROR:
        return 'Error';
      case DOWNLOAD_STATUS.PAUSED:
        return 'Pausado';
      default:
        return status;
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case DOWNLOAD_STATUS.COMPLETE:
        return '#22c55e';
      case DOWNLOAD_STATUS.ERROR:
        return '#ef4444';
      case DOWNLOAD_STATUS.DOWNLOADING:
        return 'var(--phone-primary)';
      case DOWNLOAD_STATUS.FETCHING_META:
        return '#f59e0b';
      case DOWNLOAD_STATUS.PAUSED:
        return '#94a3b8';
      default:
        return 'var(--phone-text-muted)';
    }
  };

  // Cargar imagen de fondo local desde el equipo
  const handleWallpaperFileUpload = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      const dataUrl = event.target?.result;
      if (typeof dataUrl === 'string') {
        setThemeVars((prev) => ({
          ...prev,
          bgImage: dataUrl,
          bgBlur: prev.bgBlur || 16,
          bgDim: prev.bgDim || 0.75
        }));
        alert('Imagen de fondo personalizada cargada con exito');
      }
    };
    reader.readAsDataURL(file);
  };

  // Quitar imagen de fondo
  const handleClearWallpaper = () => {
    setThemeVars((prev) => ({
      ...prev,
      bgImage: ''
    }));
  };

  // Manejador de deslizamiento (Swipe)
  const handleSwipeStart = (clientX) => {
    setTouchStartX(clientX);
  };

  const handleSwipeEnd = (clientX) => {
    if (touchStartX === null) return;
    const diff = touchStartX - clientX;
    if (diff > 40) {
      setPlayerTopTab(1); // Deslizar hacia la lirica
    } else if (diff < -40) {
      setPlayerTopTab(0); // Regresar a la portada
    }
    setTouchStartX(null);
  };

  const applyPreset = (preset) => {
    setActivePreset(preset.id);
    setThemeVars({ ...preset.vars });
  };

  const handleColorChange = (key, val) => {
    setThemeVars((prev) => ({
      ...prev,
      [key]: val,
      accentGradient:
        key === 'accentPrimary' || key === 'accentSecondary'
          ? `linear-gradient(135deg, ${key === 'accentPrimary' ? val : prev.accentPrimary} 0%, ${key === 'accentSecondary' ? val : prev.accentSecondary} 100%)`
          : prev.accentGradient
    }));
  };

  const handleSaveStylePreset = () => {
    const name = newStylePresetName.trim();
    if (!name) return;
    const newStyle = {
      id: `style-${Date.now()}`,
      name: name,
      vars: { ...themeVars }
    };
    setCustomStylePresets([...customStylePresets, newStyle]);
    setActivePreset(newStyle.id);
    setNewStylePresetName('');
  };

  const handleDeleteStylePreset = (id, e) => {
    e?.stopPropagation();
    const filtered = customStylePresets.filter((p) => p.id !== id);
    setCustomStylePresets(filtered);
    if (activePreset === id) {
      applyPreset(THEME_PRESETS[0]);
    }
  };

  const togglePlay = () => {
    if (!audioRef.current) return;
    audioEngine.resume();

    if (isPlaying) {
      audioRef.current.pause();
      synth.stop();
      setIsPlaying(false);
    } else {
      audioRef.current
        .play()
        .then(() => setIsPlaying(true))
        .catch(() => {
          synth.start();
          setIsPlaying(true);
        });
    }
  };

  const handleSelectSong = (song, customQueue = null) => {
    if (customQueue) {
      setActiveQueue([...customQueue]);
    }
    if (song.id === currentSong.id) {
      togglePlay();
      return;
    }
    setCurrentSong(song);
    setCurrentTime(0);
    setDuration(song.duration);
    setIsPlaying(true);
    audioEngine.resume();

    setTimeout(() => {
      if (audioRef.current) {
        audioRef.current.play().catch(() => synth.start());
      }
    }, 50);
  };

  const handleNext = () => {
    const queueToUse = activeQueue.length > 0 ? activeQueue : songs;
    const idx = queueToUse.findIndex((s) => s.id === currentSong.id);
    const nextIdx = (idx + 1) % queueToUse.length;
    handleSelectSong(queueToUse[nextIdx]);
  };

  const handlePrev = () => {
    const queueToUse = activeQueue.length > 0 ? activeQueue : songs;
    const idx = queueToUse.findIndex((s) => s.id === currentSong.id);
    const prevIdx = (idx - 1 + queueToUse.length) % queueToUse.length;
    handleSelectSong(queueToUse[prevIdx]);
  };

  const toggleLike = (id, e) => {
    e?.stopPropagation();
    setLikedSongIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  // Drag and Drop
  const handleDragStart = (index) => {
    setDraggedQueueIndex(index);
  };

  const handleDragOver = (e) => {
    e.preventDefault();
  };

  const handleDrop = (targetIndex) => {
    if (draggedQueueIndex === null || draggedQueueIndex === targetIndex) return;
    const newQueue = [...activeQueue];
    const [moved] = newQueue.splice(draggedQueueIndex, 1);
    newQueue.splice(targetIndex, 0, moved);
    setActiveQueue(newQueue);
    setDraggedQueueIndex(null);
  };

  const moveQueueItem = (index, direction) => {
    const targetIdx = index + direction;
    if (targetIdx < 0 || targetIdx >= activeQueue.length) return;
    const newQueue = [...activeQueue];
    const [moved] = newQueue.splice(index, 1);
    newQueue.splice(targetIdx, 0, moved);
    setActiveQueue(newQueue);
  };

  const removeQueueItem = (index, e) => {
    e?.stopPropagation();
    if (activeQueue.length <= 1) {
      alert('La lista de reproduccion debe tener al menos una pista.');
      return;
    }
    const newQueue = [...activeQueue];
    newQueue.splice(index, 1);
    setActiveQueue(newQueue);
  };

  const addTrackToQueue = (song) => {
    setActiveQueue([...activeQueue, song]);
    setShowAddTrackModal(false);
  };

  const handleSelectEqPreset = (presetId) => {
    setSelectedEqPresetId(presetId);
    const found = eqPresets.find((p) => p.id === presetId);
    if (found) {
      setEqBands([...found.gains]);
      setBassBoost(found.bass || 0);
      setVirtualizer(found.virtualizer || 0);
    }
  };

  const handleSaveCustomPreset = () => {
    const name = customPresetName.trim();
    if (!name) return;
    const newPreset = {
      id: `custom-${Date.now()}`,
      name: name,
      gains: [...eqBands],
      bass: bassBoost,
      virtualizer: virtualizer,
      isCustom: true
    };
    setEqPresets([...eqPresets, newPreset]);
    setSelectedEqPresetId(newPreset.id);
    setCustomPresetName('');
  };

  const handleDeleteCustomPreset = (presetId) => {
    const filtered = eqPresets.filter((p) => p.id !== presetId);
    setEqPresets(filtered);
    if (selectedEqPresetId === presetId) {
      handleSelectEqPreset(filtered[0]?.id || 'flat');
    }
  };

  const handleBandSlider = (index, value) => {
    const newBands = [...eqBands];
    newBands[index] = parseFloat(value);
    setEqBands(newBands);
    setSelectedEqPresetId('custom-active');
  };

  const handleCreatePlaylist = () => {
    const name = newPlaylistName.trim();
    if (!name) return;
    const newPl = {
      id: `pl-${Date.now()}`,
      name: name,
      songIds: [currentSong.id],
      cover: currentSong.cover,
      description: 'Lista personalizada creada en el prototipo'
    };
    setPlaylists([newPl, ...playlists]);
    setNewPlaylistName('');
    setShowNewPlaylistModal(false);
  };

  const formatTime = (secs) => {
    if (!secs || isNaN(secs)) return '0:00';
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  const generatedFlutterCode = `// Codigo de tema simetrico para Flutter (ThemeData)
// Proyecto: Musica para los Oidos

import 'package:flutter/material.dart';

final appTheme = ThemeData(
  brightness: Brightness.dark,
  scaffoldBackgroundColor: const Color(0xFF${themeVars.bgMain.replace('#', '')}),
  primaryColor: const Color(0xFF${themeVars.accentPrimary.replace('#', '')}),
  colorScheme: const ColorScheme.dark(
    primary: Color(0xFF${themeVars.accentPrimary.replace('#', '')}),
    secondary: Color(0xFF${themeVars.accentSecondary.replace('#', '')}),
    surface: Color(0xFF${themeVars.bgSurface.replace('#', '')}),
  ),
  cardTheme: CardTheme(
    color: const Color(0xFF${themeVars.bgSurface.replace('#', '')}),
    shape: RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(${parseInt(themeVars.radius) || 16}.0),
    ),
  ),
  sliderTheme: SliderThemeData(
    activeTrackColor: const Color(0xFF${themeVars.accentPrimary.replace('#', '')}),
    thumbColor: const Color(0xFF${themeVars.accentPrimary.replace('#', '')}),
    overlayColor: const Color(0x33${themeVars.accentPrimary.replace('#', '')}),
  ),
  fontFamily: '${themeVars.fontFamily.includes('Outfit') ? 'Outfit' : 'PlusJakartaSans'}',
);

const playerBackgroundColor = Color(0xFF${(themeVars.playerBg || themeVars.bgSurface).replace('#', '')});
const playerAccentColor = Color(0xFF${(themeVars.playerAccent || themeVars.accentPrimary).replace('#', '')});
`;

  return (
    <div
      className="workbench-container"
      style={{
        '--phone-bg': themeVars.bgMain,
        '--phone-surface': themeVars.bgSurface,
        '--phone-elevated': themeVars.bgElevated,
        '--phone-player-bg': themeVars.playerBg || themeVars.bgSurface,
        '--phone-player-accent': themeVars.playerAccent || themeVars.accentPrimary,
        '--phone-primary': themeVars.accentPrimary,
        '--phone-secondary': themeVars.accentSecondary,
        '--phone-gradient': themeVars.accentGradient,
        '--phone-text': themeVars.textColor,
        '--phone-text-muted': themeVars.textMuted,
        '--phone-radius': themeVars.radius,
        '--phone-font': themeVars.fontFamily
      }}
    >
      <audio
        ref={audioRef}
        src={currentSong.audioUrl}
        onTimeUpdate={() => {
          if (audioRef.current) setCurrentTime(audioRef.current.currentTime);
        }}
        onEnded={handleNext}
      />

      {/* Input invisible para cargar imagen de fondo personalizada */}
      <input
        type="file"
        ref={wallpaperInputRef}
        accept="image/*"
        onChange={handleWallpaperFileUpload}
        style={{ display: 'none' }}
      />

      {/* PANEL LATERAL DE CONFIGURACION */}
      <aside className="studio-sidebar">
        <div className="studio-sidebar-header">
          <div className="studio-title-wrap">
            <div className="studio-logo">
              <Headphones size={20} />
            </div>
            <div>
              <h2 className="studio-brand-title">Musica para los Oidos</h2>
              <span className="studio-brand-badge">Estudio de Interfaz</span>
            </div>
          </div>
        </div>

        <div className="studio-nav-tabs">
          <button
            className={`studio-tab-btn ${sidebarTab === 'themes' ? 'active' : ''}`}
            onClick={() => setSidebarTab('themes')}
          >
            <Palette size={14} />
            <span>Tema Global</span>
          </button>
          <button
            className={`studio-tab-btn ${sidebarTab === 'player-custom' ? 'active' : ''}`}
            onClick={() => setSidebarTab('player-custom')}
          >
            <Music size={14} />
            <span>Reproductor</span>
          </button>
          <button
            className={`studio-tab-btn ${sidebarTab === 'wallpaper' ? 'active' : ''}`}
            onClick={() => setSidebarTab('wallpaper')}
          >
            <ImageIcon size={14} />
            <span>Fondo</span>
          </button>
          <button
            className={`studio-tab-btn ${sidebarTab === 'flutter-code' ? 'active' : ''}`}
            onClick={() => setSidebarTab('flutter-code')}
          >
            <FileCode size={14} />
            <span>Flutter</span>
          </button>
        </div>

        <div className="studio-panel-body">
          {sidebarTab === 'themes' && (
            <>
              {customStylePresets.length > 0 && (
                <div>
                  <div className="studio-section-title">
                    <span>Tus Presets Guardados</span>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                    {customStylePresets.map((p) => (
                      <div
                        key={p.id}
                        className={`preset-chip ${activePreset === p.id ? 'active' : ''}`}
                        onClick={() => applyPreset(p)}
                        style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}
                      >
                        <span style={{ fontWeight: 700, fontSize: '0.82rem' }}>{p.name}</span>
                        <button
                          onClick={(e) => handleDeleteStylePreset(p.id, e)}
                          style={{ color: '#ef4444', padding: '4px' }}
                          title="Eliminar preset"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <div>
                <div className="studio-section-title">
                  <span>Presets de Diseno</span>
                </div>
                <div className="theme-preset-grid">
                  {THEME_PRESETS.map((preset) => (
                    <div
                      key={preset.id}
                      className={`preset-chip ${activePreset === preset.id ? 'active' : ''}`}
                      onClick={() => applyPreset(preset)}
                    >
                      <span style={{ fontWeight: 700, fontSize: '0.82rem' }}>
                        {preset.name}
                      </span>
                      <div className="preset-colors-row">
                        <div
                          className="color-dot"
                          style={{ background: preset.vars.bgMain }}
                        />
                        <div
                          className="color-dot"
                          style={{ background: preset.vars.bgSurface }}
                        />
                        <div
                          className="color-dot"
                          style={{ background: preset.vars.accentPrimary }}
                        />
                        <div
                          className="color-dot"
                          style={{ background: preset.vars.accentSecondary }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div>
                <div className="studio-section-title">
                  <span>Guardar Estilo Actual</span>
                </div>
                <div className="save-style-preset-row">
                  <input
                    type="text"
                    placeholder="Nombre del preset de estilo..."
                    value={newStylePresetName}
                    onChange={(e) => setNewStylePresetName(e.target.value)}
                    className="style-preset-input"
                  />
                  <button
                    onClick={handleSaveStylePreset}
                    className="eq-preset-save-btn"
                  >
                    <Save size={13} />
                    <span>Guardar</span>
                  </button>
                </div>
              </div>

              <div>
                <div className="studio-section-title">
                  <span>Colores del Sistema</span>
                </div>

                <div className="color-picker-row">
                  <span className="color-picker-label">Fondo de Pantalla</span>
                  <div className="color-input-wrap">
                    <span className="color-hex-tag">{themeVars.bgMain}</span>
                    <input
                      type="color"
                      className="color-input-native"
                      value={themeVars.bgMain}
                      onChange={(e) => handleColorChange('bgMain', e.target.value)}
                    />
                  </div>
                </div>

                <div className="color-picker-row">
                  <span className="color-picker-label">Superficie y Tarjetas</span>
                  <div className="color-input-wrap">
                    <span className="color-hex-tag">{themeVars.bgSurface}</span>
                    <input
                      type="color"
                      className="color-input-native"
                      value={themeVars.bgSurface}
                      onChange={(e) =>
                        handleColorChange('bgSurface', e.target.value)
                      }
                    />
                  </div>
                </div>

                <div className="color-picker-row">
                  <span className="color-picker-label">Acento Primario</span>
                  <div className="color-input-wrap">
                    <span className="color-hex-tag">{themeVars.accentPrimary}</span>
                    <input
                      type="color"
                      className="color-input-native"
                      value={themeVars.accentPrimary}
                      onChange={(e) =>
                        handleColorChange('accentPrimary', e.target.value)
                      }
                    />
                  </div>
                </div>

                <div className="color-picker-row">
                  <span className="color-picker-label">Color del Texto</span>
                  <div className="color-input-wrap">
                    <span className="color-hex-tag">{themeVars.textColor}</span>
                    <input
                      type="color"
                      className="color-input-native"
                      value={themeVars.textColor}
                      onChange={(e) =>
                        handleColorChange('textColor', e.target.value)
                      }
                    />
                  </div>
                </div>
              </div>
            </>
          )}

          {sidebarTab === 'player-custom' && (
            <>
              <div>
                <div className="studio-section-title">
                  <span>Color del Reproductor</span>
                </div>
                <div className="color-picker-row">
                  <span className="color-picker-label">Fondo del Reproductor</span>
                  <div className="color-input-wrap">
                    <span className="color-hex-tag">
                      {themeVars.playerBg || themeVars.bgSurface}
                    </span>
                    <input
                      type="color"
                      className="color-input-native"
                      value={themeVars.playerBg || themeVars.bgSurface}
                      onChange={(e) =>
                        handleColorChange('playerBg', e.target.value)
                      }
                    />
                  </div>
                </div>

                <div className="color-picker-row">
                  <span className="color-picker-label">Botones y Acento</span>
                  <div className="color-input-wrap">
                    <span className="color-hex-tag">
                      {themeVars.playerAccent || themeVars.accentPrimary}
                    </span>
                    <input
                      type="color"
                      className="color-input-native"
                      value={themeVars.playerAccent || themeVars.accentPrimary}
                      onChange={(e) =>
                        handleColorChange('playerAccent', e.target.value)
                      }
                    />
                  </div>
                </div>
              </div>

              <div>
                <button
                  className="btn-export"
                  onClick={() => setIsPlayerModalOpen(true)}
                >
                  <Maximize2 size={16} />
                  <span>Abrir Reproductor Completo</span>
                </button>
              </div>
            </>
          )}

          {sidebarTab === 'wallpaper' && (
            <div>
              <div className="studio-section-title">
                <span>Cargar Fondo Propio</span>
              </div>
              <div style={{ display: 'flex', gap: '8px', marginBottom: '1rem' }}>
                <button
                  className="btn-export"
                  onClick={() => wallpaperInputRef.current?.click()}
                  style={{ flex: 1 }}
                >
                  <Upload size={15} />
                  <span>Subir Imagen</span>
                </button>
                {themeVars.bgImage && (
                  <button
                    className="btn-secondary"
                    onClick={handleClearWallpaper}
                    title="Quitar fondo actual"
                  >
                    <ImageOff size={15} />
                  </button>
                )}
              </div>

              <div className="studio-section-title">
                <span>Imagenes Predefinidas</span>
              </div>
              <div className="wallpaper-grid">
                {WALLPAPER_PRESETS.map((wp) => (
                  <div
                    key={wp.id}
                    className={`wallpaper-thumb-card ${themeVars.bgImage === wp.url ? 'active' : ''}`}
                    style={{
                      backgroundImage: wp.url ? `url(${wp.url})` : 'none',
                      backgroundColor: '#121020'
                    }}
                    onClick={() =>
                      setThemeVars((prev) => ({ ...prev, bgImage: wp.url }))
                    }
                  >
                    <span className="wallpaper-thumb-name">{wp.name}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {sidebarTab === 'flutter-code' && (
            <div>
              <div className="studio-section-title">
                <span>ThemeData para Flutter</span>
              </div>
              <pre className="flutter-code-preview">{generatedFlutterCode}</pre>
              <button
                className="btn-export"
                style={{ marginTop: '1rem' }}
                onClick={() => {
                  navigator.clipboard.writeText(generatedFlutterCode);
                  alert('Codigo Flutter copiado');
                }}
              >
                <Check size={16} />
                <span>Copiar Codigo ThemeData</span>
              </button>
            </div>
          )}
        </div>
      </aside>

      {/* ESCENARIO PRINCIPAL: SIMULADOR DE TELEFONO */}
      <main className="simulator-stage">
        {/* Barra superior de herramientas */}
        <div className="stage-toolbar">
          <div
            className={`stage-tool-item ${phoneScreen === 'home' ? 'active' : ''}`}
            onClick={() => {
              setPhoneScreen('home');
              setIsPlayerModalOpen(false);
            }}
          >
            <Music size={14} />
            <span>Inicio</span>
          </div>
          <div
            className={`stage-tool-item ${phoneScreen === 'library' ? 'active' : ''}`}
            onClick={() => {
              setPhoneScreen('library');
              setIsPlayerModalOpen(false);
            }}
          >
            <ListMusic size={14} />
            <span>Libreria</span>
          </div>
          <div
            className={`stage-tool-item ${phoneScreen === 'equalizer' ? 'active' : ''}`}
            onClick={() => {
              setPhoneScreen('equalizer');
              setIsPlayerModalOpen(false);
            }}
          >
            <Sliders size={14} />
            <span>Ecualizador</span>
          </div>
          <div
            className={`stage-tool-item ${phoneScreen === 'settings' ? 'active' : ''}`}
            onClick={() => {
              setPhoneScreen('settings');
              setIsPlayerModalOpen(false);
            }}
          >
            <Palette size={14} />
            <span>Ajustes</span>
          </div>
          <div
            className={`stage-tool-item ${phoneScreen === 'downloader' ? 'active' : ''}`}
            onClick={() => {
              setPhoneScreen('downloader');
              setIsPlayerModalOpen(false);
            }}
          >
            <Download size={14} />
            <span>Descargar</span>
          </div>
          <div style={{ width: '1px', height: '16px', background: 'rgba(255,255,255,0.1)' }} />
          <div
            className="stage-tool-item"
            onClick={() => setIsFlatDevice(!isFlatDevice)}
          >
            <Eye size={14} />
            <span>{isFlatDevice ? 'Marco On' : 'Marco Off'}</span>
          </div>
          <div
            className="stage-tool-item"
            onClick={() => setDeviceScale((s) => (s === 1 ? 0.85 : 1))}
          >
            <Maximize2 size={14} />
            <span>{Math.round(deviceScale * 100)}%</span>
          </div>
        </div>

        {/* MARCO DEL SMARTPHONE */}
        <div
          className="device-scaler"
          style={{ transform: `scale(${deviceScale})` }}
        >
          <div
            className={`mobile-phone-frame ${isFlatDevice ? 'flat-mode' : ''}`}
          >
            {themeVars.bgImage && (
              <>
                <div
                  className="phone-bg-image-layer"
                  style={{
                    backgroundImage: `url(${themeVars.bgImage})`,
                    filter: `blur(${themeVars.bgBlur || 16}px)`
                  }}
                />
                <div
                  className="phone-bg-dim-overlay"
                  style={{ opacity: themeVars.bgDim || 0.7 }}
                />
              </>
            )}

            {!isFlatDevice && (
              <div className="phone-dynamic-island">
                <div className="island-camera" />
                <div className="island-music-indicator">
                  <div className={`island-bar ${isPlaying ? 'active' : ''}`} />
                  <div className={`island-bar ${isPlaying ? 'active' : ''}`} />
                  <div className={`island-bar ${isPlaying ? 'active' : ''}`} />
                </div>
                <div className="island-sensor" />
              </div>
            )}

            <div className="phone-status-bar">
              <span>9:41</span>
              <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
                <Zap size={12} color="var(--phone-primary)" />
                <span>5G</span>
                <span>100%</span>
              </div>
            </div>

            {/* PANTALLA 1: INICIO CON TARJETAS TRANSLUCIDAS */}
            {phoneScreen === 'home' && (
              <div className="phone-viewport">
                <div className="phone-screen-header">
                  <div>
                    <span
                      style={{
                        fontSize: '0.72rem',
                        color: 'var(--phone-text-muted)',
                        fontWeight: 700,
                        letterSpacing: '0.05em'
                      }}
                    >
                      MUSICA PARA LOS OIDOS
                    </span>
                    <h2 className="phone-screen-title">Inicio</h2>
                  </div>
                  <div
                    style={{
                      width: '36px',
                      height: '36px',
                      borderRadius: '50%',
                      background: 'rgba(255,255,255,0.08)',
                      backdropFilter: 'blur(12px)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      border: '1px solid rgba(255,255,255,0.1)'
                    }}
                  >
                    <Heart size={16} color="var(--phone-primary)" />
                  </div>
                </div>

                <div
                  style={{
                    background: 'rgba(255,255,255,0.06)',
                    backdropFilter: 'blur(16px)',
                    borderRadius: 'var(--phone-radius)',
                    padding: '1rem',
                    marginBottom: '1.25rem',
                    border: '1px solid rgba(255,255,255,0.08)',
                    position: 'relative'
                  }}
                >
                  <img
                    src={currentSong.cover}
                    alt=""
                    style={{
                      width: '100%',
                      height: '145px',
                      objectFit: 'cover',
                      borderRadius: 'calc(var(--phone-radius) - 6px)',
                      marginBottom: '0.8rem'
                    }}
                  />
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}
                  >
                    <div>
                      <div
                        style={{
                          fontSize: '0.95rem',
                          fontWeight: 800,
                          color: 'var(--phone-text)'
                        }}
                      >
                        {currentSong.title}
                      </div>
                      <div
                        style={{
                          fontSize: '0.75rem',
                          color: 'var(--phone-text-muted)'
                        }}
                      >
                        {currentSong.artist}
                      </div>
                    </div>
                    <button
                      onClick={togglePlay}
                      style={{
                        width: '44px',
                        height: '44px',
                        borderRadius: '50%',
                        background: 'var(--phone-player-accent)',
                        color: '#000000',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                      }}
                    >
                      {isPlaying ? <Pause size={20} /> : <Play size={20} />}
                    </button>
                  </div>
                </div>

                <h4
                  style={{
                    fontSize: '0.85rem',
                    fontWeight: 800,
                    marginBottom: '0.75rem',
                    color: 'var(--phone-text)'
                  }}
                >
                  Pistas Recientes
                </h4>
                {songs.slice(0, 4).map((song) => (
                  <div
                    key={song.id}
                    className="phone-item-row"
                    onClick={() => handleSelectSong(song, songs)}
                  >
                    <img
                      src={song.cover}
                      alt=""
                      className="phone-item-thumb"
                    />
                    <div className="phone-item-info">
                      <div className="phone-item-title">{song.title}</div>
                      <div className="phone-item-subtitle">{song.artist}</div>
                    </div>
                    <span
                      style={{
                        fontSize: '0.72rem',
                        color: 'var(--phone-text-muted)'
                      }}
                    >
                      {formatTime(song.duration)}
                    </span>
                  </div>
                ))}
              </div>
            )}

            {/* PANTALLA 2: LIBRERIA */}
            {phoneScreen === 'library' && (
              <div className="phone-viewport">
                <div className="phone-screen-header">
                  <h2 className="phone-screen-title">Tu Libreria</h2>
                  <button
                    onClick={() => setShowNewPlaylistModal(true)}
                    style={{
                      background: 'var(--phone-primary)',
                      color: '#000000',
                      borderRadius: '50%',
                      width: '32px',
                      height: '32px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center'
                    }}
                    title="Crear Lista"
                  >
                    <Plus size={18} />
                  </button>
                </div>

                <div className="library-subtabs">
                  <button
                    className={`lib-subtab-pill ${libraryTab === 'tracks' ? 'active' : ''}`}
                    onClick={() => {
                      setLibraryTab('tracks');
                      setActivePlaylistId(null);
                      setSelectedAlbumName(null);
                    }}
                  >
                    Pistas ({songs.length})
                  </button>
                  <button
                    className={`lib-subtab-pill ${libraryTab === 'playlists' ? 'active' : ''}`}
                    onClick={() => {
                      setLibraryTab('playlists');
                      setSelectedAlbumName(null);
                    }}
                  >
                    Listas ({playlists.length})
                  </button>
                  <button
                    className={`lib-subtab-pill ${libraryTab === 'albums' ? 'active' : ''}`}
                    onClick={() => {
                      setLibraryTab('albums');
                      setActivePlaylistId(null);
                    }}
                  >
                    Albumes
                  </button>
                  <button
                    className={`lib-subtab-pill ${libraryTab === 'folders' ? 'active' : ''}`}
                    onClick={() => {
                      setLibraryTab('folders');
                      setActivePlaylistId(null);
                      setSelectedAlbumName(null);
                    }}
                  >
                    Carpetas
                  </button>
                </div>

                {libraryTab === 'tracks' && (
                  <div>
                    {/* Tarjeta de Filtros de Orden y Carpetas (Fase 1.5) */}
                    <div className="lib-filters-card">
                      <div className="lib-filters-row">
                        {/* Selector de Orden */}
                        <div className="lib-filter-box">
                          <ArrowDownAZ size={13} className="lib-filter-icon" />
                          <select
                            className="lib-filter-select"
                            value={trackSortOrder}
                            onChange={(e) => {
                              setTrackSortOrder(e.target.value);
                              if (e.target.value !== 'az' && e.target.value !== 'za') {
                                setSelectedAlphaLetter(null);
                              }
                            }}
                          >
                            <option value="az">A - Z (Alfabeticamente)</option>
                            <option value="za">Z - A (Inverso)</option>
                            <option value="newest">Mas nuevas primero</option>
                            <option value="oldest">Mas antiguas primero</option>
                            <option value="default">Orden predeterminado</option>
                          </select>
                        </div>

                        {/* Selector de Carpeta */}
                        <div className="lib-filter-box">
                          <Folder size={13} className="lib-filter-icon" />
                          <select
                            className="lib-filter-select"
                            value={trackFolderFilter}
                            onChange={(e) => setTrackFolderFilter(e.target.value)}
                          >
                            <option value="all">Todas las carpetas</option>
                            {availableFolders.map((f) => (
                              <option key={f} value={f}>
                                {f.replace('/Musica/', '')}
                              </option>
                            ))}
                          </select>
                        </div>
                      </div>

                      {/* Barra de navegacion alfabetica (si el orden es alfabetico) */}
                      {(trackSortOrder === 'az' || trackSortOrder === 'za') && (
                        <div className="lib-alpha-wrap">
                          <div className="lib-alpha-strip">
                            <button
                              type="button"
                              className={`lib-alpha-chip ${selectedAlphaLetter === null ? 'active' : ''}`}
                              onClick={() => setSelectedAlphaLetter(null)}
                            >
                              Todas
                            </button>
                            {alphabetLetters.map((letter) => (
                              <button
                                key={letter}
                                type="button"
                                className={`lib-alpha-chip ${selectedAlphaLetter === letter ? 'active' : ''}`}
                                onClick={() =>
                                  setSelectedAlphaLetter((cur) => (cur === letter ? null : letter))
                                }
                              >
                                {letter}
                              </button>
                            ))}
                          </div>
                        </div>
                      )}

                      {/* Indicador de filtro activo */}
                      {(selectedAlphaLetter || trackFolderFilter !== 'all') && (
                        <div className="lib-active-filter-bar">
                          <span>
                            {selectedAlphaLetter && `Letra: "${selectedAlphaLetter}"`}
                            {selectedAlphaLetter && trackFolderFilter !== 'all' && ' • '}
                            {trackFolderFilter !== 'all' && `Carpeta: ${trackFolderFilter.replace('/Musica/', '')}`}
                            {` (${filteredAndSortedTracks.length} pistas)`}
                          </span>
                          <button
                            type="button"
                            className="lib-filter-reset-btn"
                            onClick={() => {
                              setSelectedAlphaLetter(null);
                              setTrackFolderFilter('all');
                            }}
                          >
                            Limpiar
                          </button>
                        </div>
                      )}
                    </div>

                    {/* Lista de pistas filtradas y ordenadas */}
                    {filteredAndSortedTracks.length === 0 ? (
                      <div className="dl-empty-state" style={{ padding: '2rem 1rem' }}>
                        <Music size={28} style={{ opacity: 0.3, marginBottom: '0.5rem' }} />
                        <div style={{ fontSize: '0.8rem', color: 'var(--phone-text-muted)' }}>
                          No hay pistas que coincidan con los filtros aplicados
                        </div>
                      </div>
                    ) : (
                      filteredAndSortedTracks.map((song) => (
                        <div
                          key={song.id}
                          className="phone-item-row"
                          onClick={() => handleSelectSong(song, filteredAndSortedTracks)}
                        >
                          <img
                            src={song.cover}
                            alt=""
                            className="phone-item-thumb"
                          />
                          <div className="phone-item-info">
                            <div className="phone-item-title">{song.title}</div>
                            <div className="phone-item-subtitle">
                              {song.artist} • {song.album}
                            </div>
                          </div>
                          <button
                            onClick={(e) => toggleLike(song.id, e)}
                            style={{
                              color: likedSongIds.includes(song.id)
                                ? 'var(--phone-primary)'
                                : 'var(--phone-text-muted)',
                              background: 'transparent',
                              border: 'none',
                              cursor: 'pointer',
                              padding: '4px'
                            }}
                          >
                            <Heart
                              size={16}
                              fill={
                                likedSongIds.includes(song.id)
                                  ? 'currentColor'
                                  : 'none'
                              }
                            />
                          </button>
                        </div>
                      ))
                    )}
                  </div>
                )}

                {libraryTab === 'playlists' && (
                  <div>
                    {activePlaylistId ? (
                      <div>
                        {(() => {
                          const pl = playlists.find((p) => p.id === activePlaylistId);
                          if (!pl) return null;
                          const plSongs = songs.filter((s) => pl.songIds.includes(s.id));
                          return (
                            <div>
                              <div
                                style={{
                                  display: 'flex',
                                  alignItems: 'center',
                                  gap: '12px',
                                  marginBottom: '0.8rem',
                                  background: 'rgba(255,255,255,0.06)',
                                  backdropFilter: 'blur(16px)',
                                  padding: '12px',
                                  borderRadius: 'var(--phone-radius)',
                                  border: '1px solid rgba(255,255,255,0.08)'
                                }}
                              >
                                <img
                                  src={pl.cover}
                                  alt=""
                                  style={{
                                    width: '60px',
                                    height: '60px',
                                    borderRadius: '10px',
                                    objectFit: 'cover'
                                  }}
                                />
                                <div>
                                  <div style={{ fontWeight: 800, fontSize: '0.95rem' }}>
                                    {pl.name}
                                  </div>
                                  <div
                                    style={{
                                      fontSize: '0.74rem',
                                      color: 'var(--phone-text-muted)'
                                    }}
                                  >
                                    {pl.description}
                                  </div>
                                </div>
                              </div>
                              <button
                                onClick={() => setActivePlaylistId(null)}
                                style={{
                                  color: 'var(--phone-primary)',
                                  fontSize: '0.75rem',
                                  fontWeight: 700,
                                  marginBottom: '0.8rem'
                                }}
                              >
                                ← Volver a todas las listas
                              </button>
                              {plSongs.map((song) => (
                                <div
                                  key={song.id}
                                  className="phone-item-row"
                                  onClick={() => handleSelectSong(song, plSongs)}
                                >
                                  <img
                                    src={song.cover}
                                    alt=""
                                    className="phone-item-thumb"
                                  />
                                  <div className="phone-item-info">
                                    <div className="phone-item-title">{song.title}</div>
                                    <div className="phone-item-subtitle">{song.artist}</div>
                                  </div>
                                </div>
                              ))}
                            </div>
                          );
                        })()}
                      </div>
                    ) : (
                      <div className="phone-card-grid">
                        {playlists.map((pl) => (
                          <div
                            key={pl.id}
                            className="phone-grid-card"
                            onClick={() => setActivePlaylistId(pl.id)}
                          >
                            <img
                              src={pl.cover}
                              alt=""
                              className="phone-grid-thumb"
                            />
                            <div
                              style={{
                                fontWeight: 700,
                                fontSize: '0.85rem',
                                color: 'var(--phone-text)',
                                whiteSpace: 'nowrap',
                                overflow: 'hidden',
                                textOverflow: 'ellipsis'
                              }}
                            >
                              {pl.name}
                            </div>
                            <div
                              style={{
                                fontSize: '0.72rem',
                                color: 'var(--phone-text-muted)'
                              }}
                            >
                              {pl.songIds.length} pistas
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}

                {libraryTab === 'albums' && (
                  <div>
                    {selectedAlbumName ? (
                      <div>
                        {(() => {
                          const albumSongs = songs.filter((s) => s.album === selectedAlbumName);
                          return (
                            <div>
                              <div
                                style={{
                                  display: 'flex',
                                  alignItems: 'center',
                                  gap: '12px',
                                  marginBottom: '0.8rem',
                                  background: 'rgba(255,255,255,0.06)',
                                  backdropFilter: 'blur(16px)',
                                  padding: '12px',
                                  borderRadius: 'var(--phone-radius)',
                                  border: '1px solid rgba(255,255,255,0.08)'
                                }}
                              >
                                <img
                                  src={albumSongs[0]?.cover}
                                  alt=""
                                  style={{
                                    width: '60px',
                                    height: '60px',
                                    borderRadius: '10px',
                                    objectFit: 'cover'
                                  }}
                                />
                                <div>
                                  <div style={{ fontWeight: 800, fontSize: '0.95rem' }}>
                                    {selectedAlbumName}
                                  </div>
                                  <div
                                    style={{
                                      fontSize: '0.74rem',
                                      color: 'var(--phone-text-muted)'
                                    }}
                                  >
                                    {albumSongs[0]?.artist} • {albumSongs.length} canciones
                                  </div>
                                </div>
                              </div>
                              <button
                                onClick={() => setSelectedAlbumName(null)}
                                style={{
                                  color: 'var(--phone-primary)',
                                  fontSize: '0.75rem',
                                  fontWeight: 700,
                                  marginBottom: '0.8rem'
                                }}
                              >
                                ← Volver a todos los albumes
                              </button>
                              {albumSongs.map((song) => (
                                <div
                                  key={song.id}
                                  className="phone-item-row"
                                  onClick={() => handleSelectSong(song, albumSongs)}
                                >
                                  <img
                                    src={song.cover}
                                    alt=""
                                    className="phone-item-thumb"
                                  />
                                  <div className="phone-item-info">
                                    <div className="phone-item-title">{song.title}</div>
                                    <div className="phone-item-subtitle">{song.artist}</div>
                                  </div>
                                  <span style={{ fontSize: '0.72rem', color: 'var(--phone-text-muted)' }}>
                                    {formatTime(song.duration)}
                                  </span>
                                </div>
                              ))}
                            </div>
                          );
                        })()}
                      </div>
                    ) : (
                      <div className="phone-card-grid">
                        {Array.from(new Set(songs.map((s) => s.album))).map(
                          (albumName, i) => {
                            const albumSong = songs.find((s) => s.album === albumName);
                            const count = songs.filter((s) => s.album === albumName).length;
                            return (
                              <div
                                key={i}
                                className="phone-grid-card"
                                onClick={() => setSelectedAlbumName(albumName)}
                              >
                                <img
                                  src={albumSong?.cover}
                                  alt=""
                                  className="phone-grid-thumb"
                                />
                                <div
                                  style={{
                                    fontWeight: 700,
                                    fontSize: '0.82rem',
                                    color: 'var(--phone-text)',
                                    whiteSpace: 'nowrap',
                                    overflow: 'hidden',
                                    textOverflow: 'ellipsis'
                                  }}
                                >
                                  {albumName}
                                </div>
                                <div
                                  style={{
                                    fontSize: '0.72rem',
                                    color: 'var(--phone-text-muted)'
                                  }}
                                >
                                  {albumSong?.artist} • {count} pistas
                                </div>
                              </div>
                            );
                          }
                        )}
                      </div>
                    )}
                  </div>
                )}

                {libraryTab === 'folders' && (
                  <div>
                    <div
                      style={{
                        padding: '10px 14px',
                        background: 'rgba(255,255,255,0.05)',
                        backdropFilter: 'blur(12px)',
                        borderRadius: 'var(--phone-radius)',
                        marginBottom: '1rem',
                        fontSize: '0.78rem',
                        color: 'var(--phone-text-muted)',
                        border: '1px solid rgba(255,255,255,0.06)'
                      }}
                    >
                      Explorador de almacenamiento interno del dispositivo:
                    </div>
                    {folders.map((f, i) => (
                      <div key={i} className="phone-item-row">
                        <div
                          style={{
                            width: '42px',
                            height: '42px',
                            borderRadius: '10px',
                            background: 'rgba(255,255,255,0.08)',
                            backdropFilter: 'blur(12px)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            color: 'var(--phone-primary)'
                          }}
                        >
                          <Folder size={20} />
                        </div>
                        <div className="phone-item-info">
                          <div className="phone-item-title">{f.path}</div>
                          <div className="phone-item-subtitle">
                            {f.trackCount} pistas • {f.sizeMb} MB
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* PANTALLA 3: ECUALIZADOR */}
            {phoneScreen === 'equalizer' && (
              <div className="phone-viewport eq-screen-wrap">
                <div className="phone-screen-header">
                  <div>
                    <h2 className="phone-screen-title">Ecualizador</h2>
                    <span
                      style={{
                        fontSize: '0.72rem',
                        color: 'var(--phone-text-muted)'
                      }}
                    >
                      Procesamiento de sonido en vivo
                    </span>
                  </div>
                  <button
                    onClick={() => setEqEnabled(!eqEnabled)}
                    style={{
                      padding: '4px 12px',
                      borderRadius: '999px',
                      fontSize: '0.75rem',
                      fontWeight: 800,
                      background: eqEnabled
                        ? 'var(--phone-primary)'
                        : 'rgba(255,255,255,0.1)',
                      color: eqEnabled ? '#000000' : '#ffffff'
                    }}
                  >
                    {eqEnabled ? 'ENCENDIDO' : 'APAGADO'}
                  </button>
                </div>

                <div className="vol-boost-card">
                  <div className="vol-boost-header">
                    <span>Volumen y Ganancia Preamp (Volumen++)</span>
                    <span className="vol-boost-badge">
                      {volumePercent}% {volumePercent > 100 ? '(Amplificado)' : ''}
                    </span>
                  </div>
                  <input
                    type="range"
                    min="0"
                    max="200"
                    step="5"
                    value={volumePercent}
                    onChange={(e) => setVolumePercent(parseInt(e.target.value))}
                    className="range-input"
                  />
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      fontSize: '0.68rem',
                      color: 'var(--phone-text-muted)',
                      marginTop: '4px'
                    }}
                  >
                    <span>0%</span>
                    <span>100% (Normal)</span>
                    <span style={{ color: '#38bdf8', fontWeight: 700 }}>200% (Maximo)</span>
                  </div>
                </div>

                <div className="eq-toggle-card">
                  <span
                    style={{
                      fontSize: '0.82rem',
                      fontWeight: 600,
                      color: 'var(--phone-text)'
                    }}
                  >
                    Perfil de Sonido:
                  </span>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <select
                      className="eq-preset-select"
                      value={selectedEqPresetId}
                      onChange={(e) => handleSelectEqPreset(e.target.value)}
                    >
                      {eqPresets.map((p) => (
                        <option key={p.id} value={p.id}>
                          {p.name}
                        </option>
                      ))}
                      <option value="custom-active">Personalizado Manual</option>
                    </select>

                    {(() => {
                      const cur = eqPresets.find((p) => p.id === selectedEqPresetId);
                      if (cur && cur.isCustom) {
                        return (
                          <button
                            onClick={() => handleDeleteCustomPreset(cur.id)}
                            style={{
                              color: '#ef4444',
                              padding: '4px',
                              borderRadius: '6px'
                            }}
                            title="Eliminar este preset"
                          >
                            <Trash2 size={16} />
                          </button>
                        );
                      }
                      return null;
                    })()}
                  </div>
                </div>

                <div className="eq-preset-manager-row">
                  <input
                    type="text"
                    placeholder="Nombre para guardar preset..."
                    value={customPresetName}
                    onChange={(e) => setCustomPresetName(e.target.value)}
                    className="eq-preset-input"
                  />
                  <button
                    onClick={handleSaveCustomPreset}
                    className="eq-preset-save-btn"
                  >
                    <Save size={13} />
                    <span>Guardar</span>
                  </button>
                </div>

                <div className="eq-faders-container">
                  {audioEngine.bandFrequencies.map((freq, idx) => (
                    <div key={freq} className="eq-fader-column">
                      <span className="eq-fader-val">
                        {eqBands[idx] > 0 ? `+${eqBands[idx]}` : eqBands[idx]}
                      </span>
                      <input
                        type="range"
                        min="-12"
                        max="12"
                        step="1"
                        value={eqBands[idx]}
                        onChange={(e) => handleBandSlider(idx, e.target.value)}
                        className="eq-vertical-slider"
                      />
                      <span className="eq-fader-freq">
                        {freq >= 1000 ? `${freq / 1000}k` : freq}
                      </span>
                    </div>
                  ))}
                </div>

                <div className="eq-dials-row">
                  <div className="eq-dial-card">
                    <span
                      style={{
                        fontSize: '0.72rem',
                        fontWeight: 700,
                        color: 'var(--phone-text-muted)'
                      }}
                    >
                      BASS BOOST
                    </span>
                    <div className="dial-value-badge">+{bassBoost} dB</div>
                    <input
                      type="range"
                      min="0"
                      max="12"
                      value={bassBoost}
                      onChange={(e) => {
                        setBassBoost(parseInt(e.target.value));
                        setSelectedEqPresetId('custom-active');
                      }}
                      className="range-input"
                    />
                  </div>

                  <div className="eq-dial-card">
                    <span
                      style={{
                        fontSize: '0.72rem',
                        fontWeight: 700,
                        color: 'var(--phone-text-muted)'
                      }}
                    >
                      SONIDO 3D / VIRTUAL
                    </span>
                    <div className="dial-value-badge">
                      {Math.round(virtualizer * 100)}%
                    </div>
                    <input
                      type="range"
                      min="0"
                      max="1"
                      step="0.05"
                      value={virtualizer}
                      onChange={(e) => {
                        setVirtualizer(parseFloat(e.target.value));
                        setSelectedEqPresetId('custom-active');
                      }}
                      className="range-input"
                    />
                  </div>
                </div>

                <div style={{ marginTop: '0.5rem' }}>
                  <h4
                    style={{
                      fontSize: '0.85rem',
                      fontWeight: 800,
                      marginBottom: '0.6rem',
                      color: 'var(--phone-text)'
                    }}
                  >
                    Salida de Audio y Dispositivo (Alta Fidelidad)
                  </h4>
                  <div className="output-profile-grid">
                    {OUTPUT_AUDIO_PROFILES.map((profile) => (
                      <div
                        key={profile.id}
                        className={`output-profile-card ${outputProfile === profile.id ? 'active' : ''}`}
                        onClick={() => setOutputProfile(profile.id)}
                      >
                        <div className="output-profile-title-row">
                          <span>{profile.name}</span>
                          <span className="output-profile-specs">
                            {profile.specs}
                          </span>
                        </div>
                        <span className="output-profile-desc">
                          {profile.description}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {/* PANTALLA 4: AJUSTES COMPLETOS DENTRO DE LA APP */}
            {phoneScreen === 'settings' && (
              <div className="phone-viewport">
                <div className="phone-screen-header">
                  <h2 className="phone-screen-title">Configuracion</h2>
                </div>

                {/* CARGAR IMAGEN DE FONDO PERSONALIZADA */}
                <div className="inapp-settings-section">
                  <div className="inapp-settings-title">
                    <ImageIcon size={16} color="var(--phone-primary)" />
                    <span>Fondo de Pantalla Personalizado</span>
                  </div>
                  <p style={{ fontSize: '0.74rem', color: 'var(--phone-text-muted)', marginBottom: '0.8rem' }}>
                    Sube una foto o imagen desde tu equipo para usar como fondo transparente:
                  </p>
                  <div style={{ display: 'flex', gap: '8px', marginBottom: '0.8rem' }}>
                    <button
                      className="btn-export"
                      onClick={() => wallpaperInputRef.current?.click()}
                      style={{ flex: 1 }}
                    >
                      <Upload size={14} />
                      <span>Cargar Imagen Propia</span>
                    </button>
                    {themeVars.bgImage && (
                      <button
                        className="btn-secondary"
                        onClick={handleClearWallpaper}
                        title="Quitar fondo actual"
                      >
                        <ImageOff size={14} />
                      </button>
                    )}
                  </div>

                  <div className="slider-control-group">
                    <div className="slider-label-row">
                      <span>Desenfoque (Blur)</span>
                      <span style={{ color: '#38bdf8', fontWeight: 700 }}>
                        {themeVars.bgBlur || 16}px
                      </span>
                    </div>
                    <input
                      type="range"
                      min="0"
                      max="40"
                      value={themeVars.bgBlur || 16}
                      onChange={(e) =>
                        setThemeVars((prev) => ({
                          ...prev,
                          bgBlur: parseInt(e.target.value)
                        }))
                      }
                      className="range-input"
                    />
                  </div>

                  <div className="slider-control-group">
                    <div className="slider-label-row">
                      <span>Atenuacion de Fondo</span>
                      <span style={{ color: '#38bdf8', fontWeight: 700 }}>
                        {Math.round((themeVars.bgDim || 0.7) * 100)}%
                      </span>
                    </div>
                    <input
                      type="range"
                      min="0.1"
                      max="0.95"
                      step="0.05"
                      value={themeVars.bgDim || 0.7}
                      onChange={(e) =>
                        setThemeVars((prev) => ({
                          ...prev,
                          bgDim: parseFloat(e.target.value)
                        }))
                      }
                      className="range-input"
                    />
                  </div>
                </div>

                {/* VELOCIDAD DE REPRODUCCION */}
                <div className="inapp-settings-section">
                  <div className="inapp-settings-title">
                    <Gauge size={16} color="var(--phone-primary)" />
                    <span>Velocidad de Reproduccion</span>
                  </div>
                  <div style={{ display: 'flex', gap: '6px', overflowX: 'auto' }}>
                    {[0.5, 0.75, 1.0, 1.25, 1.5, 2.0].map((speed) => (
                      <button
                        key={speed}
                        onClick={() => setPlaybackSpeed(speed)}
                        style={{
                          padding: '6px 12px',
                          borderRadius: '8px',
                          fontSize: '0.75rem',
                          fontWeight: 700,
                          background:
                            playbackSpeed === speed
                              ? 'var(--phone-primary)'
                              : 'rgba(255,255,255,0.06)',
                          color: playbackSpeed === speed ? '#000000' : '#ffffff'
                        }}
                      >
                        {speed === 1.0 ? '1.0x (Normal)' : `${speed}x`}
                      </button>
                    ))}
                  </div>
                </div>

                {/* GUARDAR PRESET DE ESTILO */}
                <div className="inapp-settings-section">
                  <div className="inapp-settings-title">
                    <Save size={16} color="var(--phone-primary)" />
                    <span>Guardar Tu Preset de Estilo</span>
                  </div>
                  <div className="save-style-preset-row">
                    <input
                      type="text"
                      placeholder="Nombre para guardar este tema..."
                      value={newStylePresetName}
                      onChange={(e) => setNewStylePresetName(e.target.value)}
                      className="style-preset-input"
                    />
                    <button
                      onClick={handleSaveStylePreset}
                      className="eq-preset-save-btn"
                    >
                      <Save size={13} />
                      <span>Guardar</span>
                    </button>
                  </div>

                  {customStylePresets.length > 0 && (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '5px', marginTop: '0.5rem' }}>
                      <span style={{ fontSize: '0.7rem', color: 'var(--phone-text-muted)', fontWeight: 700 }}>
                        Presets guardados:
                      </span>
                      {customStylePresets.map((p) => (
                        <div
                          key={p.id}
                          className={`preset-chip ${activePreset === p.id ? 'active' : ''}`}
                          onClick={() => applyPreset(p)}
                          style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}
                        >
                          <span style={{ fontWeight: 700, fontSize: '0.78rem' }}>{p.name}</span>
                          <button
                            onClick={(e) => handleDeleteStylePreset(p.id, e)}
                            style={{ color: '#ef4444', padding: '2px' }}
                            title="Eliminar preset"
                          >
                            <Trash2 size={14} />
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {/* TEMAS PREDEFINIDOS */}
                <div className="inapp-settings-section">
                  <div className="inapp-settings-title">
                    <Palette size={16} color="var(--phone-primary)" />
                    <span>Temas Predefinidos</span>
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '6px' }}>
                    {THEME_PRESETS.map((p) => (
                      <button
                        key={p.id}
                        onClick={() => applyPreset(p)}
                        style={{
                          padding: '8px 10px',
                          borderRadius: '10px',
                          fontSize: '0.74rem',
                          fontWeight: 700,
                          textAlign: 'left',
                          background:
                            activePreset === p.id
                              ? 'var(--phone-primary)'
                              : 'rgba(255,255,255,0.06)',
                          color: activePreset === p.id ? '#000000' : '#ffffff'
                        }}
                      >
                        {p.name}
                      </button>
                    ))}
                  </div>
                </div>

                {/* COLORES DE LA INTERFAZ */}
                <div className="inapp-settings-section">
                  <div className="inapp-settings-title">
                    <SlidersHorizontal size={16} color="var(--phone-primary)" />
                    <span>Colores de la Interfaz</span>
                  </div>

                  <div className="color-picker-row">
                    <span className="color-picker-label">Fondo Principal</span>
                    <div className="color-input-wrap">
                      <span className="color-hex-tag">{themeVars.bgMain}</span>
                      <input
                        type="color"
                        className="color-input-native"
                        value={themeVars.bgMain}
                        onChange={(e) => handleColorChange('bgMain', e.target.value)}
                      />
                    </div>
                  </div>

                  <div className="color-picker-row">
                    <span className="color-picker-label">Superficie y Tarjetas</span>
                    <div className="color-input-wrap">
                      <span className="color-hex-tag">{themeVars.bgSurface}</span>
                      <input
                        type="color"
                        className="color-input-native"
                        value={themeVars.bgSurface}
                        onChange={(e) => handleColorChange('bgSurface', e.target.value)}
                      />
                    </div>
                  </div>

                  <div className="color-picker-row">
                    <span className="color-picker-label">Acento Primario</span>
                    <div className="color-input-wrap">
                      <span className="color-hex-tag">{themeVars.accentPrimary}</span>
                      <input
                        type="color"
                        className="color-input-native"
                        value={themeVars.accentPrimary}
                        onChange={(e) => handleColorChange('accentPrimary', e.target.value)}
                      />
                    </div>
                  </div>

                  <div className="color-picker-row">
                    <span className="color-picker-label">Color de Texto</span>
                    <div className="color-input-wrap">
                      <span className="color-hex-tag">{themeVars.textColor}</span>
                      <input
                        type="color"
                        className="color-input-native"
                        value={themeVars.textColor}
                        onChange={(e) => handleColorChange('textColor', e.target.value)}
                      />
                    </div>
                  </div>
                </div>

                {/* COLOR DEL REPRODUCTOR */}
                <div className="inapp-settings-section">
                  <div className="inapp-settings-title">
                    <Music size={16} color="var(--phone-player-accent)" />
                    <span>Color del Reproductor</span>
                  </div>

                  <div className="color-picker-row">
                    <span className="color-picker-label">Fondo del Reproductor</span>
                    <div className="color-input-wrap">
                      <span className="color-hex-tag">{themeVars.playerBg || themeVars.bgSurface}</span>
                      <input
                        type="color"
                        className="color-input-native"
                        value={themeVars.playerBg || themeVars.bgSurface}
                        onChange={(e) => handleColorChange('playerBg', e.target.value)}
                      />
                    </div>
                  </div>

                  <div className="color-picker-row">
                    <span className="color-picker-label">Botones y Progreso</span>
                    <div className="color-input-wrap">
                      <span className="color-hex-tag">{themeVars.playerAccent || themeVars.accentPrimary}</span>
                      <input
                        type="color"
                        className="color-input-native"
                        value={themeVars.playerAccent || themeVars.accentPrimary}
                        onChange={(e) => handleColorChange('playerAccent', e.target.value)}
                      />
                    </div>
                  </div>
                </div>

                {/* EXPORTAR A FLUTTER */}
                <div className="inapp-settings-section">
                  <div className="inapp-settings-title">
                    <FileCode size={16} color="var(--phone-primary)" />
                    <span>Exportar a Flutter</span>
                  </div>
                  <button
                    className="btn-export"
                    onClick={() => {
                      navigator.clipboard.writeText(generatedFlutterCode);
                      alert('Codigo ThemeData copiado al portapapeles');
                    }}
                  >
                    <Check size={16} />
                    <span>Copiar ThemeData Generado</span>
                  </button>
                </div>
              </div>
            )}

            {/* PANTALLA 5: DESCARGADOR DE YOUTUBE (FASE 1) */}
            {phoneScreen === 'downloader' && (
              <div className="phone-viewport">
                {/* Cabecera */}
                <div className="phone-screen-header">
                  <div>
                    <span
                      style={{
                        fontSize: '0.72rem',
                        color: 'var(--phone-text-muted)',
                        fontWeight: 700,
                        letterSpacing: '0.05em'
                      }}
                    >
                      YOUTUBE
                    </span>
                    <h2 className="phone-screen-title">Descargar Musica</h2>
                  </div>
                  <div
                    style={{
                      width: '36px',
                      height: '36px',
                      borderRadius: '50%',
                      background: 'rgba(255,255,255,0.08)',
                      backdropFilter: 'blur(12px)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      border: '1px solid rgba(255,255,255,0.1)'
                    }}
                  >
                    <Download size={16} color="var(--phone-primary)" />
                  </div>
                </div>

                {/* Aviso si la app no esta visible */}
                {!isAppVisible && (
                  <div className="dl-warning-banner">
                    <AlertCircle size={13} />
                    <span>Descargas pausadas - la aplicacion no esta en primer plano</span>
                  </div>
                )}

                {/* Buscador integrado estilo YouTube */}
                <form className="yt-search-form" onSubmit={handleSearchYouTube}>
                  <div className="yt-search-input-wrap">
                    <Search size={15} className="yt-search-icon" />
                    <input
                      className="yt-search-input"
                      type="text"
                      placeholder="Buscar en YouTube canciones o artistas..."
                      value={ytSearchQuery}
                      onChange={(e) => setYtSearchQuery(e.target.value)}
                    />
                    {ytSearchQuery && (
                      <button
                        type="button"
                        className="yt-search-clear-btn"
                        onClick={() => {
                          setYtSearchQuery('');
                          setSearchResults(TRENDING_SEARCH_RESULTS);
                        }}
                      >
                        <X size={14} />
                      </button>
                    )}
                  </div>
                  <button type="submit" className="yt-search-submit-btn">
                    <span>Buscar</span>
                  </button>
                </form>

                {/* Alternador de enlace URL directo */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', margin: '0.2rem 0 0.6rem' }}>
                  <button
                    type="button"
                    onClick={() => setShowUrlInput((v) => !v)}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: 'var(--phone-primary)',
                      fontSize: '0.7rem',
                      fontWeight: 600,
                      cursor: 'pointer',
                      padding: '2px 0'
                    }}
                  >
                    {showUrlInput ? 'Ocultar enlace directo URL' : 'O pegar enlace directo URL'}
                  </button>
                </div>

                {showUrlInput && (
                  <div className="dl-input-card" style={{ marginBottom: '0.75rem' }}>
                    <div className="dl-input-label">Enlace de YouTube</div>
                    <div className="dl-input-row">
                      <input
                        className="dl-text-input"
                        type="text"
                        placeholder="https://youtu.be/... o pegar URL"
                        value={ytUrl}
                        onChange={(e) => setYtUrl(e.target.value)}
                      />
                    </div>
                    <button
                      className="dl-add-btn"
                      onClick={handleAddToQueue}
                      disabled={!ytUrl.trim()}
                      style={{ marginTop: '0.4rem' }}
                    >
                      <Download size={14} />
                      <span>Descargar desde URL</span>
                    </button>
                  </div>
                )}

                {/* Ajustes predeterminados de calidad y letra LRC */}
                <div className="dl-input-card" style={{ padding: '0.75rem 0.85rem' }}>
                  <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '8px', alignItems: 'center' }}>
                    <div>
                      <div className="dl-input-label" style={{ marginBottom: '3px' }}>Calidad predeterminada</div>
                      <select
                        className="dl-select"
                        value={selectedQuality}
                        onChange={(e) => setSelectedQuality(e.target.value)}
                        style={{ padding: '6px 8px', fontSize: '0.75rem' }}
                      >
                        {QUALITY_OPTIONS.map((opt) => (
                          <option key={opt.id} value={opt.id}>
                            {opt.label}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <div className="dl-input-label" style={{ marginBottom: '3px' }}>Letra .lrc</div>
                      <div
                        className="dl-toggle-row"
                        onClick={() => setIncludeLrc((v) => !v)}
                        style={{ padding: '3px 0' }}
                      >
                        <div className={`dl-toggle-pill ${includeLrc ? 'on' : 'off'}`}>
                          <div className="dl-toggle-knob" />
                        </div>
                        <span className="dl-toggle-label" style={{ fontSize: '0.7rem' }}>
                          Sincronizar .lrc
                        </span>
                      </div>
                    </div>
                  </div>
                </div>

                {/* RESULTADOS DE BUSQUEDA EN YOUTUBE */}
                <div className="dl-section">
                  <div className="dl-section-title">
                    <Music size={13} />
                    <span>
                      {ytSearchQuery.trim()
                        ? `Resultados de YouTube (${searchResults.length})`
                        : 'Sugerencias de YouTube'}
                    </span>
                  </div>

                  {isSearching ? (
                    <div className="dl-empty-state" style={{ padding: '1.2rem 0' }}>
                      <div style={{ fontSize: '0.8rem', color: 'var(--phone-text-muted)' }}>
                        Buscando pistas en YouTube...
                      </div>
                    </div>
                  ) : searchResults.length === 0 ? (
                    <div className="dl-empty-state" style={{ padding: '1.2rem 0' }}>
                      <div style={{ fontSize: '0.8rem', color: 'var(--phone-text-muted)' }}>
                        No se encontraron resultados
                      </div>
                    </div>
                  ) : (
                    <div className="yt-results-list">
                      {searchResults.map((item) => {
                        const isOptionsOpen = activeResultOptionsId === item.id;
                        return (
                          <div key={item.id} className="yt-result-card">
                            <div className="yt-result-top-row">
                              <div className="yt-result-thumb-box">
                                {isOnline && item.thumbnail ? (
                                  <img
                                    src={item.thumbnail}
                                    alt=""
                                    className="yt-result-thumb-img"
                                  />
                                ) : (
                                  <div className="yt-result-thumb-fallback">
                                    <Music size={18} />
                                  </div>
                                )}
                                <span className="yt-result-duration-tag">
                                  {item.duration}
                                </span>
                              </div>

                              <div className="yt-result-details">
                                <div className="yt-result-title">{item.title}</div>
                                <div className="yt-result-artist">{item.artist}</div>
                                <div className="yt-result-channel">
                                  {item.channel} • {item.views}
                                </div>
                              </div>
                            </div>

                            <div className="yt-result-actions-row">
                              <button
                                className="yt-dl-action-btn primary"
                                onClick={() => handleDownloadFromResult(item)}
                              >
                                <Download size={13} />
                                <span>Descargar ({selectedQuality.toUpperCase()})</span>
                              </button>

                              <button
                                className={`yt-dl-action-btn secondary ${isOptionsOpen ? 'active' : ''}`}
                                onClick={() =>
                                  setActiveResultOptionsId((cur) =>
                                    cur === item.id ? null : item.id
                                  )
                                }
                                title="Opciones de descarga personalizadas"
                              >
                                <SlidersHorizontal size={13} />
                                <span>Opciones</span>
                              </button>
                            </div>

                            {/* Opciones personalizadas por pista */}
                            {isOptionsOpen && (
                              <div className="yt-options-panel">
                                <div className="dl-input-label" style={{ marginBottom: '4px' }}>
                                  Elegir formato especifico
                                </div>
                                <div className="yt-quality-chips-grid">
                                  {QUALITY_OPTIONS.map((opt) => (
                                    <button
                                      key={opt.id}
                                      className={`yt-quality-chip ${selectedQuality === opt.id ? 'active' : ''}`}
                                      onClick={() => handleDownloadFromResult(item, opt.id)}
                                    >
                                      <span>{opt.format.toUpperCase()}</span>
                                      <small>{opt.id}</small>
                                    </button>
                                  ))}
                                </div>
                              </div>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>

                {/* Cola activa */}
                {downloadQueue.length > 0 && (
                  <div className="dl-section">
                    <div className="dl-section-title">
                      <Clock size={13} />
                      <span>Cola de Descarga ({downloadQueue.length})</span>
                    </div>
                    <div className="dl-items-list">
                      {downloadQueue.map((item) => (
                        <div key={item.id} className="dl-item-row">
                          <div className="dl-item-thumb-wrap">
                            {item.thumbnail ? (
                              <img
                                src={item.thumbnail}
                                alt=""
                                className="dl-item-thumb"
                                onError={(e) => {
                                  e.target.style.display = 'none';
                                }}
                              />
                            ) : (
                              <div className="dl-item-thumb-placeholder">
                                <Music size={18} />
                              </div>
                            )}
                          </div>

                          <div className="dl-item-info">
                            <input
                              className="dl-meta-input dl-meta-title"
                              value={item.meta.title}
                              onChange={(e) =>
                                handleUpdateDownloadMeta(item.id, 'title', e.target.value)
                              }
                            />
                            <input
                              className="dl-meta-input dl-meta-artist"
                              value={item.meta.artist}
                              onChange={(e) =>
                                handleUpdateDownloadMeta(item.id, 'artist', e.target.value)
                              }
                            />
                            <div className="dl-item-meta-row">
                              <span className="dl-quality-badge">
                                {QUALITY_OPTIONS.find((o) => o.id === item.quality)?.format || item.quality}
                              </span>
                              <span
                                className="dl-status-badge"
                                style={{ color: getStatusColor(item.status) }}
                              >
                                {getStatusLabel(item.status)}
                              </span>
                              {item.includeLrc && (
                                <span className="dl-quality-badge" style={{ color: 'var(--phone-primary)' }}>
                                  LRC
                                </span>
                              )}
                            </div>

                            <div className="dl-progress-track">
                              <div
                                className="dl-progress-fill"
                                style={{
                                  width: `${item.progress}%`,
                                  background: getStatusColor(item.status)
                                }}
                              />
                            </div>
                            <div className="dl-progress-pct">{item.progress}%</div>
                          </div>

                          <button
                            className="dl-cancel-btn"
                            onClick={() => handleCancelDownload(item.id)}
                            title="Cancelar descarga"
                          >
                            <XCircle size={16} />
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Historial de descargas */}
                {downloadHistory.length > 0 && (
                  <div className="dl-section">
                    <div className="dl-section-title">
                      <CheckCircle size={13} />
                      <span>Historial de Descargas ({downloadHistory.length})</span>
                    </div>
                    <div className="dl-items-list">
                      {downloadHistory.map((item) => (
                        <div key={item.id} className="dl-item-row dl-history-row">
                          <div className="dl-item-thumb-wrap">
                            {item.thumbnail ? (
                              <img
                                src={item.thumbnail}
                                alt=""
                                className="dl-item-thumb"
                                onError={(e) => {
                                  e.target.style.display = 'none';
                                }}
                              />
                            ) : (
                              <div className="dl-item-thumb-placeholder">
                                <Music size={18} />
                              </div>
                            )}
                          </div>

                          <div className="dl-item-info">
                            <div className="dl-meta-title-static">{item.meta.title}</div>
                            <div className="dl-meta-artist-static">{item.meta.artist}</div>
                            <div className="dl-item-meta-row">
                              <span className="dl-quality-badge">
                                {QUALITY_OPTIONS.find((o) => o.id === item.quality)?.format || item.quality}
                              </span>
                              <span className="dl-status-badge" style={{ color: '#22c55e' }}>
                                Completado
                              </span>
                              {item.includeLrc && (
                                <span className="dl-quality-badge" style={{ color: '#a78bfa' }}>
                                  LRC
                                </span>
                              )}
                            </div>
                          </div>

                          <div className="dl-history-actions">
                            <button
                              className="dl-lib-btn"
                              onClick={() => handleAddHistoryToLibrary(item)}
                              title="Agregar a libreria"
                            >
                              <Plus size={14} />
                            </button>
                            <button
                              className="dl-cancel-btn"
                              onClick={() => handleDeleteHistory(item.id)}
                              title="Eliminar del historial"
                            >
                              <Trash2 size={14} />
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {downloadQueue.length === 0 && downloadHistory.length === 0 && (
                  <div className="dl-empty-state">
                    <Download size={36} style={{ opacity: 0.25, marginBottom: '0.75rem' }} />
                    <div
                      style={{
                        fontSize: '0.85rem',
                        color: 'var(--phone-text-muted)',
                        textAlign: 'center'
                      }}
                    >
                      Pega un enlace de YouTube o escribe un titulo para descargar musica con portada y letra
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* MINI REPRODUCTOR PERSISTENTE */}
            <div
              className="phone-mini-player"
              onClick={() => setIsPlayerModalOpen(true)}
            >
              <img
                src={currentSong.cover}
                alt=""
                className="mini-thumb"
              />
              <div className="mini-info">
                <div className="mini-title">{currentSong.title}</div>
                <div className="mini-artist">{currentSong.artist}</div>
              </div>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  togglePlay();
                }}
                style={{
                  width: '38px',
                  height: '38px',
                  borderRadius: '50%',
                  background: 'var(--phone-player-accent)',
                  color: '#000000',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}
              >
                {isPlaying ? <Pause size={17} /> : <Play size={17} />}
              </button>
            </div>

            {/* REPRODUCTOR A PANTALLA COMPLETA */}
            {isPlayerModalOpen && (
              <div className="phone-player-modal">
                <div className="phone-modal-nav">
                  <button
                    onClick={() => setIsPlayerModalOpen(false)}
                    style={{ color: 'var(--phone-text)' }}
                  >
                    <ChevronDown size={24} />
                  </button>

                  <span
                    style={{
                      fontSize: '0.74rem',
                      fontWeight: 800,
                      color: 'var(--phone-text-muted)',
                      letterSpacing: '0.04em'
                    }}
                  >
                    {playerTopTab === 0 ? 'REPRODUCIENDO AHORA' : 'LIRICA SINCRONIZADA'}
                  </span>

                  <button
                    onClick={(e) => toggleLike(currentSong.id, e)}
                    style={{
                      color: likedSongIds.includes(currentSong.id)
                        ? 'var(--phone-player-accent)'
                        : 'var(--phone-text-muted)'
                    }}
                  >
                    <Heart
                      size={18}
                      fill={likedSongIds.includes(currentSong.id) ? 'currentColor' : 'none'}
                    />
                  </button>
                </div>

                {/* MITAD SUPERIOR DESLIZABLE: PORTADA <-> LIRICA */}
                <div
                  className="player-top-slider-wrapper"
                  onTouchStart={(e) => handleSwipeStart(e.touches[0].clientX)}
                  onTouchEnd={(e) => handleSwipeEnd(e.changedTouches[0].clientX)}
                  onMouseDown={(e) => handleSwipeStart(e.clientX)}
                  onMouseUp={(e) => handleSwipeEnd(e.clientX)}
                >
                  <div
                    className="player-top-slider-track"
                    style={{
                      transform: `translateX(${playerTopTab === 0 ? '0%' : '-50%'})`
                    }}
                  >
                    {/* SLIDE 1: PORTADA Y CONTROLES */}
                    <div className="player-slide-item">
                      <img
                        src={currentSong.cover}
                        alt=""
                        className="player-big-cover"
                      />

                      <div className="player-track-info-header">
                        <div className="player-main-title">{currentSong.title}</div>
                        <div className="player-main-artist">
                          {currentSong.artist} • {currentSong.album}
                        </div>
                      </div>

                      <div className="player-progress-wrap">
                        <div
                          className="player-progress-bar"
                          onClick={(e) => {
                            const rect = e.currentTarget.getBoundingClientRect();
                            const ratio = (e.clientX - rect.left) / rect.width;
                            if (audioRef.current) {
                              audioRef.current.currentTime = ratio * duration;
                            }
                          }}
                        >
                          <div
                            className="player-progress-fill"
                            style={{
                              width: `${((currentTime / (duration || 1)) * 100).toFixed(1)}%`
                            }}
                          />
                        </div>
                        <div className="player-times-row">
                          <span>{formatTime(currentTime)}</span>
                          <span>{formatTime(duration)}</span>
                        </div>
                      </div>

                      <div className="player-controls-compact">
                        <button
                          onClick={() => {
                            setPhoneScreen('equalizer');
                            setIsPlayerModalOpen(false);
                          }}
                          style={{ color: 'var(--phone-text-muted)' }}
                          title="Ecualizador"
                        >
                          <Sliders size={18} />
                        </button>
                        <button onClick={handlePrev} style={{ color: 'var(--phone-text)' }}>
                          <SkipBack size={24} />
                        </button>
                        <button onClick={togglePlay} className="play-circle-btn">
                          {isPlaying ? <Pause size={24} /> : <Play size={24} />}
                        </button>
                        <button onClick={handleNext} style={{ color: 'var(--phone-text)' }}>
                          <SkipForward size={24} />
                        </button>
                        <button
                          onClick={() => setPlayerTopTab(1)}
                          style={{ color: 'var(--phone-player-accent)' }}
                          title="Deslizar a la lirica"
                        >
                          <FileText size={18} />
                        </button>
                      </div>
                    </div>

                    {/* SLIDE 2: LIRICA SINCRONIZADA CON LUPITA FLOTANTE */}
                    <div className="player-slide-lyrics-wrap">
                      <div className="lyrics-slide-scroll">
                        {activeParsedLyrics.length > 0 ? (
                          activeParsedLyrics.map((item, idx) => (
                            <div
                              key={idx}
                              className={`lyrics-slide-line ${idx === currentLrcLineIndex ? 'active' : ''}`}
                              onClick={() => {
                                if (audioRef.current) {
                                  audioRef.current.currentTime = item.time;
                                }
                              }}
                            >
                              {item.text}
                            </div>
                          ))
                        ) : (
                          <div style={{ color: 'var(--phone-text-muted)', fontSize: '0.8rem', marginTop: '2rem' }}>
                            Desliza o presiona la lupita para buscar el archivo .lrc de letra sincronizada
                          </div>
                        )}
                      </div>

                      <button
                        className="lyrics-search-fab"
                        onClick={handleAutoImportLyric}
                        title="Buscar e importar archivo de letra automaticamente"
                      >
                        <Search size={18} />
                      </button>
                    </div>
                  </div>

                  <div className="slide-indicator-pills">
                    <div
                      className={`slide-dot ${playerTopTab === 0 ? 'active' : ''}`}
                      onClick={() => setPlayerTopTab(0)}
                      title="Ver Portada"
                    />
                    <div
                      className={`slide-dot ${playerTopTab === 1 ? 'active' : ''}`}
                      onClick={() => setPlayerTopTab(1)}
                      title="Ver Lirica Sincronizada"
                    />
                  </div>
                </div>

                {/* MITAD INFERIOR: Playlist en Curso con Arrastre (Drag and Drop) */}
                <div className="player-bottom-playlist-section">
                  <div className="playlist-section-header">
                    <span className="playlist-section-title">
                      Playlist en Curso ({activeQueue.length})
                    </span>
                    <button
                      onClick={() => setShowAddTrackModal(true)}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '4px',
                        fontSize: '0.7rem',
                        fontWeight: 700,
                        color: '#000000',
                        background: 'var(--phone-player-accent)',
                        padding: '3px 8px',
                        borderRadius: '6px'
                      }}
                    >
                      <Plus size={13} />
                      <span>Agregar</span>
                    </button>
                  </div>

                  <div style={{ flex: 1, overflowY: 'auto' }}>
                    {activeQueue.map((song, idx) => {
                      const isCurrent = song.id === currentSong.id;
                      return (
                        <div
                          key={`${song.id}-${idx}`}
                          draggable={true}
                          onDragStart={() => handleDragStart(idx)}
                          onDragOver={handleDragOver}
                          onDrop={() => handleDrop(idx)}
                          className={`queue-item-draggable ${isCurrent ? 'active-playing' : ''}`}
                        >
                          <GripVertical size={16} className="drag-grip-icon" />

                          <img
                            src={song.cover}
                            alt=""
                            className="queue-item-thumb"
                          />

                          <div
                            className="queue-item-info"
                            onClick={() => handleSelectSong(song)}
                            style={{ cursor: 'pointer' }}
                          >
                            <div
                              style={{
                                fontSize: '0.8rem',
                                fontWeight: isCurrent ? 800 : 600,
                                color: isCurrent
                                  ? 'var(--phone-player-accent)'
                                  : 'var(--phone-text)',
                                whiteSpace: 'nowrap',
                                overflow: 'hidden',
                                textOverflow: 'ellipsis'
                              }}
                            >
                              {idx + 1}. {song.title}
                            </div>
                            <div
                              style={{
                                fontSize: '0.68rem',
                                color: 'var(--phone-text-muted)'
                              }}
                            >
                              {song.artist}
                            </div>
                          </div>

                          <div style={{ display: 'flex', alignItems: 'center', gap: '2px' }}>
                            <button
                              className="queue-btn-icon"
                              onClick={() => moveQueueItem(idx, -1)}
                              disabled={idx === 0}
                              title="Subir"
                            >
                              <ArrowUp size={13} />
                            </button>
                            <button
                              className="queue-btn-icon"
                              onClick={() => moveQueueItem(idx, 1)}
                              disabled={idx === activeQueue.length - 1}
                              title="Bajar"
                            >
                              <ArrowDown size={13} />
                            </button>
                            <button
                              className="queue-btn-icon"
                              onClick={(e) => removeQueueItem(idx, e)}
                              title="Quitar"
                              style={{ color: '#f87171' }}
                            >
                              <Trash2 size={13} />
                            </button>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            )}

            {/* BARRA DE NAVEGACION INFERIOR */}
            <nav className="phone-bottom-nav">
              <button
                className={`phone-nav-btn ${phoneScreen === 'home' && !isPlayerModalOpen ? 'active' : ''}`}
                onClick={() => {
                  setPhoneScreen('home');
                  setIsPlayerModalOpen(false);
                }}
              >
                <Music size={18} />
                <span>Inicio</span>
              </button>
              <button
                className={`phone-nav-btn ${phoneScreen === 'library' && !isPlayerModalOpen ? 'active' : ''}`}
                onClick={() => {
                  setPhoneScreen('library');
                  setIsPlayerModalOpen(false);
                }}
              >
                <ListMusic size={18} />
                <span>Libreria</span>
              </button>
              <button
                className={`phone-nav-btn ${phoneScreen === 'equalizer' && !isPlayerModalOpen ? 'active' : ''}`}
                onClick={() => {
                  setPhoneScreen('equalizer');
                  setIsPlayerModalOpen(false);
                }}
              >
                <Sliders size={18} />
                <span>Ecualizador</span>
              </button>
              <button
                className={`phone-nav-btn ${phoneScreen === 'settings' && !isPlayerModalOpen ? 'active' : ''}`}
                onClick={() => {
                  setPhoneScreen('settings');
                  setIsPlayerModalOpen(false);
                }}
              >
                <Palette size={18} />
                <span>Ajustes</span>
              </button>
              <button
                className={`phone-nav-btn ${phoneScreen === 'downloader' && !isPlayerModalOpen ? 'active' : ''}`}
                onClick={() => {
                  setPhoneScreen('downloader');
                  setIsPlayerModalOpen(false);
                }}
              >
                <Download size={18} />
                <span>Descargar</span>
              </button>
            </nav>
          </div>
        </div>
      </main>

      {/* MODAL PARA AGREGAR PISTAS A LA LISTA */}
      {showAddTrackModal && (
        <div
          className="lyrics-modal-backdrop"
          onClick={() => setShowAddTrackModal(false)}
        >
          <div
            className="lyrics-card"
            style={{ maxWidth: '400px', maxHeight: '70vh' }}
            onClick={(e) => e.stopPropagation()}
          >
            <div className="lyrics-header">
              <h3 style={{ fontSize: '1.05rem', fontWeight: 800 }}>
                Agregar Pista a la Lista
              </h3>
              <button
                className="ctrl-btn"
                onClick={() => setShowAddTrackModal(false)}
              >
                <X size={18} />
              </button>
            </div>
            <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '0.8rem' }}>
              Selecciona una pista del catalogo para anadirla al final de la playlist:
            </p>
            <div style={{ overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '6px' }}>
              {songs.map((song) => (
                <div
                  key={song.id}
                  className="phone-item-row"
                  onClick={() => addTrackToQueue(song)}
                  style={{ background: 'rgba(255,255,255,0.03)' }}
                >
                  <img src={song.cover} alt="" className="phone-item-thumb" />
                  <div className="phone-item-info">
                    <div className="phone-item-title">{song.title}</div>
                    <div className="phone-item-subtitle">{song.artist} • {song.album}</div>
                  </div>
                  <Plus size={16} color="var(--phone-primary)" />
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* MODAL PARA CREAR PLAYLIST */}
      {showNewPlaylistModal && (
        <div
          className="lyrics-modal-backdrop"
          onClick={() => setShowNewPlaylistModal(false)}
        >
          <div
            className="lyrics-card"
            style={{ maxWidth: '380px' }}
            onClick={(e) => e.stopPropagation()}
          >
            <h3 style={{ fontSize: '1.1rem', fontWeight: 800, marginBottom: '0.8rem' }}>
              Nueva Lista de Reproduccion
            </h3>
            <input
              type="text"
              placeholder="Nombre de la lista (ej. Favoritos de Estudio)..."
              value={newPlaylistName}
              onChange={(e) => setNewPlaylistName(e.target.value)}
              style={{
                width: '100%',
                background: 'rgba(255,255,255,0.06)',
                border: '1px solid rgba(255,255,255,0.12)',
                borderRadius: '10px',
                padding: '0.75rem 1rem',
                color: 'white',
                marginBottom: '1rem',
                fontSize: '0.9rem'
              }}
            />
            <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
              <button
                className="btn-secondary"
                onClick={() => setShowNewPlaylistModal(false)}
              >
                Cancelar
              </button>
              <button className="btn-primary" onClick={handleCreatePlaylist}>
                Crear Lista
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
