/**********************************************************************
 * BAN Dashboard - Frontend Logic
 *
 * Aquest fitxer:
 *  - Detecta el context path (AT_Lab) per construir les URLs d'API
 *  - Fa fetch a endpoints REST del backend (Jersey) i mostra les respostes
 *  - Renderitza una taula de stations amb filtres i KPIs
 *  - Permet gestionar clients i subscripcions (via POST /api/clients/subscribe)
 *  - Permet enviar notificacions de slots i qualitat d'aire als clients
 *  - Mostra logs del backend
 *
 * Funcionalitats
 *  - Fa peticions HTTP al backend Jersey
 *  - Mostra resultats al panell de sortida
 *  - Permet crear clients via POST /api/clients/subscribe
 *
 * IMPORTANT:
 *  - Detectem automàticament el context path (AT_Lab)
 *  - En un war tipic el context path és (/AT_Lab)
 *  - En Docker (ROOT.war) és ""
 *  - Això permet que funcioni en local i en Docker sense canvis
 **********************************************************************/

// ========= API - Autodetecció del context root =========
// Permet que el mateix frontend funcioni tant si el war està desplegat com ROOT.war (context "") 
// com si està desplegat com AT_Lab.war (context "/AT_Lab"), 
// detectant-ho automàticament a partir de window.location.pathname.
function detectContextRoot() {
  const parts = window.location.pathname.split("/").filter(Boolean);

  // ROOT: "/" o "/index.html"
  if (parts.length === 0) return "";
  if (parts[0].includes(".")) return ""; // index.html, etc.

  // War context: "/AT_Lab/...."
  return "/" + parts[0];
}

// API_BASE (ROOT) = http(s)://host:port
const API_BASE = window.location.origin + detectContextRoot() + "/api";

//Endpoints del backend per no repetir strings i gestionar més fàcilment
const ENDPOINTS = {
  stations: API_BASE + "/stations",
  clients: API_BASE + "/clients",
  subscribe: API_BASE + "/clients/subscribe",
  notifySlots: API_BASE + "/notifier/slots",
  notifyAir: API_BASE + "/notifier/air",
  logs: API_BASE + "/logs"
};


// ========= Estat memoria frontend =========
// Cache fontend  de stations i clients per evitar recarregar-los 
let stationsCache = [];
let clientsCache = [];

// Filtres de visualització de stations (per la taula)
let filterInService = false;
let filterLowBikes = false;

// ========= Utils =========
// Parseja JSON de forma segura i ensenya error
function safeJsonParse(text) {
  try { return JSON.parse(text); } catch { return { raw: text }; }
}

// Converteix llista estacions 1,2,3 en [1,2,3]
function parseStationsIds(csv) {
  return (csv || "")
    .split(",")
    .map(s => s.trim())
    .filter(Boolean)
    .map(x => parseInt(x, 10))
    .filter(n => !Number.isNaN(n));
}

// Actualitza el text d'un element per id
// És una utilitat per actualitzar text en elements de la UI de forma segura i reutilitzable.
// Evita repetir document.getElementById(...) i .textContent en el codi de renderitzat
function setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value;
}

// Un “toast” és un missatge petit que surt uns segons (tipus notificació).
// Missatge visual amb timeout per desapareixer
function toast(msg) {
  const el = document.getElementById("toast");
  if (!el) return;
  el.textContent = msg;
  el.classList.add("show");
  window.clearTimeout(toast._t);
  toast._t = window.setTimeout(() => el.classList.remove("show"), 2600);
}

// Normalitza strings per comparacions (lowercase, trim)
function normalize(s){ return String(s || "").toLowerCase(); }

// Calcula:
// - Total de bikes disponibles
// - Total de docks disponibles
// - Total general (bikes + docks)
// - Ocupació mitjana (bikes / total) en %
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

// Mostra de forma amigable el resultat de les peticions al backend, sense raw
function showOut(id, data) {
  const el = document.getElementById(id);
  if (!el) return;

  // Si el backend retorna text (o safeJsonParse l'ha posat dins {raw: ...})
  if (typeof data === "string") {
    el.textContent = data;
    return;
  }

  if (data && typeof data === "object") {
    // Cas típic: { raw: "User is not an adult" }
    const keys = Object.keys(data);
    if (keys.length === 1 && keys[0] === "raw") {
      el.textContent = String(data.raw);
      return;
    }
    // Altres casos comuns: { message: "..."}
    if (typeof data.message === "string") {
      el.textContent = data.message;
      return;
    }
  }

  el.textContent = JSON.stringify(data, null, 2);
}

