package dk.magenta.eark.cmis.bridge.authentication;

import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeInvalidSessionTokenException;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeUserAuthenticationException;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author DarkStar1.
 */
@Contract
public interface AuthenticationService {
    /**
     * Crete an access token for a user. If one already exists remove it.
     *
     * @param userName
     * @return
     */
    AccessToken createAccessToken(String userName);

    /**
     * Get's a user access token using the userName and token id.
     * We do not care to check whether it is still valid or not; check should be done post retrieval.
     * @param userName userName of the user
     * @param tokenId the token id should be part of the session attribute returned from the UI
     * @return
     * @throws CmisBridgeInvalidSessionTokenException if token does not exist
     */
    AccessToken getUserAccessToken(String userName, String tokenId) throws CmisBridgeInvalidSessionTokenException;

    /**
     * Returns a base64 encoded string as a response to the userName password
     * @param userName
     * @param password
     * @return
     */
    String authenticateUser(String userName, String password) throws CmisBridgeUserAuthenticationException;

    /**
     * Destroys the user session by removing the user and session token from the session token map.
     * Will always return true whether or not the user session exists in the map.
     * @param userName
     * @param sessionToken
     */
    void destroyUserSession(String userName, String sessionToken);

    /**
     * Determines if the user is authenticated based on the encoded authentication string
     * @param authCredentials
     * @return
     */
    public boolean isAuthenticated(String authCredentials);

    /**
     * Generate a random string.
     *
     * @return
     */
    public String generateAccessToken();
}
