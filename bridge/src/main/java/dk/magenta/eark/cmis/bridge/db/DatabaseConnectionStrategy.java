package dk.magenta.eark.cmis.bridge.db;

import dk.magenta.eark.cmis.bridge.authentication.Person;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeDbException;
import org.jooq.Record;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DatabaseConnectionStrategy {
    /**
     * Returns a list of persons from the db
     * @return
     * @throws SQLException
     */
    List<Person> getPersons() throws SQLException;

    /**
     * Gets a single person from the db using the person's user name (user name is unique)
     *
     * @param userName the name of the person to retrieve from the db
     * @return
     * @throws SQLException
     */
    Person getPerson(String userName) throws SQLException;

    /**
     * Gets the user role from the db
     * @param userName the user name whose role is required
     * @return
     * @throws SQLException
     */
    String getUserRole(String userName) throws CmisBridgeDbException;

    /**
     * Updates a person in the db
     * @param person
     * @throws SQLException
     */
    boolean updatePerson(Person person) throws SQLException, CmisBridgeDbException ;

    /**
     * Removes a single person from the db
     * @param userName the name of the person to remove
     * @return
     * @throws SQLException
     */
    boolean deletePerson(String userName) throws SQLException;

    /**
     * Adds the person to the db
     * @param person
     * @return true | false
     * @throws SQLException
     */
    boolean createPerson(Person person) throws SQLException, CmisBridgeDbException;

    /**
     * Checks if the user exists in the db
     * @param userName
     * @return
     */
    boolean userExists(String userName);

    /**
     * Sets the user role to the specified role
     * @param userName
     * @param userRole
     * @return
     * @throws SQLException
     */
    boolean setUserRole(String userName, String userRole) throws SQLException;

    /**
     * Returns the repository  record storred in the db
     * @return org.jooq.Record
     * @throws SQLException
     */
    Record getRepository() throws SQLException;

    /**
     * Returns the repository  record stored in the db
     * @param props a map of key value properties of the repository to update. Maximum 3 and can either be all or some
     *              of the following {password | url | userName} (i.e. the map keys)
     * @return
     * @throws SQLException
     */
    boolean updateRepository(Map<String, String> props) throws SQLException;

}
