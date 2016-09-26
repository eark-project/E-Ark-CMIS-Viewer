package dk.magenta.eark.cmis.repository;

import dk.magenta.eark.cmis.bridge.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.URLDecoder;

/**
 * @author lanre.
 */

@Path("repository")
public class RepositoryResource {
    private final Logger logger = LoggerFactory.getLogger(RepositoryResource.class);

    public static final String FOLDER_OBJECT_ID = "folderObjectId";
    public static final String DOCUMENT_OBJECT_ID = "documentObjectId";

    public RepositoryResource() {}

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("connect")
    public JsonObject connect() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        JsonObject response;

            try {
                //Get a session worker
                CmisSessionWorker sessionWorker = this.getSessionWorker();

                //Build the json for the repository info
                response = sessionWorker.getRepositoryInfo();
                builder.add("repositoryInfo", response);
                builder.add("rootFolder", sessionWorker.getRootFolder());

            } catch (Exception e) {
                builder.add(Constants.SUCCESS, false);
                builder.add(Constants.ERRORMSG, e.getMessage());
            }

            builder.add(Constants.SUCCESS, true);

         /*else {
            builder.add(Constants.SUCCESS, false);
            builder.add(Constants.ERRORMSG, "The connection profile does not have a name!");
        }*/

        return builder.build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("getDocument")
    public JsonObject Document(JsonObject json) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (json.containsKey(DOCUMENT_OBJECT_ID)) {
            String documentObjectId = json.getString(DOCUMENT_OBJECT_ID);
            boolean includeContentStream = json.getBoolean("includeContentStream", false);

            try {
                //Get a session worker
                CmisSessionWorker sessionWorker = this.getSessionWorker();

                //Build the json for the repository info
                builder.add("document", sessionWorker.getDocument(documentObjectId, includeContentStream));

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

    /**
     * Just returns a folder object
     * @param json
     * @return
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("getFolder")
    public JsonObject getFolder(JsonObject json) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (json.containsKey(FOLDER_OBJECT_ID)) {

            String folderObjectId = json.getString(FOLDER_OBJECT_ID);

            try {
                CmisSessionWorker cmisSessionWorker = this.getSessionWorker();

                //Build the json for the repository info
                builder.add("folder", cmisSessionWorker.getFolder(folderObjectId));

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

    /**
     *
     * @param objectId
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("isroot/{objectId}/in/{profileName}")
    public JsonObject isROOT(@PathParam("objectId") String objectId) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (StringUtils.isNotBlank(objectId)) {
            try {
                objectId = URLDecoder.decode(objectId, "UTF-8");
                CmisSessionWorker cmisSessionWorker = this.getSessionWorker();
                JsonObject rootFolder = cmisSessionWorker.getRootFolder();
                String repoRoot =  rootFolder.getJsonObject("properties").getString("objectId") ;

                //Build the json for the repository info
                builder.add("isRoot", objectId.equalsIgnoreCase(repoRoot));

            } catch (Exception e) {
                builder.add(Constants.SUCCESS, false);
                builder.add(Constants.ERRORMSG, e.getMessage());
            }

            builder.add(Constants.SUCCESS, true);

        } else {
            builder.add(Constants.SUCCESS, false);
            builder.add(Constants.ERRORMSG, "One or more parameters missing or malformed");
        }

        return builder.build();
    }

    /**
     * Returns a cmis session worker instance given a profile name
     * @return
     */
    private CmisSessionWorker getSessionWorker(){
        try {
            return new CmisSessionWorkerImpl();
        } catch (Exception ge) {
            logger.error("Unable to create session worker due to: " + ge.getMessage());
            throw new RuntimeException(ge.getMessage());
        }
    }

}
