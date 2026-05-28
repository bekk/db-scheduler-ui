/* In-page annotation overlay for throwaway UI mockups.
 * Toggle annotate mode, click an element, write a note, copy them all out.
 * State is per-page in localStorage.
 * Page-nav strip auto-discovers other *.html files served from the same directory.
 */
(function () {
  const STORE_KEY = "mockup-notes::" + location.pathname;
  let state = load();
  let mode = "idle"; // 'idle' | 'picking'
  let panelEl = null;

  function load() {
    try {
      return JSON.parse(localStorage.getItem(STORE_KEY)) || { notes: [] };
    } catch (_) {
      return { notes: [] };
    }
  }
  function persist() {
    localStorage.setItem(STORE_KEY, JSON.stringify(state));
  }

  function shortText(el) {
    const t = (el.textContent || "").trim().replace(/\s+/g, " ");
    return t.length > 40 ? t.slice(0, 37) + "…" : t;
  }
  function closestNamed(el) {
    while (el && el !== document.body) {
      if (el.dataset && el.dataset.name) return el.dataset.name;
      el = el.parentElement;
    }
    return null;
  }
  function identify(el) {
    const section = closestNamed(el);
    const tag = el.tagName.toLowerCase();
    const classList = (el.className && typeof el.className === "string")
      ? el.className.trim().split(/\s+/).filter((c) => c && !c.startsWith("__anno-"))
      : [];
    const cls = classList.length ? "." + classList.slice(0, 2).join(".") : "";
    const text = shortText(el);
    const local = `${tag}${cls}${text ? ` "${text}"` : ""}`;
    return section ? `[${section}] ${local}` : local;
  }

  function injectStyles() {
    const css = `
      .__anno-panel {
        position: fixed; top: 12px; right: 12px;
        width: 320px; max-height: calc(100vh - 24px);
        background: #fffdf5; border: 1px solid #e8dca1; border-radius: 8px;
        box-shadow: 0 6px 24px rgba(0,0,0,0.12);
        font: 13px/1.4 -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
        color: #18181b; z-index: 99998; display: flex; flex-direction: column;
      }
      .__anno-panel header {
        padding: 10px 12px; border-bottom: 1px solid #e8dca1;
        display: flex; align-items: center; gap: 8px;
      }
      .__anno-panel header .title { font-weight: 600; font-size: 13px; flex: 1; }
      .__anno-pages {
        display: flex; flex-wrap: wrap; gap: 4px; padding: 6px 10px;
        border-bottom: 1px solid #e8dca1; background: #fef9e0;
      }
      .__anno-pages a {
        flex: 1 1 auto; min-width: 0; text-align: center; padding: 4px 6px;
        border-radius: 4px; font-size: 11px; color: #52525b;
        text-decoration: none; border: 1px solid transparent;
        white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
      }
      .__anno-pages a:hover { background: white; border-color: #e8dca1; }
      .__anno-pages a.current {
        background: white; border-color: #b88a00; color: #18181b; font-weight: 600;
      }
      .__anno-btn {
        font: inherit; padding: 4px 10px; border-radius: 5px;
        border: 1px solid #d4d4d8; background: white; cursor: pointer;
      }
      .__anno-btn:hover { background: #f4f4f5; }
      .__anno-btn.active { background: #fde68a; border-color: #b88a00; }
      .__anno-btn.danger { color: #BB0101; border-color: #f4c8c8; }
      .__anno-panel .list { overflow-y: auto; flex: 1; padding: 8px; }
      .__anno-panel .empty {
        padding: 18px 14px; color: #71717a; font-size: 12px; text-align: center;
      }
      .__anno-note {
        background: #fff8d6; border: 1px solid #e8dca1; border-radius: 6px;
        padding: 8px; margin-bottom: 8px;
      }
      .__anno-note .ident {
        font-family: ui-monospace, Menlo, Consolas, monospace;
        font-size: 11px; color: #52525b; word-break: break-all;
        margin-bottom: 4px; cursor: pointer;
      }
      .__anno-note .ident:hover { color: #002FA7; text-decoration: underline; }
      .__anno-note textarea {
        width: 100%; min-height: 50px; border: 1px solid #e4e4e7;
        border-radius: 4px; padding: 6px; font: inherit; resize: vertical;
        box-sizing: border-box;
      }
      .__anno-note .row { display: flex; justify-content: flex-end; margin-top: 4px; }
      .__anno-note .del {
        font: inherit; font-size: 11px; color: #BB0101;
        background: transparent; border: 0; cursor: pointer;
      }
      .__anno-panel footer {
        padding: 8px 12px; border-top: 1px solid #e8dca1;
        display: flex; gap: 6px;
      }
      body.__anno-picking, body.__anno-picking * { cursor: crosshair !important; }
      body.__anno-picking *:not(.__anno-panel):not(.__anno-panel *):hover {
        outline: 2px dashed #b88a00 !important; outline-offset: 1px;
      }
      .__anno-pin {
        position: absolute; width: 18px; height: 18px; background: #fde68a;
        border: 1px solid #b88a00; border-radius: 50%;
        font: 600 11px/16px -apple-system, sans-serif; text-align: center;
        color: #6b4a00; z-index: 99997; cursor: pointer; box-shadow: 0 1px 3px rgba(0,0,0,0.2);
      }
      .__anno-flash { animation: __annoFlash 0.6s ease-out; }
      @keyframes __annoFlash {
        0%   { box-shadow: 0 0 0 6px rgba(184,138,0,0.35); }
        100% { box-shadow: 0 0 0 0px rgba(184,138,0,0); }
      }
    `;
    const s = document.createElement("style");
    s.textContent = css;
    document.head.appendChild(s);
  }

  function buildPanel() {
    panelEl = document.createElement("div");
    panelEl.className = "__anno-panel";
    panelEl.innerHTML = `
      <header>
        <span class="title">📝 Notes</span>
        <button class="__anno-btn" data-act="toggle">Annotate</button>
      </header>
      <div class="__anno-pages" style="display:none"></div>
      <div class="list"></div>
      <footer>
        <button class="__anno-btn" data-act="copy">📋 Copy all</button>
        <button class="__anno-btn danger" data-act="clear">Clear</button>
      </footer>
    `;
    document.body.appendChild(panelEl);
    panelEl.addEventListener("click", (e) => {
      const act = e.target.dataset && e.target.dataset.act;
      if (act === "toggle") togglePicking();
      else if (act === "copy") copyAll();
      else if (act === "clear") clearAll();
    });
    renderList();
  }

  // --- Page nav (auto-discovered from directory listing) ---

  async function discoverPages() {
    try {
      const res = await fetch("./", { headers: { Accept: "text/html" } });
      if (!res.ok) return [];
      const html = await res.text();
      const doc = new DOMParser().parseFromString(html, "text/html");
      const seen = new Set();
      const pages = [];
      doc.querySelectorAll("a[href]").forEach((a) => {
        let h = a.getAttribute("href") || "";
        h = h.replace(/^\.\//, "").split("?")[0].split("#")[0];
        if (!/\.html?$/i.test(h)) return;
        if (h.includes("/")) return;
        if (h === "index.html") return;
        if (seen.has(h)) return;
        seen.add(h);
        pages.push(h);
      });
      return pages.sort();
    } catch (_) {
      return [];
    }
  }

  function labelFor(filename) {
    const stem = filename.replace(/\.html?$/i, "");
    const m = stem.match(/^(\d+)[-_](.+)$/);
    const rest = (m ? m[2] : stem).replace(/[-_]+/g, " ");
    const titled = rest.charAt(0).toUpperCase() + rest.slice(1);
    return m ? `${m[1]} ${titled}` : titled;
  }

  function renderPageNav(pages) {
    const nav = panelEl.querySelector(".__anno-pages");
    if (!nav) return;
    const here = location.pathname.split("/").pop() || "";
    const visible = pages.filter((p) => p !== here || pages.length > 1);
    if (visible.length <= 1) {
      nav.style.display = "none";
      return;
    }
    nav.style.display = "";
    nav.innerHTML = pages
      .map((p) => {
        const label = labelFor(p);
        const cls = p === here ? "current" : "";
        return `<a href="${p}" class="${cls}">${escape(label)}</a>`;
      })
      .join("");
  }

  function renderList() {
    const list = panelEl.querySelector(".list");
    if (state.notes.length === 0) {
      list.innerHTML = `<div class="empty">No notes yet. Click <b>Annotate</b>, then click an element on the page.</div>`;
    } else {
      list.innerHTML = "";
      state.notes.forEach((n) => {
        const row = document.createElement("div");
        row.className = "__anno-note";
        row.innerHTML = `
          <div class="ident" title="Scroll to element">${escape(n.identifier)}</div>
          <textarea placeholder="Describe the change…">${escape(n.text || "")}</textarea>
          <div class="row"><button class="del">delete</button></div>
        `;
        row.querySelector(".ident").addEventListener("click", () => scrollToNote(n.id));
        row.querySelector("textarea").addEventListener("input", (e) => {
          n.text = e.target.value;
          persist();
        });
        row.querySelector(".del").addEventListener("click", () => {
          state.notes = state.notes.filter((x) => x.id !== n.id);
          persist();
          renderList();
        });
        list.appendChild(row);
      });
    }
    renderPins();
  }

  function escape(s) {
    return String(s).replace(/[&<>"']/g, (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c])
    );
  }

  // Pins overlaid on annotated elements
  function renderPins() {
    document.querySelectorAll(".__anno-pin").forEach((el) => el.remove());
    state.notes.forEach((n, idx) => {
      const target = resolveTarget(n);
      if (!target) return;
      const rect = target.getBoundingClientRect();
      const pin = document.createElement("div");
      pin.className = "__anno-pin";
      pin.textContent = String(idx + 1);
      pin.style.left = (window.scrollX + rect.left - 9) + "px";
      pin.style.top = (window.scrollY + rect.top - 9) + "px";
      pin.title = n.identifier + (n.text ? " — " + n.text : "");
      pin.addEventListener("click", () => scrollToNote(n.id));
      document.body.appendChild(pin);
    });
  }
  window.addEventListener("scroll", renderPins, { passive: true });
  window.addEventListener("resize", renderPins);

  function resolveTarget(n) {
    if (!n.xpath) return null;
    try {
      const r = document.evaluate(n.xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);
      return r.singleNodeValue;
    } catch (_) {
      return null;
    }
  }
  function xpathOf(el) {
    if (!el || el === document.body) return "/html/body";
    const parts = [];
    while (el && el.nodeType === 1 && el !== document.body) {
      let i = 1, sib = el.previousElementSibling;
      while (sib) { if (sib.tagName === el.tagName) i++; sib = sib.previousElementSibling; }
      parts.unshift(`${el.tagName.toLowerCase()}[${i}]`);
      el = el.parentElement;
    }
    return "/html/body/" + parts.join("/");
  }

  function togglePicking() {
    mode = (mode === "picking") ? "idle" : "picking";
    document.body.classList.toggle("__anno-picking", mode === "picking");
    panelEl.querySelector('[data-act="toggle"]').classList.toggle("active", mode === "picking");
  }

  document.addEventListener("click", (e) => {
    if (mode !== "picking") return;
    if (e.target.closest(".__anno-panel") || e.target.closest(".__anno-pin")) return;
    e.preventDefault();
    e.stopPropagation();
    const el = e.target;
    el.classList.add("__anno-flash");
    setTimeout(() => el.classList.remove("__anno-flash"), 700);
    const n = {
      id: Date.now() + Math.random(),
      identifier: identify(el),
      text: "",
      xpath: xpathOf(el),
    };
    state.notes.push(n);
    persist();
    renderList();
    togglePicking();
    setTimeout(() => {
      const ta = [...panelEl.querySelectorAll("textarea")].pop();
      if (ta) ta.focus();
    }, 50);
  }, true);

  function scrollToNote(id) {
    const n = state.notes.find((x) => x.id === id);
    if (!n) return;
    const target = resolveTarget(n);
    if (target) {
      target.scrollIntoView({ behavior: "smooth", block: "center" });
      target.classList.add("__anno-flash");
      setTimeout(() => target.classList.remove("__anno-flash"), 700);
    }
  }

  async function copyAll() {
    if (state.notes.length === 0) {
      flashButton("copy", "Nothing to copy");
      return;
    }
    const lines = state.notes.map((n, i) => {
      const text = (n.text || "").trim() || "(no description)";
      return `${i + 1}. \`${n.identifier}\`\n   ${text}`;
    });
    const md = `## Mockup notes (${location.pathname.split("/").pop()})\n\n${lines.join("\n\n")}\n`;
    try {
      await navigator.clipboard.writeText(md);
      flashButton("copy", "Copied!");
    } catch (_) {
      promptManualCopy(md);
    }
  }
  function flashButton(act, label) {
    const b = panelEl.querySelector(`[data-act="${act}"]`);
    const orig = b.textContent;
    b.textContent = label;
    setTimeout(() => (b.textContent = orig), 1200);
  }
  function promptManualCopy(md) {
    const overlay = document.createElement("div");
    overlay.style.cssText = "position:fixed;inset:0;background:rgba(0,0,0,0.5);z-index:100000;display:flex;align-items:center;justify-content:center";
    overlay.innerHTML = `
      <div style="background:white;padding:18px;border-radius:8px;width:520px;max-width:90vw">
        <div style="margin-bottom:8px;font-weight:600">Copy this to your chat:</div>
        <textarea style="width:100%;height:240px;font-family:ui-monospace,Menlo,Consolas,monospace;font-size:12px"></textarea>
        <div style="text-align:right;margin-top:8px"><button class="__anno-btn">Close</button></div>
      </div>`;
    overlay.querySelector("textarea").value = md;
    overlay.querySelector("button").onclick = () => overlay.remove();
    document.body.appendChild(overlay);
    overlay.querySelector("textarea").select();
  }

  function clearAll() {
    if (state.notes.length === 0) return;
    if (!confirm(`Delete all ${state.notes.length} notes on this page?`)) return;
    state.notes = [];
    persist();
    renderList();
  }

  async function init() {
    injectStyles();
    buildPanel();
    const pages = await discoverPages();
    renderPageNav(pages);
  }
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
