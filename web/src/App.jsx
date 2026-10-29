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
  Radio,
  Search,
  Maximize2,
  ChevronDown,
  Layers,
  Sparkles,
  RotateCcw,
  Plus,
  FileCode,
  Smartphone,
  Eye,
  Check,
  Zap,
  Flame,
  Volume1,
  Share2
} from 'lucide-react';

import {
  INITIAL_SONGS,
  INITIAL_PLAYLISTS,
  FOLDERS,
  THEME_PRESETS
} from './data/songs';
import { audioEngine, EQ_PRESETS } from './utils/audioEqualizer';
import { synth } from './utils/audioSynthesizer';
import './App.css';

export default function App() {
  // --- LIBRARY STATE ---
  const [songs, setSongs] = useState(INITIAL_SONGS);
  const [playlists, setPlaylists] = useState(INITIAL_PLAYLISTS);
  const [folders, setFolders] = useState(FOLDERS);
  const [currentSong, setCurrentSong] = useState(INITIAL_SONGS[0]);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(INITIAL_SONGS[0].duration);
  const [likedSongIds, setLikedSongIds] = useState([1, 3]);

  // --- NAVIGATION STATE (INSIDE PHONE) ---
  // Screens: 'home' | 'library' | 'equalizer' | 'settings'
  const [phoneScreen, setPhoneScreen] = useState('home');
  const [isPlayerModalOpen, setIsPlayerModalOpen] = useState(false);
  const [libraryTab, setLibraryTab] = useState('tracks'); // 'tracks' | 'playlists' | 'albums' | 'folders'
  const [newPlaylistName, setNewPlaylistName] = useState('');
  const [showNewPlaylistModal, setShowNewPlaylistModal] = useState(false);

  // --- STUDIO / SIMULATOR SETTINGS ---
  const [deviceScale, setDeviceScale] = useState(0.92);
  const [isFlatDevice, setIsFlatDevice] = useState(false);
  const [sidebarTab, setSidebarTab] = useState('themes'); // 'themes' | 'flutter-code' | 'presets'

  // --- EXTREME THEME ENGINE STATE ---
  const [activePreset, setActivePreset] = useState(THEME_PRESETS[0].id);
  const [themeVars, setThemeVars] = useState(THEME_PRESETS[0].vars);

  // --- EQUALIZER STATE ---
  const [eqEnabled, setEqEnabled] = useState(true);
  const [eqPreset, setEqPreset] = useState(EQ_PRESETS[0].name);
  const [eqBands, setEqBands] = useState([...EQ_PRESETS[0].gains]);
  const [bassBoost, setBassBoost] = useState(0);
  const [virtualizer, setVirtualizer] = useState(0);

  const audioRef = useRef(null);

  // Initialize Web Audio Engine with Audio element
  useEffect(() => {
    if (audioRef.current) {
      audioEngine.init(audioRef.current);
    }
  }, []);

  // Sync EQ values with Audio Engine
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

  // Apply Theme Presets
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

  // Play / Pause handling
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

  // Equalizer Preset selection
  const handleSelectEqPreset = (presetName) => {
    setEqPreset(presetName);
    const found = EQ_PRESETS.find((p) => p.name === presetName);
    if (found) {
      setEqBands([...found.gains]);
      setBassBoost(found.bass || 0);
      setVirtualizer(found.virtualizer || 0);
    }
  };

  const handleBandSlider = (index, value) => {
    const newBands = [...eqBands];
    newBands[index] = parseFloat(value);
    setEqBands(newBands);
    setEqPreset('Personalizado');
  };

  // Create Playlist
  const handleCreatePlaylist = () => {
    if (!newPlaylistName.trim()) return;
    const newPl = {
      id: `pl-${Date.now()}`,
      name: newPlaylistName.trim(),
      songIds: [currentSong.id],
      cover: currentSong.cover,
      description: 'Lista personalizada creada en el prototipo'
    };
    setPlaylists([newPl, ...playlists]);
    setNewPlaylistName('');
    setShowNewPlaylistModal(false);
  };

  // Format MM:SS
  const formatTime = (secs) => {
    if (!secs || isNaN(secs)) return '0:00';
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  // Generate Flutter ThemeData Code snippet dynamically
  const generatedFlutterCode = `// Código generado para Flutter (ThemeData)
// Proyecto: Música para los Oídos

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
);`;

  return (
    <div
      className="workbench-container"
      style={{
        '--phone-bg': themeVars.bgMain,
        '--phone-surface': themeVars.bgSurface,
        '--phone-elevated': themeVars.bgElevated,
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

      {/* LEFT STUDIO PANEL: THEME & FLUTTER CUSTOMIZER */}
      <aside className="studio-sidebar">
        <div className="studio-sidebar-header">
          <div className="studio-title-wrap">
            <div className="studio-logo">
              <Smartphone size={20} />
            </div>
            <div>
              <h2 className="studio-brand-title">Música para los Oídos</h2>
              <span className="studio-brand-badge">Flutter UI Simulator</span>
            </div>
          </div>
        </div>

        {/* Sidebar Nav Tabs */}
        <div className="studio-nav-tabs">
          <button
            className={`studio-tab-btn ${sidebarTab === 'themes' ? 'active' : ''}`}
            onClick={() => setSidebarTab('themes')}
          >
            <Palette size={15} />
            <span>Personalizar UI</span>
          </button>
          <button
            className={`studio-tab-btn ${sidebarTab === 'flutter-code' ? 'active' : ''}`}
            onClick={() => setSidebarTab('flutter-code')}
          >
            <FileCode size={15} />
            <span>Código Flutter</span>
          </button>
        </div>

        <div className="studio-panel-body">
          {sidebarTab === 'themes' ? (
            <>
              {/* Presets Grid */}
              <div>
                <div className="studio-section-title">
                  <span>Presets de Diseño</span>
                  <Sparkles size={14} color="#a855f7" />
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

              {/* Granular Color Customizer */}
              <div>
                <div className="studio-section-title">
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
                  <span className="color-picker-label">Tarjetas / Superficie</span>
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
                  <span className="color-picker-label">Color de Texto</span>
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

              {/* Geometry & Border Radius */}
              <div>
                <div className="studio-section-title">
                  <span>Curvatura y Esquinas</span>
                </div>
                <div className="slider-control-group">
                  <div className="slider-label-row">
                    <span>Border Radius</span>
                    <span style={{ color: '#c084fc', fontWeight: 700 }}>
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

              {/* Tipografía */}
              <div>
                <div className="studio-section-title">
                  <span>Tipografía Móvil</span>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                  <button
                    className={`cat-pill ${themeVars.fontFamily.includes('Outfit') ? 'active' : ''}`}
                    onClick={() =>
                      setThemeVars((prev) => ({
                        ...prev,
                        fontFamily: "'Outfit', sans-serif"
                      }))
                    }
                  >
                    Outfit (Moderna)
                  </button>
                  <button
                    className={`cat-pill ${themeVars.fontFamily.includes('Plus Jakarta') ? 'active' : ''}`}
                    onClick={() =>
                      setThemeVars((prev) => ({
                        ...prev,
                        fontFamily: "'Plus Jakarta Sans', sans-serif"
                      }))
                    }
                  >
                    Jakarta (Clean)
                  </button>
                </div>
              </div>
            </>
          ) : (
            /* FLUTTER EXPORT TAB */
            <div>
              <div className="studio-section-title">
                <span>ThemeData para Flutter</span>
              </div>
              <p
                style={{
                  fontSize: '0.8rem',
                  color: '#94a3b8',
                  lineHeight: '1.4',
                  marginBottom: '1rem'
                }}
              >
                Copia este bloque de código directamente a tu proyecto Flutter cuando
                comencemos a compilar la app nativa:
              </p>
              <pre className="flutter-code-preview">{generatedFlutterCode}</pre>
              <button
                className="btn-export"
                style={{ marginTop: '1rem' }}
                onClick={() => {
                  navigator.clipboard.writeText(generatedFlutterCode);
                  alert('¡Código Flutter copiado al portapapeles!');
                }}
              >
                <Check size={16} />
                <span>Copiar Código ThemeData</span>
              </button>
            </div>
          )}
        </div>
      </aside>

      {/* CENTER STAGE: SMARTPHONE SIMULATOR */}
      <main className="simulator-stage">
        {/* Stage Floating Toolbar */}
        <div className="stage-toolbar">
          <div
            className={`stage-tool-item ${phoneScreen === 'home' ? 'active' : ''}`}
            onClick={() => setPhoneScreen('home')}
          >
            <Music size={14} />
            <span>Inicio</span>
          </div>
          <div
            className={`stage-tool-item ${phoneScreen === 'library' ? 'active' : ''}`}
            onClick={() => setPhoneScreen('library')}
          >
            <ListMusic size={14} />
            <span>Librería</span>
          </div>
          <div
            className={`stage-tool-item ${phoneScreen === 'equalizer' ? 'active' : ''}`}
            onClick={() => setPhoneScreen('equalizer')}
          >
            <Sliders size={14} />
            <span>Ecualizador</span>
          </div>
          <div
            className={`stage-tool-item ${phoneScreen === 'settings' ? 'active' : ''}`}
            onClick={() => setPhoneScreen('settings')}
          >
            <Palette size={14} />
            <span>Ajustes</span>
          </div>
          <div style={{ width: '1px', height: '16px', background: 'rgba(255,255,255,0.1)' }} />
          <div
            className="stage-tool-item"
            onClick={() => setIsFlatDevice(!isFlatDevice)}
            title="Alternar marco de teléfono o vista plana"
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

        {/* DEVICE FRAME */}
        <div
          className="device-scaler"
          style={{ transform: `scale(${deviceScale})` }}
        >
          <div
            className={`mobile-phone-frame ${isFlatDevice ? 'flat-mode' : ''}`}
          >
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

            {/* Status Bar */}
            <div className="phone-status-bar">
              <span>9:41</span>
              <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
                <Zap size={12} color="var(--phone-primary)" />
                <span>5G</span>
                <span>100%</span>
              </div>
            </div>

            {/* SCREEN 1: HOME */}
            {phoneScreen === 'home' && (
              <div className="phone-viewport">
                <div className="phone-screen-header">
                  <div>
                    <span
                      style={{
                        fontSize: '0.75rem',
                        color: 'var(--phone-text-muted)',
                        fontWeight: 600
                      }}
                    >
                      MÚSICA PARA LOS OÍDOS
                    </span>
                    <h2 className="phone-screen-title">Buenas noches</h2>
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
                      border: '1px solid rgba(255,255,255,0.1)'
                    }}
                  >
                    <Heart size={16} color="var(--phone-primary)" />
                  </div>
                </div>

                {/* Featured Track Card */}
                <div
                  style={{
                    background: 'var(--phone-surface)',
                    borderRadius: 'var(--phone-radius)',
                    padding: '1rem',
                    marginBottom: '1.25rem',
                    border: '1px solid rgba(255,255,255,0.08)',
                    position: 'relative',
                    overflow: 'hidden'
                  }}
                >
                  <img
                    src={currentSong.cover}
                    alt=""
                    style={{
                      width: '100%',
                      height: '140px',
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
                        width: '42px',
                        height: '42px',
                        borderRadius: '50%',
                        background: 'var(--phone-primary)',
                        color: '#000000',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                      }}
                    >
                      {isPlaying ? <Pause size={18} /> : <Play size={18} />}
                    </button>
                  </div>
                </div>

                {/* Mood Quick Pills */}
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
                      padding: '6px 12px',
                      borderRadius: '999px',
                      background: 'var(--phone-surface)',
                      fontSize: '0.75rem',
                      fontWeight: 700,
                      color: 'var(--phone-primary)',
                      border: '1px solid var(--phone-primary)'
                    }}
                  >
                    ⚡ Energía
                  </span>
                  <span
                    style={{
                      padding: '6px 12px',
                      borderRadius: '999px',
                      background: 'var(--phone-surface)',
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      color: 'var(--phone-text-muted)'
                    }}
                  >
                    ☕ Lo-Fi
                  </span>
                  <span
                    style={{
                      padding: '6px 12px',
                      borderRadius: '999px',
                      background: 'var(--phone-surface)',
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      color: 'var(--phone-text-muted)'
                    }}
                  >
                    🌿 Meditación
                  </span>
                </div>

                {/* Recent Tracks List */}
                <h4
                  style={{
                    fontSize: '0.85rem',
                    fontWeight: 800,
                    marginBottom: '0.75rem',
                    color: 'var(--phone-text)'
                  }}
                >
                  Escuchado recientemente
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

            {/* SCREEN 2: LIBRARY (SISTEMA DE LIBRERÍAS) */}
            {phoneScreen === 'library' && (
              <div className="phone-viewport">
                <div className="phone-screen-header">
                  <h2 className="phone-screen-title">Tu Biblioteca</h2>
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
                    title="Crear Playlist"
                  >
                    <Plus size={18} />
                  </button>
                </div>

                {/* Subtabs: Tracks | Playlists | Albums | Folders */}
                <div className="library-subtabs">
                  <button
                    className={`lib-subtab-pill ${libraryTab === 'tracks' ? 'active' : ''}`}
                    onClick={() => setLibraryTab('tracks')}
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
                    onClick={() => setLibraryTab('albums')}
                  >
                    Álbumes
                  </button>
                  <button
                    className={`lib-subtab-pill ${libraryTab === 'folders' ? 'active' : ''}`}
                    onClick={() => setLibraryTab('folders')}
                  >
                    Carpetas
                  </button>
                </div>

                {/* Subtab Content: TRACKS */}
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

                {/* Subtab Content: PLAYLISTS */}
                {libraryTab === 'playlists' && (
                  <div className="phone-card-grid">
                    {playlists.map((pl) => (
                      <div key={pl.id} className="phone-grid-card">
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
                          {pl.songIds.length} canciones
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {/* Subtab Content: ALBUMS */}
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

                {/* Subtab Content: FOLDERS / STORAGE SCANNER */}
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
                      Explorador de almacenamiento del móvil:
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

            {/* SCREEN 3: EQUALIZER (ECUALIZADOR GRÁFICO 10 BANDAS) */}
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
                      Procesador de audio en vivo
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

                {/* Preset Dropdown */}
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
                  <select
                    className="eq-preset-select"
                    value={eqPreset}
                    onChange={(e) => handleSelectEqPreset(e.target.value)}
                  >
                    {EQ_PRESETS.map((p) => (
                      <option key={p.name} value={p.name}>
                        {p.name}
                      </option>
                    ))}
                    <option value="Personalizado">Personalizado</option>
                  </select>
                </div>

                {/* 10 Vertical Faders */}
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

                {/* Bass Boost & Virtualizer 3D Controls */}
                <div className="eq-dials-row">
                  <div className="eq-dial-card">
                    <span
                      style={{
                        fontSize: '0.75rem',
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
                        setEqPreset('Personalizado');
                      }}
                      className="range-input"
                    />
                  </div>

                  <div className="eq-dial-card">
                    <span
                      style={{
                        fontSize: '0.75rem',
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
                        setEqPreset('Personalizado');
                      }}
                      className="range-input"
                    />
                  </div>
                </div>
              </div>
            )}

            {/* SCREEN 4: SETTINGS (AJUSTES INTERFAZ MÓVIL) */}
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
                    Tema Visual Activo
                  </h4>
                  <p
                    style={{
                      fontSize: '0.75rem',
                      color: 'var(--phone-text-muted)',
                      marginBottom: '1rem'
                    }}
                  >
                    Usa el panel de la izquierda para cambiar colores en tiempo real.
                  </p>
                  <div
                    style={{
                      display: 'flex',
                      gap: '8px',
                      flexWrap: 'wrap'
                    }}
                  >
                    {THEME_PRESETS.map((p) => (
                      <button
                        key={p.id}
                        onClick={() => applyPreset(p)}
                        style={{
                          padding: '6px 12px',
                          borderRadius: '8px',
                          fontSize: '0.75rem',
                          fontWeight: 700,
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

                <div
                  style={{
                    background: 'var(--phone-surface)',
                    borderRadius: 'var(--phone-radius)',
                    padding: '1.2rem',
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
                    Calidad de Audio
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
                      Reproducción Hi-Fi FLAC
                    </span>
                    <span style={{ color: 'var(--phone-primary)', fontWeight: 700 }}>
                      Activada
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
                      Transición suave (Crossfade)
                    </span>
                    <span style={{ color: 'var(--phone-text)', fontWeight: 600 }}>
                      2.5s
                    </span>
                  </div>
                </div>
              </div>
            )}

            {/* MINI PERSISTENT PLAYER BAR */}
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
                  width: '36px',
                  height: '36px',
                  borderRadius: '50%',
                  background: 'var(--phone-primary)',
                  color: '#000000',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}
              >
                {isPlaying ? <Pause size={16} /> : <Play size={16} />}
              </button>
            </div>

            {/* FULL-SCREEN EXPANDED PLAYER MODAL */}
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
                      fontSize: '0.8rem',
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

                <div style={{ marginBottom: '1.5rem' }}>
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
                      fontSize: '0.9rem',
                      color: 'var(--phone-text-muted)'
                    }}
                  >
                    {currentSong.artist}
                  </p>
                </div>

                {/* Scrubber / Progress */}
                <div style={{ marginBottom: '1.5rem' }}>
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
                        background: 'var(--phone-primary)'
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

                {/* Big Controls */}
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '0 1rem'
                  }}
                >
                  <button
                    onClick={() => {
                      setPhoneScreen('equalizer');
                      setIsPlayerModalOpen(false);
                    }}
                    style={{ color: 'var(--phone-text-muted)' }}
                    title="Abrir ecualizador"
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
                      background: 'var(--phone-primary)',
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
                    onClick={() => alert(`Letra: ${currentSong.lyrics[0]}`)}
                    style={{ color: 'var(--phone-text-muted)' }}
                  >
                    <Music size={20} />
                  </button>
                </div>
              </div>
            )}

            {/* BOTTOM NAV BAR */}
            <nav className="phone-bottom-nav">
              <button
                className={`phone-nav-btn ${phoneScreen === 'home' ? 'active' : ''}`}
                onClick={() => setPhoneScreen('home')}
              >
                <Music size={18} />
                <span>Inicio</span>
              </button>
              <button
                className={`phone-nav-btn ${phoneScreen === 'library' ? 'active' : ''}`}
                onClick={() => setPhoneScreen('library')}
              >
                <ListMusic size={18} />
                <span>Librería</span>
              </button>
              <button
                className={`phone-nav-btn ${phoneScreen === 'equalizer' ? 'active' : ''}`}
                onClick={() => setPhoneScreen('equalizer')}
              >
                <Sliders size={18} />
                <span>Ecualizador</span>
              </button>
              <button
                className={`phone-nav-btn ${phoneScreen === 'settings' ? 'active' : ''}`}
                onClick={() => setPhoneScreen('settings')}
              >
                <Palette size={18} />
                <span>Ajustes</span>
              </button>
            </nav>
          </div>
        </div>
      </main>

      {/* NEW PLAYLIST MODAL */}
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
              Nueva Lista de Reproducción
            </h3>
            <input
              type="text"
              placeholder="Nombre de la lista (ej. Favoritos 2026)..."
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
