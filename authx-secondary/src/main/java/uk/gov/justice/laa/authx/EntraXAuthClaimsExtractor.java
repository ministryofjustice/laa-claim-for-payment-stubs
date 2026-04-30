package uk.gov.justice.laa.authx;

import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/** An extractor for extracting claims from an X-Auth token using the Entra JwtDecoder. */
@SuppressWarnings({
  "checkstyle:MemberNameCheck",
  "checkstyle:ParameterNameCheck",
  "checkstyle:AbbreviationAsWordInName",
  "checkstyle:LocalVariableName"
})
public final class EntraXAuthClaimsExtractor implements XAuthClaimsExtractor {

  private final JwtDecoder decoder;

  public EntraXAuthClaimsExtractor(JwtDecoder decoder) {
    this.decoder = decoder;
  }

  @Override
  public Map<String, Object> extractClaims(String tokenValue) {
    Jwt jwt = decoder.decode(tokenValue);
    return jwt.getClaims();
  }
}
