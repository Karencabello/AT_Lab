package upf.at.ban.service;

import upf.at.ban.model.Message;
import upf.at.ban.util.Constants;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TelegramService {

    private static final Logger logger = LogManager.getLogger(TelegramService.class);
    
    // Enviar missatge a Telegram
    // RECORDATOR: el bot no rep missatges com un endpoint, sinó que el client ha de fer una petició POST a Telegram 
    // amb el missatge i el token del bot. Telegram s'encarrega d'entregar el missatge al bot.
    public String sendMessage(String token, Message message){ 
        
        // 1. Creem client
        Client client = ClientBuilder.newClient();

        // 2. Creem URL
        WebTarget targetSendMessage = client.target(Constants.TELEGRAM_API_URL + token).path("sendMessage");

        // 3. Fem petició POST
        long t0 = System.currentTimeMillis(); // veure quant triguem als logs
        String response = targetSendMessage.request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(message, MediaType.APPLICATION_JSON_TYPE), String.class);
        long tookMs = System.currentTimeMillis() - t0;

        // log
        logger.info("TELEGRAM_SEND chatId={} tookMs={} ok={}",
                message == null ? null : message.getChat_id(),
                tookMs,
                (response != null && !response.contains("error")));

        // 4. Tanquem client
        client.close();
        
        return response;
    }
}

