/**********************************************************************
 * BAN Dashboard - Frontend Logic
 *
 * Aquest fitxer:
 *  - Fa peticions HTTP al backend Jersey
 *  - Mostra resultats al panell de sortida
 *  - Permet crear clients via POST /api/clients/subscribe
 *
 * IMPORTANT:
 *  - Detectem automàticament el context path (AT_Lab)
 *  - Això permet que funcioni en local i en Docker sense canvis
 **********************************************************************/

// ========= API base autodetect (same idea as yours) =========
const PATH = window.location.pathname;
const CONTEXT = PATH.replace(/\/[^\/]*$/, "").replace(/\/$/, "");
const API_BASE = window.location.origin + CONTEXT + "/api";

const ENDPOINTS = {
  stations: API_BASE + "/stations",
  clients: API_BASE + "/clients",
  subscribe: API_BASE + "/clients/subscribe",
  notifySlots: API_BASE + "/notifier/slots",
  notifyAir: API_BASE + "/notifier/air",
  logs: API_BASE + "/logs"
};

document.getElementById("apiBaseHint").textContent = API_BASE;

// ========= State =========
let stationsCache = [];
let clientsCache = [];
let filterInService = false;
let filterLowBikes = false;

// ========= Utils =========
function safeJsonParse(text) {
  try { return JSON.parse(text); } catch { return { raw: text }; }
}

function parseStationsIds(csv) {
  return (csv || "")
    .split(",")
    .map(s => s.trim())
    .filter(Boolean)
    .map(x => parseInt(x, 10))
    .filter(n => !Number.isNaN(n));
}

function setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value;
}

function toast(msg) {
  const el = document.getElementById("toast");
  if (!el) return;
  el.textContent = msg;
  el.classList.add("show");
  window.clearTimeout(toast._t);
  toast._t = window.setTimeout(() => el.classList.remove("show"), 2600);
}

function normalize(s){ return String(s || "").toLowerCase(); }

function stationTotals(stations) {
  let bikes = 0, docks = 0, total = 0, occSum = 0, occCount = 0;
  for (const st of stations) {
    const b = Number(st.bikes ?? st.num_bikes_available ?? st.bikesAvailable ?? 0);
    const d = Number(st.docks ?? st.num_docks_available ?? st.docksAvailable ?? 0);
    const t = Number(st.totalDocks ?? st.num_docks ?? st.capacity ?? (b + d) ?? 0);
    bikes += b;
    docks += d;
    total += (t || (b + d));
    const denom = (t || (b + d));
    if (denom > 0) { occSum += (b / denom); occCount += 1; }
  }
  const occ = occCount ? Math.round((occSum / occCount) * 100) : 0;
  return { bikes, docks, total, occ };
}

// ========= Render: raw outputs =========
function showOut(id, data) {
  const el = document.getElementById(id);
  if (el) el.textContent = typeof data === "string" ? data : JSON.stringify(data, null, 2);
}

// ========= Render: stations table =========
function getStationField(st, keys, fallback = "") {
  for (const k of keys) {
    if (st && st[k] !== undefined && st[k] !== null) return st[k];
  }
  return fallback;
}

function renderStations() {
  const tbody = document.getElementById("stationsTbody");
  if (!tbody) return;

  const q = normalize(document.getElementById("stationSearch")?.value || "");

  let list = [...stationsCache];

  if (filterInService) {
    list = list.filter(s => normalize(getStationField(s, ["status", "station_status"])) === "in_service");
  }

  if (filterLowBikes) {
    list = list.filter(s => Number(getStationField(s, ["bikes", "num_bikes_available", "bikesAvailable"], 0)) <= 2);
  }

  if (q) {
    list = list.filter(s => {
      const id = String(getStationField(s, ["id", "station_id", "stationId"], ""));

      return id.includes(q) || name.includes(q);
    });
  }

  // KPIs based on displayed list
  const totals = stationTotals(list);
  setText("kpiStations", String(list.length));
  setText("kpiBikes", String(totals.bikes));
  setText("kpiDocks", String(totals.docks));
  setText("kpiOcc", totals.occ + "%");

  tbody.innerHTML = "";

  if (!list.length) {
    const tr = document.createElement("tr");
    tr.innerHTML = `<td colspan="6" class="empty">No stations match your filters.</td>`;
    tbody.appendChild(tr);
    return;
  }

  for (const st of list) {
    const id = getStationField(st, ["station_id", "id"], "");
  const status = getStationField(st, ["status"], "—");
  const bikes = Number(getStationField(st, ["num_bikes_available", "bikes"], 0));
  const docks = Number(getStationField(st, ["num_docks_available", "docks"], 0));
  const charging = !!getStationField(st, ["is_charging_station"], false);
  const last = Number(getStationField(st, ["last_reported"], 0));
  const lastText = last ? new Date(last * 1000).toLocaleString() : "—";
  const total = bikes + docks;
  const occ = total > 0 ? Math.round((bikes / total) * 100) : 0;

  tr.innerHTML = `
    <td>${id}</td>
    <td><span class="badge">${escapeHtml(status)}</span></td>
    <td>${bikes}</td>
    <td>${docks}</td>
    <td>${charging ? "Yes" : "No"}</td>
    <td>${escapeHtml(lastText)}</td>
  `;
    tbody.appendChild(tr);
  }
}

