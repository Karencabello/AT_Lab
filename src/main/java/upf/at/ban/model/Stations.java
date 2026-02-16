package upf.at.ban.model;

import java.util.List;

public class Stations {
    private List<Station> stations;

    public Stations() { } 

    public Stations(List<Station> stations) {
        this.stations = stations;
    }

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

}
