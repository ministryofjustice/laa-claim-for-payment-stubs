package uk.gov.justice.laa.stubs.oidcserver.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.util.StringUtils;
import uk.gov.justice.laa.stubs.oidcserver.model.TestUser;

/**
 * Configuration for the mock OIDC server, including security chains, registered clients, JWK
 * source, test users, and token customizer.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(AuthorizationServerClientsProperties.class)
public class OidcServerConfig {

  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(OidcServerConfig.class);

  @Value("${auth.mock.issuer:http://localhost:8091}")
  private String issuer;

  @Bean
  OAuth2AuthorizationService authorizationService(RegisteredClientRepository clients) {
    return new InMemoryOAuth2AuthorizationService();
  }

  @Bean
  RegisteredClientRepository registeredClientRepository(
      AuthorizationServerClientsProperties properties) {

    var clients = properties.getClient().values().stream().map(this::toRegisteredClient).toList();

    return new InMemoryRegisteredClientRepository(clients);
  }

  private RegisteredClient toRegisteredClient(AuthorizationServerClientsProperties.Client client) {

    var registration = client.getRegistration();
    var clientSettingsProps = client.getSettings().getClient();

    ClientSettings clientSettings =
        ClientSettings.builder().requireProofKey(clientSettingsProps.isRequireProofKey()).build();

    RegisteredClient.Builder builder =
        RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(registration.getClientId())
            .clientSecret(registration.getClientSecret())
            .clientSettings(clientSettings);

    registration
        .getClientAuthenticationMethods()
        .forEach(m -> builder.clientAuthenticationMethod(new ClientAuthenticationMethod(m)));

    registration
        .getAuthorizationGrantTypes()
        .forEach(g -> builder.authorizationGrantType(new AuthorizationGrantType(g)));

    registration.getRedirectUris().forEach(builder::redirectUri);

    registration.getPostLogoutRedirectUris().forEach(builder::postLogoutRedirectUri);

    registration.getScopes().forEach(builder::scope);

    return builder.build();
  }

  @Bean
  RequestCache requestCache() {
    return new HttpSessionRequestCache();
  }

  @Bean
  public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
    return new NimbusJwtEncoder(jwkSource);
  }

  /**
   * Token generator that supports JWT encoding with our custom claims, as well as refresh and
   * opaque access tokens (if needed). The custom JWT generator is needed to support the OBO flow.
   */
  @Bean
  public OAuth2TokenGenerator<?> tokenGenerator(
      JwtEncoder jwtEncoder, OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer) {

    // This handles BOTH JWT Access Tokens and OIDC ID Tokens
    JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
    jwtGenerator.setJwtCustomizer(tokenCustomizer);
    OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
    OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();

    return new DelegatingOAuth2TokenGenerator(
        jwtGenerator, refreshTokenGenerator, accessTokenGenerator);
  }

  /**
   * Authorization Server security filter chain, with custom token endpoint config to support JWT
   * bearer.
   */
  @Bean
  @Order(1)
  public SecurityFilterChain authorizationServer(
      HttpSecurity http,
      OAuth2AuthorizationService authorizationService,
      OAuth2TokenGenerator<?> tokenGenerator,
      Map<String, TestUser> profiles)
      throws Exception {

    OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
        new OAuth2AuthorizationServerConfigurer();

    // this seems to be needed to support the JWT bearer grant type, which isn't natively supported
    // by Spring's default converters but is required by Entra.
    authorizationServerConfigurer.tokenEndpoint(
        tokenEndpoint -> {
          tokenEndpoint.accessTokenRequestConverters(
              converters -> {
                converters.add(
                    0,
                    request -> {
                      String grantType = request.getParameter("grant_type");
                      String tokenUse = request.getParameter("requested_token_use");
                      if (!"urn:ietf:params:oauth:grant-type:jwt-bearer".equals(grantType)
                          || !"on_behalf_of".equals(tokenUse)) {
                        return null;
                      }

                      String assertion = request.getParameter("assertion");
                      if (assertion == null || assertion.isEmpty()) {
                        return null;
                      }

                      // Get the client principal (already verified by the filter)
                      Authentication clientPrincipal =
                          SecurityContextHolder.getContext().getAuthentication();
                      Set<String> requestedScopes =
                          StringUtils.commaDelimitedListToSet(request.getParameter("scope"));

                      // Pass the assertion in the parameters map
                      return new Oauth2JwtBearerAuthenticationToken(
                          clientPrincipal, assertion, requestedScopes);
                    });
              });
          tokenEndpoint.authenticationProvider(
              jwtBearerAuthenticationProvider(authorizationService, tokenGenerator));
        });

    http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
        .securityContext(
            context ->
                context.securityContextRepository(new HttpSessionSecurityContextRepository()))
        .requestCache(c -> c.requestCache(requestCache()))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .exceptionHandling(
            ex -> ex.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
        .csrf(
            csrf ->
                csrf.requireCsrfProtectionMatcher(
                    request -> !request.getServletPath().startsWith("/oauth2/")))
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
  AuthorizationServerSettings authorizationServerSettings() {
    return AuthorizationServerSettings.builder().issuer(issuer).build();
  }

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
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
    return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
  }

  private AuthenticationProvider jwtBearerAuthenticationProvider(
      OAuth2AuthorizationService authService,
      OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {

    return new AuthenticationProvider() {
      @Override
      public Authentication authenticate(Authentication authentication)
          throws AuthenticationException {
        Oauth2JwtBearerAuthenticationToken jwtGrant =
            (Oauth2JwtBearerAuthenticationToken) authentication;

        RegisteredClient client =
            ((OAuth2ClientAuthenticationToken) jwtGrant.getPrincipal()).getRegisteredClient();

        Object assertionObj = jwtGrant.getAdditionalParameters().get("assertion");
        String assertionValue = String.valueOf(assertionObj);

        String userSubject;

        try {
          userSubject =
              com.nimbusds.jwt.JWTParser.parse(assertionValue).getJWTClaimsSet().getSubject();
        } catch (Exception e) {
          throw new OAuth2AuthenticationException(
              new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST),
              "Malformed or invalid assertion JWT",
              e);
        }

        Authentication userPrincipal =
            new UsernamePasswordAuthenticationToken(userSubject, null, Collections.emptyList());
        // stub: we trust the assertion and just issue the next token.
        OAuth2Authorization authorization =
            OAuth2Authorization.withRegisteredClient(client)
                .principalName(userSubject)
                .authorizationGrantType(jwtGrant.getGrantType())
                .authorizedScopes(jwtGrant.getScopes())
                .build();

        var contextBuilder =
            DefaultOAuth2TokenContext.builder()
                .registeredClient(client)
                .principal(userPrincipal)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorization(authorization)
                .authorizedScopes(jwtGrant.getScopes())
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .authorizationGrantType(jwtGrant.getGrantType())
                .authorizationGrant(jwtGrant);

        OAuth2Token generatedToken = tokenGenerator.generate(contextBuilder.build());

        if (generatedToken == null) {
          throw new OAuth2AuthenticationException(OAuth2ErrorCodes.SERVER_ERROR);
        }

        Jwt jwt = (Jwt) generatedToken;
        Set<String> authorizedScopes = jwtGrant.getScopes();
        OAuth2AccessToken accessToken =
            new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                jwt.getTokenValue(),
                jwt.getIssuedAt(),
                jwt.getExpiresAt(),
                authorizedScopes);

        OAuth2Authorization.Builder authBuilder =
            OAuth2Authorization.from(authorization)
                .token(
                    accessToken,
                    metadata ->
                        metadata.put(
                            OAuth2Authorization.Token.CLAIMS_METADATA_NAME, jwt.getClaims()));

        authService.save(authBuilder.build());

        return new OAuth2AccessTokenAuthenticationToken(client, jwtGrant, accessToken);
      }

      @Override
      public boolean supports(Class<?> auth) {
        return Oauth2JwtBearerAuthenticationToken.class.isAssignableFrom(auth);
      }
    };
  }
}
