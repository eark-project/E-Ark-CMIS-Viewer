package dk.magenta.eark.cmis.bridge.authentication;

import dk.magenta.eark.cmis.bridge.Constants;
import dk.magenta.eark.cmis.bridge.db.DatabaseWorker;
import dk.magenta.eark.cmis.bridge.db.DatabaseWorkerImpl;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeUserAdminException;
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
        if (json.containsKey(Constants.USER_NAME) && json.containsKey(Constants.PASSWORD) ) {
            String userName = json.getString(Constants.USER_NAME);
            String password = json.getString(Constants.PASSWORD);

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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("person")
    public JsonObject createPerson(JsonObject json){
        JsonObjectBuilder builder = Json.createObjectBuilder();
            try {
                String userName = json.getString(Constants.USER_NAME);
                DatabaseWorker databaseWorker = new DatabaseWorkerImpl();
                if (databaseWorker.userExists(userName))
                    throw new CmisBridgeUserAdminException("A user with "+userName+" already exists");

                if(databaseWorker.createPerson(json)){
                    builder.add(Constants.SUCCESS, true);
                    builder.add(Constants.MESSAGE, "User created");
                }
                else{
                    builder.add(Constants.SUCCESS, false);
                    builder.add(Constants.ERRORMSG, "Unable to create use. Please check server logs for further details");
                }
            } catch (Exception e) {
                builder.add(Constants.SUCCESS, false);
                builder.add(Constants.ERRORMSG, e.getMessage());
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

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("person/{userName}")
    public JsonObject updatePerson(@PathParam("userName") String userName, JsonObject json ){
        JsonObjectBuilder builder = Json.createObjectBuilder();
            try {
                //Get a session worker
                DatabaseWorker databaseWorker = new DatabaseWorkerImpl();
                databaseWorker.updatePerson(userName, json);
                builder.add(Constants.SUCCESS, true);
                builder.add(Constants.MESSAGE, userName+" was successfully updated.");
            } catch (Exception e) {
                builder.add(Constants.SUCCESS, false);
                builder.add(Constants.ERRORMSG, e.getMessage());
            }
        return builder.build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("person/{userName}")
    public JsonObject deletePerson(@PathParam("userName") String userName ){
        JsonObjectBuilder builder = Json.createObjectBuilder();
            try {
                //Get a session worker
                DatabaseWorker databaseWorker = new DatabaseWorkerImpl();
                databaseWorker.deletePerson(userName);
                builder.add(Constants.SUCCESS, true);
                builder.add(Constants.MESSAGE, userName+" was successfully removed from the system.");
            } catch (Exception e) {
                builder.add(Constants.SUCCESS, false);
                builder.add(Constants.ERRORMSG, e.getMessage());
            }
        return builder.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("userName/{userName}/check")
    public JsonObject checkUserNameExists(@PathParam("userName") String userName){
        JsonObjectBuilder builder = Json.createObjectBuilder();
        try {
            //Get a session worker
            DatabaseWorker databaseWorker = new DatabaseWorkerImpl();
            if(databaseWorker.userExists(userName)){
                builder.add(Constants.SUCCESS, true);
                builder.add(Constants.MESSAGE, userName+" already exists.");
            }
            else{
                builder.add(Constants.SUCCESS, false);
                builder.add(Constants.MESSAGE, userName+" does not exist");
            }
        } catch (Exception e) {
            builder.add(Constants.SUCCESS, false);
            builder.add(Constants.ERRORMSG, e.getMessage());
        }
        return builder.build();
    }
}
