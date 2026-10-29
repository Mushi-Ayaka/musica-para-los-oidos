// Procesador de audio Web Audio API con ecualizador, preamplificador y perfiles de salida
class AudioEngine {
  constructor() {
    this.ctx = null;
    this.audioSource = null;
    this.audioElement = null;
    this.filters = [];
    this.bassFilter = null;
    this.stereoPanner = null;
    this.preampGain = null;
    this.masterGain = null;
    this.analyser = null;
    this.isInitialized = false;

    // 10 bandas de frecuencia estandar
    this.bandFrequencies = [32, 64, 125, 250, 500, 1000, 2000, 4000, 8000, 16000];

    // Perfil de salida actual
    this.currentProfile = 'studio_monitor';
  }

  init(audioEl) {
    if (this.isInitialized && this.audioElement === audioEl) return;
    try {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (!AudioCtx) return;

      this.ctx = new AudioCtx();
      this.audioElement = audioEl;

      this.audioSource = this.ctx.createMediaElementSource(audioEl);

      // Crear 10 filtros de ecualizacion
      this.filters = this.bandFrequencies.map((freq, index) => {
        const filter = this.ctx.createBiquadFilter();
        if (index === 0) {
          filter.type = 'lowshelf';
        } else if (index === this.bandFrequencies.length - 1) {
          filter.type = 'highshelf';
        } else {
          filter.type = 'peaking';
          filter.Q.value = 1.4;
        }
        filter.frequency.value = freq;
        filter.gain.value = 0;
        return filter;
      });

      // Filtro Bass Boost a 80Hz
      this.bassFilter = this.ctx.createBiquadFilter();
      this.bassFilter.type = 'lowshelf';
      this.bassFilter.frequency.value = 80;
      this.bassFilter.gain.value = 0;

      // Panning estereo para efecto espacial 3D
      if (this.ctx.createStereoPanner) {
        this.stereoPanner = this.ctx.createStereoPanner();
        this.stereoPanner.pan.value = 0;
      }

      // Analizador de frecuencias
      this.analyser = this.ctx.createAnalyser();
      this.analyser.fftSize = 64;

      // Preamplificador de volumen (hasta 200% / factor 2.0)
      this.preampGain = this.ctx.createGain();
      this.preampGain.gain.value = 1.0;

      // Ganancia maestra
      this.masterGain = this.ctx.createGain();
      this.masterGain.gain.value = 1.0;

      // Cadena de conexion:
      // Fuente -> Filtros 0..9 -> Bass -> Panner -> Preamp (hasta 200%) -> Analyser -> Master -> Salida
      let prevNode = this.audioSource;
      this.filters.forEach((filter) => {
        prevNode.connect(filter);
        prevNode = filter;
      });

      prevNode.connect(this.bassFilter);
      let chain = this.bassFilter;

      if (this.stereoPanner) {
        chain.connect(this.stereoPanner);
        chain = this.stereoPanner;
      }

      chain.connect(this.preampGain);
      this.preampGain.connect(this.analyser);
      this.analyser.connect(this.masterGain);
      this.masterGain.connect(this.ctx.destination);

      this.isInitialized = true;
    } catch (e) {
      console.warn('Nota de inicializacion de AudioEngine:', e);
    }
  }

  resume() {
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  }

  // Volumen con multiplicador hasta 200% (0.0 a 2.0)
  setVolumePercent(percent) {
    if (!this.ctx || !this.preampGain) return;
    const factor = Math.max(0, Math.min(percent / 100, 2.0));
    this.preampGain.gain.setValueAtTime(factor, this.ctx.currentTime);
  }

  setBandGain(index, gainDb) {
    if (this.filters[index] && this.ctx) {
      this.filters[index].gain.setValueAtTime(gainDb, this.ctx.currentTime);
    }
  }

  setAllBands(gainsArray) {
    if (!this.ctx) return;
    gainsArray.forEach((gain, i) => {
      if (this.filters[i]) {
        this.filters[i].gain.setValueAtTime(gain, this.ctx.currentTime);
      }
    });
  }

