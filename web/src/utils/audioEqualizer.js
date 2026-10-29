// Web Audio Equalizer & Sound Processor Engine
class AudioEngine {
  constructor() {
    this.ctx = null;
    this.audioSource = null;
    this.audioElement = null;
    this.filters = [];
    this.bassFilter = null;
    this.stereoPanner = null;
    this.masterGain = null;
    this.analyser = null;
    this.isInitialized = false;

    // Equalizer frequencies (10 bands)
    this.bandFrequencies = [32, 64, 125, 250, 500, 1000, 2000, 4000, 8000, 16000];
  }

  init(audioEl) {
    if (this.isInitialized && this.audioElement === audioEl) return;
    try {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (!AudioCtx) return;

      this.ctx = new AudioCtx();
      this.audioElement = audioEl;

      // Source node from HTML5 Audio
      this.audioSource = this.ctx.createMediaElementSource(audioEl);

      // Create 10 peaking filters
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
        filter.gain.value = 0; // dB
        return filter;
      });

      // Bass Boost filter (lowshelf at 80Hz)
      this.bassFilter = this.ctx.createBiquadFilter();
      this.bassFilter.type = 'lowshelf';
      this.bassFilter.frequency.value = 80;
      this.bassFilter.gain.value = 0;

      // Stereo panner for Virtualizer / Surround simulation
      if (this.ctx.createStereoPanner) {
        this.stereoPanner = this.ctx.createStereoPanner();
        this.stereoPanner.pan.value = 0;
      }

      // Analyser for real-time waveform visualization
      this.analyser = this.ctx.createAnalyser();
      this.analyser.fftSize = 64;

      // Master Gain
      this.masterGain = this.ctx.createGain();
      this.masterGain.gain.value = 1.0;

      // Connect filter chain: Source -> Filter 0 -> Filter 1 ... -> Bass -> Panner -> Analyser -> MasterGain -> Destination
      let prevNode = this.audioSource;
      this.filters.forEach((filter) => {
        prevNode.connect(filter);
        prevNode = filter;
      });

      prevNode.connect(this.bassFilter);
      let afterBass = this.bassFilter;

      if (this.stereoPanner) {
        afterBass.connect(this.stereoPanner);
        this.stereoPanner.connect(this.analyser);
      } else {
        afterBass.connect(this.analyser);
      }

      this.analyser.connect(this.masterGain);
      this.masterGain.connect(this.ctx.destination);

      this.isInitialized = true;
    } catch (e) {
      console.warn('AudioEngine init note (cross-origin or already attached):', e);
    }
  }

  resume() {
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
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

  getFrequencyData() {
    if (!this.analyser) return null;
    const dataArray = new Uint8Array(this.analyser.frequencyBinCount);
    this.analyser.getByteFrequencyData(dataArray);
    return dataArray;
  }
}

export const audioEngine = new AudioEngine();

export const EQ_PRESETS = [
  { name: 'Plano (Flat)', gains: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0], bass: 0 },
  { name: 'Bass Boost (Super Graves)', gains: [8, 6, 4, 2, 0, 0, 0, 1, 2, 3], bass: 8 },
  { name: 'Vocal / Acústico', gains: [-2, -1, 1, 3, 4, 4, 3, 2, 1, 0], bass: 0 },
  { name: 'Electrónica / EDM', gains: [6, 5, 2, 0, -2, 2, 4, 5, 6, 6], bass: 6 },
  { name: 'Rock Clásico', gains: [5, 3, 2, -1, -2, 1, 3, 5, 4, 4], bass: 4 },
  { name: 'Pop Brillante', gains: [2, 1, 0, 2, 4, 3, 2, 3, 4, 5], bass: 2 },
  { name: 'Chill & Lo-Fi', gains: [4, 4, 2, 1, 0, 0, -1, -2, -3, -4], bass: 3 },
  { name: 'Envolvente 8D', gains: [3, 2, 0, -1, 1, 3, 4, 5, 6, 6], bass: 4, virtualizer: 0.6 }
];
