// Recorder — foreground MediaRecorder wrapper.
// No background mode: tab must stay open. Enforces 5s min / 5min max.

export const MIN_DURATION_MS = 5_000;
export const MAX_DURATION_MS = 5 * 60_000;

const CANDIDATE_MIMES = [
  "audio/webm;codecs=opus",
  "audio/webm",
  "audio/mp4",
];

export class Recorder {
  constructor() {
    this.mediaRecorder = null;
    this.stream = null;
    this.chunks = [];
    this.startedAt = 0;
    this.mimeType = "";
    this.blob = null;
  }

  get state() {
    return this.mediaRecorder?.state ?? "inactive";
  }

  async start() {
    this.stream = await navigator.mediaDevices.getUserMedia({
      audio: {
        channelCount: 1,
        sampleRate: 44100,
        echoCancellation: false,
        noiseSuppression: false,
      },
    });
    this.mimeType = CANDIDATE_MIMES.find(m => MediaRecorder.isTypeSupported(m)) || "";
    this.mediaRecorder = new MediaRecorder(
      this.stream,
      this.mimeType ? { mimeType: this.mimeType } : undefined
    );
    this.chunks = [];
    this.blob = null;
    this.mediaRecorder.ondataavailable = e => {
      if (e.data && e.data.size) this.chunks.push(e.data);
    };
    this.mediaRecorder.start();
    this.startedAt = Date.now();
  }

  stop() {
    return new Promise((resolve, reject) => {
      if (!this.mediaRecorder || this.state === "inactive") {
        return reject(new Error("not recording"));
      }
      const elapsed = Date.now() - this.startedAt;
      if (elapsed < MIN_DURATION_MS) {
        return reject(new Error("min-duration"));
      }
      this.mediaRecorder.onstop = () => {
        this.stream.getTracks().forEach(t => t.stop());
        this.blob = new Blob(this.chunks, { type: this.mimeType || "audio/webm" });
        resolve({
          blob: this.blob,
          mimeType: this.mimeType,
          durationMs: elapsed,
          startedAt: this.startedAt,
        });
      };
      this.mediaRecorder.stop();
    });
  }

  elapsedMs() {
    return this.startedAt ? Date.now() - this.startedAt : 0;
  }
}

export function formatTimer(ms) {
  const s = Math.floor(ms / 1000);
  const mm = String(Math.floor(s / 60)).padStart(2, "0");
  const ss = String(s % 60).padStart(2, "0");
  return `${mm}:${ss}`;
}
