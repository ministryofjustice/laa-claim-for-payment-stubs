package uk.gov.justice.laa.stubs.oidcserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class OidcServerConfigTest {

    private OAuth2TokenCustomizer<JwtEncodingContext> customizer;

    @BeforeEach
    void setUp() {
        OidcServerConfig config = new OidcServerConfig();
        this.customizer = config.tokenCustomizer(config.testProfiles());
    }

    @ParameterizedTest
    @MethodSource("users")
    void idTokenCustomizerAddsClaims(TestUser user, String role) {
        JwtEncodingContext context = buildContext(user, role, new OAuth2TokenType(OidcParameterNames.ID_TOKEN));

        customizer.customize(context);

        Map<String, Object> claims = context.getClaims().build().getClaims();

        assertThat(claims.get("sub")).isEqualTo(user.username());
        assertThat(claims.get("FIRM_CODE")).isEqualTo(user.firmId());
        assertThat((claims.get("USER_NAME"))).isEqualTo(user.providerUserId());
        assertThat((List<String>) claims.get("roles")).containsExactly(role);
        assertThat(claims.get("email")).isEqualTo(user.email());
        assertThat(claims.get("name")).isEqualTo(user.displayName());
        assertThat(claims.get("preferred_username")).isEqualTo(user.username());
        assertThat(claims).doesNotContainKeys("aud");
    }

    @ParameterizedTest
    @MethodSource("users")
    void accessTokenCustomizerAddsClaims(TestUser user, String role) {
        JwtEncodingContext context = buildContext(user, role, OAuth2TokenType.ACCESS_TOKEN);

        customizer.customize(context);

        Map<String, Object> claims = context.getClaims().build().getClaims();

        assertThat(claims.get("sub")).isEqualTo(user.username());
        assertThat(claims.get("FIRM_CODE")).isEqualTo(user.firmId());
        assertThat(claims.get("USER_NAME")).isEqualTo(user.providerUserId());
        assertThat((List<String>) claims.get("roles")).containsExactly(role);
        assertThat(claims).doesNotContainKeys("email", "name", "preferred_username");
        assertThat(claims.get("aud")).isEqualTo(List.of("api-audience"));
    }

    private JwtEncodingContext buildContext(TestUser user, String role, OAuth2TokenType tokenType) {
        Authentication principal = new UsernamePasswordAuthenticationToken(
            user.username(),
            "password",
            List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        JwsHeader.Builder jwsHeaderBuilder = JwsHeader.with(SignatureAlgorithm.RS256);

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder().subject(user.username());

        return JwtEncodingContext.with(jwsHeaderBuilder, claimsBuilder)
            .principal(principal)
            .tokenType(tokenType)
            .build();
    }

    private static Stream<Arguments> users() {
        return Stream.of(
            Arguments.of(
                new TestUser(
                    "alice",
                    "Alice Smith",
                    "alice.smith@example.test",
                    "prov-123",
                    UUID.fromString("d9c4b277-941c-451c-81c4-6b46b7f7ab59")
                ),
                "caseworker"
            ),
            Arguments.of(
                new TestUser(
                    "bob",
                    "Bob Jones",
                    "bob.jones@example.test",
                    "prov-456",
                    UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
                ),
                "admin"
            )
        );
    }
}
