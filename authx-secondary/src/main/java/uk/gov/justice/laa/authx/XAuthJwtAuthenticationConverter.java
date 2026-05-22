package uk.gov.justice.laa.authx;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * A converter that enriches JWT authentication tokens with additional claims from a second token.
 */
@SuppressWarnings({
  "checkstyle:MemberNameCheck",
  "checkstyle:ParameterNameCheck",
  "checkstyle:AbbreviationAsWordInName",
  "checkstyle:LocalVariableName"
})
@Slf4j
public class XAuthJwtAuthenticationConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {
  private static final String X_AUTH_HEADER = "X-Auth";
  private final Converter<Jwt, ? extends AbstractAuthenticationToken> delegate;

  private final XAuthClaimsExtractor claimsExtractor;
  private final Set<String> allowedXAuthClaims =
      Set.of("FIRM_CODE", "FIRM_NAME", "LAA_ACCOUNTS", "LAA_APP_ROLES", "USER_EMAIL", "USER_NAME");

  /** Creates a new instance of the converter. */
  public XAuthJwtAuthenticationConverter(
      Converter<Jwt, ? extends AbstractAuthenticationToken> delegate,
      XAuthClaimsExtractor xAuthClaimsExtractor) {
    this.delegate = delegate;
    this.claimsExtractor = xAuthClaimsExtractor;
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {

    AbstractAuthenticationToken base = delegate.convert(jwt);

    if (!(base instanceof JwtAuthenticationToken jwtAuth)) {
      log.debug(
          "Delegate converter did not return a JwtAuthenticationToken, skipping X-Auth enrichment");
      return base;
    }

    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    if (attrs == null) {
      log.debug("No request attributes available, skipping X-Auth enrichment");
      return base;
    }

    String xAuth = attrs.getRequest().getHeader(X_AUTH_HEADER);
    if (xAuth == null || xAuth.isBlank()) {
      log.debug("X-Auth header is missing or blank, skipping X-Auth enrichment");
      return base;
    }

    Map<String, Object> extraClaims;
    try {
      extraClaims = claimsExtractor.extractClaims(xAuth);
    } catch (Exception e) {
      log.debug("Failed to extract claims from X-Auth header", e);
      // fail-soft by design
      return base;
    }

    if (!jwt.getClaimAsString("oid").equals(extraClaims.get("oid"))) {
      log.debug(
          "OID claim in X-Auth does not match OID in access token, skipping X-Auth enrichment");
      return base;
    }

    if (extraClaims == null || extraClaims.isEmpty()) {
      log.debug("Extracted claims are null or empty, skipping X-Auth enrichment");
      return base;
    }

    log.debug("###################################################");
    log.debug(
        "Original Access Token:\n" + extractTokenDeleteMe(jwtAuth.getToken().getTokenValue()));
    log.debug("X-Auth Token:\n" + extractTokenDeleteMe(xAuth));
    log.debug("Extracted claims from X-Auth:" + extraClaims.toString());
    log.debug("###################################################");

    Jwt mergedJwt = mergeClaims(jwtAuth.getToken(), extraClaims);

    return new JwtAuthenticationToken(mergedJwt, jwtAuth.getAuthorities(), jwtAuth.getName());
  }

  private Jwt mergeClaims(Jwt baseJwt, Map<String, Object> extraClaims) {
    Map<String, Object> mergedClaims = new HashMap<>(baseJwt.getClaims());

    // Explicit rule: base token claims always win
    extraClaims.forEach(
        (key, value) -> {
          if (allowedXAuthClaims.contains(key) && !mergedClaims.containsKey(key)) {
            mergedClaims.put(key, value);
          }
        });

    return new Jwt(
        baseJwt.getTokenValue(), // same token value
        baseJwt.getIssuedAt(),
        baseJwt.getExpiresAt(),
        baseJwt.getHeaders(),
        Map.copyOf(mergedClaims));
  }

  private String extractTokenDeleteMe(String token) {
    String[] parts = token.split("\\.");
    if (parts.length != 3) {
      return "Invalid token format, cannot extract payload";
    }
    byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
    String payloadJson = new String(decoded, StandardCharsets.UTF_8);

    JSONObject obj = new JSONObject(payloadJson);

    return obj.toString(2);
  }
}
