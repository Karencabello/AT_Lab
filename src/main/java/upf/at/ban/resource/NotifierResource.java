package upf.at.ban.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import upf.at.ban.model.NotifierResponse;
import upf.at.ban.service.NotifierService;

/**
 * Notifier service (BAN API):
 * - notify slots
 * - notify air quality (després)
 *
 * Aquest Resource només exposa endpoints HTTP i delega la lògica al servei.
 */
@Path("/notifier")
public class NotifierResource {

    // Servei amb la lògica de negoci
    private static final NotifierService notifierService = new NotifierService();

    /**
     * GET /notifier/slots/{phone}
     * Envia un missatge de Telegram amb les free slots de les estacions subscrites del client.
     */
    @GET
    @Path("/slots/{phone}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response notifySlots(@PathParam("phone") String phone) {
        NotifierResponse result = notifierService.notifySlots(phone);

        if ("ERROR".equals(result.getStatus())) {
        return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
        }

        return Response.ok(result).build();
    }

    /**
     * GET /notifier/air/{phone}/{ip}
     * Envia un missatge de Telegram amb la qualitat de l'aire
     */
    @GET
    @Path("/air/{phone}/{ip}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response notifyAirQuality(@PathParam("phone") String phone, @PathParam("ip") String ip){
        NotifierResponse result = notifierService.notifyAirQuality(phone, ip);

        if ("ERROR".equals(result.getStatus())) {
            return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
        }

        return Response.ok(result).build();
    }

} 