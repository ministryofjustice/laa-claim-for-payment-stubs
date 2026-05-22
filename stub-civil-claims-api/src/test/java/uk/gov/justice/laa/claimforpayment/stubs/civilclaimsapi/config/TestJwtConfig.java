package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.support.TestJwtKeys;

/** Test configuration for JWT encoding and decoding. */
@TestConfiguration
@Profile("test")
@SuppressWarnings({
  "checkstyle:MethodName"
})
public class TestJwtConfig {

  @Bean
  RSAKey testRsaJwk() {
    return TestJwtKeys.rsa();
  }

  @Bean
  JwtEncoder jwtEncoder(RSAKey testRsaJwk) {
    return new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(new JWKSet(testRsaJwk)));
  }

  @Bean
  @Qualifier("accessToken")
  JwtDecoder jwtDecoder(RSAKey testRsaJwk) throws JOSEException {

    return NimbusJwtDecoder.withPublicKey(testRsaJwk.toRSAPublicKey()).build();
  }

  @Bean
  @Qualifier("xAuth")
  JwtDecoder xAuthJwtDecoder(RSAKey testRsaJwk) throws JOSEException {
    return NimbusJwtDecoder.withPublicKey(testRsaJwk.toRSAPublicKey()).build();
  }
}
