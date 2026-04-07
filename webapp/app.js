import { getLocation, formatGps } from "./location.js";
import { Recorder, formatTimer, MAX_DURATION_MS } from "./recorder.js";

const $ = id => document.getElementById(id);
const gpsEl = $("gpsBadge");
const timerEl = $("timer");
const stateEl = $("stateText");
const recordBtn = $("recordBtn");
const stopBtn = $("stopBtn");
const preview = $("preview");
const snackbar = $("snackbar");

const recorder = new Recorder();
let timerInterval = null;
let currentLocation = null;

function toast(msg, kind = "") {
  snackbar.textContent = msg;
  snackbar.className = "snackbar " + kind;
  snackbar.hidden = false;
  clearTimeout(toast._t);
  toast._t = setTimeout(() => (snackbar.hidden = true), 2500);
}

async function refreshGps() {
  gpsEl.textContent = "GPS: fetching\u2026";
  currentLocation = await getLocation();
  gpsEl.textContent = formatGps(currentLocation);
}

function tick() {
  const ms = recorder.elapsedMs();
  timerEl.textContent = formatTimer(ms);
  if (ms >= MAX_DURATION_MS) stopRecording();
}

async function startRecording() {
  try {
    await recorder.start();
    stateEl.textContent = "Recording";
    recordBtn.disabled = true;
    stopBtn.disabled = false;
    preview.hidden = true;
    timerInterval = setInterval(tick, 200);
    refreshGps();
  } catch (e) {
    toast("Microphone permission denied", "error");
  }
}

async function stopRecording() {
  try {
    const result = await recorder.stop();
    clearInterval(timerInterval);
    stateEl.textContent = "Stopped";
    recordBtn.disabled = false;
    stopBtn.disabled = true;
    preview.src = URL.createObjectURL(result.blob);
    preview.hidden = false;
  } catch (e) {
    if (e.message === "min-duration") toast("Minimum 5 seconds", "error");
  }
}

recordBtn.addEventListener("click", startRecording);
stopBtn.addEventListener("click", stopRecording);

refreshGps();
