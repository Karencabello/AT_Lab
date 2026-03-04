package upf.at.ban.resource;

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
import upf.at.ban.service.ValidateNameService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/clients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClientResource {

    // Logger per aquesta classe
    private static final Logger logger = LogManager.getLogger(ClientResource.class);

    // Repository per guardar i llegir clients
    private ClientRepository repository = ClientRepository.getInstance();  //guardar i llegir clients
    
    // Servei per validar edat
    private AgeVerificationService ageService = new AgeVerificationService(); //validar edat

    // Servei per validar nom
     private ValidateNameService nameService = new ValidateNameService();

    @GET
    public Collection<Client> getClients() {
        logger.info("API_GET_CLIENTS");
        return repository.getAllClients();
    }

    //Utilitzem response per que permet: Definir status code, 
    //definir missatge i control total de resposta
    @POST
    @Path("/subscribe")
    public Response subscribe(Client client) {

        if (client == null) {
            logger.warn("SUBSCRIBE_REJECT reason=client_null");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Client required")
                    .build();
        }

        // log
        logger.info("\"API_SUBSCRIBE phone={} name={} stations={} chatId={}\"", client.getPhone(), client.getName(), client.getStationsIDs(), client.getChat_id());


        // si no phone, retornem 400 (Bad Request)
        if(client.getPhone() == null || client.getPhone().isEmpty()){
            logger.warn("SUBSCRIBE_REJECT reason=phone_missing");
            return Response.status(Response.Status.BAD_REQUEST).entity("Phone number required").build();
        }

        // si no name, retornem 400
        if (client.getName() == null || client.getName().trim().isEmpty()) {
            logger.warn("SUBSCRIBE_REJECT reason=name_missing phone={}", client.getPhone());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Name required")
                    .build();
        }

        //si no adult, retornem 403 (Forbidden)
        if(!ageService.isAdult(client.getPhone())){ 
            logger.warn("SUBSCRIBE_REJECT reason=not_adult phone={}", client.getPhone());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("User is not an adult")
                    .build();
        }
        // MatchName, si l'API retorna nameMatch != true --> no deixem subscriure
        boolean nameOk;
        try {
            nameOk = nameService.isNameMatchingPhone(client.getPhone(), client.getName());
        } catch (Exception e) {
            logger.error("SUBSCRIBE_REJECT reason=kyc_match_error phone={} err={}",
                    client.getPhone(), e.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unable to validate name with phone number")
                    .build();
        }

        if (!nameOk) {
            logger.warn("SUBSCRIBE_REJECT reason=name_mismatch phone={} name={}",
                    client.getPhone(), client.getName());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Name does not match phone owner")
                    .build();
        }

        //si Client ja existeix, retornem 409 (Conflict)
        if(repository.getClientByPhone(client.getPhone()) != null){
            logger.warn("SUBSCRIBE_REJECT reason=already_exists phone={}", client.getPhone());
            return Response.status(Response.Status.CONFLICT)
                    .entity("Client already exists")
                    .build();
        }

        repository.addClient(client);

        logger.info("SUBSCRIBE_OK phone={}", client.getPhone());

        //quan client creat, retornem 201 (Created)
        return Response.status(Response.Status.CREATED)
                .entity(client)
                .build();
    }
}
