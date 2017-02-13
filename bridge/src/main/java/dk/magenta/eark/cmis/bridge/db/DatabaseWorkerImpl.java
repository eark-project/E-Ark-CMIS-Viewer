package dk.magenta.eark.cmis.bridge.db;

import dk.magenta.eark.cmis.Repository;
import dk.magenta.eark.cmis.bridge.Constants;
import dk.magenta.eark.cmis.bridge.authentication.Person;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeDbException;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeUserAdminException;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeUserAuthenticationException;
import dk.magenta.eark.cmis.repository.Cmis1Connector;
import org.apache.commons.lang3.RandomStringUtils;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author DarkStar1.
 */
@Service
public class DatabaseWorkerImpl implements DatabaseWorker {
    private final Logger logger = LoggerFactory.getLogger(DatabaseWorkerImpl.class);
    
    @Inject
    DatabaseConnectionStrategy dbConnectionStrategy;

    /**
     * Returns a json object representing a person from the db if the user name and password match
     *
     * @param userName
     * @param password
     * @return
     */
    @Override
    public JsonObject authenticatePerson(String userName, String password) throws CmisBridgeUserAuthenticationException {
        try {
            Person person = dbConnectionStrategy.getPerson(userName);
            if (person != Person.EMPTY && person.getPassword().equals(password))
                return person.toJson();
            else {
                logger.error("******** Error retrieving user from db *********");
                throw new CmisBridgeUserAuthenticationException("Unable to find userName with matching password");
            }
        } catch (SQLException sqe) {
            logger.error("********** Error Retrieving person **********");
            sqe.printStackTrace();
            throw new CmisBridgeUserAuthenticationException("Unable to find userName with matching password");
        }
    }

    /**
     * Creates a user in the db
     *
     * @param jsonObject an object representing the full user details
     * @return {true | false}
     * @throws CmisBridgeUserAdminException
     */
    @Override
    public boolean createPerson(JsonObject jsonObject) throws CmisBridgeUserAdminException {
        try {
            Person person = dbConnectionStrategy.getPerson(jsonObject.getString(Constants.USER_NAME));
            if (!person.equals(Person.EMPTY))
                throw new CmisBridgeUserAdminException("A person with the userName already exists in the db");
            else {
                String userName = jsonObject.getString(Constants.USER_NAME);
                String password = jsonObject.getString(Constants.PASSWORD);
                person = new Person(userName, password);
                if (jsonObject.containsKey(Constants.FIRST_NAME))
                    person.setFirstName(jsonObject.getString(Constants.FIRST_NAME));
                if (jsonObject.containsKey(Constants.LAST_NAME))
                    person.setLastName(jsonObject.getString(Constants.LAST_NAME));
                if (jsonObject.containsKey(Constants.EMAIL))
                    person.setEmail(jsonObject.getString(Constants.EMAIL));
                if (jsonObject.containsKey(Constants.USER_ROLE))
                    person.setRole(jsonObject.getString(Constants.USER_ROLE));
                dbConnectionStrategy.createPerson(person);
                return true;
            }
        } catch (SQLException | CmisBridgeDbException | NullPointerException ge) {
            logger.error("********** Error *********");
            ge.printStackTrace();
            throw new CmisBridgeUserAdminException("Unable to create person. Check server logs for further details");
        }
    }

    /**
     * Returns a json object representing a person from the repository based on the supplied user name
     *
     * @param userName person's user name
     * @return JsonObject {person}
     */
    @Override
    public JsonObject getPersonJson(String userName) {
        try {
            Person person = dbConnectionStrategy.getPerson(userName);
            return person.toJson();
        } catch (SQLException sqe) {
            logger.error("********** Error Retrieving person **********");
            sqe.printStackTrace();
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
            jsonObjectBuilder.add(Constants.ERRORMSG, sqe.getMessage());
            return jsonObjectBuilder.build();
        }
    }

    /**
     * Returns a json object with the list of everyone on the system
     *
     * @return JsonObject {everyone}
     */
    @Override
    public JsonObject getPeople() {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        try {
            JsonArrayBuilder users = Json.createArrayBuilder();
            List<JsonObject> persons = dbConnectionStrategy.getPersons().stream().map(Person::toJson)
                    .collect(Collectors.toList());
            persons.forEach(users::add);
            jsonObjectBuilder.add(Constants.SUCCESS, true);
            jsonObjectBuilder.add("users", users.build());

        } catch (SQLException sqe) {
            logger.error("********** Error Retrieving person **********");
            sqe.printStackTrace();
            jsonObjectBuilder.add(Constants.SUCCESS, false);
            jsonObjectBuilder.add(Constants.ERRORMSG, sqe.getMessage());
        }
        return jsonObjectBuilder.build();
    }

