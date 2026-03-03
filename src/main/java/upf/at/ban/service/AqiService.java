package upf.at.ban.service;

import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import upf.at.ban.util.Constants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * AqiService
 *
 * Objectiu (segons el PDF):
 * - Obtenir l'Air Quality Index (AQI) a partir del NOM DE LA CIUTAT.
 * - Usant el projecte World Air Quality Index (aqicn.org / api.waqi.info) + token.
 *
 * IMPORTANT:
 * - Fer directament /feed/<city> NO funciona per totes les ciutats (ex: "Banyoles").
 * - Solució robusta "by city name":
 *    1) SEARCH per keyword=<city>  -> ens retorna estacions candidates (uid)
 *    2) FEED per @uid              -> ens retorna AQI fiable
 *
 * Això segueix complint: "AQI using the city name" perquè l'entrada és el city name.
 */
public class AqiService {

    // Logger per aquesta classe
    private static final Logger logger = LogManager.getLogger(AqiService.class);

    /**
     * Mètode principal que crida NotifierService:
     * - Rep el city name
     * - Fa search -> uid
     * - Fa feed/@uid -> aqi
     */
    public Integer getAqiByCity(String city) {

        if (city == null || city.trim().isEmpty()) return null;

        // 1) Search stations by keyword (city)
        Integer uid = searchFirstStationUid(city.trim());
        if (uid == null) {
            logger.warn("AQI_CITY no station found for city={}", city);
            return null;
        }

        // 2) Fetch AQI by station UID
        Integer aqi = getAqiByUid(uid);
        if (aqi == null) {
            logger.warn("AQI_CITY feed by uid failed city={} uid={}", city, uid);
        }

        return aqi;
    }

    /**
     * SEARCH endpoint (AQICN):
     *   GET https://api.waqi.info/search/?keyword=<city>&token=...
     *
     * Retorna una llista de resultats (stations). Cada resultat porta un "uid".
     * Aquí agafem el primer resultat (és suficient per la pràctica/demo).
     */
    private Integer searchFirstStationUid(String city) {

        Client client = ClientBuilder.newClient();

        try {
            WebTarget target = client
                    .target("https://api.waqi.info/search/")
                    .queryParam("keyword", city)
                    .queryParam("token", Constants.AQI_TOKEN);

            Map<String, Object> response = target
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(new GenericType<Map<String, Object>>() {});

            if (response == null) return null;

            String status = String.valueOf(response.get("status"));
            if (!"ok".equals(status)) {
                // Log important per entendre errors (token, quota, etc.)
                logger.warn("AQI_SEARCH not ok city={} status={} data={}", city, status, response.get("data"));
                return null;
            }

            Object dataObj = response.get("data");
            if (!(dataObj instanceof List)) return null;

            List<?> data = (List<?>) dataObj;
            if (data.isEmpty()) return null;

            Object first = data.get(0);
            if (!(first instanceof Map)) return null;

            Map<?, ?> firstMap = (Map<?, ?>) first;
            Object uidObj = firstMap.get("uid");

            if (!(uidObj instanceof Number)) return null;

            Integer uid = ((Number) uidObj).intValue();
            logger.info("AQI_SEARCH ok city={} uid={}", city, uid);
            return uid;

        } catch (Exception e) {
            logger.error("AQI_SEARCH error city={} err={}", city, e.toString());
            return null;

        } finally {
            client.close();
        }
    }

    /**
     * FEED endpoint per UID (AQICN):
     *   GET https://api.waqi.info/feed/@<uid>/?token=...
     *
     * Retorna JSON amb data.aqi.
     */
    private Integer getAqiByUid(int uid) {

        Client client = ClientBuilder.newClient();

        try {
            WebTarget target = client
                    .target(Constants.AQI_API_URL)     // "https://api.waqi.info/feed/"
                    .path("@" + uid + "/")
                    .queryParam("token", Constants.AQI_TOKEN);

            Map<String, Object> response = target
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(new GenericType<Map<String, Object>>() {});

            if (response == null) return null;

            String status = String.valueOf(response.get("status"));
            if (!"ok".equals(status)) {
                logger.warn("AQI_FEED_UID not ok uid={} status={} data={}", uid, status, response.get("data"));
                return null;
            }

            Object dataObj = response.get("data");
            if (!(dataObj instanceof Map)) return null;

            Map<?, ?> data = (Map<?, ?>) dataObj;
            Object aqiObj = data.get("aqi");

            // Convertim a Integer de forma robusta
            if (aqiObj instanceof Number) {
                Integer aqi = ((Number) aqiObj).intValue();
                logger.info("AQI ok city_uid=@{} aqi={}", uid, aqi);
                return aqi;
            }

            return null;

        } catch (Exception e) {
            logger.error("AQI_FEED_UID error uid={} err={}", uid, e.toString());
            return null;

        } finally {
            client.close();
        }
    }
}
