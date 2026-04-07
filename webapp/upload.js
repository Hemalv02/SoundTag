// Upload — POSTs audio + metadata as multipart/form-data.
// Endpoint is configured in config.js so it can be swapped without
// touching the rest of the app.

import { UPLOAD_ENDPOINT } from "./config.js";

/**
 * @param {Blob} audioBlob
 * @param {object} metadata - JSON sidecar object
 * @returns {Promise<{ok: boolean, error?: string}>}
 */
export async function uploadRecording(audioBlob, metadata) {
  if (!UPLOAD_ENDPOINT) {
    return { ok: false, error: "Upload endpoint not configured" };
  }
  const fd = new FormData();
  fd.append("audio", audioBlob, metadata.filename);
  fd.append("metadata", JSON.stringify(metadata));
  try {
    const r = await fetch(UPLOAD_ENDPOINT, { method: "POST", body: fd });
    if (!r.ok) return { ok: false, error: `HTTP ${r.status}` };
    return { ok: true };
  } catch (e) {
    return { ok: false, error: e.message };
  }
}
