/**********************************************************************
 * BAN Dashboard - Frontend Logic
 *
 * Aquest fitxer:
 *  - Fa peticions AJAX a la BAN API (Jersey backend)
 *  - Mostra els resultats en format taula i JSON
 *  - Permet crear i subscriure clients (quan el backend estigui fet)
 *
 * IMPORTANT:
 *  - Fem servir fetch() (AJAX modern)
 *  - Fem servir rutes RELATIVES ("api/...") perquè funcioni
 *    tant en local com en AWS sense modificar el codi.
 **********************************************************************/

/* ==========================================================
   CONFIGURACIÓ D'ENDPOINTS
   ========================================================== */

/*
 * Base definida per @ApplicationPath("/api") al backend.
 * Això vol dir que tots els recursos pengen de /api/...
 */
const API = "api";

/*
 * Centralitzem tots els endpoints aquí.
 * Si el backend canvia el path final, només hem de modificar
 * aquestes línies i no tot el codi.
 */
const ENDPOINTS = {
  stations: `${API}/stations`,              // GET
  clients: `${API}/clients`,                // GET
  subscribe: `${API}/clients/subscribe`     // POST
};

/*
 * Mostrem visualment la base de l'API a la interfície.
 */
document.getElementById("basePath").textContent = API + "/";


/* ==========================================================
   FUNCIONS UTILITÀRIES
   ========================================================== */

/*
 * Converteix un string a JSON de forma segura.
 * Si el backend retorna HTML o error en text pla,
 * evitem que el codi peti.
 */
function safeJsonParse(text) {
  try {
    return JSON.parse(text);
  } catch {
    return { raw: text };
  }
}

/*
 * Converteix un input tipus:
 * "12,34,56"
 * en un array:
 * [12, 34, 56]
 */
function parseStationsIds(csv) {
  return (csv || "")
    .split(",")
    .map(s => s.trim())
    .filter(Boolean)
    .map(x => parseInt(x, 10))
    .filter(n => !Number.isNaN(n));
}


/* ==========================================================
   RENDERITZACIÓ DE TAULES
   ========================================================== */

/*
 * Construeix la taula HTML de stations.
 * Rep un array de Station (del backend).
 */
function renderStationsTable(stations) {

  // Si no hi ha dades
  if (!Array.isArray(stations) || stations.length === 0) {
    return `<div class="empty">No hi ha dades disponibles.</div>`;
  }

  // Generem files HTML dinàmicament
  const rows = stations.map(s => `
    <tr>
      <td>${s.station_id ?? ""}</td>
      <td>${s.num_bikes_available ?? ""}</td>
      <td>${s.num_docks_available ?? ""}</td>
      <td>${s.status ?? ""}</td>
      <td>${s.last_reported ?? ""}</td>
      <td>${s.is_charging_station ?? ""}</td>
    </tr>
  `).join("");

  return `
    <table>
      <thead>
        <tr>
          <th>station_id</th>
          <th>bikes</th>
          <th>docks</th>
          <th>status</th>
          <th>last_reported</th>
          <th>charging</th>
        </tr>
      </thead>
      <tbody>${rows}</tbody>
    </table>
  `;
}


/*
 * Renderitza la llista de clients.
 * Fem servir noms flexibles per si el backend
 * usa camelCase o snake_case.
 */
function renderClientsList(clients) {

  if (!Array.isArray(clients) || clients.length === 0) {
    return `<div class="empty">No hi ha clients.</div>`;
  }

  return clients.map(c => {

    // Fem tolerant el render
    const phone = c.phone ?? c.phone_number ?? "—";
    const token = c.telegramToken ?? c.telegram_token ?? "—";
    const chat = c.chat_id ?? c.chatId ?? "—";
    const stations = c.stationsIds ?? c.stations_ids ?? [];

    const stationsTxt = Array.isArray(stations)
      ? stations.join(", ")
      : String(stations);

    return `
      <div class="clientCard">
        <div><b>Phone:</b> ${phone}</div>
        <div><b>Chat ID:</b> ${chat}</div>
        <div><b>Token:</b> ${token}</div>
        <div><b>Stations:</b> ${stationsTxt}</div>
      </div>
    `;
  }).join("");
}


/* ==========================================================
   API CALLS
   ========================================================== */

/*
 * GET /api/stations
 * Recupera totes les estacions.
 */
async function getStations() {

  try {

    // Fetch envia una petició HTTP GET
    const resp = await fetch(ENDPOINTS.stations, {
      headers: { "Accept": "application/json" }
    });

    const text = await resp.text();
    const data = safeJsonParse(text);

    // Mostrem JSON cru
    document.getElementById("outStations").textContent =
      JSON.stringify(data, null, 2);

    // Renderitzem taula
    document.getElementById("stationsTableWrap").innerHTML =
      renderStationsTable(data);

  } catch (e) {

    document.getElementById("outStations").textContent =
      JSON.stringify({ error: String(e) }, null, 2);
  }
}


/*
 * GET /api/clients
 * Recupera la llista de clients.
 * Funcionarà quan el backend estigui implementat.
 */
async function getClients() {

  try {

    const resp = await fetch(ENDPOINTS.clients, {
      headers: { "Accept": "application/json" }
    });

    const text = await resp.text();
    const data = safeJsonParse(text);

    document.getElementById("outClients").textContent =
      JSON.stringify(data, null, 2);

    document.getElementById("clientsWrap").innerHTML =
      renderClientsList(data);

  } catch (e) {

    document.getElementById("outClients").textContent =
      JSON.stringify({ error: String(e) }, null, 2);
  }
}


/*
 * POST /api/clients/subscribe
 * Crea un client i el subscriu a estacions.
 */
async function subscribe() {

  // Construïm el payload a partir del formulari
  const payload = {
    phone: document.getElementById("subPhone").value.trim(),
    stationsIds: parseStationsIds(
      document.getElementById("subStations").value
    ),
    telegramToken: document.getElementById("subTgToken").value.trim(),
    chat_id: Number(document.getElementById("subChatId").value.trim())
  };

  try {

    const resp = await fetch(ENDPOINTS.subscribe, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Accept": "application/json"
      },
      body: JSON.stringify(payload)
    });

    const text = await resp.text();
    const data = safeJsonParse(text);

    document.getElementById("outSubscribe").textContent =
      JSON.stringify(data, null, 2);

  } catch (e) {

    document.getElementById("outSubscribe").textContent =
      JSON.stringify({ error: String(e) }, null, 2);
  }
}


/* ==========================================================
   EVENT LISTENERS
   ========================================================== */

document.getElementById("btnGetStations")
  .addEventListener("click", getStations);

document.getElementById("btnGetClients")
  .addEventListener("click", getClients);

document.getElementById("btnSubscribe")
  .addEventListener("click", subscribe);
