import React, { useState, useRef, useEffect } from 'react';
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
  Image as ImageIcon
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
import './App.css';

export default function App() {
  // LIBRERIA
  const [songs, setSongs] = useState(INITIAL_SONGS);
  const [playlists, setPlaylists] = useState(INITIAL_PLAYLISTS);
  const [folders, setFolders] = useState(FOLDERS);
  const [currentSong, setCurrentSong] = useState(INITIAL_SONGS[0]);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(INITIAL_SONGS[0].duration);
  const [likedSongIds, setLikedSongIds] = useState([1, 3]);

  // NAVEGACION DENTRO DEL TELEFONO
  // 'home' | 'library' | 'equalizer' | 'settings'
  const [phoneScreen, setPhoneScreen] = useState('home');
  const [isPlayerModalOpen, setIsPlayerModalOpen] = useState(false);
  const [libraryTab, setLibraryTab] = useState('tracks'); // 'tracks' | 'playlists' | 'albums' | 'folders'
  const [activePlaylistId, setActivePlaylistId] = useState(null);
  const [newPlaylistName, setNewPlaylistName] = useState('');
  const [showNewPlaylistModal, setShowNewPlaylistModal] = useState(false);

  // CONFIGURACION DEL ESTUDIO / SIMULADOR
  const [deviceScale, setDeviceScale] = useState(0.92);
  const [isFlatDevice, setIsFlatDevice] = useState(false);
  const [sidebarTab, setSidebarTab] = useState('themes'); // 'themes' | 'player-custom' | 'wallpaper' | 'flutter-code'

  // PERSONALIZACION DE LA INTERFAZ
  const [activePreset, setActivePreset] = useState(THEME_PRESETS[0].id);
  const [themeVars, setThemeVars] = useState(THEME_PRESETS[0].vars);

  // ECUALIZADOR Y SONIDO AVANZADO
  const [eqPresets, setEqPresets] = useState(DEFAULT_EQ_PRESETS);
  const [eqEnabled, setEqEnabled] = useState(true);
  const [selectedEqPresetId, setSelectedEqPresetId] = useState(DEFAULT_EQ_PRESETS[0].id);
  const [eqBands, setEqBands] = useState([...DEFAULT_EQ_PRESETS[0].gains]);
  const [bassBoost, setBassBoost] = useState(0);
  const [virtualizer, setVirtualizer] = useState(0);
  const [customPresetName, setCustomPresetName] = useState('');

  // VOLUMEN++ (HASTA 200%) Y PERFIL DE SALIDA
  const [volumePercent, setVolumePercent] = useState(100);
  const [isMuted, setIsMuted] = useState(false);
  const [outputProfile, setOutputProfile] = useState('studio_monitor');

  const audioRef = useRef(null);

  // Inicializar Web Audio Engine
  useEffect(() => {
    if (audioRef.current) {
      audioEngine.init(audioRef.current);
    }
  }, []);

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

  // Aplicar tema completo
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

  // Reproduccion y pausa
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

  const handleSelectSong = (song) => {
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
    const idx = songs.findIndex((s) => s.id === currentSong.id);
    const nextIdx = (idx + 1) % songs.length;
    handleSelectSong(songs[nextIdx]);
  };

  const handlePrev = () => {
    const idx = songs.findIndex((s) => s.id === currentSong.id);
    const prevIdx = (idx - 1 + songs.length) % songs.length;
    handleSelectSong(songs[prevIdx]);
  };

  const toggleLike = (id, e) => {
    e?.stopPropagation();
    setLikedSongIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  // Seleccionar preset de ecualizador
  const handleSelectEqPreset = (presetId) => {
    setSelectedEqPresetId(presetId);
    const found = eqPresets.find((p) => p.id === presetId);
    if (found) {
      setEqBands([...found.gains]);
      setBassBoost(found.bass || 0);
      setVirtualizer(found.virtualizer || 0);
    }
  };

  // Guardar preset personalizado
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

  // Eliminar preset personalizado
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

  // Crear Lista de reproduccion
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

  // Codigo generado para Flutter ThemeData
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

// Configuracion de colores del reproductor
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

        {/* Pestanas del panel */}
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
          {/* PESTANA 1: TEMA GLOBAL */}
          {sidebarTab === 'themes' && (
            <>
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
                  <span className="color-picker-label">Acento Secundario</span>
                  <div className="color-input-wrap">
                    <span className="color-hex-tag">{themeVars.accentSecondary}</span>
                    <input
                      type="color"
                      className="color-input-native"
                      value={themeVars.accentSecondary}
                      onChange={(e) =>
                        handleColorChange('accentSecondary', e.target.value)
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

              <div>
                <div className="studio-section-title">
                  <span>Curvatura y Esquinas</span>
                </div>
                <div className="slider-control-group">
                  <div className="slider-label-row">
                    <span>Radio de Borde</span>
                    <span style={{ color: '#38bdf8', fontWeight: 700 }}>
                      {themeVars.radius}
                    </span>
                  </div>
                  <input
                    type="range"
                    min="4"
                    max="36"
                    step="2"
                    value={parseInt(themeVars.radius) || 16}
                    onChange={(e) =>
                      setThemeVars((prev) => ({
                        ...prev,
                        radius: `${e.target.value}px`
                      }))
                    }
                    className="range-input"
                  />
                </div>
              </div>
            </>
          )}

          {/* PESTANA 2: PERSONALIZACION ESPECIFICA DEL REPRODUCTOR */}
          {sidebarTab === 'player-custom' && (
            <>
              <div>
                <div className="studio-section-title">
                  <span>Color del Reproductor</span>
                </div>
                <p
                  style={{
                    fontSize: '0.78rem',
                    color: '#94a3b8',
                    marginBottom: '1rem',
                    lineHeight: '1.4'
                  }}
                >
                  Configura los colores independientes del mini-reproductor y la
                  pantalla completa de reproduccion:
                </p>

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

          {/* PESTANA 3: FONDO CON IMAGEN */}
          {sidebarTab === 'wallpaper' && (
            <>
              <div>
                <div className="studio-section-title">
                  <span>Imagenes de Fondo</span>
                </div>
                <p
                  style={{
                    fontSize: '0.78rem',
                    color: '#94a3b8',
                    marginBottom: '0.8rem'
                  }}
                >
                  Selecciona una imagen de fondo o pega una direccion web:
                </p>
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

                <div style={{ marginTop: '0.8rem' }}>
                  <span
                    style={{
                      fontSize: '0.78rem',
                      color: '#cbd5e1',
                      display: 'block',
                      marginBottom: '0.4rem'
                    }}
                  >
                    URL de imagen personalizada:
                  </span>
                  <input
                    type="text"
                    placeholder="https://..."
                    value={themeVars.bgImage || ''}
                    onChange={(e) =>
                      setThemeVars((prev) => ({ ...prev, bgImage: e.target.value }))
                    }
                    style={{
                      width: '100%',
                      background: 'rgba(255,255,255,0.06)',
                      border: '1px solid rgba(255,255,255,0.12)',
                      borderRadius: '8px',
                      padding: '0.5rem 0.75rem',
                      color: 'white',
                      fontSize: '0.78rem'
                    }}
                  />
                </div>
              </div>

              <div>
                <div className="studio-section-title">
                  <span>Ajustes del Fondo</span>
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
                    <span>Opacidad de Atenuacion</span>
                    <span style={{ color: '#38bdf8', fontWeight: 700 }}>
                      {Math.round((themeVars.bgDim || 0.8) * 100)}%
                    </span>
                  </div>
                  <input
                    type="range"
                    min="0.2"
                    max="0.95"
                    step="0.05"
                    value={themeVars.bgDim || 0.8}
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
            </>
          )}

          {/* PESTANA 4: CODIGO FLUTTER */}
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
            {/* Capa de imagen de fondo configurable */}
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
                  style={{ opacity: themeVars.bgDim || 0.8 }}
                />
              </>
            )}

            {/* Dynamic Island */}
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

            {/* Barra de estado */}
            <div className="phone-status-bar">
              <span>9:41</span>
              <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
                <Zap size={12} color="var(--phone-primary)" />
                <span>5G</span>
                <span>100%</span>
              </div>
            </div>

            {/* PANTALLA 1: INICIO */}
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
                    <h2 className="phone-screen-title">Bienvenido</h2>
                  </div>
                  <div
                    style={{
                      width: '36px',
                      height: '36px',
                      borderRadius: '50%',
                      background: 'var(--phone-surface)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      border: '1px solid rgba(255,255,255,0.08)'
                    }}
                  >
                    <Heart size={16} color="var(--phone-primary)" />
                  </div>
                </div>

                {/* Tarjeta destacada simetrica */}
                <div
                  style={{
                    background: 'var(--phone-surface)',
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

                {/* Accesos rapidos de estado de animo */}
                <div
                  style={{
                    display: 'flex',
                    gap: '8px',
                    overflowX: 'auto',
                    marginBottom: '1.2rem',
                    paddingBottom: '4px'
                  }}
                >
                  <span
                    style={{
                      padding: '6px 14px',
                      borderRadius: '999px',
                      background: 'var(--phone-surface)',
                      fontSize: '0.75rem',
                      fontWeight: 700,
                      color: 'var(--phone-primary)',
                      border: '1px solid var(--phone-primary)'
                    }}
                  >
                    Energia
                  </span>
                  <span
                    style={{
                      padding: '6px 14px',
                      borderRadius: '999px',
                      background: 'var(--phone-surface)',
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      color: 'var(--phone-text-muted)'
                    }}
                  >
                    Lo-Fi
                  </span>
                  <span
                    style={{
                      padding: '6px 14px',
                      borderRadius: '999px',
                      background: 'var(--phone-surface)',
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      color: 'var(--phone-text-muted)'
                    }}
                  >
                    Meditacion
                  </span>
                </div>

                {/* Lista reciente simetrica */}
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
                    onClick={() => handleSelectSong(song)}
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

                {/* Pestanas: Pistas | Listas | Albumes | Carpetas */}
                <div className="library-subtabs">
                  <button
                    className={`lib-subtab-pill ${libraryTab === 'tracks' ? 'active' : ''}`}
                    onClick={() => {
                      setLibraryTab('tracks');
                      setActivePlaylistId(null);
                    }}
                  >
                    Pistas ({songs.length})
                  </button>
                  <button
                    className={`lib-subtab-pill ${libraryTab === 'playlists' ? 'active' : ''}`}
                    onClick={() => setLibraryTab('playlists')}
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
                    }}
                  >
                    Carpetas
                  </button>
                </div>

                {/* CONTENIDO PISTAS */}
                {libraryTab === 'tracks' && (
                  <div>
                    {songs.map((song) => (
                      <div
                        key={song.id}
                        className="phone-item-row"
                        onClick={() => handleSelectSong(song)}
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
                              : 'var(--phone-text-muted)'
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
                    ))}
                  </div>
                )}

                {/* CONTENIDO LISTAS (PLAYLISTS) */}
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
                                  marginBottom: '1rem',
                                  background: 'var(--phone-surface)',
                                  padding: '12px',
                                  borderRadius: 'var(--phone-radius)'
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
                                  <div style={{ fontWeight: 800, fontSize: '1rem' }}>
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
                                  onClick={() => handleSelectSong(song)}
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

                {/* CONTENIDO ALBUMES */}
                {libraryTab === 'albums' && (
                  <div className="phone-card-grid">
                    {Array.from(new Set(songs.map((s) => s.album))).map(
                      (albumName, i) => {
                        const albumSong = songs.find((s) => s.album === albumName);
                        return (
                          <div key={i} className="phone-grid-card">
                            <img
                              src={albumSong?.cover}
                              alt=""
                              className="phone-grid-thumb"
                            />
                            <div
                              style={{
                                fontWeight: 700,
                                fontSize: '0.82rem',
                                color: 'var(--phone-text)'
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
                              {albumSong?.artist}
                            </div>
                          </div>
                        );
                      }
                    )}
                  </div>
                )}

                {/* CONTENIDO CARPETAS */}
                {libraryTab === 'folders' && (
                  <div>
                    <div
                      style={{
                        padding: '10px 14px',
                        background: 'rgba(255,255,255,0.03)',
                        borderRadius: 'var(--phone-radius)',
                        marginBottom: '1rem',
                        fontSize: '0.78rem',
                        color: 'var(--phone-text-muted)'
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
                            background: 'var(--phone-surface)',
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

            {/* PANTALLA 3: ECUALIZADOR CON VOLUMEN++ Y GESTION DE PRESETS */}
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

                {/* VOLUMEN++ HASTA 200% */}
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

                {/* SELECTOR DE PRESETS CON BOTON DE ELIMINAR */}
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

                    {/* Boton para eliminar preset personalizado */}
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

                {/* GUARDAR PRESET ACTUAL */}
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

                {/* 10 FADERS VERTICALES */}
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

                {/* BASS BOOST Y VIRTUALIZADOR 3D */}
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

                {/* AJUSTES DE SALIDA DE AUDIO (AUDIFONOS DE BUENA CALIDAD) */}
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

            {/* PANTALLA 4: AJUSTES */}
            {phoneScreen === 'settings' && (
              <div className="phone-viewport">
                <div className="phone-screen-header">
                  <h2 className="phone-screen-title">Ajustes</h2>
                </div>

                <div
                  style={{
                    background: 'var(--phone-surface)',
                    borderRadius: 'var(--phone-radius)',
                    padding: '1.2rem',
                    marginBottom: '1rem',
                    border: '1px solid rgba(255,255,255,0.08)'
                  }}
                >
                  <h4
                    style={{
                      fontSize: '0.88rem',
                      fontWeight: 800,
                      marginBottom: '0.5rem',
                      color: 'var(--phone-text)'
                    }}
                  >
                    Perfil de Audio Activo
                  </h4>
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      fontSize: '0.8rem',
                      padding: '8px 0',
                      borderBottom: '1px solid rgba(255,255,255,0.06)'
                    }}
                  >
                    <span style={{ color: 'var(--phone-text-muted)' }}>
                      Modo Seleccionado
                    </span>
                    <span style={{ color: 'var(--phone-primary)', fontWeight: 700 }}>
                      {OUTPUT_AUDIO_PROFILES.find((p) => p.id === outputProfile)?.name}
                    </span>
                  </div>
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      fontSize: '0.8rem',
                      padding: '8px 0',
                      borderBottom: '1px solid rgba(255,255,255,0.06)'
                    }}
                  >
                    <span style={{ color: 'var(--phone-text-muted)' }}>
                      Volumen++ Amplificado
                    </span>
                    <span style={{ color: '#38bdf8', fontWeight: 700 }}>
                      {volumePercent}%
                    </span>
                  </div>
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      fontSize: '0.8rem',
                      padding: '8px 0'
                    }}
                  >
                    <span style={{ color: 'var(--phone-text-muted)' }}>
                      Transicion Suave (Crossfade)
                    </span>
                    <span style={{ color: 'var(--phone-text)', fontWeight: 600 }}>
                      2.5 segundos
                    </span>
                  </div>
                </div>
              </div>
            )}

            {/* MINI REPRODUCTOR PERSISTENTE CON COLOR CONFIGURABLE */}
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

            {/* REPRODUCTOR A PANTALLA COMPLETA CON COLOR CONFIGURABLE */}
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
                      fontSize: '0.75rem',
                      fontWeight: 700,
                      color: 'var(--phone-text-muted)'
                    }}
                  >
                    REPRODUCIENDO AHORA
                  </span>
                  <button
                    onClick={(e) => toggleLike(currentSong.id, e)}
                    style={{
                      color: likedSongIds.includes(currentSong.id)
                        ? 'var(--phone-primary)'
                        : 'var(--phone-text-muted)'
                    }}
                  >
                    <Heart
                      size={20}
                      fill={
                        likedSongIds.includes(currentSong.id)
                          ? 'currentColor'
                          : 'none'
                      }
                    />
                  </button>
                </div>

                <img
                  src={currentSong.cover}
                  alt=""
                  className="player-big-cover"
                />

                <div style={{ marginBottom: '1.2rem', textAlign: 'center' }}>
                  <h3
                    style={{
                      fontSize: '1.35rem',
                      fontWeight: 800,
                      color: 'var(--phone-text)',
                      marginBottom: '0.2rem'
                    }}
                  >
                    {currentSong.title}
                  </h3>
                  <p
                    style={{
                      fontSize: '0.88rem',
                      color: 'var(--phone-text-muted)'
                    }}
                  >
                    {currentSong.artist} • {currentSong.album}
                  </p>
                </div>

                {/* Barra de progreso */}
                <div style={{ marginBottom: '1.2rem' }}>
                  <div
                    style={{
                      height: '6px',
                      background: 'rgba(255,255,255,0.1)',
                      borderRadius: '999px',
                      overflow: 'hidden',
                      marginBottom: '0.4rem',
                      cursor: 'pointer'
                    }}
                    onClick={(e) => {
                      const rect = e.currentTarget.getBoundingClientRect();
                      const ratio = (e.clientX - rect.left) / rect.width;
                      if (audioRef.current) {
                        audioRef.current.currentTime = ratio * duration;
                      }
                    }}
                  >
                    <div
                      style={{
                        height: '100%',
                        width: `${((currentTime / (duration || 1)) * 100).toFixed(1)}%`,
                        background: 'var(--phone-player-accent)'
                      }}
                    />
                  </div>
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      fontSize: '0.72rem',
                      color: 'var(--phone-text-muted)'
                    }}
                  >
                    <span>{formatTime(currentTime)}</span>
                    <span>{formatTime(duration)}</span>
                  </div>
                </div>

                {/* Controles principales simetricos */}
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '0 0.5rem',
                    marginBottom: '1rem'
                  }}
                >
                  <button
                    onClick={() => {
                      setPhoneScreen('equalizer');
                      setIsPlayerModalOpen(false);
                    }}
                    style={{ color: 'var(--phone-text-muted)' }}
                    title="Ecualizador"
                  >
                    <Sliders size={20} />
                  </button>
                  <button onClick={handlePrev} style={{ color: 'var(--phone-text)' }}>
                    <SkipBack size={26} />
                  </button>
                  <button
                    onClick={togglePlay}
                    style={{
                      width: '64px',
                      height: '64px',
                      borderRadius: '50%',
                      background: 'var(--phone-player-accent)',
                      color: '#000000',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      boxShadow: '0 8px 25px rgba(0,0,0,0.5)'
                    }}
                  >
                    {isPlaying ? <Pause size={28} /> : <Play size={28} />}
                  </button>
                  <button onClick={handleNext} style={{ color: 'var(--phone-text)' }}>
                    <SkipForward size={26} />
                  </button>
                  <button
                    onClick={() => {
                      setSidebarTab('player-custom');
                    }}
                    style={{ color: 'var(--phone-text-muted)' }}
                    title="Color del Reproductor"
                  >
                    <Palette size={20} />
                  </button>
                </div>

                {/* Control de Volumen++ rapido dentro del reproductor */}
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '10px',
                    padding: '0 0.5rem'
                  }}
                >
                  <Volume2 size={16} color="var(--phone-text-muted)" />
                  <input
                    type="range"
                    min="0"
                    max="200"
                    value={volumePercent}
                    onChange={(e) => setVolumePercent(parseInt(e.target.value))}
                    className="range-input"
                  />
                  <span
                    style={{
                      fontSize: '0.72rem',
                      fontFamily: 'monospace',
                      color: '#38bdf8',
                      minWidth: '42px',
                      textAlign: 'right'
                    }}
                  >
                    {volumePercent}%
                  </span>
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
            </nav>
          </div>
        </div>
      </main>

      {/* MODAL PARA CREAR LISTA DE REPRODUCCION */}
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
