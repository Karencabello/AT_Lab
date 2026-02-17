package upf.at.ban.controller;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import upf.at.ban.model.Station;
import upf.at.ban.service.CacheService;

// No crida directament a bicing, sempre passa per cache 

@Path("/stations")
public class StationResource {
    private static final CacheService cacheService = new CacheService();

    @GET
    @Produces(MediaType.APPLICATION_JSON)

    public List<Station> geStations(){
        return cacheService.getStationsCached();
    }
}
