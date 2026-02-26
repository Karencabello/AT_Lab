package upf.at.ban.service;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import upf.at.ban.util.Constants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AqiService {

    // Logger per aquesta classe
    private static final Logger logger = LogManager.getLogger(AqiService.class);

    public Integer getAqiByCity(String city) {

        //1. Creem client HTTP
        Client client = ClientBuilder.newClient();

        try {
            //2. Construïm URL
            WebTarget target = client
                    .target(Constants.AQI_API_URL)
                    .path(city)
                    .queryParam("token", Constants.AQI_TOKEN);

            //3. Fem GET i convertim resposta JSON en Map
            Map<String, Object> response = target
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(new GenericType<Map<String, Object>>() {});

            //si no resposta --> error
            if (response == null) return null;

            //si no error --> status:ok
            if (!"ok".equals(response.get("status"))) return null;

            //4. Objecte data amb tota la info
            Map<String, Object> data = (Map<String, Object>) response.get("data");

            if (data == null) return null;

            //5. Extreiem camp "aqi"
            Object aqiObj = data.get("aqi");

            //6. Convertim valor a enter
            if (aqiObj instanceof Integer) { 
                Integer aqi = (Integer) aqiObj;
                logger.info("AQI ok city={} aqi={}", city, aqi);
                return aqi;
            } else if (aqiObj instanceof Double) {
                Integer aqi = ((Double) aqiObj).intValue();
                logger.info("AQI ok city={} aqi={}", city, aqi);
                return aqi;
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("AQI error city={} err={}", city, e.toString());
            return null;
        } finally {
            client.close();
        }
    }
}
