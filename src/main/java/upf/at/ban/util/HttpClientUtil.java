package upf.at.ban.util;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class HttpClientUtil {

    private static final Client client = ClientBuilder.newClient();

    public static String sendGet(String url) {

        WebTarget target = client.target(url);

        Response response = target
                .request(MediaType.APPLICATION_JSON)
                .get();

        return response.readEntity(String.class);
    }

    public static String sendPost(String url, Object body) {

        WebTarget target = client.target(url);

        Response response = target
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(body, MediaType.APPLICATION_JSON));

        return response.readEntity(String.class);
    }
}
