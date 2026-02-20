package upf.at.ban.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

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
    public Response notifySlots(@PathParam("phone") String phone) {
        return notifierService.notifySlots(phone);
    }
}