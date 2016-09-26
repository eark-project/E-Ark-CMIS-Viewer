package dk.magenta.eark.cmis.bridge.db;

import dk.magenta.eark.cmis.bridge.Constants;
import dk.magenta.eark.cmis.bridge.authentication.Person;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeDbException;
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
}
