package upf.at.ban.util;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// Helper per fer GET/POST amb JAX-RS client
// Evitem repetir el mateix codi a cada Service (BicingService, TelegramService, etc.)
public class HttpClientUtil {

    // Client reutilitzable
    private static final Client client = ClientBuilder.newClient();

    // Envia un GET a una URL i retorna el body com String.
    public static String sendGet(String url) {

        WebTarget target = client.target(url);

        Response response = null;

        try {
            response = target
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            return response.readEntity(String.class);

        } finally {
            // Tanquem sempre el Response (important)
            if (response != null) response.close();
        }

    }

    // // Envia un POST a una URL amb un body i retorna el body com String.
    public static String sendPost(String url, Object body) {

        WebTarget target = client.target(url);

        Response response = null;
        
        try {
            response = target
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(body, MediaType.APPLICATION_JSON));

            return response.readEntity(String.class);

        } finally {
            if (response != null) response.close();
        }
    }
}
