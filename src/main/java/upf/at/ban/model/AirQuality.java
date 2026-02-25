package upf.at.ban.model;

public class AirQuality {
    
    private String city;
    private int aqi;
    private String level;
    
    public AirQuality() {}

    public AirQuality(String city, int aqi, String level) {
        this.city = city;
        this.aqi = aqi;
        this.level = level;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getAqi() {
        return aqi;
    }

    public void setAqi(int aqi) {
        this.aqi = aqi;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    
}
