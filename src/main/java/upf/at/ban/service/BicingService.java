package upf.at.ban.service;

import upf.at.ban.model.Data;
import upf.at.ban.util.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;


public class BicingService {

    public Data getStations(){
        
        // 1. Creem client
        Client client = ClientBuilder.newClient();
        
        // 2. Creem URL
        WebTarget target = client.target(Constants.BICING_URL).path(Constants.BICING_PATH);

        // 3. Fem petició GET
        // - Accept: aplicació/JSON
        // - Header: token
        // - Parse del JSON cap a clase data
        Data data = target.request(MediaType.APPLICATION_JSON_TYPE).header("Authorization", Constants.BICING_TOKEN).get(new GenericType<Data>() {});

        return data;
    }

}
