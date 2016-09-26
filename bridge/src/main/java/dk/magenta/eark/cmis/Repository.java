package dk.magenta.eark.cmis;

import dk.magenta.eark.cmis.bridge.db.DatabaseConnectionStrategy;
import dk.magenta.eark.cmis.bridge.db.JDBCConnectionStrategy;
import dk.magenta.eark.cmis.system.PropertiesHandlerImpl;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.sql.SQLException;

public class Repository {
    private final Logger logger = LoggerFactory.getLogger(Repository.class);
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String USERNAME = "userName";
    public static final String PASSWORD = "password";

    private String name, url, userName, password;

    /**
     * Empty constructor meaning
     */
    private static Repository repository = new Repository();

    /**
     * A private Constructor prevents any other
     * class from instantiating.
     */
    private Repository() {
        refreshDetails();
    }

    /* Static 'instance' method */
    public static Repository getInstance() {
        return repository;
    }

    //<editor-fold desc="Getter methods">
    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    //</editor-fold

    /**
     * Returns a json representation of an instance of this object
     *
     * @return
     */
    public JsonObject toJsonObject() {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("name", this.getName());
        jsonObjectBuilder.add("url", this.getUrl());
        jsonObjectBuilder.add("userName", this.getUserName());
        jsonObjectBuilder.add("password", this.getPassword());

        return jsonObjectBuilder.build();
    }
    public void refreshDetails(){
        try {
            DatabaseConnectionStrategy dbConnectionStrategy = new JDBCConnectionStrategy(new
                    PropertiesHandlerImpl("settings.properties"));
            Record record = dbConnectionStrategy.getRepository();
            this.name = record.get(NAME).toString();
            this.url = record.get(URL).toString();
            this.userName = record.get(USERNAME).toString();
            this.password = record.get(PASSWORD).toString();
        }
        catch(SQLException sqe){
            logger.error("unable to acquire connection resource.");
            sqe.printStackTrace();
        }
    }

}