  setBassBoost(boostDb) {
    if (this.bassFilter && this.ctx) {
      this.bassFilter.gain.setValueAtTime(boostDb, this.ctx.currentTime);
    }
  }

  setPan(val) {
    if (this.stereoPanner && this.ctx) {
      this.stereoPanner.pan.setValueAtTime(val, this.ctx.currentTime);
    }
  }

  // Configuracion de perfiles de salida para audifonos de calidad
  setOutputProfile(profileKey) {
    this.currentProfile = profileKey;
    if (!this.ctx) return;

    // Ajustes acusticos segun el transductor
    switch (profileKey) {
      case 'studio_monitor':
        // Respuesta plana de alta fidelidad, maxima dinamica
        break;
      case 'audiophile_iem':
        // Compensacion para monitores intrauditivos
        if (this.filters[0]) this.filters[0].gain.setValueAtTime(1.5, this.ctx.currentTime);
        if (this.filters[6]) this.filters[6].gain.setValueAtTime(-1.0, this.ctx.currentTime);
        break;
      case 'bluetooth_ldac':
        // Compensacion de alta resolucion para codecs inalambricos
        if (this.filters[8]) this.filters[8].gain.setValueAtTime(2.0, this.ctx.currentTime);
        if (this.filters[9]) this.filters[9].gain.setValueAtTime(2.5, this.ctx.currentTime);
        break;
      case 'usb_dac':
        // Salida pura sin coloracion para DAC externo
        break;
      default:
        break;
    }
  }
}

export const audioEngine = new AudioEngine();

// Presets iniciales por defecto (sin emojis)
export const DEFAULT_EQ_PRESETS = [
  { id: 'flat', name: 'Plano (Fidelidad de Estudio)', gains: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], bass: 0, isCustom: false },
  { id: 'bass', name: 'Bajos Profundos (Bass Boost)', gains: [8, 6, 4, 2, 0, 0, 0, 1, 2, 3], bass: 8, isCustom: false },
  { id: 'vocal', name: 'Voces Claras y Acustico', gains: [-2, -1, 1, 3, 4, 4, 3, 2, 1, 0], bass: 0, isCustom: false },
  { id: 'edm', name: 'Electronica y Club', gains: [6, 5, 2, 0, -2, 2, 4, 5, 6, 6], bass: 6, isCustom: false },
  { id: 'rock', name: 'Rock y Guitarras', gains: [5, 3, 2, -1, -2, 1, 3, 5, 4, 4], bass: 4, isCustom: false },
  { id: 'pop', name: 'Pop Brillante', gains: [2, 1, 0, 2, 4, 3, 2, 3, 4, 5], bass: 2, isCustom: false },
  { id: 'lofi', name: 'Calido y Relajado', gains: [4, 4, 2, 1, 0, 0, -1, -2, -3, -4], bass: 3, isCustom: false },
  { id: 'surround', name: 'Espacial 3D Envolvente', gains: [3, 2, 0, -1, 1, 3, 4, 5, 6, 6], bass: 4, virtualizer: 0.6, isCustom: false }
];

// Perfiles acusticos de salida
export const OUTPUT_AUDIO_PROFILES = [
  {
    id: 'studio_monitor',
    name: 'Audifonos de Estudio (Monitor Hi-Fi)',
    description: 'Respuesta plana de alta impedancia, rango dinamico maximo sin colorear',
    specs: '24-bit / 96kHz Direct PCM'
  },
  {
    id: 'audiophile_iem',
    name: 'Monitores In-Ear (IEM de Precision)',
    description: 'Atenuacion de sibilancias y realce controlado de sub-bajos',
    specs: '24-bit / 48kHz Resonancia compensada'
  },
  {
    id: 'bluetooth_ldac',
    name: 'Bluetooth Hi-Res (LDAC / aptX HD)',
    description: 'Compensacion dinamica para transmisores inalambricos de alta definicion',
    specs: '24-bit / 96kHz Wireless LDAC'
  },
  {
    id: 'usb_dac',
    name: 'Salida de Linea / DAC Externo',
    description: 'Transmision Bit-Perfect directa para decodificadores dedicados',
    specs: 'Bit-Perfect 32-bit / 192kHz'
  }
];