// ========= Render: stations table =========

// Renderitza la taula de stations aplicant els filtres i actualitzant els KPIs
// Filtres:
// - filterInService: només stations amb status "in_service"
// - filterLowBikes: només stations amb 2 o menys bikes disponibles
// - stationSearch: cerca per ID de station (inclusió parcial)
// KPIs:
// - kpiStations: total stations mostrades
// - kpiBikes: total bikes disponibles
// - kpiDocks: total docks disponibles
// - kpiOcc: ocupació mitjana en %
function renderStations() {
  const tbody = document.getElementById("stationsTbody");
  if (!tbody) return;

  const q = normalize(document.getElementById("stationSearch")?.value || "");

  // copia per no untilizar original (cache) i aplicar filtres
  let list = Array.isArray(stationsCache) ? [...stationsCache] : [];

  // Filtres: in_service i low bikes
  if (filterInService) list = list.filter(s => normalize(s?.status) === "in_service");
  if (filterLowBikes)  list = list.filter(s => Number(s?.num_bikes_available ?? 0) <= 2);

  // Filtre de búsqueda per ID 
  if (q) list = list.filter(s => String(s?.station_id ?? "").includes(q));

  // Calcula KPIs a partir de la llista filtrada
  const totals = stationTotals(list);
  setText("kpiStations", String(list.length));
  setText("kpiBikes", String(totals.bikes));
  setText("kpiDocks", String(totals.docks));
  setText("kpiOcc", totals.occ + "%");

  // Si no queda cap station després de filtres, mostra missatge 
  if (!list.length) {
    tbody.innerHTML = `<tr><td colspan="5" class="empty">No stations match your filters.</td></tr>`;
    return;
  }

  // Fem taula
  tbody.innerHTML = list.map(s => {
    const id = s?.station_id ?? "—";
    const status = s?.status ?? "—";
    const bikes = Number(s?.num_bikes_available ?? 0);
    const docks = Number(s?.num_docks_available ?? 0);
    const total = bikes + docks;
    const occ = total > 0 ? Math.round((bikes / total) * 100) : 0;

    return `
      <tr>
        <td>${id}</td>
        <td><span class="badge">${escapeHtml(status)}</span></td>
        <td>${bikes}</td>
        <td>${docks}</td>
        <td>${occ}%</td>
      </tr>
    `;
  }).join("");
}

// Escapa caracteres HTML para evitar inyección en la tabla
// Per seguritat
function escapeHtml(str){
  return String(str).replace(/[&<>"']/g, s => ({
    "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"
  }[s]));
}

// ========= Render: clients cards =========

// És una representació visual dels clients. El JSON encara el mostro a outClientsRaw, 
// però les cards són per fer la demo més llegible
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

// Guardamos el teléfono en localStorage para mantener la sesión entre recargas y 
// mostrar las estaciones a las que el cliente está suscrito.
const SESSION_KEY = "ban_session_phone";

// Carrega el telefon i refresca les subscripcions del client al carregar la pàgina. 
function loadSession() {
  const phone = localStorage.getItem(SESSION_KEY) || "";
  const sessInput = document.getElementById("sessPhone");
  if (sessInput) sessInput.value = phone;
  updateMySubscriptions();
}

// Guarda el telefon com a sessio
function saveSession() {
  const phone = document.getElementById("sessPhone")?.value.trim() || "";
  localStorage.setItem(SESSION_KEY, phone);
  toast(phone ? "Session saved." : "Session cleared.");
  updateMySubscriptions();
}

// Borra sessió
function clearSession() {
  localStorage.removeItem(SESSION_KEY);
  const sessInput = document.getElementById("sessPhone");
  if (sessInput) sessInput.value = "";
  toast("Logged out.");
  updateMySubscriptions();
}

// Actualitza la llista de subscripcions del client actual (segons el telèfon de sessió) 
// a partir de clientsCache.
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

