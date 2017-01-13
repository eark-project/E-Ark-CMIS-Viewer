package dk.magenta.eark.cmis.repository;

import dk.magenta.eark.cmis.bridge.Constants;
import dk.magenta.eark.cmis.bridge.Utils;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeDbException;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeDirectoryException;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeIOException;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.ExtensionLevel;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.spi.*;
import org.apache.commons.lang3.StringUtils;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lanre.
 */
@Service
public class CmisSessionWorkerImpl implements CmisSessionWorker {
    private final Logger logger = LoggerFactory.getLogger(CmisSessionWorkerImpl.class);
    private Session session;
    private ObjectFactory objectFactory;
    private OperationContext operationContext;

    public CmisSessionWorkerImpl() {

        try {
            Cmis1Connector cmis1Connector = new Cmis1Connector();
            //Get a CMIS session object
            this.session = cmis1Connector.getAtomPubSession("admin");
            this.operationContext = this.session.createOperationContext();
            this.session.setDefaultContext(this.operationContext);
            this.objectFactory = session.getObjectFactory();
        } catch (Exception sqe) {
            sqe.printStackTrace();
            throw new CmisBridgeDbException("Unable to establish a session with the repository due to an issue with the " +
                    "db: " + sqe.getMessage());
        }
    }

    //<editor-fold desc="Webservices endpoints">

    /**
     * Returns the Navigation service
     *
     * @return
     */
    @Override
    public NavigationService getNavigationService() {
        return this.session.getBinding().getNavigationService();
    }

    /**
     * Returns the Repository service
     *
     * @return
     */
    @Override
    public RepositoryService getRepositoryService() {
        return this.session.getBinding().getRepositoryService();
    }

    /**
     * Returns the Versioning service
     *
     * @return
     */
    @Override
    public VersioningService getVersioningService() {
        return this.session.getBinding().getVersioningService();
    }

    /**
     * Returns the AclService service
     *
     * @return
     */
    @Override
    public AclService getACLService() {
        return this.session.getBinding().getAclService();
    }

    /**
     * Returns the Relationship service
     *
     * @return
     */
    @Override
    public RelationshipService getRelationshipService() {
        return this.session.getBinding().getRelationshipService();
    }

    /**
     * Returns the Policy service
     *
     * @return
     */
    @Override
    public PolicyService getPolicyService() {
        return this.session.getBinding().getPolicyService();
    }

    /**
     * Returns the Object service
     *
     * @return
     */
    @Override
    public ObjectService getObjectService() {
        return this.session.getBinding().getObjectService();
    }

    /**
     * Returns the Discovery service
     *
     * @return
     */
    @Override
    public DiscoveryService getDiscoveryService() {
        return this.session.getBinding().getDiscoveryService();
    }

    /**
     * Returns the MultiFiling service
     *
     * @return
     */
    @Override
    public MultiFilingService getMultiFilingService() {
        return this.session.getBinding().getMultiFilingService();
    }
    //</editor-fold>

    /**
     * Returns the properties and, optionally, the content stream of a document
     *
     * @param documentObjectId     the document objectId
     * @param includeContentStream boolean value which specifies whether to return the content stream of the document
     * @return
     */
    @Override
    public JsonObject getDocument(String documentObjectId, boolean includeContentStream) {
        JsonObjectBuilder documentBuilder = Json.createObjectBuilder();
        try {
            Document document = (Document) this.session.getObject(documentObjectId);
            JsonObject tmp = this.extractUsefulProperties(document);
            documentBuilder.add("properties", tmp);
            if (includeContentStream) {
                documentBuilder.add("contentStream", IOUtils.readAllLines(document.getContentStream().getStream()));
            }
        } catch (Exception ge) {
            logger.error("********** Stacktrace **********\n");
            ge.printStackTrace();
            throw new CmisBridgeIOException("\nUnable to read document:\n" + ge.getMessage());
        }
        return documentBuilder.build();
    }

