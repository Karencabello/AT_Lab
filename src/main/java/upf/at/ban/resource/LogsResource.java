package upf.at.ban.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/logs")
public class LogsResource {

    private static final java.nio.file.Path LOG_FILE =
            Paths.get("/tmp/log4j-application.log");

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLogs(@QueryParam("lines") @DefaultValue("200") int lines) {
        int n = Math.max(1, Math.min(lines, 5000));

        if (!Files.exists(LOG_FILE)) {
            return Response.ok("(no logs yet)\n").build();
        }

        try {
            List<String> all = Files.readAllLines(LOG_FILE, StandardCharsets.UTF_8);
            int from = Math.max(0, all.size() - n);
            String body = String.join("\n", all.subList(from, all.size()));
            if (!body.endsWith("\n")) body += "\n";
            return Response.ok(body).build();
        } catch (IOException e) {
            return Response.serverError()
                    .entity("Error reading log file: " + e.getMessage() + "\n")
                    .build();
        }
    }
}
