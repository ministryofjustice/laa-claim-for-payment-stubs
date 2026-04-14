package uk.gov.justice.laa.stubs.oidcserver.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.laa.stubs.oidcserver.model.TestUser;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class OidcServerConfigTest {

  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(OidcServerConfigTest.class);

  @Autowired private MockMvc mockMvc;

  @Autowired private RegisteredClientRepository registeredClientRepository;

  @Autowired private JwtDecoder jwtDecoder;

  @Nested
  class TokenRequestTests {

    @Test
    @DisplayName("Front-end client should not be able to request scope for arbitrary API")
    void frontEndClientCannotRequestApiToken() throws Exception {

      RegisteredClient client = registeredClientRepository.findByClientId("caa-client");
      String someOtherApiScope = "https://admin-api/claims_write";
      assertThrows(Exception.class, () -> authenticateAndReturnCode(client, someOtherApiScope));
    }

    @Test
    @DisplayName("Front-end client should be able to request scope for claims API")
    void frontEndClientCanRequestClaimsApiToken() throws Exception {

      RegisteredClient client = registeredClientRepository.findByClientId("caa-client");
      String apiScope = "https://claims-api/claims_write";
      String code = authenticateAndReturnCode(client, apiScope);

      MvcResult result =
          mockMvc
              .perform(
                  post("/oauth2/token")
                      .param("grant_type", "authorization_code")
                      .param("code", code)
                      .param("client_id", "caa-client")
                      .param("client_secret", "caa-mock-secret")
                      .param("scope", apiScope)
                      .param("redirect_uri", "http://localhost:3000/callback"))
              .andExpect(jsonPath("$.access_token").exists())
              .andExpect(status().isOk())
              .andReturn();

      String responseBody = result.getResponse().getContentAsString();
      String accessToken = JsonPath.read(responseBody, "$.access_token");

      Jwt jwt = jwtDecoder.decode(accessToken);

      assertThat(jwt.getAudience()).contains("https://claims-api");
      assertThat(jwt.getClaimAsStringList("scope")).contains("https://claims-api/claims_write");
    }

    @Test
    @DisplayName(
        "Claims API client should be able to exchange access token for downstream access token"
            + " scopes")
    void apiClientCanExchangeToken() throws Exception {
      RegisteredClient client = registeredClientRepository.findByClientId("caa-client");
      String apiScope = "https://claims-api/claims_write";
      String code = authenticateAndReturnCode(client, apiScope);
      MvcResult result =
          mockMvc
              .perform(
                  post("/oauth2/token")
                      .param("grant_type", "authorization_code")
                      .param("code", code)
                      .param("client_id", "caa-client")
                      .param("client_secret", "caa-mock-secret")
                      .param("scope", apiScope)
                      .param("redirect_uri", "http://localhost:3000/callback"))
              .andExpect(jsonPath("$.access_token").exists())
              .andExpect(status().isOk())
              .andReturn();

      String responseBody = result.getResponse().getContentAsString();
      String accessToken = JsonPath.read(responseBody, "$.access_token");

      MvcResult oboResult =
          mockMvc
              .perform(
                  post("/oauth2/token")
                      .contentType(
                          org.springframework.http.MediaType
                              .APPLICATION_FORM_URLENCODED) 
                      .param("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                      .param("assertion", accessToken) 
                      .param("client_id", "https://claims-api") 
                      .param("client_secret", "api-mock-secret")
                      .param("scope", "https://downstream-api/data_read")
                      .param("requested_token_use", "on_behalf_of"))
              .andExpect(status().isOk())
              .andDo(print())
              .andReturn();

      String exchangeResponseBody = oboResult.getResponse().getContentAsString();
      String downstreamToken = JsonPath.read(exchangeResponseBody, "$.access_token");

      Jwt decodedJwt = jwtDecoder.decode(downstreamToken);


      assertThat(decodedJwt.getSubject()).isEqualTo("alice"); // Identity is preserved
      assertThat(decodedJwt.getAudience())
          .contains("https://downstream-api"); // Audience has shifted
    }

    private String authenticateAndReturnCode(RegisteredClient client, String apiScope)
        throws Exception {

      String redirect = client.getRedirectUris().iterator().next();
      MvcResult authoriseResult =
          mockMvc
              .perform(
                  get("/oauth2/authorize")
                      .queryParam("response_type", "code")
                      .queryParam("client_id", client.getClientId())
                      .queryParam("redirect_uri", redirect)
                      .queryParam("scope", "openid " + apiScope)
                      .queryParam("state", "state"))
              .andExpect(status().is3xxRedirection())
              .andReturn();

      MockHttpSession session = (MockHttpSession) authoriseResult.getRequest().getSession(false);

      MvcResult loginResult =
          mockMvc
              .perform(
                  post("/login")
                      .session(session)
                      .param("username", "alice")
                      .param("password", "password")
                      .with(csrf()))
              .andExpect(status().is3xxRedirection())
              .andReturn();

      String continueUrl = loginResult.getResponse().getHeader("Location");

      MvcResult finalResult =
          mockMvc
              .perform(get(URI.create(continueUrl)).session(session).accept(MediaType.TEXT_HTML))
              .andReturn();

      String redirectedUrl = finalResult.getResponse().getHeader("Location");

      return UriComponentsBuilder.fromUriString(redirectedUrl)
          .build()
          .getQueryParams()
          .getFirst("code");
    }
  }

  @Nested
  class CustomizerTests {

    private OAuth2TokenCustomizer<JwtEncodingContext> customizer;

    @BeforeEach
    void setUp() {
      OidcServerConfig config = new OidcServerConfig();
      Map<String, TestUser> profiles =
          Map.of(
              USER_1.username(), USER_1,
              USER_2.username(), USER_2);
      this.customizer = config.tokenCustomizer(profiles);
    }

    @Test
    void customizerDoesNotAddClaimsForUnknownUser() {
      TestUser user =
          new TestUser(
              "jane",
              "Jane Doe",
              "jane.doe@example.test",
              "prov-789",
              UUID.fromString("f0a6917d-40e3-4308-8754-352bbb74b271"),
              "password",
              List.of("role3"));

      JwtEncodingContext context =
          buildContext(user, new OAuth2TokenType(OidcParameterNames.ID_TOKEN));

      customizer.customize(context);

      Map<String, Object> claims = context.getClaims().build().getClaims();

      assertThat(claims.get("sub")).isEqualTo(user.username());
      assertThat(claims)
          .doesNotContainKeys(
              "FIRM_CODE", "USER_NAME", "roles", "email", "name", "preferred_username", "aud");
    }

    @ParameterizedTest
    @MethodSource("users")
    void idTokenCustomizerAddsClaims(TestUser user) {
      JwtEncodingContext context =
          buildContext(user, new OAuth2TokenType(OidcParameterNames.ID_TOKEN));

      customizer.customize(context);

      Map<String, Object> claims = context.getClaims().build().getClaims();

      assertThat(claims.get("sub")).isEqualTo(user.username());
      assertThat(claims.get("FIRM_CODE")).isEqualTo(user.firmId());
      assertThat((claims.get("USER_NAME"))).isEqualTo(user.providerUserId());
      assertThat(claims.get("roles")).isEqualTo(user.roles());
      assertThat(claims.get("email")).isEqualTo(user.email());
      assertThat(claims.get("name")).isEqualTo(user.displayName());
      assertThat(claims.get("preferred_username")).isEqualTo(user.username());
      assertThat(claims).doesNotContainKeys("aud");
    }

    @ParameterizedTest
    @MethodSource("users")
    void accessTokenCustomizerAddsClaims(TestUser user) {
      JwtEncodingContext context = buildContext(user, OAuth2TokenType.ACCESS_TOKEN);

      customizer.customize(context);

      Map<String, Object> claims = context.getClaims().build().getClaims();

      assertThat(claims.get("sub")).isEqualTo(user.username());
      assertThat(claims.get("FIRM_CODE")).isEqualTo(user.firmId());
      assertThat((claims.get("USER_NAME"))).isEqualTo(user.providerUserId());
      assertThat(claims.get("roles")).isEqualTo(user.roles());
      assertThat(claims).doesNotContainKeys("email", "name", "preferred_username");
      assertThat(claims.get("aud")).isEqualTo(List.of("https://claims-api"));
    }

    private JwtEncodingContext buildContext(TestUser user, OAuth2TokenType tokenType) {
      List<SimpleGrantedAuthority> authorities =
          user.roles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();

      Authentication principal =
          new UsernamePasswordAuthenticationToken(user.username(), user.password(), authorities);

      JwsHeader.Builder jwsHeaderBuilder = JwsHeader.with(SignatureAlgorithm.RS256);

      JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder().subject(user.username());
      Set<String> authorizedScopes = Set.of("openid", "profile", "https://claims-api/claims_write");

      return JwtEncodingContext.with(jwsHeaderBuilder, claimsBuilder)
          .principal(principal)
          .tokenType(tokenType)
          .authorizedScopes(authorizedScopes)
          .build();
    }

    private static Stream<Arguments> users() {
      return Stream.of(Arguments.of(USER_1), Arguments.of(USER_2));
    }
  }

  @Nested
  class JWKSourceTests {

    private JWKSource<SecurityContext> jwkSource;

    @BeforeEach
    void setUp() {
      OidcServerConfig config = new OidcServerConfig();
      this.jwkSource = config.jwkSource();
    }

    @Test
    void jwkSourceReturnsJwkWithCorrectKeyId() throws JOSEException {
      JWKSelector selector = new JWKSelector(new JWKMatcher.Builder().keyID("mock-rsa").build());

      List<JWK> jwks = jwkSource.get(selector, null);

      assertThat(jwks).isNotNull();
      assertThat(jwks).hasSize(1);

      RSAKey rsaKey = (RSAKey) jwks.get(0);
      assertThat(rsaKey.getKeyID()).isEqualTo("mock-rsa");
      assertThat(rsaKey.toRSAPublicKey()).isNotNull();
      assertThat(rsaKey.toRSAPrivateKey()).isNotNull();
    }
  }

  @Nested
  class UserDetailsServiceTest {

    private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
      OidcServerConfig config = new OidcServerConfig();
      Map<String, TestUser> profiles =
          Map.of(
              USER_1.username(), USER_1,
              USER_2.username(), USER_2);
      PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
      when(passwordEncoder.encode(any())).thenReturn("ENC_password");
      this.userDetailsService = config.users(profiles, passwordEncoder);
    }

    @ParameterizedTest
    @MethodSource("users")
    void testTestProfilesBean(TestUser user) {
      UserDetails result = userDetailsService.loadUserByUsername(user.username());

      Set<SimpleGrantedAuthority> authorities =
          user.roles().stream()
              .map(x -> new SimpleGrantedAuthority("ROLE_" + x))
              .collect(Collectors.toSet());

      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo(user.username());
      assertThat(result.getPassword()).isEqualTo("ENC_password");
      assertThat(result.getAuthorities()).isEqualTo(authorities);
    }

    private static Stream<Arguments> users() {
      return Stream.of(Arguments.of(USER_1), Arguments.of(USER_2));
    }
  }

  @Nested
  class PasswordEncoderTests {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
      OidcServerConfig config = new OidcServerConfig();
      this.passwordEncoder = config.passwordEncoder();
    }

    @Test
    void testAuthorizationServerSettingsArePresent() {
      assertThat(passwordEncoder).isInstanceOf(DelegatingPasswordEncoder.class);
    }
  }

  private static final TestUser USER_1 =
      new TestUser(
          "joe",
          "Joe Bloggs",
          "joe.bloggs@example.test",
          "prov-123",
          UUID.fromString("1faf90d6-e969-4d8e-beba-0e081ea62c60"),
          "password",
          List.of("role1"));

  private static final TestUser USER_2 =
      new TestUser(
          "john",
          "John Doe",
          "john.doe@example.test",
          "prov-456",
          UUID.fromString("b669b893-270f-4242-abec-96494da2ebf9"),
          "password",
          List.of("role2"));
}
