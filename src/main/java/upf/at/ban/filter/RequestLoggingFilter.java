package upf.at.ban.filter;

import java.net.URI;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Logs every incoming/outgoing HTTP request handled by Jersey.
 * This gives you "global" traces without having to log in each resource.
 */
@Provider
public class RequestLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger logger = LogManager.getLogger(RequestLoggingFilter.class);
    private static final String START_NS = "ban.startNs";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        requestContext.setProperty(START_NS, System.nanoTime());

        URI uri = requestContext.getUriInfo().getRequestUri();
        logger.info("HTTP_IN {} {}", requestContext.getMethod(), uri.getPath());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object start = requestContext.getProperty(START_NS);
        long ms = -1;
        if (start instanceof Long) {
            ms = (System.nanoTime() - (Long) start) / 1_000_000L;
        }

        URI uri = requestContext.getUriInfo().getRequestUri();
        logger.info("HTTP_OUT {} {} -> {} ({} ms)",
                requestContext.getMethod(),
                uri.getPath(),
                responseContext.getStatus(),
                ms);
    }
}
