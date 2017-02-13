package dk.magenta.eark.cmis.bridge.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * @author DarkStar1.
 */
public class Person {
    private static final Logger logger = LoggerFactory.getLogger(Person.class);
    public static final String DEFAULT_ROLE = "admin";
    private String userName, firstName, lastName, email, password;
    private Role role;

    public enum Role {ADMIN, STANDARD}

    //Constructors
    public Person() { //used to denote an empty person
    }
    // creating a person with just the mandatory properties
    public Person(String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.setRole(DEFAULT_ROLE);
    }
    // creating a person with just the mandatory properties
    public Person(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.setRole(DEFAULT_ROLE);
    }
    // creating a person with just the mandatory properties with role
    public Person(String userName, String email, String password, String role) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.setRole(role);
    }
    // creating a person with all properties
    public Person(String userName, String email, String password, String firstName, String lastName) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.setRole(DEFAULT_ROLE);
    }
    //With role
    public Person(String userName, String email, String password, String firstName, String lastName, String role) {
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.setRole(role);
    }

    //<editor-fold desc="Getters and setters">
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    public void setRole(String role) {
        try {
            this.role = Role.valueOf(role.toUpperCase());
        }
        catch (IllegalArgumentException | NullPointerException ine){
            logger.warn("******** Warning!!! ********\nUnknown role setting user to normal user");
        }
    }

    //</editor-fold>

    public JsonObject toJson(){
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("email", this.getEmail());
        jsonObjectBuilder.add("firstName", this.getFirstName());
        jsonObjectBuilder.add("lastName", this.getLastName());
        jsonObjectBuilder.add("userName", this.getUserName());
        jsonObjectBuilder.add("role", this.getRole().toString());

        return jsonObjectBuilder.build();
    }

    /**
     * Used to return an empty map (to avoid null patterns)
     */
    public static final Person EMPTY = new Person();
}
