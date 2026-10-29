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
  Radio,
  Search,
  Smartphone,
  Monitor,
  Disc3,
  Sparkles,
  Shuffle,
  Repeat,
  FileText,
  X,
  Compass,
  Headphones,
  CheckCircle2,
  ExternalLink
} from 'lucide-react';

import { SAMPLE_SONGS, CATEGORIES, APP_CONCEPTS } from './data/songs';
import { synth } from './utils/audioSynthesizer';
import './App.css';

export default function App() {
  const [songs] = useState(SAMPLE_SONGS);
  const [currentSong, setCurrentSong] = useState(SAMPLE_SONGS[0]);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(SAMPLE_SONGS[0].duration);
  const [volume, setVolume] = useState(0.8);
  const [isMuted, setIsMuted] = useState(false);
  const [isShuffle, setIsShuffle] = useState(false);
  const [isRepeat, setIsRepeat] = useState(false);
  const [activeCategory, setActiveCategory] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [likedSongIds, setLikedSongIds] = useState([1, 3]);
  const [showLyrics, setShowLyrics] = useState(false);
  const [currentView, setCurrentView] = useState('desktop'); // 'desktop' | 'mobile'
  const [activeConcept, setActiveConcept] = useState('aura');

  const audioRef = useRef(null);

  // Sync volume with audio element & synth
  useEffect(() => {
    if (audioRef.current) {
      audioRef.current.volume = isMuted ? 0 : volume;
    }
    synth.setVolume(isMuted ? 0 : volume);
  }, [volume, isMuted]);

  // Handle Play/Pause
  const togglePlay = () => {
    if (!audioRef.current) return;

    if (isPlaying) {
      audioRef.current.pause();
      synth.stop();
      setIsPlaying(false);
    } else {
      audioRef.current.play().then(() => {
        setIsPlaying(true);
      }).catch(() => {
        // Fallback to ambient synthesized harmony if network stream fails
        synth.start();
        setIsPlaying(true);
      });
    }
  };

  const handleSelectSong = (song) => {
    const isSame = song.id === currentSong.id;
    if (isSame) {
      togglePlay();
      return;
    }
    setCurrentSong(song);
    setCurrentTime(0);
    setDuration(song.duration);
    setIsPlaying(true);

    setTimeout(() => {
      if (audioRef.current) {
        audioRef.current.play().catch(() => {
          synth.start();
        });
      }
    }, 50);
  };

  const handleNext = () => {
    const currentIndex = songs.findIndex((s) => s.id === currentSong.id);
    let nextIndex;
    if (isShuffle) {
      nextIndex = Math.floor(Math.random() * songs.length);
    } else {
      nextIndex = (currentIndex + 1) % songs.length;
    }
    handleSelectSong(songs[nextIndex]);
  };

  const handlePrev = () => {
    const currentIndex = songs.findIndex((s) => s.id === currentSong.id);
    const prevIndex = (currentIndex - 1 + songs.length) % songs.length;
    handleSelectSong(songs[prevIndex]);
  };

  const handleTimeUpdate = () => {
    if (audioRef.current) {
      setCurrentTime(audioRef.current.currentTime);
      if (audioRef.current.duration) {
        setDuration(audioRef.current.duration);
      }
    }
  };

  const handleSeek = (e) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const newProgress = Math.max(0, Math.min(1, clickX / rect.width));
    const newTime = newProgress * (duration || 1);
    setCurrentTime(newTime);
    if (audioRef.current) {
      audioRef.current.currentTime = newTime;
    }
  };

  const toggleLike = (id, e) => {
    e?.stopPropagation();
    setLikedSongIds((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    );
  };

  const formatTime = (secs) => {
    if (!secs || isNaN(secs)) return '0:00';
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  // Filter songs
  const filteredSongs = songs.filter((song) => {
    const matchesCategory =
      activeCategory === 'all'
        ? true
        : activeCategory === 'favorites'
        ? likedSongIds.includes(song.id)
        : song.genre === activeCategory;

    const matchesQuery =
      song.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      song.artist.toLowerCase().includes(searchQuery.toLowerCase()) ||
      song.mood.toLowerCase().includes(searchQuery.toLowerCase());

    return matchesCategory && matchesQuery;
  });

  return (
    <div className="app-container">
      {/* Hidden Audio Player */}
      <audio
        ref={audioRef}
        src={currentSong.audioUrl}
        onTimeUpdate={handleTimeUpdate}
        onEnded={handleNext}
        onError={() => {
          if (isPlaying) synth.start();
        }}
      />

      {/* Top Header */}
      <header className="app-header">
        <div className="brand-section" onClick={() => setActiveCategory('all')}>
          <div className="brand-logo-icon">
            <Headphones size={22} />
          </div>
          <div>
            <h1 className="brand-title">Música para los Oídos</h1>
            <span className="brand-tag">Fase 1: Prototipo Web & Flutter</span>
          </div>
        </div>

        {/* Search */}
        <div className="header-search">
          <Search size={18} color="var(--text-muted)" />
          <input
            type="text"
            placeholder="Buscar por canción, artista o estado de ánimo..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        {/* View Switcher: Web vs Flutter Preview */}
        <div className="view-switcher">
          <button
            className={`view-btn ${currentView === 'desktop' ? 'active' : ''}`}
            onClick={() => setCurrentView('desktop')}
            title="Vista completa de escritorio"
          >
            <Monitor size={16} />
            <span>Vista Web</span>
          </button>
          <button
            className={`view-btn ${currentView === 'mobile' ? 'active' : ''}`}
            onClick={() => setCurrentView('mobile')}
            title="Vista previa de cómo se sentirá la app Flutter"
          >
            <Smartphone size={16} />
            <span>Mockup Flutter</span>
          </button>
        </div>
      </header>

      {/* Main Content */}
      <main className="main-wrapper">
        {currentView === 'desktop' ? (
          /* DESKTOP WEB EXPERIENCE */
          <>
            {/* Hero / Featured Song */}
            <section className="hero-card">
              <div
                className="hero-bg-blur"
                style={{ backgroundImage: `url(${currentSong.cover})` }}
              />
              <div className="hero-overlay" />
              <div className="hero-content">
                <div>
                  <div className="hero-badge">
                    <Sparkles size={14} />
                    <span>Sonando Ahora • {currentSong.mood}</span>
                  </div>
                  <h2 className="hero-title">{currentSong.title}</h2>
                  <p className="hero-artist">
                    {currentSong.artist} — Álbum: <em>{currentSong.album}</em>
                  </p>
                  <div className="hero-actions">
                    <button className="btn-primary" onClick={togglePlay}>
                      {isPlaying ? <Pause size={18} /> : <Play size={18} />}
                      <span>{isPlaying ? 'Pausar' : 'Reproducir Ahora'}</span>
                    </button>
                    <button
                      className="btn-secondary"
                      onClick={() => setShowLyrics(true)}
                    >
                      <FileText size={16} />
                      <span>Ver Letra</span>
                    </button>
                  </div>
                </div>

                <img
                  src={currentSong.cover}
                  alt={currentSong.title}
                  className="hero-cover-preview"
                />
              </div>
            </section>

            {/* Filter Pills */}
            <div className="category-bar">
              {CATEGORIES.map((cat) => (
                <button
                  key={cat.id}
                  className={`cat-pill ${activeCategory === cat.id ? 'active' : ''}`}
                  onClick={() => setActiveCategory(cat.id)}
                >
                  {cat.label}
                </button>
              ))}
            </div>

            {/* Songs Grid */}
            <div className="section-title">
              <span>Explorar Catálogo ({filteredSongs.length})</span>
              <span className="section-subtitle">
                Haz clic en cualquier tarjeta para escuchar
              </span>
            </div>

            <div className="song-grid">
              {filteredSongs.map((song) => {
                const isCurrent = song.id === currentSong.id;
                const isLiked = likedSongIds.includes(song.id);

                return (
                  <div
                    key={song.id}
                    className={`song-card ${isCurrent && isPlaying ? 'playing' : ''}`}
                    onClick={() => handleSelectSong(song)}
                  >
                    <div className="song-card-thumb-wrap">
                      <img
                        src={song.cover}
                        alt={song.title}
                        className="song-card-thumb"
                      />
                      <div className="card-play-overlay">
                        <div className="card-play-btn">
                          {isCurrent && isPlaying ? (
                            <Pause size={20} />
                          ) : (
                            <Play size={20} />
                          )}
                        </div>
                      </div>
                    </div>

                    <h4 className="song-card-title">{song.title}</h4>
                    <p className="song-card-artist">{song.artist}</p>

                    <div className="song-card-meta">
                      <span>{song.mood}</span>
                      <button
                        className={`like-btn ${isLiked ? 'liked' : ''}`}
                        onClick={(e) => toggleLike(song.id, e)}
                        title={isLiked ? 'Quitar de favoritos' : 'Añadir a favoritos'}
                      >
                        <Heart
                          size={16}
                          fill={isLiked ? 'currentColor' : 'none'}
                        />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Interactive Concept Showcase */}
            <section className="concepts-section">
              <div className="section-title">
                <span>💡 Ideas para la Dirección de la App (Plantillas & Conceptos)</span>
                <span className="section-subtitle">
                  Elige el enfoque que más te guste para expandir la web y Flutter
                </span>
              </div>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.92rem' }}>
                Hemos preparado 3 visiones diferentes para "Música para los Oídos".
                Selecciona una para ver los puntos clave y dime cuál se adapta mejor a tu idea:
              </p>

              <div className="concepts-grid">
                {APP_CONCEPTS.map((concept) => (
                  <div
                    key={concept.id}
                    className="concept-card"
                    style={{
                      borderColor:
                        activeConcept === concept.id ? concept.color : 'var(--border-glass)',
                      boxShadow:
                        activeConcept === concept.id
                          ? `0 0 20px ${concept.color}33`
                          : 'none'
                    }}
                    onClick={() => setActiveConcept(concept.id)}
                  >
                    <span
                      className="concept-badge"
                      style={{ color: concept.color }}
                    >
                      {concept.badge}
                    </span>
                    <h3 className="concept-title">{concept.title}</h3>
                    <p className="concept-desc">{concept.description}</p>
                    <div className="concept-tags">
                      {concept.highlights.map((h, i) => (
                        <span key={i} className="concept-tag">
                          ✓ {h}
                        </span>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </section>
          </>
        ) : (
          /* FLUTTER MOBILE MOCKUP PREVIEW */
          <div className="mobile-mockup-wrapper">
            <div className="phone-shell">
              <div className="phone-notch">
                <div className="phone-camera" />
              </div>

              <div className="phone-content">
                {/* Mobile App Bar */}
                <div className="mobile-header">
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Headphones size={20} color="var(--accent-primary)" />
                    <span style={{ fontWeight: 800, fontSize: '1rem' }}>
                      Para los Oídos
                    </span>
                  </div>
                  <div className="waveform-bars">
                    <div className={`wave-bar ${isPlaying ? 'animated' : ''}`} />
                    <div className={`wave-bar ${isPlaying ? 'animated' : ''}`} />
                    <div className={`wave-bar ${isPlaying ? 'animated' : ''}`} />
                    <div className={`wave-bar ${isPlaying ? 'animated' : ''}`} />
                  </div>
                </div>

                {/* Big Mobile Player Card */}
                <div
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    borderRadius: '24px',
                    padding: '1.2rem',
                    textAlign: 'center',
                    marginBottom: '1.5rem',
                    border: '1px solid rgba(255,255,255,0.08)'
                  }}
                >
                  <img
                    src={currentSong.cover}
                    alt={currentSong.title}
                    style={{
                      width: '100%',
                      aspectRatio: '1',
                      borderRadius: '16px',
                      objectFit: 'cover',
                      marginBottom: '1rem',
                      boxShadow: '0 8px 24px rgba(0,0,0,0.5)'
                    }}
                  />
                  <h3 style={{ fontSize: '1.2rem', fontWeight: 800 }}>
                    {currentSong.title}
                  </h3>
                  <p
                    style={{
                      color: 'var(--text-muted)',
                      fontSize: '0.85rem',
                      marginBottom: '1rem'
                    }}
                  >
                    {currentSong.artist}
                  </p>

                  {/* Quick controls */}
                  <div
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '1.2rem'
                    }}
                  >
                    <button onClick={handlePrev} className="ctrl-btn">
                      <SkipBack size={22} />
                    </button>
                    <button
                      onClick={togglePlay}
                      className="play-pause-btn"
                      style={{ width: '52px', height: '52px' }}
                    >
                      {isPlaying ? <Pause size={24} /> : <Play size={24} />}
                    </button>
                    <button onClick={handleNext} className="ctrl-btn">
                      <SkipForward size={22} />
                    </button>
                  </div>
                </div>

                {/* Upcoming Playlist in Mobile */}
                <h4
                  style={{
                    fontSize: '0.9rem',
                    fontWeight: 700,
                    marginBottom: '0.8rem',
                    color: 'var(--text-muted)'
                  }}
                >
                  Siguiente en cola (Flutter list)
                </h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {songs.slice(0, 3).map((song) => (
                    <div
                      key={song.id}
                      onClick={() => handleSelectSong(song)}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '10px',
                        background: 'rgba(255,255,255,0.03)',
                        padding: '8px 12px',
                        borderRadius: '12px',
                        cursor: 'pointer'
                      }}
                    >
                      <img
                        src={song.cover}
                        alt=""
                        style={{
                          width: '40px',
                          height: '40px',
                          borderRadius: '8px',
                          objectFit: 'cover'
                        }}
                      />
                      <div style={{ flex: 1, minWidth: 0 }}>
                        <div
                          style={{
                            fontWeight: 600,
                            fontSize: '0.85rem',
                            whiteSpace: 'nowrap',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis'
                          }}
                        >
                          {song.title}
                        </div>
                        <div
                          style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}
                        >
                          {song.artist}
                        </div>
                      </div>
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>
                        {formatTime(song.duration)}
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Mobile Bottom Navigation */}
              <div className="mobile-bottom-nav">
                <div className="mobile-nav-item active">
                  <Disc3 size={18} />
                  <span>Inicio</span>
                </div>
                <div className="mobile-nav-item">
                  <Search size={18} />
                  <span>Buscar</span>
                </div>
                <div className="mobile-nav-item">
                  <Heart size={18} />
                  <span>Favoritos</span>
                </div>
                <div className="mobile-nav-item">
                  <Radio size={18} />
                  <span>Radio</span>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Sticky Bottom Player */}
      <footer className="bottom-player-bar">
        {/* Left: Info */}
        <div className="player-left">
          <img
            src={currentSong.cover}
            alt={currentSong.title}
            className="player-thumb"
          />
          <div className="player-track-info">
            <span className="player-track-title">{currentSong.title}</span>
            <span className="player-track-artist">{currentSong.artist}</span>
          </div>
          <button
            className={`like-btn ${likedSongIds.includes(currentSong.id) ? 'liked' : ''}`}
            onClick={(e) => toggleLike(currentSong.id, e)}
          >
            <Heart
              size={18}
              fill={
                likedSongIds.includes(currentSong.id) ? 'currentColor' : 'none'
              }
            />
          </button>
        </div>

        {/* Center: Controls & Progress */}
        <div className="player-center">
          <div className="player-controls">
            <button
              className={`ctrl-btn ${isShuffle ? 'active' : ''}`}
              onClick={() => setIsShuffle(!isShuffle)}
              title="Aleatorio"
            >
              <Shuffle size={17} />
            </button>
            <button className="ctrl-btn" onClick={handlePrev} title="Anterior">
              <SkipBack size={20} />
            </button>
            <button
              className="play-pause-btn"
              onClick={togglePlay}
              title={isPlaying ? 'Pausar' : 'Reproducir'}
            >
              {isPlaying ? <Pause size={20} /> : <Play size={20} />}
            </button>
            <button className="ctrl-btn" onClick={handleNext} title="Siguiente">
              <SkipForward size={20} />
            </button>
            <button
              className={`ctrl-btn ${isRepeat ? 'active' : ''}`}
              onClick={() => setIsRepeat(!isRepeat)}
              title="Repetir"
            >
              <Repeat size={17} />
            </button>
          </div>

          <div className="player-timeline">
            <span>{formatTime(currentTime)}</span>
            <div className="progress-track" onClick={handleSeek}>
              <div
                className="progress-fill"
                style={{
                  width: `${((currentTime / (duration || 1)) * 100).toFixed(2)}%`
                }}
              />
            </div>
            <span>{formatTime(duration)}</span>
          </div>
        </div>

        {/* Right: Volume & Waveform */}
        <div className="player-right">
          <button
            className="ctrl-btn"
            onClick={() => setShowLyrics(true)}
            title="Ver Letras"
          >
            <FileText size={18} />
          </button>

          <div className="waveform-bars">
            <div className={`wave-bar ${isPlaying ? 'animated' : ''}`} />
            <div className={`wave-bar ${isPlaying ? 'animated' : ''}`} />
            <div className={`wave-bar ${isPlaying ? 'animated' : ''}`} />
            <div className={`wave-bar ${isPlaying ? 'animated' : ''}`} />
            <div className={`wave-bar ${isPlaying ? 'animated' : ''}`} />
          </div>

          <div className="volume-wrapper">
            <button
              className="ctrl-btn"
              onClick={() => setIsMuted(!isMuted)}
              title={isMuted ? 'Activar sonido' : 'Silenciar'}
            >
              {isMuted || volume === 0 ? (
                <VolumeX size={18} />
              ) : (
                <Volume2 size={18} />
              )}
            </button>
            <input
              type="range"
              min="0"
              max="1"
              step="0.01"
              value={isMuted ? 0 : volume}
              onChange={(e) => {
                setVolume(parseFloat(e.target.value));
                if (isMuted) setIsMuted(false);
              }}
              className="volume-slider"
            />
          </div>
        </div>
      </footer>

      {/* Lyrics Modal */}
      {showLyrics && (
        <div className="lyrics-modal-backdrop" onClick={() => setShowLyrics(false)}>
          <div className="lyrics-card" onClick={(e) => e.stopPropagation()}>
            <div className="lyrics-header">
              <div>
                <h3 style={{ fontSize: '1.25rem', fontWeight: 800 }}>
                  {currentSong.title}
                </h3>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                  {currentSong.artist}
                </p>
              </div>
              <button
                className="ctrl-btn"
                onClick={() => setShowLyrics(false)}
                title="Cerrar"
              >
                <X size={20} />
              </button>
            </div>

            <div className="lyrics-body">
              {currentSong.lyrics && currentSong.lyrics.length > 0 ? (
                currentSong.lyrics.map((line, idx) => (
                  <p
                    key={idx}
                    className={`lyrics-line ${idx === 2 && isPlaying ? 'highlight' : ''}`}
                  >
                    {line}
                  </p>
                ))
              ) : (
                <p>No hay letra disponible para esta pista.</p>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
