package dk.magenta.eark.cmis.bridge.authentication;

import dk.magenta.eark.cmis.bridge.Constants;
import dk.magenta.eark.cmis.bridge.db.DatabaseWorker;
import dk.magenta.eark.cmis.bridge.db.DatabaseWorkerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author lanre.
 */

@Path("authentication")
public class AuthenticationResource {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationResource.class);
    public AuthenticationResource() {}

    //TODO Implement a more secure authentication mechanism. For now this is only for proof of concept
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("login")
    public JsonObject connect(JsonObject json) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (json.containsKey(Person.USERNAME) && json.containsKey(Person.PASSWORD) ) {
            String userName = json.getString(Person.USERNAME);
            String password = json.getString(Person.PASSWORD);

            try {
                //Get a session worker
                DatabaseWorker databaseWorker = new DatabaseWorkerImpl();
                return databaseWorker.authenticatePerson(userName, password);

            } catch (Exception e) {
                builder.add(Constants.SUCCESS, false);
                builder.add(Constants.ERRORMSG, e.getMessage());
            }

            builder.add(Constants.SUCCESS, true);

        } else {
            builder.add(Constants.SUCCESS, false);
            builder.add(Constants.ERRORMSG, "The connection profile does not have a name!");
        }
        return builder.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("person/{userName}")
    public JsonObject getPerson(@PathParam("userName") String userName ){
            try {
                //Get a session worker
                DatabaseWorker databaseWorker = new DatabaseWorkerImpl();
                return databaseWorker.getPersonJson(userName);
            } catch (Exception e) {
                JsonObjectBuilder builder = Json.createObjectBuilder();
                builder.add(Constants.SUCCESS, false);
                builder.add(Constants.ERRORMSG, e.getMessage());
                return builder.build();
            }
    }


}