function escapeHtml(str){
  return String(str).replace(/[&<>"']/g, s => ({
    "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"
  }[s]));
}

// ========= Render: clients cards =========
function renderClients() {
  const wrap = document.getElementById("clientsWrap");
  if (!wrap) return;

  wrap.innerHTML = "";
  if (!clientsCache.length) {
    wrap.innerHTML = `<div class="empty">No clients loaded. Click “Get clients”.</div>`;
    return;
  }

  for (const c of clientsCache) {
    const phone = c.phone ?? "—";
    const stations = Array.isArray(c.stationsIDs) ? c.stationsIDs : [];
    const card = document.createElement("div");
    card.className = "clientCard";
    card.innerHTML = `
      <div class="top">
        <div><strong>${escapeHtml(phone)}</strong></div>
        <div class="badge">${stations.length} stations</div>
      </div>
      <div class="meta">
        <span>chat_id: ${escapeHtml(c.chat_id ?? "—")}</span>
        <span>tg token: ${c.telegramToken ? "✓ set" : "—"}</span>
      </div>
    `;
    wrap.appendChild(card);
  }
}

// ========= Session + subscriptions =========
const SESSION_KEY = "ban_session_phone";

function loadSession() {
  const phone = localStorage.getItem(SESSION_KEY) || "";
  const sessInput = document.getElementById("sessPhone");
  if (sessInput) sessInput.value = phone;
  updateMySubscriptions();
}

function saveSession() {
  const phone = document.getElementById("sessPhone")?.value.trim() || "";
  localStorage.setItem(SESSION_KEY, phone);
  toast(phone ? "Session saved." : "Session cleared.");
  updateMySubscriptions();
}

function clearSession() {
  localStorage.removeItem(SESSION_KEY);
  const sessInput = document.getElementById("sessPhone");
  if (sessInput) sessInput.value = "";
  toast("Logged out.");
  updateMySubscriptions();
}

function updateMySubscriptions() {
  const phone = localStorage.getItem(SESSION_KEY) || "";
  const hint = document.getElementById("mySubsHint");
  const chips = document.getElementById("mySubsChips");
  if (!chips || !hint) return;

  chips.innerHTML = "";

  if (!phone) {
    hint.textContent = "Set a phone to view your subscribed stations after you subscribe.";
    return;
  }

  const client = clientsCache.find(c => (c.phone || "").trim() === phone.trim());
  if (!client) {
    hint.textContent = "No client found for this phone (load clients or subscribe first).";
    return;
  }

  const stations = Array.isArray(client.stationsIDs) ? client.stationsIDs : [];
  if (!stations.length) {
    hint.textContent = "You have no stations subscribed.";
    return;
  }

  hint.textContent = "Subscribed station IDs:";
  for (const id of stations) {
    const pill = document.createElement("div");
    pill.className = "chipPill";
    pill.textContent = String(id);
    chips.appendChild(pill);
  }
}

// ========= API calls =========
async function getStations() {
  try {
    const resp = await fetch(ENDPOINTS.stations, { headers: { "Accept":"application/json" } });
    const text = await resp.text();
    const data = safeJsonParse(text);
    showOut("outMain", data);

      // Accepta:
  // 1) resposta directa: [ {station_id...}, ... ]
  // 2) wrapper: { data: { stations: [...] } }
  // 3) wrapper extra: { data: { stations: { stations: [...] } } }
  stationsCache =
    (Array.isArray(data)) ? data :
    (Array.isArray(data?.data?.stations)) ? data.data.stations :
    (Array.isArray(data?.data?.stations?.stations)) ? data.data.stations.stations :
    (Array.isArray(data?.stations)) ? data.stations :
    (Array.isArray(data?.stations?.stations)) ? data.stations.stations :
    [];

  renderStations();
  toast(`Stations loaded: ${stationsCache.length}`);
  } catch (e) {
    showOut("outMain", { error: String(e) });
    toast("Error loading stations.");
  }
}

async function getClients() {
  try {
    const resp = await fetch(ENDPOINTS.clients, { headers: { "Accept":"application/json" } });
    const text = await resp.text();
    const data = safeJsonParse(text);

    showOut("outClientsRaw", data);
    clientsCache = Array.isArray(data) ? data : (Array.isArray(data?.clients) ? data.clients : []);
    renderClients();
    updateMySubscriptions();
    toast(`Clients loaded: ${clientsCache.length}`);
  } catch (e) {
    showOut("outClientsRaw", { error: String(e) });
    toast("Error loading clients.");
  }
}

