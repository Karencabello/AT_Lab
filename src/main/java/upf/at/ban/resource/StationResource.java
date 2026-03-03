package upf.at.ban.resource;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


import upf.at.ban.model.Station;
import upf.at.ban.service.CacheService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// No crida directament a bicing, sempre passa per cache 

@Path("/stations")
public class StationResource {
    private static final CacheService cacheService = new CacheService();

    // Logs
    private static final Logger log = LogManager.getLogger(StationResource.class);

    // Endpoint per obtenir totes les estacions
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Station> getAllStations() {
        log.debug("GET /stations called");
        List<Station> stations = cacheService.getStationsCached();
        log.debug("Returning {} stations", stations != null ? stations.size() : 0);
        return stations;
    }    
}
