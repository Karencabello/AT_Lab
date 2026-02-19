package upf.at.ban.controller;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import upf.at.ban.model.Client;
import upf.at.ban.repository.ClientRepository;
import upf.at.ban.service.AgeVerificationService;

@Path("/clients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClientResource {

    private ClientRepository repository = ClientRepository.getInstance();  //guardar i llegir clients
    private AgeVerificationService ageService = new AgeVerificationService(); //validar edat

    @GET
    public Collection<Client> getClients() {
        return repository.getAllClients();
    }

    //Utilitzem response per que permet: Definir status code, 
    //definir missatge i control total de resposta
    @POST
    @Path("/subscribe")
    public Response subscribe(Client client) {

        //si no client, retornem 400 (Bad Request)
        if(client==null){ 
            return Response.status(Response.Status.BAD_REQUEST).entity("Client required").build();
        }

        // si no phone, retornem 400 (Bad Request)
        if(client.getPhone() == null || client.getPhone().isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST).entity("Phone number required").build();
        }

        //si no adult, retornem 403 (Forbidden)
        if(!ageService.isAdult(client.getPhone())){ 
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("User is not an adult")
                    .build();
        }

        //si Client ja existeix, retornem 409 (Conflict)
        if(repository.getClientByPhone(client.getPhone()) != null){
            return Response.status(Response.Status.CONFLICT)
                    .entity("Client already exists")
                    .build();
        }

        repository.addClient(client);

        //quan client creat, retornem 201 (Created)
        return Response.status(Response.Status.CREATED)
                .entity(client)
                .build();
    }
}