    /**
     * provides a URL from which the requested document can be downloaded.
     *
     * @param documentObjectId the id of the document to retrieve
     * @return
     */
    @Override
    public String getBufferedDocumentPath(String documentObjectId) {
        try {
             /*Create a temp file we might need to use this for pre-processing before storing. e.g. validation */
            Document document = (Document) this.session.getObject(documentObjectId);
            String docName = StringUtils.substringBeforeLast(document.getName(), ".");
            //Make sure that the file name is URL encoded to get around special characters
            //See https://github.com/magenta-aps/E-Ark-CMIS-Viewer/issues/17
            docName = URLEncoder.encode(docName, "UTF-8");

            String docPostfix = StringUtils.substringAfterLast(document.getName(), ".");
            File tempFile = File.createTempFile(docName, "." + docPostfix);
            InputStream documentInputStream = document.getContentStream().getStream();

            int rd = (int) Files.copy(documentInputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            org.apache.commons.io.IOUtils.closeQuietly(documentInputStream);
            return tempFile.getAbsolutePath();
        } catch (Exception ge) {
            logger.error("********** Document Read Error: **********\n");
            ge.printStackTrace();
            throw new CmisBridgeIOException("Unable to read document:\n" + ge.getMessage());
        }
    }

    /**
     * Returns a list of CmisObject representing the children of a folder given it's cmis object id
     *
     * @return
     */
    @Override
    public List<CmisObject> getFolderChildren(String folderObjectId) {
        List<ObjectInFolderData> children = getNavigationService().getChildren(this.session.getRepositoryInfo().getId(), folderObjectId,
                null, null, false, IncludeRelationships.BOTH, null, false, null, null, null).getObjects();
        return children.stream().map(t -> objectFactory.convertObject(t.getObject(), this.operationContext)).collect(Collectors.toList());
    }

    /**
     * Returns the parent folder of a folder object
     *
     * @param folderObjectId the id of the folder
     * @return
     */
    @Override
    public CmisObject getFolderParent(String folderObjectId) {
        ObjectData parent_ = getNavigationService().getFolderParent(this.session.getRepositoryInfo().getId(), folderObjectId, null, null);
        return this.objectFactory.convertObject(parent_, this.operationContext);
    }

    /**
     * Returns a filtered view of the folder by restricting the returned types to the list of types defined in the mapping file.
     *
     * @param folderObjectId The object id of the folder we're interested in
     * @return Json object that represents the folder
     */
    @Override
    public JsonObject getFolder(String folderObjectId) {
        JsonObjectBuilder folderBuilder = Json.createObjectBuilder();
        try {
            Folder folder = (Folder) this.session.getObject(folderObjectId);
            List<CmisObject> children = this.getFolderChildren(folder.getId());
            List<JsonObject> jsonRep = children.stream().map(this::extractUsefulProperties).collect(Collectors.toList());
            JsonArrayBuilder cb = Json.createArrayBuilder();
            JsonObject tmp = this.extractUsefulProperties(folder);
            jsonRep.forEach(cb::add);
            folderBuilder.add("properties", tmp);
            folderBuilder.add("children", cb.build());

        } catch (Exception ge) {
            logger.error("********** Error getting folder **********");
            ge.printStackTrace();
            throw new CmisBridgeDirectoryException("Unable to read folder items for folder:\n" + ge.getMessage());
        }

        return folderBuilder.build();
    }

    /**
     * returns the root folder
     *
     * @return
     */
    @Override
    public JsonObject getRootFolder() {
        JsonObjectBuilder rootFolder = Json.createObjectBuilder();

        try {
            String rootFolderId = this.session.getRepositoryInfo().getRootFolderId();
            Folder root = (Folder) this.session.getObject(rootFolderId);
            List<CmisObject> children = this.getFolderChildren(root.getId());
            List<JsonObject> jsonRep = children.stream().map(this::extractUsefulProperties).collect(Collectors.toList());
            JsonArrayBuilder cb = Json.createArrayBuilder();
            JsonObject tmp = this.extractUsefulProperties(root);
            jsonRep.forEach(cb::add);
            rootFolder.add("properties", tmp);
            rootFolder.add("children", cb.build());

        } catch (Exception ge) {
            logger.error("******** Error ********\n");
            ge.printStackTrace();
            logger.error("\n******** End ********\n");
            throw new CmisBridgeDirectoryException("Unable to read folder items for root folder:\n" + ge.getMessage());
        }
        return rootFolder.build();
    }

    /**
     * Self explanatory
     *
     * @return
     */
    @Override
    public JsonObject getRepositoryInfo() {
        RepositoryInfo repositoryInfo = this.session.getRepositoryInfo();
        InputStream tmp = new ByteArrayInputStream(JSONConverter.convert(repositoryInfo, null, null, true)
                .toString().getBytes(StandardCharsets.UTF_8));
        JsonReader rdr = Json.createReader(tmp);
        return rdr.readObject();
    }

    @Override
    public Session getSession() {
        return session;
    }

    /**
     * Just to filter out the useful properties for UI consumption.
     * A CMIS capable repository can return too much metadata about an object, until we fix the mapping feature, we use
     * this to filter out useful generic properties for the UI
     *
     * @param cmisObject
     * @return
     */
    private JsonObject extractUsefulProperties(CmisObject cmisObject) {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

        switch (cmisObject.getBaseTypeId().value()) {
            case "cmis:document":
                Document doc = (Document) cmisObject;
                jsonBuilder.add(Constants.BASETYPE_ID, "document");
                jsonBuilder.add(Constants.LAST_MOD_DATE, Utils.convertToISO8601Date(cmisObject.getLastModificationDate()));
                jsonBuilder.add(Constants.CONTENT_SIZE, doc.getContentStreamLength());
                jsonBuilder.add(Constants.CONTENT_STREAM_LENGTH, doc.getContentStreamLength());
                jsonBuilder.add(Constants.CONTENT_STREAM_MIMETYPE, doc.getContentStreamMimeType());
                jsonBuilder.add(Constants.CONTENT_STREAM_FILENAME, doc.getContentStreamFileName());
                jsonBuilder.add(Constants.PATH, doc.getPaths().get(0));
                break;
            case "cmis:folder":
                Folder folder = (Folder) cmisObject;
                jsonBuilder.add(Constants.BASETYPE_ID, "folder");
                jsonBuilder.add(Constants.PATH, folder.getPath());
                break;
            default:
                Utils.getPropertyPostFixValue(cmisObject.getBaseTypeId().value());
                break;
        }
        jsonBuilder.add(Constants.OBJECT_ID, cmisObject.getId());
        jsonBuilder.add(Constants.OBJECT_TYPE_ID, cmisObject.getType().getId());
        jsonBuilder.add(Constants.NAME, cmisObject.getName());
        jsonBuilder.add(Constants.CREATION_DATE, Utils.convertToISO8601Date(cmisObject.getCreationDate()));
        jsonBuilder.add(Constants.CREATED_BY, cmisObject.getCreatedBy());
        jsonBuilder.add(Constants.LAST_MODIFIED, cmisObject.getLastModifiedBy());
        jsonBuilder.add("allProperties", extractProps(cmisObject));
        jsonBuilder.add("extensionProperties", getExtendedProperties(cmisObject));

        return jsonBuilder.build();
    }

    /**
     * Extracts all the properties of the object
     *
     * @param cmisObject
     * @return
     */
    private JsonObject extractProps(CmisObject cmisObject) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        List<Property<?>> objectProps = cmisObject.getProperties();
        for (Property prop : objectProps) {
            jsonObjectBuilder.add(prop.getDisplayName(), prop.getValueAsString() == null ? "" : prop.getValueAsString());
        }
        return jsonObjectBuilder.build();
    }

