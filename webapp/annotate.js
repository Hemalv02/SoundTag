// Annotate screen data — label/severity/env/context options with emojis.

export const LABELS = [
  { id: "traffic",      emoji: "🚗", text: "Traffic" },
  { id: "horn",         emoji: "📯", text: "Horn" },
  { id: "construction", emoji: "🔨", text: "Construction" },
  { id: "industrial",   emoji: "🏭", text: "Industrial" },
  { id: "crowd",        emoji: "👥", text: "Crowd" },
  { id: "nature",       emoji: "🌿", text: "Nature" },
  { id: "silence",      emoji: "🤫", text: "Silence" },
  { id: "mixed",        emoji: "🎚️", text: "Mixed" },
];

export const SEVERITY_LEVELS = ["low", "medium", "high"];
export const SEVERITY_SCORES = { low: 1, medium: 2, high: 4 };
export const ENVIRONMENTS = ["outdoor", "indoor"];
export const CONTEXTS = [
  "roadside", "market", "residential",
  "construction site", "industrial zone", "other",
];

export const defaultSelections = () => ({
  label: "traffic",
  isNoise: true,
  severity: "medium",
  score: 2,
  environment: "outdoor",
  locationContext: "roadside",
});

export function renderChipGroup(container, items, currentValue, onSelect) {
  container.replaceChildren();
  for (const it of items) {
    const chip = document.createElement("div");
    const id = typeof it === "string" ? it : it.id;
    const emoji = typeof it === "string" ? "" : it.emoji;
    const text = typeof it === "string" ? it : it.text;
    chip.className = "chip" + (id === currentValue ? " active" : "");
    if (emoji) {
      const e = document.createElement("span");
      e.textContent = emoji;
      chip.appendChild(e);
    }
    const t = document.createElement("span");
    t.textContent = text;
    chip.appendChild(t);
    chip.addEventListener("click", () => onSelect(id));
    container.appendChild(chip);
  }
}

export function renderToggleGroup(container, options, current, onSelect) {
  container.replaceChildren();
  for (const opt of options) {
    const el = document.createElement("div");
    el.className = "toggle-opt" + (opt === current ? " active" : "");
    el.textContent = opt[0].toUpperCase() + opt.slice(1);
    el.addEventListener("click", () => onSelect(opt));
    container.appendChild(el);
  }
}

export function renderSeverityRow(container, current, onSelect) {
  container.replaceChildren();
  for (const lvl of SEVERITY_LEVELS) {
    const b = document.createElement("button");
    b.className = "sev-btn" + (lvl === current ? " active" : "");
    b.textContent = lvl[0].toUpperCase() + lvl.slice(1);
    b.addEventListener("click", () => onSelect(lvl));
    container.appendChild(b);
  }
}

export function renderScoreBoxes(container, current, onSelect) {
  container.replaceChildren();
  for (let i = 1; i <= 5; i++) {
    const b = document.createElement("div");
    b.className = "score-box" + (i === current ? " active" : "");
    b.textContent = String(i);
    b.addEventListener("click", () => onSelect(i));
    container.appendChild(b);
  }
}

export function autoFilename(label) {
  const n = String(Math.floor(Math.random() * 900) + 100);
  return `${label}_${n}`;
}
