package upf.at.ban.service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.GenericType;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class IpGeoService {

    // Logger per aquesta classe
    private static final Logger logger = LogManager.getLogger(IpGeoService.class);

    //URL base de l’API pública
    private static final String IP_API_URL = "http://ip-api.com/json/";

    public String getCityByIp(String ip) {

        //1. Creem client HTTP per fer la petició
        Client client = ClientBuilder.newClient();

        try {
            //2. construim URL final
            WebTarget target = client.target(IP_API_URL + ip);

            //3. Fem petició GET 
            /**
             * accepta JSON
             * Jersey converteix automàticament la resposta en un Map
             */
            Map<String, Object> response = target
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(new GenericType<Map<String, Object>>() {});

            //4. si no resposta --> error
            if (response == null) return null;

            /** 5.
             * si tot be --> success
             * ni error --> null
             */
            String status = (String) response.get("status");
            if (!"success".equals(status)) return null;

            //6. Extraiem el camp "city" del JSON
            String city = (String) response.get("city");
            logger.info("IP_GEO ok ip={} city={}", ip, city);
            return city;

        } catch (Exception e) { //gestió error
            e.printStackTrace();
            logger.error("IP_GEO error ip={} err={}", ip, e.toString());
            return null;
        } finally {
            client.close();
        }
    }
}
