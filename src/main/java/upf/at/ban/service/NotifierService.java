package upf.at.ban.service;

import java.util.List;
import java.util.stream.Collectors;

import upf.at.ban.model.Client;
import upf.at.ban.model.Message;
import upf.at.ban.model.NotifierResponse;
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

    // Servei per obtenir ciutat a partir de IP
    private static final IpGeoService ipGeoService = new IpGeoService();

    // Servei per obtenir AQI a partir de ciutat
    private static final AqiService aqiService = new AqiService();

    // Envia slots disponibles al client indicat per phone
    public NotifierResponse notifySlots(String phone) {

        // 1. Buscar client pel phone
        Client client = clientRepo.getClientByPhone(phone);
        if (client == null) return new NotifierResponse("ERROR", "Client not found");

        // 2. Mirem que tinguem chat_id
        if (client.getChat_id() == null) return new NotifierResponse("ERROR", "Client has no chat_id");

        // 3. Mirem que el client tingui estacions subscrites
        if(client.getStationsIDs() == null || client.getStationsIDs().isEmpty()){
            return new NotifierResponse("ERROR", "Client has no subscribed stations");
        }

        // 4. Obtenim estacions del cache
        List<Station> allStations = cacheService.getStationsCached();

        if (allStations == null || allStations.isEmpty()) {
            return new NotifierResponse("ERROR", "No station data available");
        }

        // 5. Filtrar estacions que el client ha seleccionat
        List<Station> subscribed = allStations.stream()
            .filter(station -> client.getStationsIDs().contains(station.getStation_id()))
            .collect(Collectors.toList());

        if (subscribed.isEmpty()) {
            return new NotifierResponse("ERROR", "No matching stations found");
        }

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
        return new NotifierResponse("OK", "Notification sent successfully");
    }

    //Mètode per notify air quality (després) --> utilitzar ipgeoservice, aqiservice i telegramservice
    public NotifierResponse notifyAirQuality(String phone, String ip) {

        // 1. Buscar client
        Client client = clientRepo.getClientByPhone(phone);
        if (client == null) {
            return new NotifierResponse("ERROR", "Client not found");
        }

        if (client.getChat_id() == null) {
            return new NotifierResponse("ERROR", "Client has no chat_id");
        }

        // 2. Obtenir ciutat a partir de IP
        String city = ipGeoService.getCityByIp(ip);
        if (city == null || city.isEmpty()) {
            return new NotifierResponse("ERROR", "Could not determine city from IP");
        }

        // 3. Obtenir AQI a partir de ciutat
        Integer aqi = aqiService.getAqiByCity(city);
        if (aqi == null) {
            return new NotifierResponse("ERROR", "Could not retrieve AQI data");
        }

        // 4. Convertir AQI a nivell conceptual
        String level = translateAqiLevel(aqi);

        // 5. Construir missatge
        String text = "Air Quality in " + city + ":\n"
                + "AQI: " + aqi + "\n"
                + "Level: " + level;

        Message message = new Message(client.getChat_id(), text);

        // 6. Enviar Telegram
        telegramService.sendMessage(Constants.TELEGRAM_TOKEN, message);

        // 7. Retornar resposta
        return new NotifierResponse("OK", "Air quality notification sent successfully");
    }

    private String translateAqiLevel(int aqi) {
        if (aqi <= 50) return "Good";
        if (aqi <= 100) return "Moderate";
        if (aqi <= 150) return "Unhealthy for Sensitive Groups";
        if (aqi <= 200) return "Unhealthy";
        if (aqi <= 300) return "Very Unhealthy";
        return "Hazardous";
    }
}