    /**
     * Updates a person given the details of what's in the json object
     *
     * @param userName   the name of the user to update.
     * @param jsonObject can only contain {firstName | lastName | email | password} username is mandatory and must be
     *                   unique hence should not be changeable after creation
     * @return {true | false}
     */
    @Override
    public boolean updatePerson(String userName, JsonObject jsonObject) throws CmisBridgeUserAdminException {
        try {
            Person person = dbConnectionStrategy.getPerson(userName);
            if (person.equals(Person.EMPTY))
                throw new NullPointerException("User was not retrieved from the db");
            else {
                if (jsonObject.containsKey(Constants.FIRST_NAME))
                    person.setFirstName(jsonObject.getString(Constants.FIRST_NAME));
                if (jsonObject.containsKey(Constants.LAST_NAME))
                    person.setLastName(jsonObject.getString(Constants.LAST_NAME));
                if (jsonObject.containsKey(Constants.EMAIL))
                    person.setEmail(jsonObject.getString(Constants.EMAIL));
                if (jsonObject.containsKey(Constants.PASSWORD))
                    person.setPassword(jsonObject.getString(Constants.PASSWORD));
                if (jsonObject.containsKey(Constants.USER_ROLE))
                    person.setRole(jsonObject.getString(Constants.USER_ROLE));

                dbConnectionStrategy.updatePerson(person);
                return true;
            }
        } catch (SQLException | CmisBridgeDbException | NullPointerException ge) {
            logger.error("********** Error *********");
            ge.printStackTrace();
            throw new CmisBridgeUserAdminException("Unable to update person. Check server logs for further details");
        }
    }

    /**
     * Removes a person from the db
     *
     * @param userName of the person to be removed
     * @return {true | false}
     * @throws CmisBridgeUserAdminException if anything goes wrong with the operation
     */
    @Override
    public boolean deletePerson(String userName) throws CmisBridgeUserAdminException {
        try {
            dbConnectionStrategy.deletePerson(userName);
            return true;
        } catch (SQLException | CmisBridgeDbException | NullPointerException ge) {
            logger.error("********** Error *********");
            ge.printStackTrace();
            throw new CmisBridgeUserAdminException("Unable to update person. Check server logs for further details");
        }
    }

    /**
     * Gets the cmis repository details from the db
     *
     * @return a JSON object representing the repository details
     * @throws CmisBridgeDbException
     */
    @Override
    public JsonObject getRepositoryDetails() throws CmisBridgeDbException {
        JsonObjectBuilder json = Json.createObjectBuilder();
        try {
            Cmis1Connector cmis1Connector = new Cmis1Connector();
            Map<String, String> details = cmis1Connector.getRepoDetails();
            json.add(Repository.URL, details.get(Repository.URL));
            json.add(Repository.USERNAME, details.get(Repository.USERNAME));
            json.add(Repository.PASSWORD, "thouShaltNotCovetMinePassw0rd");
        } catch (Exception ge) {
            String rnd = RandomStringUtils.random(16, true, true);
            logger.error("********** Error (" + rnd + ") **********");
            ge.printStackTrace();
            throw new CmisBridgeDbException("Unable to retrieve repository details. See error [" + rnd + "] in server logs for details");
        }
        return json.build();
    }

    /**
     * @param repoProperties a map containing the repository properties to update
     * @return JSON object representing the repository
     * @throws CmisBridgeDbException
     */
    @Override
    public JsonObject updateRepoDetails(Map<String, String> repoProperties) throws CmisBridgeDbException {
        try {
            if (dbConnectionStrategy.updateRepository(repoProperties)) {
                //Refresh the singleton
                Repository.getInstance().refreshDetails();
                //Clear any cached sessions
                Cmis1Connector.clearSessions();
                return this.getRepositoryDetails();
            } else throw new CmisBridgeDbException("Unable to update repository details");
        } catch (Exception ge) {
            String rnd = RandomStringUtils.random(7, true, true);
            logger.error("********** Error (" + rnd + ") **********");
            ge.printStackTrace();
            throw new CmisBridgeDbException("Unable to update repository details. See error [" + rnd + "] in server logs for details");
        }
    }

    /**
     * Returns true or false as to whether a userName already exists in the db
     *
     * @param userName user name string to check
     * @return {true | false}
     */
    @Override
    public boolean userExists(String userName) {
        return dbConnectionStrategy.userExists(userName);
    }
}
