package dk.magenta.eark.cmis.bridge.authentication;

import dk.magenta.eark.cmis.bridge.Constants;
import dk.magenta.eark.cmis.bridge.db.DatabaseWorker;
import dk.magenta.eark.cmis.bridge.db.SearchQueryObj;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeUserAdminException;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author DarkStar1.
 */

@Path("authentication")
public class AuthenticationResource {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationResource.class);

    public AuthenticationResource() {
    }

    @Inject
    AuthenticationService authenticationService;

    @Inject
    DatabaseWorker databaseWorker;

    //TODO Implement a more secure authentication mechanism. For now this is only for proof of concept
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("login")
    public JsonObject connect(JsonObject json) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (json.containsKey(Constants.USER_NAME) && json.containsKey(Constants.PASSWORD)) {
            String userName = json.getString(Constants.USER_NAME);
            String password = json.getString(Constants.PASSWORD);

            try {
                String authToken = authenticationService.authenticateUser(userName, password);
                builder.add("userName", userName);
                builder.add("sessionTicket", authToken);

            } catch (Exception e) {
                this.errorResponse(Response.Status.UNAUTHORIZED, "Username or password is incorrect");
            }

            builder.add(Constants.SUCCESS, true);

        } else {
            this.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "The connection profile does not have a name!");
        }
        return builder.build();
    }

    /**
     * We actually don't need to logout in the backend since, for the moment, Another request to login should invalidate
     * the previous session ticket, thereby acheiving the effect of logging the user out. However this method has been
     * implemented for future flexibility purposes, in case we might need to tidy up some resources at some future
     * juncture.
     * @param json
     * @return
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("logout")
    public JsonObject disconnect(JsonObject json) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        try {
            if (json.containsKey(Constants.USER_NAME)) {
                String userName = json.getString(Constants.USER_NAME);
                if (json.containsKey(Constants.SESSION_TICKET))
                    authenticationService.destroyUserSession(userName, json.getString(Constants.SESSION_TICKET));
                builder.add(Constants.SUCCESS, true);
            }
        }
        catch(Exception ge){
            builder.add(Constants.SUCCESS, false);
            builder.add(Constants.ERRORMSG, "There was an error in attempting to logout: " + ge.getMessage());
        }
        return builder.build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("person")
    public JsonObject createPerson(JsonObject json) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        try {
            String userName = json.getString(Constants.USER_NAME);
            if (databaseWorker.userExists(userName))
                throw new CmisBridgeUserAdminException("A user with " + userName + " already exists");

            if (databaseWorker.createPerson(json)) {
                builder.add(Constants.SUCCESS, true);
                builder.add(Constants.MESSAGE, "User created");
            } else {
                this.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Unable to create user. Please check " +
                        "server error logs for further details");
            }
        } catch (Exception e) {
            this.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return builder.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("person/{userName}")
    public JsonObject getPerson(@PathParam("userName") String userName) {
        try {
            //Get a session worker
            return this.databaseWorker.getPersonJson(userName);
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
    public JsonObject updatePerson(@PathParam("userName") String userName, JsonObject json) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        try {
            databaseWorker.updatePerson(userName, json);
            builder.add(Constants.SUCCESS, true);
            builder.add(Constants.MESSAGE, userName + " was successfully updated.");
        } catch (Exception e) {
            builder.add(Constants.SUCCESS, false);
            builder.add(Constants.ERRORMSG, e.getMessage());
        }
        return builder.build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("person/{userName}")
    public JsonObject deletePerson(@PathParam("userName") String userName) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        try {
            if (userName.equalsIgnoreCase("admin"))
                throw new CmisBridgeUserAdminException("Admin account can not be deleted. Thou shalt not pass!!!!");
            databaseWorker.deletePerson(userName);
            builder.add(Constants.SUCCESS, true);
            builder.add(Constants.MESSAGE, userName + " was successfully removed from the system.");
        } catch (Exception e) {
            builder.add(Constants.SUCCESS, false);
            builder.add(Constants.ERRORMSG, e.getMessage());
        }
        return builder.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("userName/{userName}/check")
    public JsonObject checkUserNameExists(@PathParam("userName") String userName) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        try {
            //Get a session worker
            if (databaseWorker.userExists(userName)) {
                builder.add(Constants.SUCCESS, true);
                builder.add(Constants.MESSAGE, userName + " already exists.");
            } else {
                builder.add(Constants.SUCCESS, false);
                builder.add(Constants.MESSAGE, userName + " does not exist");
            }
        } catch (Exception e) {
            builder.add(Constants.SUCCESS, false);
            builder.add(Constants.ERRORMSG, e.getMessage());
        }
        return builder.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("people")
    public JsonObject getPeople(@DefaultValue("userName") @QueryParam(value = "sortBy") final String sortBy,
                                @DefaultValue("asc") @QueryParam(value = "dir") final String direction,
                                @DefaultValue("all") @QueryParam(value = "filter") final String searchTerm,
                                @DefaultValue("100") @QueryParam(value = "maxResults") final String maxResults) {
        try {
            SearchQueryObj searchQueryObj = new SearchQueryObj(searchTerm,sortBy,direction, NumberUtils.toInt(maxResults));
            return databaseWorker.getPeople();
        } catch (Exception e) {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add(Constants.SUCCESS, false);
            builder.add(Constants.ERRORMSG, e.getMessage());
            return builder.build();
        }
    }

    /**
     * Throws an error response for us with server messages
     * @param statusType
     * @param message
     */
    private void errorResponse(Response.StatusType statusType, String message){
        Response.ResponseBuilder responseBuilder;
        responseBuilder = Response.status(statusType);
        responseBuilder.entity(message);
        Response response = responseBuilder.build();
        throw new WebApplicationException(response);
    }
}
