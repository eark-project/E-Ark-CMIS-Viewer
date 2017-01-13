package dk.magenta.eark.cmis.bridge.authentication;

import java.util.Date;

/**
 * POJO to hold access token about an individual login session
 * access token bound to a userName.
 * The first parameter in the constructor is not needed now as we're implementing the 'one user
 * session at any one time' approach; but it's implemented for flexibility in future use (like allow same user to logon
 * from multiple locations)
 */
public class AccessToken {
    private Date issuedAt;
    private Date expiresAt;
    private String tokenId;
    private String userName;

    public AccessToken(String tokenId, Date expiresAt, String userName) {
        this.tokenId = tokenId;
        this.issuedAt = new Date();
        this.expiresAt = expiresAt;
        this.userName = userName;
    }

    /**
     * Return whether the access token has been issued and not expired at
     * the current time.
     *
     * @return
     */
    public boolean isValid() {
        return isValid(new Date());
    }

    /**
     * Return whether the access token is valid for the given date.
     * @return
     */
    public boolean isValid(Date when) {
        return when.after(issuedAt) && when.before(expiresAt);
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
}
