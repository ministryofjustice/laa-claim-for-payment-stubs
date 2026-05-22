package uk.gov.justice.laa.authx;

import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/** A factory for creating Entra JwtDecoders. */
@Slf4j
public final class EntraJwtDecoderFactory {

  private EntraJwtDecoderFactory() {}

  /** Strict validation (issuer + aud + exp). */
  public static JwtDecoder strict(String jwksUri, String issuer, Set<String> allowedAudiences) {

    log.debug("EntraJwtDecoderFactory - Creating string decoder");
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri).build();

    OAuth2TokenValidator<Jwt> validator =
        new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer(issuer),
            new MultiAudienceValidator(allowedAudiences));

    decoder.setJwtValidator(validator);
    return decoder;
  }

  /** X‑Auth validation (issuer + aud, ignores exp). */
  public static JwtDecoder ignoringExpiry(
      String jwksUri, String issuer, Set<String> allowedAudiences) {

    log.debug("EntraJwtDecoderFactory - Creating expiry-ignoring decoder");

    NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri).build();

    OAuth2TokenValidator<Jwt> validator =
        new DelegatingOAuth2TokenValidator<>(
            new JwtIssuerValidator(issuer), new MultiAudienceValidator(allowedAudiences)
            // no JwtTimestampValidator
            );

    decoder.setJwtValidator(validator);
    return decoder;
  }

  /** X‑Auth validation (issuer + aud, ignores exp). */
  public static JwtDecoder ignoringExpiry(
      NimbusJwtDecoder decoder, String issuer, Set<String> audiences) {
    log.debug("EntraJwtDecoderFactory - Creating expiry-ignoring decoder");

    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(
            new JwtIssuerValidator(issuer), new MultiAudienceValidator(audiences)));

    return decoder;
  }
}
