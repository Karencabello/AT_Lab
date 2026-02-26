package upf.at.ban.service;

import java.util.List;

import upf.at.ban.model.Data;
import upf.at.ban.model.Station;
import upf.at.ban.util.Constants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Si cache buida o han passat 120s --> crida a BicingService i actualitza
// Si no --> retorna cache sense tocar Bicing
public class CacheService {

    // Logger per aquesta classe
    private static final Logger logger = LogManager.getLogger(CacheService.class);

    // Crida a l'API de Bicing (a BicingService)
    private final BicingService bicingService;

    // Llista d'estacions guardada
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
        boolean cacheExpired = (now - cache_ts) > Constants.CACHE_TTL;

        // Si la cache està buida o ha caducat
        if (cacheEmpty || cacheExpired){
            logger.info("CACHE_MISS empty={} expired={} ageMs={} ttlMs={}", cacheEmpty, cacheExpired, (now - cache_ts), Constants.CACHE_TTL);
            
            long t0 = System.currentTimeMillis();

            // 1. Demanem a Bicing
            Data data = bicingService.getStations();

            // 2. Guardem a cache les estacions
            cachedStations = data.getData().getStations();
            
            // 3. guardem el moment actual com a timestamp
            cache_ts = now;
            logger.info("CACHE_REFRESH stations={} tookMs={}", cachedStations.size(), (System.currentTimeMillis() - t0));
        }
        else {
            logger.debug("CACHE_HIT stations={} ageMs={}", cachedStations.size(), (now - cache_ts));
        }

        // Retornem llista
        return cachedStations;
    }
}
