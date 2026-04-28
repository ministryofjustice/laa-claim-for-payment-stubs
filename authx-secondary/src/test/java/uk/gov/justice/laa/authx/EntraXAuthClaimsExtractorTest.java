package uk.gov.justice.laa.authx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import uk.gov.justice.laa.authx.support.TestJwtBuilder;

/** Tests for {@link EntraXAuthClaimsExtractor}. */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class EntraXAuthClaimsExtractorTest {

  NimbusJwtDecoder testDecoder =
      NimbusJwtDecoder.withPublicKey((RSAPublicKey) TestJwtBuilder.getKeyPair().getPublic())
          .build();

  @Test
  void extractClaims_validSignedJwt_returnsClaims() {
    String jwt =
        TestJwtBuilder.withIssuer("https://issuer.test")
            .withClaim("tenant", "abc")
            .withClaim("aud", "http:/some-api")
            .signed();

    JwtDecoder decoder =
        EntraJwtDecoderFactory.ignoringExpiry(
            testDecoder, "https://issuer.test", Set.of("api://test", "http:/some-api"));

    XAuthClaimsExtractor extractor = new EntraXAuthClaimsExtractor(decoder);

    Map<String, Object> claims = extractor.extractClaims(jwt);

    assertThat(claims).containsEntry("tenant", "abc").containsEntry("iss", "https://issuer.test");
  }

  @Test
  void extractClaims_expiredJwt_isAccepted() {
    TestJwtBuilder.builder();
    String jwt =
        TestJwtBuilder
            .withIssuer("https://issuer.test")
            .withAudience("api://service-a")
            .withClaim("tenant", "abc")
            .expired()
            .signed();

    JwtDecoder decoder =
        EntraJwtDecoderFactory.ignoringExpiry(
            testDecoder, "https://issuer.test", Set.of("api://service-a"));

    XAuthClaimsExtractor extractor = new EntraXAuthClaimsExtractor(decoder);

    Map<String, Object> claims = extractor.extractClaims(jwt);

    assertThat(claims).containsEntry("tenant", "abc");
  }

  @Test
  void extractClaims_invalidSignature_throwsJwtException() {
    TestJwtBuilder.builder();
    String jwtSignedWithWrongKey =
        TestJwtBuilder
            .withIssuer("https://issuer.test")
            .withAudience("api://service-a")
            .signedWithDifferentKey();

    JwtDecoder decoder =
        EntraJwtDecoderFactory.ignoringExpiry(
            testDecoder, "https://issuer.test", Set.of("api://service-a"));

    XAuthClaimsExtractor extractor = new EntraXAuthClaimsExtractor(decoder);

    assertThatThrownBy(() -> extractor.extractClaims(jwtSignedWithWrongKey))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void extractClaims_wrongIssuer_throwsJwtException() {
    TestJwtBuilder.builder();
    String jwt =
        TestJwtBuilder
            .withIssuer("https://evil.example.com")
            .withAudience("api://service-a")
            .signed();

    JwtDecoder decoder =
        EntraJwtDecoderFactory.ignoringExpiry(
            testDecoder, "https://issuer.test", Set.of("api://service-a"));

    XAuthClaimsExtractor extractor = new EntraXAuthClaimsExtractor(decoder);

    assertThatThrownBy(() -> extractor.extractClaims(jwt)).isInstanceOf(JwtException.class);
  }

  @Test
  void extractClaims_anyAllowedAudience_isAccepted() {
    TestJwtBuilder.builder();
    String jwt =
        TestJwtBuilder
            .withIssuer("https://issuer.test")
            .withAudience("api://service-b")
            .withClaim("tenant", "abc")
            .signed();

    JwtDecoder decoder =
        EntraJwtDecoderFactory.ignoringExpiry(
            testDecoder, "https://issuer.test", Set.of("api://service-a", "api://service-b"));

    XAuthClaimsExtractor extractor = new EntraXAuthClaimsExtractor(decoder);

    Map<String, Object> claims = extractor.extractClaims(jwt);

    assertThat(claims).containsEntry("tenant", "abc");
  }

  @Test
  void extractClaims_disallowedAudience_throwsJwtException() {
    TestJwtBuilder.builder();
    String jwt =
        TestJwtBuilder
            .withIssuer("https://issuer.test")
            .withAudience("api://evil")
            .signed();

    JwtDecoder decoder =
        EntraJwtDecoderFactory.ignoringExpiry(
            testDecoder, "https://issuer.test", Set.of("api://service-a"));

    XAuthClaimsExtractor extractor = new EntraXAuthClaimsExtractor(decoder);

    assertThatThrownBy(() -> extractor.extractClaims(jwt)).isInstanceOf(JwtException.class);
  }
}
