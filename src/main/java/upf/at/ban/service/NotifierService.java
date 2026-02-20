package upf.at.ban.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import upf.at.ban.model.Client;
import upf.at.ban.model.Message;
import upf.at.ban.model.Station;
import upf.at.ban.repository.ClientRepository;
import upf.at.ban.util.Constants;

/**
 * NotifierService:
 * Aquí està la lògica:
 * - buscar client
 * - obtenir stations del cache
 * - filtrar subscrites
 * - construir text
 * - enviar per Telegram
 */

public class NotifierService {

    // Repositori per buscar clients
    private static final ClientRepository clientRepo = ClientRepository.getInstance();

    //Cache Bicing
    private static final CacheService cacheService = new CacheService();

    // Servei per enviar missatges a Telegram
    private static final TelegramService telegramService = new TelegramService();

    // Envia slots disponibles al client indicat per phone
    public Response notifySlots(String phone) {

        // 1. Buscar client pel phone
        Client client = clientRepo.getClientByPhone(phone);
        if (client == null) return Response.status(Response.Status.NOT_FOUND).entity("Client not found").build();

        // 2. Mirem que tinguem chat_id
        if (client.getChat_id() == null) return Response.status(Response.Status.BAD_REQUEST).entity("Client has no chat_id").build();

        // 3. Mirem que el client tingui estacions subscrites
        if(client.getStationsIDs() == null || client.getStationsIDs().isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST).entity("Client has no subscribed stations").build();
        }

        // 4. Obtenim estacions del cache
        List<Station> allStations = cacheService.getStationsCached();

        // 5. Filtrar estacions que el client ha seleccionat
        List<Station> subscribed = allStations.stream()
            .filter(station -> client.getStationsIDs().contains(station.getStation_id()))
            .collect(Collectors.toList());

        // 6. Construir missatge
        StringBuilder mssg = new StringBuilder("Free slots at your subscribed stations:\n");
        for (Station s : subscribed){
            mssg.append("• Station ")
              .append(s.getStation_id())
              .append(" -> free slots: ")
              .append(s.getNum_docks_available())
              .append(" | bikes: ")
              .append(s.getNum_bikes_available())
              .append("\n");
        }
        
        // 7. Creem missatge
        Message message = new Message(client.getChat_id(), mssg.toString());

        // 8. Enviem missatge a Telegram
        String telegramResponse = telegramService.sendMessage(Constants.TELEGRAM_TOKEN, message);

        // 9. Retornem resposta
        return Response.ok().entity(telegramResponse).build();
    }

    // TODO: Mètode per notify air quality (després) --> utilitzar ipgeoservice, aqiservice i telegramservice
}
