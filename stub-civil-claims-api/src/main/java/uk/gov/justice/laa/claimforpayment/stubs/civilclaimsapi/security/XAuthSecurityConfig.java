package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import uk.gov.justice.laa.authx.EntraXAuthClaimsExtractor;
import uk.gov.justice.laa.authx.XAuthClaimsExtractor;
import uk.gov.justice.laa.authx.XAuthJwtAuthenticationConverter;

/** Security configuration for X-Auth authentication. */
@SuppressWarnings({
  "checkstyle:MemberNameCheck",
  "checkstyle:ParameterNameCheck",
  "checkstyle:AbbreviationAsWordInName",
  "checkstyle:LocalVariableName",
  "checkstyle:MethodName"
})
@Configuration
public class XAuthSecurityConfig {

  @Bean
  XAuthClaimsExtractor xAuthClaimsExtractor(@Qualifier("xAuth") JwtDecoder xAuthJwtDecoder) {
    return new EntraXAuthClaimsExtractor(xAuthJwtDecoder);
  }

  @Bean
  Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter(
      XAuthClaimsExtractor extractor) {
    JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();

    return new XAuthJwtAuthenticationConverter(delegate, extractor);
  }
}
