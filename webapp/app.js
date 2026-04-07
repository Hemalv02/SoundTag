import { getLocation, formatGps } from "./location.js";

const gpsEl = document.getElementById("gpsBadge");

async function refreshGps() {
  gpsEl.textContent = "GPS: fetching\u2026";
  const loc = await getLocation();
  gpsEl.textContent = formatGps(loc);
  return loc;
}

refreshGps();