/**
 * Mostra/amaga l'alerta de la secció "My session".
 * type: classe extra CSS (ex: "ok", "err") per canviar color/estil
 * msg: text a mostrar; si és buit, s'amaga
 */
function setSessionAlert(type, msg) {
  const el = document.getElementById("sessionAlert");
  if (!el) return;
  el.className = "alert " + (type || "");
  el.textContent = msg;
  el.style.display = msg ? "block" : "none";
}

/**
 * Mostra/amaga l'alerta de la secció "Subscribe client".
 * type: classe extra CSS (ex: "ok", "err") per canviar color/estil
 * msg: text a mostrar; si és buit, s'amaga
 */
function setSubscribeAlert(type, msg) {
  const el = document.getElementById("subscribeAlert");
  if (!el) return;
  el.className = "alert " + (type || "");
  el.textContent = msg;
  el.style.display = msg ? "block" : "none";
}


// ========= API calls =========

// GET /stations: carrega les stations del backend, les guarda a stationsCache, 
// mostra el resultat raw i renderitza la taula de stations.
async function getStations() {
  const url = ENDPOINTS.stations;
  showOut("outMain", { info: "Requesting…", url });

  try {
    const resp = await fetch(url, { headers: { "Accept": "application/json" } });
    const text = await resp.text();

    // Muestra siempre lo que vuelve el servidor (aunque sea HTML de error)
    let data = safeJsonParse(text);

    // Si NO es 2xx, no lo tratamos como excepción: lo mostramos claro
    if (!resp.ok) {
      showOut("outMain", {
        error: "HTTP error",
        url,
        status: resp.status,
        statusText: resp.statusText,
        bodyPreview: text.slice(0, 500)
      });
      toast(`HTTP ${resp.status} on /stations`);
      return;
    }

    showOut("outMain", data);

    // Soporta array directo o wrappers
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
    // Aquí caen errores reales de red (ECONNREFUSED, CORS, etc.)
    showOut("outMain", {
      error: "Network/Fetch failed",
      url,
      message: String(e),
      stack: e?.stack || "(no stack)"
    });
    toast("Error loading stations.");
  }
}

// GET /clients: carrega els clients del backend, els guarda a clientsCache, 
// mostra el resultat raw i renderitza les cards de clients.
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

// POST /clients/subscribe: crea o actualitza un client amb les dades del formulari, 
// mostra el resultat raw i un missatge d'èxit o error segons la resposta del servidor. 
// També actualitza la llista de clients per mostrar la nova subscripció.
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
    const msg = 
      (typeof data === "string") ? data :
      (data?.raw) ? data.raw :
      (data?.message) ? data.message :
      (resp.ok ? "OK" : "Subscription failed");

    // Alert específic del formulari de subscripció
    if (resp.ok) setSubscribeAlert("ok", "Subscribed correctly.");
    else setSubscribeAlert("err", msg);


    if (resp.ok) {
      toast("Successful subscription ");
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

// POST /notifier/slots/{phone}: envia notificació de slots al client,
// mostra el resultat raw i un missatge d'èxit o error segons la resposta del servidor.
// Envia missatge
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

// POST /notifier/air/{phone}/{ip}: envia notificació de qualitat d'aire al client,
// Envia missatge
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

// GET /logs: carrega els logs del backend, mostra el resultat raw i un missatge d'èxit 
// o error segons la resposta del servidor. Permet especificar el nombre de línies a mostrar.
async function refreshLogs() {
  const lines = Number(document.getElementById("logLines")?.value || 200);
  const url = ENDPOINTS.logs + "?lines=" + encodeURIComponent(lines);

  try {
    const resp = await fetch(url, { headers: { "Accept": "text/plain" } });
    const text = await resp.text();

    if (!resp.ok) {
      showOut("outLogs", `HTTP ${resp.status} ${resp.statusText}\nURL: ${url}\n\n${text.slice(0, 1200)}`);
      return;
    }
    showOut("outLogs", text);
  } catch (e) {
    showOut("outLogs", "Error loading logs: " + String(e) + "\nURL: " + url);
  }
}

// ========= Conectar botons/inputs amb funcions =========
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