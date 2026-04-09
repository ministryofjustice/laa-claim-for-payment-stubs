package uk.gov.justice.laa.stubs.oidcserver.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import uk.gov.justice.laa.stubs.oidcserver.model.TestUser;

/**
 * Configuration for the mock OIDC server, including security chains, registered clients, JWK
 * source, test users, and token customizer.
 */
@Configuration
@EnableWebSecurity
public class OidcServerConfig {

  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(OidcServerConfig.class);

  // ----- configurable bits (override in application.yml if you like) -----
  @Value("${auth.mock.issuer:http://localhost:8091}")
  private String issuer;

  @Value("${auth.mock.redirect-cfe:http://localhost:3000/callback}")
  private String cfeRedirect;

  @Value("${auth.mock.logout-cfe:http://localhost:3000}")
  private String cfeLogout;

  @Value("${auth.mock.redirect-afe:http://localhost:3001/callback}")
  private String afeRedirect;

  @Value("${auth.mock.logout-afe:http://localhost:3001}")
  private String afeLogout;

  @Value("${stub-oidc-server.client-secret:mock-secret}")
  private String clientSecret;

  private String claimsApiScope = "https://claims-api/claims_write";

  @Bean
  OAuth2AuthorizationService authorizationService(RegisteredClientRepository clients) {
    return new InMemoryOAuth2AuthorizationService();
  }

  @Bean
  RequestCache requestCache() {
    return new HttpSessionRequestCache();
  }

  /** Authorisation Server endpoints (discovery, authorize, token, jwks, userinfo). */
  @Bean
  @Order(1)
  public SecurityFilterChain authorizationServer(HttpSecurity http, Map<String, TestUser> profiles)
      throws Exception {

    OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
        new OAuth2AuthorizationServerConfigurer();

    http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
        .securityContext(
            context ->
                context.securityContextRepository(new HttpSessionSecurityContextRepository()))
        .requestCache(c -> c.requestCache(requestCache()))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .exceptionHandling(
            ex -> ex.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(
            csrf ->
                csrf.ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()))
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .apply(authorizationServerConfigurer);

    // Customize OIDC userinfo endpoint mapping inside the authorizationServerConfigurer
    authorizationServerConfigurer.oidc(
        oidc ->
            oidc.userInfoEndpoint(
                userInfo ->
                    userInfo.userInfoMapper(
                        ctx -> {
                          String sub = ctx.getAuthorization().getPrincipalName();
                          TestUser user = profiles.get(sub);
                          Map<String, Object> claims = new HashMap<>();
                          claims.put("sub", sub);
                          if (user != null) {
                            claims.put("name", user.displayName());
                            claims.put("preferred_username", user.username());
                            claims.put("email", user.email());
                            claims.put("FIRM_CODE", user.firmId());
                          }
                          return new OidcUserInfo(claims);
                        })));

    return http.build();
  }

