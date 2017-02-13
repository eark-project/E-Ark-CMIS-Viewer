package dk.magenta.eark.cmis.repository;

import dk.magenta.eark.cmis.Repository;
import dk.magenta.eark.cmis.bridge.Constants;
import dk.magenta.eark.cmis.bridge.db.DatabaseWorker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author DarkStar1.
 */

@Path("repository")
public class RepositoryResource {
    private final Logger logger = LoggerFactory.getLogger(RepositoryResource.class);

    public static final String FOLDER_OBJECT_ID = "folderObjectId";
    public static final String DOCUMENT_OBJECT_ID = "documentObjectId";
    @Inject
    CmisSessionWorker cmisSessionWorker;

    @Inject
    DatabaseWorker databaseWorker;

    public RepositoryResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("connect")
    public JsonObject connect() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        JsonObject response;

        try {

            //Build the json for the repository info
            response = cmisSessionWorker.getRepositoryInfo();
            builder.add("repositoryInfo", response);
            builder.add("rootFolder", cmisSessionWorker.getRootFolder());

        } catch (Exception e) {
            builder.add(Constants.SUCCESS, false);
            builder.add(Constants.ERRORMSG, e.getMessage());
        }

        builder.add(Constants.SUCCESS, true);
        return builder.build();
    }

    @GET
    @Path("document/{docId}")
    @Produces("*/*")
    public Response DownloadDocument(@PathParam("docId") String documentId) {
        try {
            documentId = URLDecoder.decode(documentId, "UTF-8");
            java.nio.file.Path source = Paths.get(cmisSessionWorker.getBufferedDocumentPath(documentId));
            File theFile = source.toFile();
            Response.ResponseBuilder rb = Response.ok(theFile);

            rb.header("Content-Disposition","inline;filename="+theFile.getName())
              .header("Content-Type", new MimetypesFileTypeMap().getContentType(theFile));
            return rb.build();
        }
        catch(Exception ge){
            return Response.serverError().build();
        }
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
                //Build the json for the repository info
                builder.add("document", cmisSessionWorker.getDocument(documentObjectId, includeContentStream));

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
     *
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
                JsonObject rootFolder = cmisSessionWorker.getRootFolder();
                String repoRoot = rootFolder.getJsonObject("properties").getString("objectId");

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

    //CMIS repository RU
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("details")
    public JsonObject getRepoDetails() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        try {
            //Build the json for the repository details
            builder.add("repository", databaseWorker.getRepositoryDetails());
            builder.add(Constants.SUCCESS, true);

        } catch (Exception e) {
            builder.add(Constants.SUCCESS, false);
            builder.add(Constants.ERRORMSG, e.getMessage());
        }

        return builder.build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("update")
    public JsonObject updateRepoDetails(JsonObject json) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (!json.isEmpty()) {
            Map<String, String> repoProps = new HashMap<>();
            if (json.containsKey(Repository.URL))
                repoProps.put(Repository.URL, json.getString(Repository.URL));
            if (json.containsKey(Repository.USERNAME))
                repoProps.put((Repository.USERNAME), json.getString(Repository.USERNAME));
            if (json.containsKey(Repository.PASSWORD))
                repoProps.put((Repository.PASSWORD), json.getString(Repository.PASSWORD));

            try {
                //Build the json for the repository details
                builder.add("repository", databaseWorker.updateRepoDetails(repoProps));
                builder.add(Constants.MESSAGE, "Repository details successfully updated");
                builder.add(Constants.SUCCESS, true);
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
}
