// Metadata builder — produces the same JSON sidecar shape as the Android app.

export const SEVERITY_SCORES = {
  low: 1, medium: 2, high: 4, severe: 5,
};

function pad(n) { return String(Math.abs(n)).padStart(2, "0"); }

function localIsoWithOffset(d) {
  const off = -d.getTimezoneOffset();
  const sign = off >= 0 ? "+" : "-";
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` +
    `T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}` +
    `${sign}${pad(Math.floor(Math.abs(off) / 60))}:${pad(Math.abs(off) % 60)}`;
}

export function extensionForMime(mime) {
  if (!mime) return "webm";
  if (mime.includes("mp4")) return "m4a";
  return "webm";
}

export function encodingForMime(mime) {
  return extensionForMime(mime) === "m4a" ? "AAC" : "Opus";
}

/**
 * Build the JSON sidecar object.
 * @param {object} args
 * @param {string} args.filenameBase - without extension
 * @param {string} args.mimeType
 * @param {number} args.startedAt - epoch ms
 * @param {number} args.durationMs
 * @param {object|null} args.location - { lat, lon, acc } or null
 * @param {object} args.annotation - { label, severity, environment, locationContext, isNoise, annotatorId, notes }
 */
export function buildMetadata({
  filenameBase, mimeType, startedAt, durationMs, location, annotation,
}) {
  const ext = extensionForMime(mimeType);
  const startDate = new Date(startedAt);
  return {
    label: annotation.label,
    filename: `${filenameBase}.${ext}`,
    is_noise: annotation.isNoise,
    severity: annotation.severity,
    severity_score: SEVERITY_SCORES[annotation.severity] ?? 2,
    environment: annotation.environment,
    location_context: annotation.locationContext,
    latitude: location?.lat ?? null,
    longitude: location?.lon ?? null,
    location_accuracy_m: location?.acc ?? null,
    started_at_local: localIsoWithOffset(startDate),
    started_at_utc: startDate.toISOString(),
    duration_seconds: Math.round(durationMs / 1000),
    sample_rate_hz: 44100,
    channels: 1,
    encoding: encodingForMime(mimeType),
    device_model: navigator.platform || "web",
    android_version: null,
    app_version: "web-1.0.0",
    annotator_id: annotation.annotatorId || "anonymous",
    notes: annotation.notes || "",
  };
}

/** Trigger a browser download for a blob. */
export function downloadBlob(blob, name) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = name;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

/** Save audio + JSON sidecar locally as two downloads. */
export function saveLocally(audioBlob, metadata) {
  downloadBlob(audioBlob, metadata.filename);
  const jsonName = metadata.filename.replace(/\.[^.]+$/, ".json");
  const jsonBlob = new Blob([JSON.stringify(metadata, null, 2)], {
    type: "application/json",
  });
  downloadBlob(jsonBlob, jsonName);
}
