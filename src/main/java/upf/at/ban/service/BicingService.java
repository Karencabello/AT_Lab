package upf.at.ban.service;

import upf.at.ban.model.Data;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

public class BicingService {
    private static final String bicingURL = "https://opendata-ajuntament.barcelona.cat/data/dataset";

    private static final String bicingPath = "6aa3416d-ce1a-494d-861b-7bd07f069600/resource/1b215493-9e63-4a12-8980-2d7e0fa19f85/download";

    private static final String token = "afegirtoken";

    public Data getStations(){

        // 1. Creem client
        Client client = ClientBuilder.newClient();
        
        // 2. Creem URL
        WebTarget target = client.target(bicingURL).path(bicingPath);

        // 3. Fem petició GET
        // - Accept: aplicació/JSON
        // - Header: token
        // - Parse del JSON cap a clase data
        Data data = target.request(MediaType.APPLICATION_JSON_TYPE).header("Authorization", token).get(new GenericType<Data>() {});

        return data;
    }

}
