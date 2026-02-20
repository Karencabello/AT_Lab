package upf.at.ban.service;

import upf.at.ban.model.Message;
import upf.at.ban.util.HttpClientUtil;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class TelegramService {

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";

    public void sendMessage(String token, Message message) {
        String url = TELEGRAM_API_URL + token + "/sendMessage";
        HttpClientUtil.sendPost(url, message);
    }
}
