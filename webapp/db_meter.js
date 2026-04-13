// Live dB meter using Web Audio AnalyserNode.
// Computes RMS on each frame and maps to a relative dBFS value.
// Not an absolute SPL reading — browsers don't expose calibrated
// microphone gain — so values are anchored to a noise-floor baseline
// captured in the first second, matching the Android app's approach.

export class DbMeter {
  constructor(stream) {
    this.ctx = new (window.AudioContext || window.webkitAudioContext)();
    this.source = this.ctx.createMediaStreamSource(stream);
    this.analyser = this.ctx.createAnalyser();
    this.analyser.fftSize = 2048;
    this.source.connect(this.analyser);
    this.buf = new Float32Array(this.analyser.fftSize);
    this.baseline = null;
    this.baselineSamples = [];
    this.startedAt = Date.now();
  }

  /** @returns {number} relative dB value */
  sample() {
    this.analyser.getFloatTimeDomainData(this.buf);
    let sum = 0;
    for (let i = 0; i < this.buf.length; i++) sum += this.buf[i] * this.buf[i];
    const rms = Math.sqrt(sum / this.buf.length);
    const dbfs = rms > 0 ? 20 * Math.log10(rms) : -100;

    // First second: collect noise-floor baseline.
    if (this.baseline === null) {
      this.baselineSamples.push(dbfs);
      if (Date.now() - this.startedAt > 1000) {
        this.baselineSamples.sort((a, b) => a - b);
        // median of lowest half = quiet baseline
        const half = this.baselineSamples.slice(
          0, Math.max(1, Math.floor(this.baselineSamples.length / 2))
        );
        this.baseline = half[Math.floor(half.length / 2)];
      }
      return 0;
    }
    // Anchor relative to baseline, clamp at 0.
    return Math.max(0, dbfs - this.baseline);
  }

  dispose() {
    try { this.source.disconnect(); } catch {}
    try { this.ctx.close(); } catch {}
  }
}
