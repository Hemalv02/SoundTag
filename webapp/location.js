// LocationHelper — mirror of Android LocationHelper.kt.
// Fetches the current GPS fix with a hard timeout so nothing ever
// blocks the recording flow. Resolves to null on failure/timeout.

export function getLocation(timeoutMs = 5000) {
  return new Promise(resolve => {
    if (!navigator.geolocation) return resolve(null);
    let done = false;
    const finish = v => { if (!done) { done = true; resolve(v); } };
    const to = setTimeout(() => finish(null), timeoutMs);
    navigator.geolocation.getCurrentPosition(
      p => { clearTimeout(to); finish({
        lat: p.coords.latitude,
        lon: p.coords.longitude,
        acc: p.coords.accuracy,
      }); },
      _ => { clearTimeout(to); finish(null); },
      { enableHighAccuracy: true, timeout: timeoutMs, maximumAge: 10000 }
    );
  });
}

export function formatGps(loc) {
  if (!loc) return "GPS: unavailable";
  return `GPS: ${loc.lat.toFixed(4)}, ${loc.lon.toFixed(4)} \u00b1${Math.round(loc.acc)}m`;
}
