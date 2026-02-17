package upf.at.ban.controller;

import upf.at.ban.model.Client;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/clients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClientResource {

    @GET
    public List<Client> getClients() {
        // TODO: return repository.findAll();
        throw new WebApplicationException("Not implemented", 501);
    }

    @POST
    public Client createClient(Client client) {
        // TODO: return repository.save(client);
        throw new WebApplicationException("Not implemented", 501);
    }
}
