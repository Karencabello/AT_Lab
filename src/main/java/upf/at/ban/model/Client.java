package upf.at.ban.model;

import java.util.List;

public class Client {
    private String phone;
    private String telegramToken;
    private Long chat_id;
    private List<Integer> stationsIDs;

    public Client() { }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTelegramToken() {
        return telegramToken;
    }

    public void setTelegramToken(String telegramToken) {
        this.telegramToken = telegramToken;
    }

    public Long getChat_id() {
        return chat_id;
    }

    public void setChat_id(Long chat_id) {
        this.chat_id = chat_id;
    }

    public List<Integer> getStationsIDs() {
        return stationsIDs;
    }

    public void setStationsIDs(List<Integer> stationsIDs) {
        this.stationsIDs = stationsIDs;
    }

    
}