    private JsonArray getExtendedProperties(CmisObject cmisObject) {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        List<CmisExtensionElement> extensions = Collections.EMPTY_LIST;

        if (cmisObject.getExtensions(ExtensionLevel.OBJECT) != null &&
                !cmisObject.getExtensions(ExtensionLevel.OBJECT).isEmpty())
            // object extensions
            extensions = cmisObject.getExtensions(ExtensionLevel.OBJECT);

        if (cmisObject.getExtensions(ExtensionLevel.PROPERTIES) != null &&
                !cmisObject.getExtensions(ExtensionLevel.PROPERTIES).isEmpty()) {
            // property extensions
            if (extensions.isEmpty())
                extensions = cmisObject.getExtensions(ExtensionLevel.PROPERTIES);
            else
                extensions.addAll(cmisObject.getExtensions(ExtensionLevel.PROPERTIES));
        }

        JsonObject temp;
        if (extensions.size() > 0) {
            temp = new ExtensionNode(extensions.get(0)).toJson();
            jsonArrayBuilder.add(temp);
        }
        return jsonArrayBuilder.build();
    }

    static class ExtensionNode {
        private final CmisExtensionElement extension;

        public ExtensionNode(CmisExtensionElement extension) {
            this.extension = extension;
        }

        @Override
        public String toString() {
            return (extension.getNamespace() == null ? "" : "{" + extension.getNamespace() + "}") + extension.getName()
                    + (!extension.getAttributes().isEmpty() ? " " + extension.getAttributes() : "")
                    + (extension.getChildren().isEmpty() ? ": " + extension.getValue() : "");
        }

        //Return a Json representation on the tree.
        public JsonObject toJson(){
            JsonArrayBuilder children = Json.createArrayBuilder();
            JsonObjectBuilder node = Json.createObjectBuilder();
            node.add("nameSpace", extension.getNamespace() == null ? "" : "{" + extension.getNamespace() + "}");
            node.add("name", extension.getName() == null ? "" : extension.getName());
            if(extension.getChildren().isEmpty())
                node.add("value", extension.getValue());
            else{
                for(CmisExtensionElement child : extension.getChildren()){
                    children.add(new ExtensionNode(child).toJson());
                }
                node.add("children", children.build());
            }
            return node.build();
        }
    }

}
