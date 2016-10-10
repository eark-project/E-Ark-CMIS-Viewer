package dk.magenta.eark.cmis.bridge.db;

import dk.magenta.eark.cmis.bridge.Constants;
import dk.magenta.eark.cmis.bridge.authentication.Person;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeDbException;
import dk.magenta.eark.cmis.viewer.db.connector.cmis_bridge.tables.Repository;
import dk.magenta.eark.cmis.viewer.db.connector.cmis_bridge.tables.Roles;
import dk.magenta.eark.cmis.viewer.db.connector.cmis_bridge.tables.User;
import dk.magenta.eark.cmis.system.PropertiesHandler;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JDBCConnectionStrategy implements DatabaseConnectionStrategy {
    private final Logger logger = LoggerFactory.getLogger(JDBCConnectionStrategy.class);
    private Connection connection;
    private static final String REPOSITORY_NAME = "repository";

    public JDBCConnectionStrategy(PropertiesHandler propertiesHandler) throws SQLException{
        PropertiesHandler propertiesHandler1 = propertiesHandler;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String host = propertiesHandler.getProperty("host");
            String userName = propertiesHandler.getProperty("userName");
            String password = propertiesHandler.getProperty("password");
            //Create a connection
            this.connection = DriverManager.getConnection(host, userName, password);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //TODO: Switch several of the statements to use transactions and handle transaction errors properly

    //<editor-fold desc="Users CRUD">
    /**
     * Returns a list of persons from the db
     * @return
     * @throws SQLException
     */
    public List<Person> getPersons() throws SQLException {
        try (DSLContext db = DSL.using(connection, SQLDialect.MYSQL)) {
            return db.select().from(User.USER)
                    .fetch()
                    .stream()
                    .map(this::convertToPerson)
                    .collect(Collectors.toList());
        }
        catch (Exception ge){
            ge.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Gets a single person from the db using the person's user name (user name is unique)
     *
     * @param userName the name of the person to retrieve from the db
     * @return
     * @throws SQLException
     */
    @Override
    public Person getPerson(String userName) throws SQLException{
        Person user;
        try (DSLContext db = DSL.using(connection, SQLDialect.MYSQL)) {
            //Written just to understand how to chain the result from Jooq to a J8 stream. Actually does nothing
            user = db.select().from(User.USER).fetch().stream()
                    .filter(t -> t.getValue(User.USER.USERNAME).equalsIgnoreCase(userName))
                    .limit(1) //limit it to just the one result
                    .map(this::convertToPerson)
                    .collect(Collectors.toList()).get(0);
        }
        catch (Exception ge) {
            logger.warn("There was an error in attempting to retrieve the person: ");
            ge.printStackTrace();
            return Person.EMPTY;
        }
        return user;
    }

    /**
     * Gets the user role from the db
     *
     * @param userName the user name whose role is required
     * @return
     * @throws SQLException
     */
    @Override
    public String getUserRole(String userName) throws CmisBridgeDbException {
        String role;
        try (DSLContext db = DSL.using(connection, SQLDialect.MYSQL)) {
            role = db.select().from(Roles.ROLES).fetch().stream()
                    .filter(t -> t.getValue(Roles.ROLES.USERNAME).equalsIgnoreCase(userName))
                    .limit(1) //limit it to just the one result
                    .map(t -> t.getValue(Roles.ROLES.ROLE))
                    .collect(Collectors.toList()).get(0);
        }
        catch (Exception ge) {
            logger.error("There was an error in attempting to retrieve the user role");
            ge.printStackTrace();
            throw new CmisBridgeDbException("Unable to get the user role from the db. Check server error logs for details");
        }
        return role;
    }

    /**
     * Checks if the user exists in the db
     *
     * @param userName
     * @return
     */
    @Override
    public boolean userExists(String userName) {
        try {
            Person p = this.getPerson(userName);
            if(p != Person.EMPTY && StringUtils.isNotBlank(p.getUserName()) )
                return true;
        }
        catch (SQLException sqe){
            logger.error("There was an error finding the user in the db. Please view error log for details");
            sqe.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * Adds the person to the db
     * @param person
     * @return true | false
     * @throws SQLException
     */
    @Override
    public boolean createPerson(Person person) throws SQLException, CmisBridgeDbException {
        int result;
        try (DSLContext db = DSL.using(connection, SQLDialect.MYSQL)) {
            result = db.transactionResult( configuration -> {
                int commits = DSL.using(configuration).insertInto(User.USER, User.USER.USERNAME, User.USER.FIRSTNAME,
                        User.USER.LASTNAME, User.USER.EMAIL, User.USER.PASSWORD)
                        .values(person.getUserName(), person.getFirstName(), person.getLastName(),person.getEmail(),
                                person.getPassword() )
                        .execute();

                if(commits != 1) {
                    logger.error("The data may not have been persisted correctly. Output of the result was: "+ commits);
                }
                return commits;
            });
            //Set the role after creating the user
            setUserRole(person.getUserName(), person.getRole().toString());
        } catch (Exception ge){
            logger.error("An issue with persisting the person in the db.\n"+ ge.getMessage());
            ge.printStackTrace();
            throw new CmisBridgeDbException("There was an issue with persisting the person in the db:" + ge.getMessage());
        }
        return result==1;
    }

    /**
     * Removes a single person from the db
     * @param userName the name of the person to remove
     * @return
     * @throws SQLException
     */
    @Override
    public boolean deletePerson(String userName) throws SQLException {
        int result;
        try (DSLContext db = DSL.using(connection, SQLDialect.MYSQL)) {
            result = db.transactionResult( configuration -> {
                int deletes = DSL.using(configuration).delete(User.USER)
                        .where(User.USER.USERNAME.equalIgnoreCase(userName))
                        .execute();

                if(deletes >= 1) {
                    logger.error("*****Error*****\nThe number of deletions made were ["+ (deletes - 1) +"] more than " +
                            "was necessary");
                }
                if(deletes < 1) {
                    logger.error("No deletions were performed. DB transaction result: "+ deletes);
                }
                return deletes;
            });
        } catch (Exception ge){
            ge.printStackTrace();
            logger.error("An issue when attempting to remove the person from the db.\n"+ ge.getMessage());
            return false;
        }
        return result==1;
    }

    /**
     * Updates a person in the db
     * @param person
     * @throws SQLException
     */
    @Override
    public boolean updatePerson(Person person) throws SQLException, CmisBridgeDbException  {
        int result;
        try (DSLContext db = DSL.using(connection, SQLDialect.MYSQL)) {
            result = db.transactionResult( configuration -> {
                //Update the profile in question. This should actually lock the record during update. I think/hope
               int commits = DSL.using(configuration).update(User.USER)
                        .set(User.USER.FIRSTNAME, person.getFirstName())
                        .set(User.USER.LASTNAME, person.getLastName())
                        .set(User.USER.EMAIL, person.getEmail())
                        .set(User.USER.PASSWORD, person.getPassword())
                        .where(User.USER.USERNAME.equalIgnoreCase(person.getUserName()))
                        .execute();
               if(commits != 1) {
                   logger.warn("The update may not have been done correctly. Output of the result was: " + commits);
               }
               return commits;
            });

        } catch (Exception ge) {
            logger.error("******* Error when attempting to update person *******");
            ge.printStackTrace();
            throw new CmisBridgeDbException("There was an error in attempting to update the person's details:\n\t"
                    + ge.getMessage());
        }
        return result==1;
    }

    /**
     * Sets the user role to the specified role
     *
     * @param userName
     * @param userRole
     * @return
     * @throws SQLException
     */
    @Override
    public boolean setUserRole(String userName, String userRole) throws SQLException {
        int result;
        try (DSLContext db = DSL.using(connection, SQLDialect.MYSQL)) {
            result = db.transactionResult( configuration -> {
                int commits = DSL.using(configuration).insertInto(Roles.ROLES, Roles.ROLES.USERNAME, Roles.ROLES.ROLE)
                        .values(userName,userRole)
                        .execute();

                if(commits != 1) {
                    logger.error("The data may not have been persisted correctly. Output of the result was: " + commits);
                }
                return commits;
            });
        } catch (Exception ge){
            logger.error("An issue with setting the user role in the db.\n"+ ge.getMessage());
            ge.printStackTrace();
            throw new CmisBridgeDbException("There was an issue with setting the user role in the db:" + ge.getMessage());
        }
        return result==1;
    }

    //</editor-fold>

    //<editor-fold desc="Repository">
    /**
     * Returns the repository  record storred in the db
     * @return org.jooq.Record
     * @throws SQLException
     */
    @Override
    public Record getRepository() throws SQLException{
        try (DSLContext db = DSL.using(connection, SQLDialect.MYSQL)) {
            //Written just to understand how to chain the result from Jooq to a J8 stream. Actually does nothing
            return db.select().from(Repository.REPOSITORY)
                    .where(Repository.REPOSITORY.NAME.equalIgnoreCase(REPOSITORY_NAME))
                    .fetchOne();
        }
        catch (Exception ge) {
            logger.error("There was an error retrieving stored repository details from the db: ");
            ge.printStackTrace();
            throw new CmisBridgeDbException("Unable to retrieve repository details from the database");
        }
    }
    /**
     * Returns the repository  record stored in the db
     * @param props a map of key value properties of the repository to update. Maximum 3 and can either be all or some
     *              of the following {password | url | userName} (i.e. the map keys)
     * @return
     * @throws SQLException
     */
    @Override
    public boolean updateRepository(Map<String, String> props) throws SQLException {
        int result;
        try (DSLContext db = DSL.using(connection, SQLDialect.MYSQL)) {

            List<Query> queries = new ArrayList<>();
            Query passwordQuery = db.update(Repository.REPOSITORY).set(Repository.REPOSITORY.PASSWORD, props.get("password"))
                    .where(Repository.REPOSITORY.NAME.equal(REPOSITORY_NAME));
            Query userNameQuery = db.update(Repository.REPOSITORY).set(Repository.REPOSITORY.USERNAME, props.get(Constants.USER_NAME))
                    .where(Repository.REPOSITORY.NAME.equal(REPOSITORY_NAME));
            Query urlQuery = db.update(Repository.REPOSITORY).set(Repository.REPOSITORY.URL, props.get("url"))
                    .where(Repository.REPOSITORY.NAME.equal(REPOSITORY_NAME));

            if(props.containsKey("password"))
                queries.add(passwordQuery);
            if(props.containsKey("url"))
                queries.add(urlQuery);
            if(props.containsKey("userName"))
                queries.add(userNameQuery);

            result = db.transactionResult( configuration -> {
                int[] commits = DSL.using(configuration).batch(queries).execute();

                if(commits.length != queries.size()) {
                    logger.error("The data may not have been persisted correctly." + commits +
                            ": number of queries were executed instead of " + queries.size() + " queries");
                }
                return commits.length;

            });

        } catch (Exception ge) {
            logger.error("******* Error when attempting to update repository details *******");
            ge.printStackTrace();
            throw new CmisBridgeDbException("There was an error in attempting to update the repository details:\n\t"
                    + ge.getMessage());
        }
        return result >= 1;
    }
    //</editor-fold>

    /**
     * Converts a single record to a profile
     *
     * @param r a single record from from the db
     * @return
     */
    private Person convertToPerson(Record r) {
        logger.info("-----> Converting record to person");
        Person person =  r.into(Person.class);
        String rle = getUserRole(person.getUserName());
        person.setRole(rle);
        return person;
    }

    private void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}

