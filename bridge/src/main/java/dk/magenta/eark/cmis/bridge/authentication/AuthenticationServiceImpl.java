package dk.magenta.eark.cmis.bridge.authentication;

import dk.magenta.eark.cmis.bridge.db.DatabaseWorker;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeInvalidSessionTokenException;
import dk.magenta.eark.cmis.bridge.exceptions.CmisBridgeUserAuthenticationException;
import dk.magenta.eark.cmis.system.PropertiesHandler;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

/**
 * @author lanre.
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    private static final long ONE_MINUTE_MS = 1000 * 60; //One minute
    private static final long DEFAULT_TTL = 10 * ONE_MINUTE_MS;
    //Keep a static map of the session tokens
    private static Map<String, AccessToken> sessionTokenMap = new HashMap<>();
    long TTL;

    private SecureRandom random = new SecureRandom();

    @Inject
    private PropertiesHandler propertiesHandler;

    @Inject
    private DatabaseWorker databaseWorker;

    /**
     * Constructor
     */
    public AuthenticationServiceImpl() {
        //Set the TTL for session tokens
        try {
            TTL = ONE_MINUTE_MS * Integer.valueOf(propertiesHandler.getProperty("session.timeout"));
        } catch (Exception ge) {
            TTL = DEFAULT_TTL;
        }
    }

    /**
     * Crete an access token for a user. If one already exists remove it.
     *
     * @param userName
     * @return
     */
    @Override
    public AccessToken createAccessToken(String userName) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + TTL);

        //Check if a token already exists for the user we remove it
        AccessToken userSessiontoken = sessionTokenMap.get(userName);
        if (userSessiontoken != null) {
            sessionTokenMap.remove(userName);
        }

        return new AccessToken(generateAccessToken(), expiryDate, userName);
    }

    /**
     * Get's a user access token using the userName and token id.
     * We do not care to check whether it is still valid or not; check should be done post retrieval.
     *
     * @param userName userName of the user
     * @param tokenId  the token id should be part of the session attribute returned from the UI
     * @return
     * @throws CmisBridgeInvalidSessionTokenException if token does not exist
     */
    @Override
    public AccessToken getUserAccessToken(String userName, String tokenId) throws CmisBridgeInvalidSessionTokenException {
        try {
            AccessToken userAccessToken = sessionTokenMap.get(userName);
            if (tokenId != null && userAccessToken.getTokenId().equals(tokenId))
                return userAccessToken;
            else
                throw new CmisBridgeInvalidSessionTokenException("The session token for user does not exist");
        } catch (Exception ge) {
            logger.error("******** Error getting user access token ********");
            ge.printStackTrace();
            throw new CmisBridgeInvalidSessionTokenException("The session token for user does not exist");
        }
    }

    /**
     * Returns a base64 encoded string as a response to the userName password
     *
     * @param userName
     * @param password
     * @return
     */
    @Override
    public String authenticateUser(String userName, String password) throws CmisBridgeUserAuthenticationException {
        try {
            //If this fails for whatever reason, an exception is thrown so need to make use of the returned value for now.
            JsonObject person = databaseWorker.authenticatePerson(userName, password);
            AccessToken sessionToken = createAccessToken(userName);
            sessionTokenMap.put(userName, sessionToken);
            String ueSessionToken = userName + ';' + sessionToken.getTokenId();
            //Encode
            Base64.Encoder encoder = Base64.getEncoder();
            return encoder.encodeToString(ueSessionToken.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ge) {
            logger.error("******** Error authenticating user [" + userName + "] ********");
            ge.printStackTrace();
            throw new CmisBridgeUserAuthenticationException("Unable to authenticate user. Check error logs for details");
        }
    }

    /**
     * Destroys the user session by removing the user and session token from the session token map.
     * Will always return true whether or not the user session exists in the map.
     *
     * @param userName
     * @param sessionToken
     */
    @Override
    public void destroyUserSession(String userName, String sessionToken) {

        try {
            Pair<String,String> userNameId = this.decodeAccessToken(sessionToken);
            if (this.getUserAccessToken(userName, sessionToken) != null)
                sessionTokenMap.remove(userName);
        }
        catch(Exception ge){
            logger.error("******** Error ********\nThere was an error in attempting to delete user session token");
            ge.printStackTrace();
        }
    }

    /**
     * Determines if the user is authenticated based on the encoded authentication string
     *
     * @param authCredentials
     * @return
     */
    @Override
    public boolean isAuthenticated(String authCredentials) {
        Pair<String,String> userNameId = this.decodeAccessToken(authCredentials);
        return getUserAccessToken(userNameId.getLeft(), userNameId.getRight()).isValid();
    }

    /**
     * Generate a random string.
     *
     * @return
     */
    public String generateAccessToken() {
        return new BigInteger(130, random).toString(32);
    }

    /**
     * Decodes the token received in the Authorization header
     * @param token
     * @return
     */
    public Pair<String,String> decodeAccessToken(String token){
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decodedByteArr = decoder.decode(token);
        String userNameId = new String(decodedByteArr, StandardCharsets.UTF_8);
        final StringTokenizer tokenizer = new StringTokenizer(userNameId, ";");
        final String userName = tokenizer.nextToken();
        final String tokenId = tokenizer.nextToken();
        return new ImmutablePair<>(userName, tokenId);
    }

}