async function subscribe() {
  const phone = document.getElementById("subPhone")?.value.trim() || "";
  const payload = {
    phone,
    stationsIDs: parseStationsIds(document.getElementById("subStations")?.value || ""),
    telegramToken: (document.getElementById("subTgToken")?.value || "").trim(),
    chat_id: Number((document.getElementById("subChatId")?.value || "").trim())
  };

  try {
    const resp = await fetch(ENDPOINTS.subscribe, {
      method:"POST",
      headers: { "Content-Type":"application/json", "Accept":"application/json" },
      body: JSON.stringify(payload)
    });

    const text = await resp.text();
    const data = safeJsonParse(text);
    showOut("outSubscribe", data);

    if (resp.ok) {
      toast("Successful subscription ✅");
      // Convenience: if session empty, auto-set it to the subscribed phone
      if (!(localStorage.getItem(SESSION_KEY) || "").trim() && phone) {
        localStorage.setItem(SESSION_KEY, phone);
        const sess = document.getElementById("sessPhone");
        if (sess) sess.value = phone;
      }
      // refresh clients to reflect new subscription
      await getClients();
    } else {
      toast(typeof data === "string" ? data : (data?.raw || "Subscription failed."));
    }
  } catch (e) {
    showOut("outSubscribe", { error: String(e) });
    toast("Subscription error.");
  }
}

async function notifySlots() {
  try {
    const phone = document.getElementById("notifyPhone")?.value.trim() || "";
    const resp = await fetch(ENDPOINTS.notifySlots + "/" + encodeURIComponent(phone), {
      headers: { "Accept":"application/json" }
    });
    const text = await resp.text();
    const data = safeJsonParse(text);
    showOut("outNotify", data);
    toast(resp.ok ? "Slots notification sent." : "Notifier error.");
  } catch (e) {
    showOut("outNotify", { error: String(e) });
    toast("Notifier error.");
  }
}

async function notifyAirQuality() {
  try {
    const phone = document.getElementById("notifyPhone")?.value.trim() || "";
    const ip = document.getElementById("notifyIp")?.value.trim() || "";

    if (!phone) {
      toast("Enter phone first.");
      document.getElementById("notifyPhone")?.focus();
      return;
    }
    if (!ip) {
      toast("IP is required for air quality.");
      document.getElementById("notifyIp")?.focus();
      return;
    }

    const resp = await fetch(
      ENDPOINTS.notifyAir + "/" + encodeURIComponent(phone) + "/" + encodeURIComponent(ip),
      { headers: { "Accept":"application/json" } }
    );
    const text = await resp.text();
    const data = safeJsonParse(text);
    showOut("outNotify", data);
    toast(resp.ok ? "Air quality notification sent." : "Notifier error.");
  } catch (e) {
    showOut("outNotify", { error: String(e) });
    toast("Notifier error.");
  }
}

async function refreshLogs() {
  try {
    const lines = Number(document.getElementById("logLines")?.value || 200);
    const resp = await fetch(ENDPOINTS.logs + "?lines=" + encodeURIComponent(lines), {
      headers: { "Accept":"text/plain" }
    });
    const text = await resp.text();
    showOut("outLogs", text);
  } catch (e) {
    showOut("outLogs", "Error loading logs: " + String(e));
  }
}

// ========= Events =========
document.getElementById("btnGetStations")?.addEventListener("click", getStations);
document.getElementById("btnGetClients")?.addEventListener("click", getClients);
document.getElementById("btnSubscribe")?.addEventListener("click", subscribe);
document.getElementById("btnNotifySlots")?.addEventListener("click", notifySlots);
document.getElementById("btnNotifyAir")?.addEventListener("click", notifyAirQuality);
document.getElementById("btnRefreshLogs")?.addEventListener("click", refreshLogs);

document.getElementById("btnClearOutput")?.addEventListener("click", () => {
  showOut("outMain", "{ }");
  showOut("outClientsRaw", "{ }");
  showOut("outSubscribe", "{ }");
  showOut("outNotify", "{ }");
});

document.getElementById("btnSaveSession")?.addEventListener("click", saveSession);
document.getElementById("btnClearSession")?.addEventListener("click", clearSession);
document.getElementById("btnRefreshFromClients")?.addEventListener("click", updateMySubscriptions);

document.getElementById("stationSearch")?.addEventListener("input", renderStations);
document.getElementById("btnFilterInService")?.addEventListener("click", (e) => {
  filterInService = !filterInService;
  e.target.classList.toggle("secondary", filterInService);
  renderStations();
});
document.getElementById("btnFilterLowBikes")?.addEventListener("click", (e) => {
  filterLowBikes = !filterLowBikes;
  e.target.classList.toggle("secondary", filterLowBikes);
  renderStations();
});

// initial
loadSession();
refreshLogs();