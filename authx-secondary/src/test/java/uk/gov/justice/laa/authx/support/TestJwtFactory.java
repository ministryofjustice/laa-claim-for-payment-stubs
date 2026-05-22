package uk.gov.justice.laa.authx.support;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;

/** A factory for creating test JWTs. */
public final class TestJwtFactory {

  private TestJwtFactory() {}

  /**
   * Creates a valid test JWT.
   *
   * @return a valid test JWT
   */
  public static Jwt validJwt() {
    return validJwt("test-user");
  }

  /**
   * Creates a valid test JWT with the specified subject.
   *
   * @param subject the subject of the JWT
   * @return a valid test JWT
   */
  public static Jwt validJwt(String subject) {
    return jwtWithClaims(Map.of("sub", subject));
  }

  /** 
   * Creates a test JWT with the specified claims.
   */
  public static Jwt jwtWithClaims(Map<String, Object> claims) {
    Map<String, Object> finalClaims = new HashMap<>(claims);
    finalClaims.put("scope", "read write");
    return new Jwt(
        "test-token",
        Instant.now().minusSeconds(60),
        Instant.now().plusSeconds(300),
        Map.of("alg", "none"),
        finalClaims);
  }
}
