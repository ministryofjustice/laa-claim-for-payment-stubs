package uk.gov.justice.laa.authx;

import java.util.Collection;
import java.util.Set;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * A validator for checking if a JWT has an allowed audience.
 */
public final class MultiAudienceValidator implements OAuth2TokenValidator<Jwt> {

  private final Set<String> allowedAudiences;

  public MultiAudienceValidator(Set<String> allowedAudiences) {
    this.allowedAudiences = Set.copyOf(allowedAudiences);
  }

  @Override
  public OAuth2TokenValidatorResult validate(Jwt jwt) {

    Collection<String> tokenAudiences = jwt.getAudience();

    if (tokenAudiences == null || tokenAudiences.isEmpty()) {
      return OAuth2TokenValidatorResult.failure(
          new OAuth2Error("invalid_token", "Missing aud claim", null));
    }

    boolean anyMatch = tokenAudiences.stream().anyMatch(allowedAudiences::contains);

    return anyMatch
        ? OAuth2TokenValidatorResult.success()
        : OAuth2TokenValidatorResult.failure(
            new OAuth2Error("invalid_token", "Token audience not allowed", null));
  }
}
