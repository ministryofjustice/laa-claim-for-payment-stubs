package uk.gov.justice.laa.stubs.oidcserver.config;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

/** An authentication token for the JWT Bearer grant type. */
public class Oauth2JwtBearerAuthenticationToken
    extends OAuth2AuthorizationGrantAuthenticationToken {
  private final Set<String> scopes;

  /**
   * Creates a new OAuth2JwtBearerAuthenticationToken instance.
   *
   * @param clientPrincipal the client principal
   * @param assertion the JWT assertion
   * @param scopes the requested scopes
   */
  public Oauth2JwtBearerAuthenticationToken(
      Authentication clientPrincipal, String assertion, Set<String> scopes) {
    super(
        new AuthorizationGrantType("urn:ietf:params:oauth:grant-type:jwt-bearer"),
        clientPrincipal,
        Map.of("assertion", assertion, "scope", scopes != null ? scopes : Collections.emptySet()));
    this.scopes = Collections.unmodifiableSet(scopes != null ? scopes : Collections.emptySet());
  }

  public Set<String> getScopes() {
    return this.scopes;
  }

  public String getAssertion() {
    return (String) getAdditionalParameters().get("assertion");
  }
}