  /** App web security (serves login page, etc.). */
  @Bean
  @Order(2)
  SecurityFilterChain application(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/login", "/error", "/css/**", "/js/**", "/actuator/**", "/favicon.ico")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .requestCache(c -> c.requestCache(requestCache()))
        .formLogin(Customizer.withDefaults());

    return http.build();
  }

  @Bean
  @Profile("!test")
  RegisteredClientRepository registeredClientRepository(PasswordEncoder encoder) {
    log.debug("####### using non-test client repo");
    RegisteredClient claimFrontEndClient =
        buildRegisteredClient(
            "caa-client",
            "mock-secret",
            encoder,
            AuthorizationGrantType.AUTHORIZATION_CODE,
            cfeRedirect,
            List.of(OidcScopes.OPENID, OidcScopes.PROFILE, OidcScopes.EMAIL, claimsApiScope),
            false);

    RegisteredClient assessFrontEndClient =
        buildRegisteredClient(
            "afe-client",
            "mock-secret",
            encoder,
            AuthorizationGrantType.AUTHORIZATION_CODE,
            afeRedirect,
            List.of(OidcScopes.OPENID, OidcScopes.PROFILE, OidcScopes.EMAIL, claimsApiScope),
            false);

    return new InMemoryRegisteredClientRepository(claimFrontEndClient, assessFrontEndClient);
  }

  @Bean
  @Profile("test")
  RegisteredClientRepository testRegisteredClientRepository(PasswordEncoder encoder) {
    log.debug("####### using TEST client repo");

    RegisteredClient claimFrontEndClient =
        buildRegisteredClient(
            "caa-client",
            "mock-secret",
            encoder,
            AuthorizationGrantType.AUTHORIZATION_CODE,
            cfeRedirect,
            List.of(OidcScopes.OPENID, OidcScopes.PROFILE, OidcScopes.EMAIL, claimsApiScope),
            true);

    RegisteredClient assessFrontEndClient =
        buildRegisteredClient(
            "afe-client",
            "mock-secret",
            encoder,
            AuthorizationGrantType.AUTHORIZATION_CODE,
            afeRedirect,
            List.of(OidcScopes.OPENID, OidcScopes.PROFILE, OidcScopes.EMAIL, claimsApiScope),
            true);

    log.debug(
        "Test client repo loaded, caa-client requireProofKey={}",
        claimFrontEndClient.getClientSettings().isRequireProofKey());

    return new InMemoryRegisteredClientRepository(claimFrontEndClient, assessFrontEndClient);
  }

  @Bean
  AuthorizationServerSettings authorizationServerSettings() {
    return AuthorizationServerSettings.builder().issuer(issuer).build();
  }

  /** JWK for signing tokens; JWKS exposed automatically at /oauth2/jwks. */
  @Bean
  JWKSource<SecurityContext> jwkSource() {
    try {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
      kpg.initialize(2048);
      KeyPair kp = kpg.generateKeyPair();
      RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
      RSAPrivateKey priv = (RSAPrivateKey) kp.getPrivate();

      RSAKey jwk = new RSAKey.Builder(pub).privateKey(priv).keyID("mock-rsa").build();
      JWKSet jwkSet = new JWKSet(jwk);
      // The encoder will propagate the JWK's kid into the JOSE header automatically.
      return (selector, ctx) -> selector.select(jwkSet);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to create RSA JWK", e);
    }
  }

  /** In-memory users for login form (alice/bob : password). */
  @Bean
  UserDetailsService users(Map<String, TestUser> profiles, PasswordEncoder encoder) {
    List<UserDetails> users =
        profiles.values().stream().map(x -> x.toUserDetails(encoder)).toList();
    return new InMemoryUserDetailsManager(users);
  }

  /** Extra profile data surfaced in tokens and /userinfo. */
  @Bean
  Map<String, TestUser> testProfiles(ExternalConfig config) {
    return config.getUsers().stream().collect(Collectors.toMap(TestUser::username, user -> user));
  }

  /** Add Entra-style + custom claims. */
  @Bean
  OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(Map<String, TestUser> profiles) {
    return ctx -> {
      Set<String> authorizedScopes = ctx.getAuthorizedScopes();

      List<String> audiences =
          authorizedScopes.stream()
              .filter(
                  scope ->
                      scope.contains(
                          "/")) // crude way to identify "resource scopes" like "api/resource.read"
              .map(
                  scope -> {
                    int lastSlash = scope.lastIndexOf("/");
                    return scope.substring(0, lastSlash);
                  })
              .distinct()
              .collect(Collectors.toList());

      TestUser u = profiles.get(ctx.getPrincipal().getName());
      if (u != null) {
        var roles =
            ((Authentication) ctx.getPrincipal())
                .getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> a.startsWith("ROLE_"))
                    .map(a -> a.substring(5))
                    .toList();

        // ID token: rich identity claims
        if (OidcParameterNames.ID_TOKEN.equals(ctx.getTokenType().getValue())) {
          ctx.getClaims()
              .claim("name", u.displayName())
              .claim("preferred_username", u.username())
              .claim("email", u.email())
              .claim("FIRM_CODE", u.firmId())
              .claim("USER_NAME", u.providerUserId())
              .claim("roles", roles);
        }

        // Access token: include providerId so API can authorise with it
        if (OAuth2TokenType.ACCESS_TOKEN.equals(ctx.getTokenType())) {
          ctx.getClaims()
              .audience(audiences)
              .claim("FIRM_CODE", u.firmId())
              .claim("USER_NAME", u.providerUserId())
              .claim("roles", roles);
        }
      }
    };
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
    return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
  }

  private RegisteredClient buildRegisteredClient(
      String clientId,
      String secret,
      PasswordEncoder encoder,
      AuthorizationGrantType grantType,
      String redirectUri,
      List<String> scopes,
      boolean disablePkceInTest) {

    ClientSettings clientSettings =
        ClientSettings.builder()
            .requireProofKey(
                grantType == AuthorizationGrantType.AUTHORIZATION_CODE && !disablePkceInTest)
            .build();
    RegisteredClient.Builder builder =
        RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(clientId)
            .clientSecret(encoder.encode(secret))
            .clientAuthenticationMethod(
                grantType == AuthorizationGrantType.CLIENT_CREDENTIALS
                    ? ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                    : ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .authorizationGrantType(grantType)
            .clientSettings(clientSettings);

    if (grantType == AuthorizationGrantType.AUTHORIZATION_CODE) {
      builder
          .authorizationGrantType(
              AuthorizationGrantType.AUTHORIZATION_CODE) // ensure code grant stays
          .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
          .redirectUri(redirectUri);
      scopes.forEach(builder::scope);
    } else if (grantType == AuthorizationGrantType.CLIENT_CREDENTIALS) {
      scopes.forEach(builder::scope);
    }
    RegisteredClient client = builder.build();
    log.debug("AuthorizationGrantTypes: {}", client.getAuthorizationGrantTypes());
    return client;
  }
}
