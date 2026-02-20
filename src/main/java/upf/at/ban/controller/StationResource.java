package upf.at.ban.controller;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;


import upf.at.ban.model.Client;
import upf.at.ban.model.Station;
import upf.at.ban.service.CacheService;
import upf.at.ban.repository.ClientRepository;

// No crida directament a bicing, sempre passa per cache 

@Path("/stations")
public class StationResource {
    private static final CacheService cacheService = new CacheService();
    private static final ClientRepository clientRepository = ClientRepository.getInstance();

    // Endpoint per obtenir totes les estacions
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllStations(){
        List<Station> stations = cacheService.getStationsCached();
        return Response.ok(stations).build();
    }
    
    // Endpoint per obtenir estacions seleccionades per un client
    @GET
    @Path("/{phone}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStationsByClient(@PathParam("phone") String phone){
        
        // Busquem client per phone
        Client client = clientRepository.getClientByPhone(phone);
        if (client == null){
            return Response.status(Response.Status.NOT_FOUND).entity("Client not found").build();
        }

        // Obtenim estacions del cache
        List<Station> allStations = cacheService.getStationsCached();

        // Obtenim IDs d'estacions del client
        List<Integer> stationIDs = client.getStationsIDs();

        // Filtrar estacions que el client ha seleccionat
        List<Station> filtered = allStations.stream()
            .filter(station -> stationIDs.contains(station.getStation_id()))
            .collect(Collectors.toList());

        return Response.ok(filtered).build();
    }
}
