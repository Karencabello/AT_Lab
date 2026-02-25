package upf.at.ban.service;

import upf.at.ban.model.Message;
import upf.at.ban.util.Constants;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;


public class TelegramService {

    // Enviar missatge a Telegram
    // RECORDATOR: el bot no rep missatges com un endpoint, sinó que el client ha de fer una petició POST a Telegram 
    // amb el missatge i el token del bot. Telegram s'encarrega d'entregar el missatge al bot.
    public String sendMessage(String token, Message message){ 
        
        // 1. Creem client
        Client client = ClientBuilder.newClient();

        // 2. Creem URL
        WebTarget targetSendMessage = client.target(Constants.TELEGRAM_API_URL + token).path("sendMessage");

        // 3. Fem petició POST
        String response = targetSendMessage.request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(message, MediaType.APPLICATION_JSON_TYPE), String.class);

        // 4. Tanquem client
        client.close();
        
        return response;
    }
}

