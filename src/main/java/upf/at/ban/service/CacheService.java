package upf.at.ban.service;

import java.util.List;

import upf.at.ban.model.Data;
import upf.at.ban.model.Station;

// Si cache buida o han passat 120s --> crida a BicingService i actualitza
// Si no --> retorna cache sense tocar Bicing
public class CacheService {

    // Temps vida cache --> 120s en ms
    private static final long ttl = 120_000;

    // Crida a l'API de Bicing (a BicingService)
    private final BicingService bicingService;

    // Llista d'estacions guardad
    private List<Station> cachedStations = null;

    // Moment (timestamp) que es va actualitzar cache ultima vegada
    private long cache_ts = 0;

    // Inicialitzem servei 
    public CacheService(){
        this.bicingService = new BicingService();
    }

    // Métode principal
    public synchronized List<Station> getStationsCached(){

        // Obtenim temps actual
        long now = System.currentTimeMillis();

        // Comprovem si està buida cache 
        boolean cacheEmpty = (cachedStations == null);

        // Comprovem si han passat més de 120s
        boolean cacheExpired = (now - cache_ts) > ttl;

        // Si la cache està buida o ha caducat
        if (cacheEmpty || cacheExpired){
            // 1. Demanem a Bicing
            Data data = bicingService.getStations();

            // 2. Guardem a cache les estacions
            cachedStations = data.getData().getStations();
            
            // 3. guardem el moment actual com a timestamp
            cache_ts = now;
        }

        // Retornem llista
        return cachedStations;
    }
}
