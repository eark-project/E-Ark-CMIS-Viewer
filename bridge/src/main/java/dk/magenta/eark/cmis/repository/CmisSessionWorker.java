package dk.magenta.eark.cmis.repository;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.spi.*;

import javax.json.JsonObject;
import java.util.List;

/**
 * @author lanre.
 */
public interface CmisSessionWorker {

    //<editor-fold desc="webservices endpoints">

    /**
     * Returns the Navigation service
     *
     * @return
     */
    NavigationService getNavigationService();

    /**
     * Returns the Repository service
     *
     * @return
     */
    RepositoryService getRepositoryService();

    /**
     * Returns the Versioning service
     *
     * @return
     */
    VersioningService getVersioningService();

    /**
     * Returns the AclService service
     *
     * @return
     */
    AclService getACLService();

    /**
     * Returns the Relationship service
     *
     * @return
     */
    RelationshipService getRelationshipService();

    /**
     * Returns the Policy service
     *
     * @return
     */
    PolicyService getPolicyService();

    /**
     * Returns the Object service
     *
     * @return
     */
    ObjectService getObjectService();

    /**
     * Returns the Discovery service
     *
     * @return
     */
    DiscoveryService getDiscoveryService();

    /**
     * Returns the MultiFiling service
     *
     * @return
     */
    MultiFilingService getMultiFilingService();
    //</editor-fold>

    /**
     * Returns the properties and, optionally, the content stream of a document
     *
     * @param documentObjectId     the document objectId
     * @param includeContentStream boolean value which specifies whether to return the content stream of the document
     * @return
     */
    JsonObject getDocument(String documentObjectId, boolean includeContentStream);

    /**
     * Returns the parent folder of a folder object
     *
     * @param folderObjectId
     * @return
     */
    CmisObject getFolderParent(String folderObjectId);

    /**
     * Returns a list of CmisObject representing the children of a folder given it's id
     *
     * @return
     */
    List<CmisObject> getFolderChildren(String folderObjectId);

    /**
     * Returns a filtered view of the folder by restricting the returned types to the list of types defined in the mapping file.
     * @param folderObjectId The object id of the folder we're interested in
     * @return Json object that represents the folder
     */
    JsonObject getFolder(String folderObjectId);

    /**
     * Returns the root folder
     * @return
     */
    JsonObject getRootFolder();

    /**
     * Self explanatory
     *
     * @return
     */
    JsonObject getRepositoryInfo();

    /**
     * Get the CMIS session
     *
     * @return
     */
    Session getSession();

}
