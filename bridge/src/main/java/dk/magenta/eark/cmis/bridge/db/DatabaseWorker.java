package dk.magenta.eark.cmis.bridge.db;

import javax.json.JsonObject;

/**
 * @author lanre.
 */
public interface DatabaseWorker {
    /**
     * Returns a json object representing a person from the repository based on the supplied user name
     * @param userName person's user name
     * @return JsonObject {person}
     */
    JsonObject getPersonJson(String userName);


    /**
     * Returns a json object representing a person from the db if the user name and password match
     * @param userName
     * @param password
     * @return
     */
    JsonObject authenticatePerson(String userName, String password);
}
