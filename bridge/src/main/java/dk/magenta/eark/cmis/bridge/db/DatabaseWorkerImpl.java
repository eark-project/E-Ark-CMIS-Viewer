package dk.magenta.eark.cmis.bridge.db;

import dk.magenta.eark.cmis.bridge.Constants;
import dk.magenta.eark.cmis.bridge.authentication.Person;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeDbException;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeUserAdminException;
import dk.magenta.eark.cmis.system.PropertiesHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.sql.SQLException;

/**
 * @author lanre.
 */
public class DatabaseWorkerImpl implements DatabaseWorker {
    private final Logger logger = LoggerFactory.getLogger(DatabaseWorkerImpl.class);
    private DatabaseConnectionStrategy dbConnectionStrategy;

    /**
     *
     */
    public DatabaseWorkerImpl() {

        try {
            this.dbConnectionStrategy = new JDBCConnectionStrategy(new PropertiesHandlerImpl("settings.properties"));
        }
        catch (SQLException sqe) {
            logger.error("********** Error Initialising Database Connector **********");
            sqe.printStackTrace();
            throw new CmisBridgeDbException("Unable to initialise new db connection. See error logs for " +
                    "details\n"+sqe.getMessage());
        }
    }

    /**
     * Returns a json object representing a person from the db if the user name and password match
     * @param userName
     * @param password
     * @return
     */
    @Override
    public JsonObject authenticatePerson(String userName, String password) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        try {
            Person person = dbConnectionStrategy.getPerson(userName);
            if(person != Person.EMPTY && person.getPassword().equals(password))
                return person.toJson();
            else{
                jsonObjectBuilder.add(Constants.SUCCESS, false);
                jsonObjectBuilder.add(Constants.ERRORMSG, "User not found");
            }
        }
        catch (SQLException sqe){
            logger.error("********** Error Retrieving person **********");
            sqe.printStackTrace();
            jsonObjectBuilder.add(Constants.SUCCESS, false);
            jsonObjectBuilder.add(Constants.ERRORMSG, sqe.getMessage());
        }
        return jsonObjectBuilder.build();

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
            Person person = this.dbConnectionStrategy.getPerson(jsonObject.getString(Constants.USER_NAME));
            if(!person.equals(Person.EMPTY))
                throw new CmisBridgeUserAdminException("A person with the userName already exists in the db");
            else{
                String userName = jsonObject.getString(Constants.USER_NAME);
                String password = jsonObject.getString(Constants.PASSWORD);
                person = new Person(userName, password);
                if(jsonObject.containsKey(Constants.FIRST_NAME))
                    person.setFirstName(jsonObject.getString(Constants.FIRST_NAME));
                if(jsonObject.containsKey(Constants.LAST_NAME))
                    person.setLastName(jsonObject.getString(Constants.LAST_NAME));
                if(jsonObject.containsKey(Constants.EMAIL))
                    person.setEmail(jsonObject.getString(Constants.EMAIL));
                if(jsonObject.containsKey(Constants.USER_ROLE))
                    person.setRole(jsonObject.getString(Constants.USER_ROLE));
                this.dbConnectionStrategy.createPerson(person);
                return true;
            }
        }
        catch (SQLException | CmisBridgeDbException | NullPointerException ge){
            logger.error("********** Error *********");
            ge.printStackTrace();
            throw new CmisBridgeUserAdminException ("Unable to create person. Check server logs for further details");
        }
    }

    /**
     * Returns a json object representing a person from the repository based on the supplied user name
     * @param userName person's user name
     * @return JsonObject {person}
     */
    @Override
    public JsonObject getPersonJson(String userName) {
        try {
            Person person = dbConnectionStrategy.getPerson(userName);
            return person.toJson();
        }
        catch (SQLException sqe){
            logger.error("********** Error Retrieving person **********");
            sqe.printStackTrace();
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
            jsonObjectBuilder.add(Constants.ERRORMSG, sqe.getMessage());
            return jsonObjectBuilder.build();
        }
    }

    /**
     * Updates a person given the details of what's in the json object
     * @param userName the name of the user to update.
     * @param jsonObject can only contain {firstName | lastName | email | password} username is mandatory and must be
     *                   unique hence should not be changeable after creation
     * @return {true | false}
     */
    @Override
    public boolean updatePerson(String userName, JsonObject jsonObject) throws CmisBridgeUserAdminException {
        try {
            Person person = this.dbConnectionStrategy.getPerson(userName);
            if(person.equals(Person.EMPTY))
                throw new NullPointerException("User was not retrieved from the db");
            else{
                if(jsonObject.containsKey(Constants.FIRST_NAME))
                    person.setFirstName(jsonObject.getString(Constants.FIRST_NAME));
                if(jsonObject.containsKey(Constants.LAST_NAME))
                    person.setLastName(jsonObject.getString(Constants.LAST_NAME));
                if(jsonObject.containsKey(Constants.EMAIL))
                    person.setEmail(jsonObject.getString(Constants.EMAIL));
                if(jsonObject.containsKey(Constants.PASSWORD))
                    person.setPassword(jsonObject.getString(Constants.PASSWORD));
                if(jsonObject.containsKey(Constants.USER_ROLE))
                    person.setRole(jsonObject.getString(Constants.USER_ROLE));

                this.dbConnectionStrategy.updatePerson(person);
                return true;
            }
        }
        catch (SQLException | CmisBridgeDbException | NullPointerException ge){
            logger.error("********** Error *********");
            ge.printStackTrace();
            throw new CmisBridgeUserAdminException ("Unable to update person. Check server logs for further details");
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
            this.dbConnectionStrategy.deletePerson(userName);
            return true;
        }
        catch (SQLException | CmisBridgeDbException | NullPointerException ge){
            logger.error("********** Error *********");
            ge.printStackTrace();
            throw new CmisBridgeUserAdminException ("Unable to update person. Check server logs for further details");
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
        return this.dbConnectionStrategy.userExists(userName);
    }
}
