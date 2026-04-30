package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.security;

import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import uk.gov.justice.laa.authx.EntraJwtDecoderFactory;

/** Configuration for the X-Auth JWT decoder in production environments. */
@Configuration
@Profile("!test")
@SuppressWarnings({
  "checkstyle:MemberNameCheck",
  "checkstyle:ParameterNameCheck",
  "checkstyle:AbbreviationAsWordInName",
  "checkstyle:LocalVariableName",
  "checkstyle:MethodName"
})
public class XAuthProdDecoderConfig {

  @Value("${app.security.jwks-uri}")
  private String jwksUri;

  @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
  private String issuerUri;

  @Value("${app.security.allowed-audiences}")
  private String allowedAudiences;

  @Bean
  @Qualifier("xAuth")
  JwtDecoder xAuthJwtDecoder() {
    return EntraJwtDecoderFactory.ignoringExpiry(jwksUri, issuerUri, Set.of(allowedAudiences));
  }

  @Bean
  @Qualifier("accessToken")
  JwtDecoder accessTokenJwtDecoder(OAuth2ResourceServerProperties properties) {
    return NimbusJwtDecoder.withIssuerLocation(properties.getJwt().getIssuerUri()).build();
  }
}
