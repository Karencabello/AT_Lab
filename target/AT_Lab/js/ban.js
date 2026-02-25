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


/* ==========================================================
   CONFIGURACIÓ DE L'API
   ========================================================== */

/*
 * window.location.pathname pot ser:
 *   /AT_Lab/
 *
 * El que fem és eliminar l'últim slash
 * i afegir /api
 */
const CONTEXT = window.location.pathname.replace(/\/$/, "");
const API_BASE = window.location.origin + CONTEXT + "/api";

/*
 * Definim els endpoints reals del backend
 */
const ENDPOINTS = {
  stations: API_BASE + "/stations",
  clients: API_BASE + "/clients",
  subscribe: API_BASE + "/clients/subscribe"
};



/* ==========================================================
   FUNCIONS UTILITÀRIES
   ========================================================== */

/*
 * Intenta parsejar text com JSON.
 * Si el backend retorna HTML (error 404/500),
 * evitem que el JS peti.
 */
function safeJsonParse(text) {
  try {
    return JSON.parse(text);
  } catch {
    return { raw: text };
  }
}

/*
 * Converteix "12,34,56" → [12, 34, 56]
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
   MOSTRAR RESULTATS
   ========================================================== */

/*
 * Escriu resposta al panell principal
 */
function showMainOutput(data) {
  const el = document.getElementById("outMain");
  if (el) {
    el.textContent = JSON.stringify(data, null, 2);
  }
}

/*
 * Escriu resposta al panell de subscribe
 */
function showSubscribeOutput(data) {
  const el = document.getElementById("outSubscribe");
  if (el) {
    el.textContent = JSON.stringify(data, null, 2);
  }
}



/* ==========================================================
   API CALLS
   ========================================================== */

/*
 * GET /api/stations
 */
async function getStations() {

  try {
    const resp = await fetch(ENDPOINTS.stations, {   
      headers: { "Accept": "application/json" }
    });

    const text = await resp.text();
    const data = safeJsonParse(text);

    showMainOutput(data);

  } catch (e) {
    showMainOutput({ error: String(e) });
  }
}



/*
 * GET /api/clients
 */
async function getClients() {

  try {
    const resp = await fetch(ENDPOINTS.clients, {
      headers: { "Accept": "application/json" }
    });

    const text = await resp.text();
    const data = safeJsonParse(text);

    showMainOutput(data);

  } catch (e) {
    showMainOutput({ error: String(e) });
  }
}


/*
 * POST /api/clients/subscribe
 */
async function subscribe() {

  /*
   * Construïm el JSON que espera el backend.
   * Ha de coincidir EXACTAMENT amb el model Client.java
   */
  const payload = {
    phone: document.getElementById("subPhone").value.trim(),
    stationsIDs: parseStationsIds(
      document.getElementById("subStations").value
    ),
    telegramToken: document.getElementById("subTgToken").value.trim(),
    chat_id: Number(
      document.getElementById("subChatId").value.trim()
    )
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

    showSubscribeOutput(data);

  } catch (e) {
    showSubscribeOutput({ error: String(e) });
  }
}



/* ==========================================================
   EVENT LISTENERS
   ========================================================== */

document.getElementById("btnGetStations")
  ?.addEventListener("click", getStations);

document.getElementById("btnGetClients")
  ?.addEventListener("click", getClients);

document.getElementById("btnSubscribe")
  ?.addEventListener("click", subscribe);
