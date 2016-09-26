package dk.magenta.eark.cmis.repository;

import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeConnectionException;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeURLException;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Cmis1Connector {

    private final Logger logger = LoggerFactory.getLogger(Cmis1Connector.class);
    private static Map<String, Session> connections = new ConcurrentHashMap<String, Session>();

    dk.magenta.eark.cmis.Repository repo = dk.magenta.eark.cmis.Repository.getInstance();

    public Cmis1Connector() {
    }

    /**
     * Get an Open CMIS session to use when talking to the CMIS repo.
     * Will check if there is already a connection to the CMIS repo
     * and re-use that session.
     * Uses Atom pub
     *
     * @return an Open CMIS Session object
     */
    public Session getAtomPubSession(String userName) {
        Session session = connections.get(userName);
        if (session == null) {
            // No connection to CMIS repo available, create a new one
            SessionFactory sessionFactory = SessionFactoryImpl.newInstance();

            Map<String, String> parameters = new HashMap<>();
            parameters.put(SessionParameter.USER, repo.getUserName());
            parameters.put(SessionParameter.PASSWORD, repo.getPassword());
            parameters.put(SessionParameter.ATOMPUB_URL, repo.getUrl());
            parameters.put(SessionParameter.FORCE_CMIS_VERSION, "1.0");
            parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            parameters.put(SessionParameter.COMPRESSION, "true");
            parameters.put(SessionParameter.CACHE_TTL_OBJECTS, "0"); // Caching is turned off

            try{
                Repository repository = sessionFactory.getRepositories(parameters).get(0);
                session = repository.createSession();
                session.getDefaultContext().setIncludeAllowableActions(false);
                // Save connection for reuse
                connections.put(userName, session);
            }
            catch (CmisConnectionException | CmisRuntimeException ce){
                logger.error("********** Repository connection exception **********");
                ce.printStackTrace();
                throw new CmisBridgeConnectionException("Unable to connect to the cmis repositorydue to; \n"
                        +ce.getMessage());
            }

        } else {
            logger.info("Already connected to cmis repository with the connection id (" + userName + ")");
        }

        return session;
    }

    /**
     * Get an Open CMIS session to use when talking to the CMIS repo.
     * Will check if there is already a connection to the CMIS repo
     * and re-use that session.
     *
     * @return an Open CMIS Session object
     */
    public Session getSession(String userName) {
        Session session = connections.get(userName);
        URL wsdlUrl;
        Map<String, String> serviceEndpoints;
        if (session == null) {
            //Validate that the url contains the wsdl we require or else throw an exception
            try {
                wsdlUrl = new URL(repo.getUrl());
                serviceEndpoints = this.extractWebservicesUrls(wsdlUrl);
                addWSDL(serviceEndpoints);
                if (!isValidWebServicesUrls(wsdlUrl))
                    throw new CmisBridgeURLException("No wsdl document or no service endpoints in " +
                            "document retrieved from: " + repo.getUrl());
            } catch (Exception ge) {
                logger.error("There's a problem with the url format.\nSee http://microformats.org/wiki/url-formats#HTTP " +
                        "or https://www.w3.org/Protocols/HTTP/HTTP2.html to make sure it conforms to the standards.\n");
                throw new CmisBridgeURLException(ge.getMessage());
            }

            // No connection to CMIS repo available, create a new one
            SessionFactory sessionFactory = SessionFactoryImpl.newInstance();

            Map<String, String> parameters = new HashMap<>();
            parameters.put(SessionParameter.USER, repo.getUserName());
            parameters.put(SessionParameter.PASSWORD, repo.getPassword());
            parameters.put(SessionParameter.FORCE_CMIS_VERSION, "1.0");
            parameters.put(SessionParameter.BINDING_TYPE, BindingType.WEBSERVICES.value());
            parameters.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, serviceEndpoints.get("RepositoryService"));
            parameters.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, serviceEndpoints.get("NavigationService"));
            parameters.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, serviceEndpoints.get("ObjectService"));
            parameters.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, serviceEndpoints.get("VersioningService"));
            parameters.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, serviceEndpoints.get("DiscoveryService"));
            parameters.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, serviceEndpoints.get("RelationshipService"));
            parameters.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, serviceEndpoints.get("MultiFilingService"));
            parameters.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, serviceEndpoints.get("PolicyService"));
            parameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE, serviceEndpoints.get("ACLService"));
            parameters.put(SessionParameter.COMPRESSION, "true");
            parameters.put(SessionParameter.CACHE_TTL_OBJECTS, "0"); // Caching is turned off

            // If there is only one repository exposed, these lines will help detect it and its ID
            Repository cmisRepository = null;
            try {
                List<Repository> repositories = sessionFactory.getRepositories(parameters);
                if (repositories != null && repositories.size() > 0) {
                    logger.info("Found (" + repositories.size() + ") repositories");
                    cmisRepository = repositories.get(0);

                } else {
                    throw new CmisConnectionException("Could not connect to the cmis server, no repository found!");
                }

                // Create a new session with the cmis repository
                session = cmisRepository.createSession();
                session.getDefaultContext().setIncludeAllowableActions(false);

                // Save connection for reuse
                connections.put(userName, session);
            } catch (Exception ge) {
                System.out.print("******** Error *********\n");
                ge.printStackTrace();
                throw new CmisBridgeConnectionException("There is an issue connecting to the repository:\n\n"
                        + ge.getMessage());
            }

        } else {
            logger.info("Already connected to cmis repository with the connection id (" + userName + ")");
        }

        return session;
    }

    public void listRepoCapabilities(RepositoryInfo repositoryInfo) {
        RepositoryCapabilities repoCapabilities = repositoryInfo.getCapabilities();

        System.out.println("aclCapability = " + repoCapabilities.getAclCapability().name());
        System.out.println("changesCapability = " + repoCapabilities.getChangesCapability().name());
        System.out.println("contentStreamUpdatable = " + repoCapabilities.getContentStreamUpdatesCapability().name());
        System.out.println("joinCapability = " + repoCapabilities.getJoinCapability().name());
        System.out.println("queryCapability = " + repoCapabilities.getQueryCapability().name());
        System.out.println("renditionCapability = " + repoCapabilities.getRenditionsCapability().name());
        System.out.println("allVersionsSearchable? = " + repoCapabilities.isAllVersionsSearchableSupported());
        System.out.println("getDescendantSupported? = " + repoCapabilities.isGetDescendantsSupported());
        System.out.println("getFolderTreeSupported? = " + repoCapabilities.isGetFolderTreeSupported());
        System.out.println("multiFilingSupported? = " + repoCapabilities.isMultifilingSupported());
        System.out.println("privateWorkingCopySearchable? = " + repoCapabilities.isPwcSearchableSupported());
        System.out.println("pwcUpdateable? = " + repoCapabilities.isPwcUpdatableSupported());
        System.out.println("unfilingSupported? = " + repoCapabilities.isUnfilingSupported());
        System.out.println("versionSpecificFilingSupported? = " + repoCapabilities.isVersionSpecificFilingSupported());
    }

    public void searchMetadataAndFTS(Session session) {
        // Check if the repo supports Metadata search and Full Text Search (FTS)
        RepositoryInfo repoInfo = session.getRepositoryInfo();
        if (repoInfo.getCapabilities().getQueryCapability().equals(CapabilityQuery.METADATAONLY)) {
            logger.warn("Repository does not support FTS [repoName=" + repoInfo.getProductName() +
                    "][repoVersion=" + repoInfo.getProductVersion() + "]");
        } else {
            String query = "SELECT * FROM cmis:document WHERE cmis:name LIKE 'OpenCMIS%'";
            ItemIterable<QueryResult> searchResult = session.query(query, false);
            logSearchResult(query, searchResult);

            query = "SELECT * FROM cmis:document WHERE cmis:name LIKE 'OpenCMIS%' AND CONTAINS('testing')";
            searchResult = session.query(query, false);
            logSearchResult(query, searchResult);
        }
    }

    private void logSearchResult(String query, ItemIterable<QueryResult> searchResult) {
        logger.info("Results from query " + query);
        int i = 1;
        for (QueryResult resultRow : searchResult) {
            logger.info("--------------------------------------------\n" + i + " , "
                    + resultRow.getPropertyByQueryName("cmis:objectId").getFirstValue() + " , "
                    + resultRow.getPropertyByQueryName("cmis:objectTypeId").getFirstValue() + " , "
                    + resultRow.getPropertyByQueryName("cmis:name").getFirstValue());
            i++;
        }
    }

    /**
     * Get a CMIS Object by name from a specified folder.
     *
     * @param parentFolder the parent folder where the object might exist
     * @param objectName   the name of the object that we are looking for
     * @return the Cmis Object if it existed, otherwise null
     */
    private CmisObject getObject(Session session, Folder parentFolder, String objectName) {
        CmisObject object = null;

        try {
            String path2Object = parentFolder.getPath();
            if (!path2Object.endsWith("/")) {
                path2Object += "/";
            }
            path2Object += objectName;
            object = session.getObjectByPath(path2Object);
        } catch (CmisObjectNotFoundException nfe0) {
            // Nothing to do, object does not exist
        }

        return object;
    }

    /**
     * Get the absolute path to the passed in Document object.
     * Called the primary folder path in the Alfresco world as most documents only have one parent folder.
     *
     * @param document the Document object to get the path for
     * @return the path to the passed in Document object, or "Un-filed/{object name}" if it does not have a parent folder
     */
    private String getDocumentPath(Document document) {
        String path2Doc = getParentFolderPath(document);
        if (!path2Doc.endsWith("/")) {
            path2Doc += "/";
        }
        path2Doc += document.getName();
        return path2Doc;
    }

    /**
     * Get the parent folder path for passed in Document object
     *
     * @param document the document object to get the path for
     * @return the parent folder path, or "Un-filed" if the document is un-filed and does not have a parent folder
     */
    private String getParentFolderPath(Document document) {
        Folder parentFolder = getDocumentParentFolder(document);
        return parentFolder == null ? "Un-filed" : parentFolder.getPath();
    }

    /**
     * Get the parent folder for the passed in Document object.
     * Called the primary parent folder in the Alfresco world as most documents only have one parent folder.
     *
     * @param document the Document object to get the parent folder for
     * @return the parent Folder object, or null if it does not have a parent folder and is un-filed
     */
    private Folder getDocumentParentFolder(Document document) {
        // Get all the parent folders (could be more than one if multi-filed)
        List<Folder> parentFolders = document.getParents();

        // Grab the first parent folder
        if (parentFolders.size() > 0) {
            if (parentFolders.size() > 1) {
                logger.info("The " + document.getName() + " has more than one parent folder, it is multi-filed");
            }

            return parentFolders.get(0);
        } else {
            logger.info("Document " + document.getName() + " is un-filed and does not have a parent folder");
            return null;
        }
    }

    /**
     * Returns date as a string
     *
     * @param date date object
     * @return date as a string formatted with "yyyy-MM-dd HH:mm:ss z"
     */
    private String date2String(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(date);
    }

    /**
     * Extracts the various CMIS 1.0 webservices endpoints from the wsdl file at the repository url.
     * This is how we determine whether the webservices url is valid
     *
     * @param url for the wsdl file
     * @return
     */
    public boolean isValidWebServicesUrls(URL url) {
        try {
            Map<String, String> servicesListURL;
            File wsdlTemp = File.createTempFile("repoWSDL", ".xml"); //create temp file
            FileUtils.copyURLToFile(url, wsdlTemp);// copy the wsdl document so we can extract service endpoints
            SAXBuilder builder = new SAXBuilder();
            org.jdom2.Document wsdlDoc = builder.build(wsdlTemp);
            Element rootNode = wsdlDoc.getRootElement();//extract root xml document
            Namespace defaultNamespace = rootNode.getNamespace();
            //never use the other method as it returns nothing. http://stackoverflow.com/a/12582380/107301
            List<Element> servicesList = rootNode.getChildren("service", defaultNamespace);
            servicesListURL = servicesList.stream().map(this::extractEndpoints).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
            if (servicesListURL.size() > 1)
                return true;
        } catch (Exception ge) {
            System.out.println("************ Error ***********\n\t\t\t" + ge.getMessage() + "\n\n=== Full stack trace ===\n");
            ge.printStackTrace();
        }
        return false;
    }

    /**
     * Extracts the various CMIS 1.0 webservices endpoints from the wsdl file at the repository url.
     *
     * @param url for the wsdl file
     * @return
     */
    private Map<String, String> extractWebservicesUrls(URL url) {
        try {
            File wsdlTemp = File.createTempFile("repoWSDL", ".xml"); //create temp file
            FileUtils.copyURLToFile(url, wsdlTemp);// copy the wsdl document so we can extract service endpoints
            SAXBuilder builder = new SAXBuilder();
            org.jdom2.Document wsdlDoc = builder.build(wsdlTemp);
            Element rootNode = wsdlDoc.getRootElement();//extract root xml document
            Namespace defaultNamespace = rootNode.getNamespace();
            //never use the other method as it returns nothing. http://stackoverflow.com/a/12582380/107301
            List<Element> servicesList = rootNode.getChildren("service", defaultNamespace);
            return servicesList.stream().map(this::extractEndpoints).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        } catch (Exception ge) {
            System.out.println("************ Error ***********\n\t\t\t" + ge.getMessage() + "\n\n=== Full stack trace ===\n");
            ge.printStackTrace();
        }
        return Collections.<String, String>emptyMap();
    }

    /**
     * returns a Pair<String,String> containing the service name and it's webservices endpoint url from a
     * jDom xml element which is in turn extracted from a the wsdl.xml
     *
     * @param svc xml jdom element representing the webservice endpoint
     * @return
     */
    private ImmutablePair<String, String> extractEndpoints(Element svc) {
        Namespace defaultNamespace = svc.getNamespace();
        Namespace soapNamespace = svc.getNamespace("soap");
        return new ImmutablePair<>(svc.getAttributeValue("name"), svc.getChild("port", defaultNamespace)
                .getChild("address", soapNamespace)
                .getAttributeValue("location")
        );
    }

    /**
     * Adds a wsdl to the soap address location of each endpoint if they're not present
     *
     * @param servicesMap
     */
    private void addWSDL(Map<String, String> servicesMap) {
        String urlValue;
        for (String key : servicesMap.keySet()) {
            urlValue = servicesMap.get(key);
            if (!StringUtils.endsWith(urlValue, "?wsdl"))
                servicesMap.put(key, urlValue + "?wsdl");
        }
    }

    /**
     * Just a short simple test against the CMIS repository to see if it's alive by retrieving a few attributes about it
     */
    public void testCMISConnection() {
        try {
            Cmis1Connector tstConnector = new Cmis1Connector();
            Session tstSess = tstConnector.getSession("admin");
            RepositoryInfo repoInfo = tstSess.getRepositoryInfo();

            System.out.println("RepositoryId => " + repoInfo.getId());
            System.out.println("Supported CMIS version => " + repoInfo.getCmisVersionSupported());
            System.out.println("Root Folder ID - " + repoInfo.getRootFolderId());
            System.out.println("Listing capabilities......\n");
            listRepoCapabilities(repoInfo);
        } catch (Exception ge) {
            logger.warn("There was an exception testing the repository's CMIS capabilities: \n" + ge.getMessage());
        }
    }


}
