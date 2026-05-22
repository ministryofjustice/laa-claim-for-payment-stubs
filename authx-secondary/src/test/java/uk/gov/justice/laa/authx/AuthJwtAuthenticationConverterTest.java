package uk.gov.justice.laa.authx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.justice.laa.authx.support.TestJwtFactory;

/** Tests for {@link XAuthJwtAuthenticationConverter}. */
@SuppressWarnings({"checkstyle:AbbreviationAsWordInName", "checkstyle:LocalVariableName"})
public class AuthJwtAuthenticationConverterTest {

  @BeforeEach
  void setup() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Auth", "x-auth-token");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  @AfterEach
  void clearRequestContext() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void convert_withoutXAuthHeader_returnsStandardAuthentication() {
    RequestContextHolder.resetRequestAttributes();
    Jwt jwt = TestJwtFactory.validJwt();

    XAuthClaimsExtractor extractor = mock(XAuthClaimsExtractor.class);

    Converter<Jwt, AbstractAuthenticationToken> delegate =
        j -> new JwtAuthenticationToken(j, List.of());

    XAuthJwtAuthenticationConverter converter =
        new XAuthJwtAuthenticationConverter(delegate, extractor);

    AbstractAuthenticationToken auth = converter.convert(jwt);

    assertThat(auth).isInstanceOf(JwtAuthenticationToken.class);
    assertThat(auth.getDetails()).isNull();
    verifyNoInteractions(extractor);
  }

  @Test
  void convert_baseJwtClaimsAlwaysTakePrecedence() {
    Jwt baseJwt =
        TestJwtFactory.jwtWithClaims(Map.of("tenant", "access-token-tenant", "oid", "user-123"));

    Converter<Jwt, AbstractAuthenticationToken> delegate =
        jwt -> new JwtAuthenticationToken(jwt, List.of());

    XAuthClaimsExtractor extractor = mock(XAuthClaimsExtractor.class);
    when(extractor.extractClaims("x-auth-token"))
        .thenReturn(Map.of("tenant", "x-auth-tenant", "oid", "user-123"));

    JwtAuthenticationToken result =
        (JwtAuthenticationToken)
            new XAuthJwtAuthenticationConverter(delegate, extractor).convert(baseJwt);

    assertThat(result.getToken().getClaims())
        .containsEntry("tenant", "access-token-tenant")
        .doesNotContainEntry("tenant", "x-auth-tenant");
  }

  @Test
  void convert_nonMatchingOidReturnsStandardAuthentication() {
    Jwt baseJwt = TestJwtFactory.jwtWithClaims(Map.of("sub", "user-123", "oid", "base-oid"));

    Converter<Jwt, AbstractAuthenticationToken> delegate =
        jwt -> new JwtAuthenticationToken(jwt, List.of());

    XAuthClaimsExtractor extractor = mock(XAuthClaimsExtractor.class);
    when(extractor.extractClaims("x-auth-token"))
        .thenReturn(Map.of("FIRM_NAME", "SOME_FIRM", "oid", "x-auth-oid"));

    JwtAuthenticationToken result =
        (JwtAuthenticationToken)
            new XAuthJwtAuthenticationConverter(delegate, extractor).convert(baseJwt);

    assertThat(result.getToken().getClaims())
        .containsEntry("oid", "base-oid")
        .doesNotContainEntry("oid", "x-auth-oid")
        .doesNotContainEntry("FIRM_NAME", "SOME_FIRM");
  }

  @Test
  void convert_ignoresDisallowedXAuthClaims() {
    Jwt baseJwt = TestJwtFactory.jwtWithClaims(Map.of("oid", "user-123"));

    Converter<Jwt, AbstractAuthenticationToken> delegate =
        jwt -> new JwtAuthenticationToken(jwt, List.of());

    Map<String, Object> xAuthClaims =
        Map.of(
            "USER_EMAIL", "user@example.com",
            "aud", "some-audience",
            "oid", "user-123",
            "FIRM_NAME", "Some Firm");

    XAuthClaimsExtractor extractor = mock(XAuthClaimsExtractor.class);
    when(extractor.extractClaims("x-auth-token")).thenReturn(xAuthClaims);

    XAuthJwtAuthenticationConverter converter =
        new XAuthJwtAuthenticationConverter(delegate, extractor);

    JwtAuthenticationToken result = (JwtAuthenticationToken) converter.convert(baseJwt);

    Map<String, Object> claims = result.getToken().getClaims();

    assertThat(claims)
        .containsEntry("FIRM_NAME", "Some Firm")
        .containsEntry("USER_EMAIL", "user@example.com")
        .containsEntry("oid", "user-123") // from base JWT
        .doesNotContainKey("aud");
  }
}
