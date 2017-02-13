package dk.magenta.eark.cmis.bridge.authentication;

import dk.magenta.eark.cmis.bridge.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author DarkStar1.
 */
@Provider
public class RestAuthenticationFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationFilter.class);
    private static final String TEST_PATH = "system/check";
    private static final String LOGIN_PATH = "authentication/login";
    public static final String AUTHENTICATION_HEADER = "Authorization";

    @Inject
    private AuthenticationService authenticationService;

    /**
     * Filter method called before a request has been dispatched to a resource.
     * <p/>
     * <p>
     * Filters in the filter chain are ordered according to their {@code javax.annotation.Priority}
     * class-level annotation value.
     * If a request filter produces a response by calling {@link ContainerRequestContext#abortWith}
     * method, the execution of the (either pre-match or post-match) request filter
     * chain is stopped and the response is passed to the corresponding response
     * filter chain (either pre-match or post-match). For example, a pre-match
     * caching filter may produce a response in this way, which would effectively
     * skip any post-match request filters as well as post-match response filters.
     * Note however that a responses produced in this manner would still be processed
     * by the pre-match response filter chain.
     * </p>
     *
     * @param requestContext request context.
     * @throws IOException if an I/O exception occurs.
     * @see PreMatching
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws WebApplicationException {
        final UriInfo reqURI = requestContext.getUriInfo();
        final String relPath = reqURI.getPath();
        //proceed as normal
        if (!LOGIN_PATH.equals(relPath) && !TEST_PATH.equals(relPath)) {
            String authCredentials = this.resolveAuthToken(requestContext);
            if (!authenticationService.isAuthenticated(authCredentials)) {
                JsonObjectBuilder message = Json.createObjectBuilder();
                message.add(Constants.ERRORMSG, "Session expired");
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                        .entity(message)
                        .build());
            }
        }
    }

    /**
     * Checks the request context to see if we have a token in the header or in a query parameter list.
     * Returns null if both conditions are false;
     * @param requestContext
     * @return
     */
    private String resolveAuthToken(ContainerRequestContext requestContext) {
        String authHeaderToken = requestContext.getHeaderString(AUTHENTICATION_HEADER);
        String authQueryToken = requestContext.getUriInfo().getQueryParameters().getFirst("sessionToken");

        if (StringUtils.isNotBlank(authHeaderToken))
            return authHeaderToken;
        else if (StringUtils.isNotBlank(authQueryToken))
            return authQueryToken;
        else return null;
    }
}
