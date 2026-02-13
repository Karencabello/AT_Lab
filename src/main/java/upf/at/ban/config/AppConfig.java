package upf.at.ban.config;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api")
public class AppConfig extends ResourceConfig {
    public AppConfig() {
        packages("upf.at.ban"); // IMPORTANT: que coincideixi amb els packages reals
    }
}

