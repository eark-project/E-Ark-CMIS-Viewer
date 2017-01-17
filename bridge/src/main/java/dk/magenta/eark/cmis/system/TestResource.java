package dk.magenta.eark.cmis.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author DarkStar1.
 */

@Path("system")
public class TestResource {
    private final Logger logger = LoggerFactory.getLogger(TestResource.class);

    public TestResource() {
    }

    /**
     * Returns a simple HTML which indicate
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("check")
    public String returnSuccessfulCheck() {

        return "<html><body><h1>CMIS Bridge is up and running!</h1></body></html>";
    }

}
