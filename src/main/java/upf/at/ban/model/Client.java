package upf.at.ban.model;


import java.util.List;

public class Client {
    private String phone;
    private String telgramToken;
    private Long chat_id;
    private List<Integer> StationsIDs;

    public Client() { }

    public Client(String phone, String telgramToken, Long chat_id, List<Integer> stationsIDs) {
        this.phone = phone;
        this.telgramToken = telgramToken;
        this.chat_id = chat_id;
        StationsIDs = stationsIDs;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTelgramToken() {
        return telgramToken;
    }

    public void setTelgramToken(String telgramToken) {
        this.telgramToken = telgramToken;
    }

    public Long getChat_id() {
        return chat_id;
    }

    public void setChat_id(Long chat_id) {
        this.chat_id = chat_id;
    }

    public List<Integer> getStationsIDs() {
        return StationsIDs;
    }

    public void setStationsIDs(List<Integer> stationsIDs) {
        StationsIDs = stationsIDs;
    }

    

}