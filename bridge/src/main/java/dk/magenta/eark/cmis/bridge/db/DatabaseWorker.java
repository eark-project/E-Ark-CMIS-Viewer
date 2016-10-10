package dk.magenta.eark.cmis.bridge.db;

import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeDbException;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeUserAdminException;

import javax.json.JsonObject;
import java.util.Map;

/**
 * @author lanre.
 */
public interface DatabaseWorker {

    //TODO since we have no notion of who is logged in, there needs to be away of dtermining that the updates are being perfomred by an admin or someone

    /**
     * Returns a json object representing a person from the db if the user name and password match
     * @param userName
     * @param password
     * @return
     */
    JsonObject authenticatePerson(String userName, String password);

    /**
     * Creates a user in the db
     * @param jsonObject an object representing the full user details
     * @return {true | false}
     * @throws CmisBridgeUserAdminException
     */
    boolean createPerson(JsonObject jsonObject) throws CmisBridgeUserAdminException;

    /**
     * Returns a json object with the list of everyone on the system
     * @return JsonObject {everyone}
     */
    JsonObject getPeople();

    /**
     * Returns a json object representing a person from the repository based on the supplied user name
     * @param userName person's user name
     * @return JsonObject {person}
     */
    JsonObject getPersonJson(String userName);

    /**
     * Updates a person given the details of what's in the json object
     * @param userName the name of the user to update.
     * @param jsonObject can only contain {firstName | lastName | email | password} username is mandatory and must be
     *                   unique hence should not be changeable after creation
     * @return {true | false}
     */
    boolean updatePerson(String userName, JsonObject jsonObject) throws CmisBridgeUserAdminException;

    /**
     * Removes a person from the db
     * @param userName of the person to be removed
     * @return {true | false}
     * @throws CmisBridgeUserAdminException if anything goes wrong with the operation
     */
    boolean deletePerson(String userName) throws CmisBridgeUserAdminException;

    /**
     * Gets the cmis repository details from the db
     * @return a JSON object representing the repository details
     * @throws CmisBridgeDbException
     */
    JsonObject getRepositoryDetails() throws CmisBridgeDbException;

    /**
     *
     * @param repoProperties a map containing the repository properties to update
     * @return JSON object representing the repository
     * @throws CmisBridgeDbException
     */
    JsonObject updateRepoDetails(Map<String, String> repoProperties) throws CmisBridgeDbException;

    /**
     * Returns true or false as to whether a userName already exists in the db
     * @param userName user name string to check
     * @return {true | false}
     */
    public boolean userExists(String userName);
}
