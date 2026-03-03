package upf.at.ban.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import upf.at.ban.model.NotifierResponse;
import upf.at.ban.service.NotifierService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Notifier service (BAN API):
 * - notify slots
 * - notify air quality (després)
 *
 * Aquest Resource només exposa endpoints HTTP i delega la lògica al servei.
 */
@Path("/notifier")
public class NotifierResource {

    // logs
    private static final Logger log = LogManager.getLogger(NotifierResource.class);

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
        // Log d'entrada (útil per traçar demo)
        log.info("notifySlots called for phone={}", phone);

        try {
            NotifierResponse result = notifierService.notifySlots(phone);

            // Log del resultat
            if ("ERROR".equals(result.getStatus())) {
                // BAD_REQUEST perquè el problema és de dades/entrada (client no existeix, etc.)
                log.warn("notifySlots ERROR for phone={} message={}", phone, result.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
            }

            log.info("notifySlots OK for phone={} message={}", phone, result.getMessage());
            return Response.ok(result).build();

        } catch (Exception e) {
            // Catch de seguretat per si alguna cosa peta inesperadament
            log.error("notifySlots EXCEPTION for phone={}", phone, e);

            NotifierResponse err = new NotifierResponse();
            err.setStatus("ERROR");
            err.setMessage("Internal error sending slots notification");

            return Response.serverError().entity(err).build();
        }
    }

    /**
     * GET /notifier/air/{phone}/{ip}
     * Envia un missatge de Telegram amb la qualitat de l'aire
     */
    @GET
    @Path("/air/{phone}/{ip}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response notifyAirQuality(@PathParam("phone") String phone, @PathParam("ip") String ip){
        // Log d'entrada (útil per traçar demo)
        log.info("notifySlots called for phone={}", phone);

        try {
            NotifierResponse result = notifierService.notifySlots(phone);

            // Log del resultat
            if ("ERROR".equals(result.getStatus())) {
                // BAD_REQUEST perquè el problema és de dades/entrada (client no existeix, etc.)
                log.warn("notifySlots ERROR for phone={} message={}", phone, result.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
            }

            log.info("notifySlots OK for phone={} message={}", phone, result.getMessage());
            return Response.ok(result).build();

        } catch (Exception e) {
            // Catch de seguretat per si alguna cosa peta inesperadament
            log.error("notifySlots EXCEPTION for phone={}", phone, e);

            NotifierResponse err = new NotifierResponse();
            err.setStatus("ERROR");
            err.setMessage("Internal error sending slots notification");

            return Response.serverError().entity(err).build();
        }
    }

} 