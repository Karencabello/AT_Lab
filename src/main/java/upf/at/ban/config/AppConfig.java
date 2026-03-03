package upf.at.ban.config;

import org.glassfish.jersey.server.ResourceConfig;

// Defineix com es monta l'API
// Indica a jersey on a de buscar les classes @Path
public class AppConfig extends ResourceConfig {
    public AppConfig() {
        // Jersey escaneja tot el que hi ha aqui per trobar paths
        packages("upf.at.ban");
    }
}


