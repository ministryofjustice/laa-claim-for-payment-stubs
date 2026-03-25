package uk.gov.justice.laa.stubs.oidcserver.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.stubs.oidcserver.model.TestUser;

@SpringBootTest
@AutoConfigureMockMvc
public class OidcServerConfigIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void openidConfigurationIsExposed() throws Exception {
    mockMvc
        .perform(get("/.well-known/openid-configuration"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.issuer").value("http://localhost:8091"));
  }

  @Test
  void actuatorReturnsUpStatus() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"alice", "bob"})
  void userCanLoginWithCorrectPassword(String user) throws Exception {
    mockMvc
        .perform(formLogin("/login").user(user).password("password"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andDo(print());
  }

  @ParameterizedTest
  @ValueSource(strings = {"alice", "bob"})
  void userCannotLoginWithIncorrectPassword(String user) throws Exception {
    mockMvc
        .perform(formLogin("/login").user(user).password("incorrect"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login?error"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"charlotte", "dave"})
  void unknownUserCannotLogin(String user) throws Exception {
    mockMvc
        .perform(formLogin("/login").user(user).password("password"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login?error"));
  }

  @Nested
  class RegisteredClientRepositoryTests {

    @Autowired private RegisteredClientRepository registeredClientRepository;

    @Test
    void ssrClientHasCorrectConfig() {
      RegisteredClient ssr = registeredClientRepository.findByClientId("caa-client");

      assertThat(ssr).isNotNull();
      assertThat(ssr.getClientId()).isEqualTo("caa-client");
      assertThat(ssr.getClientSecret()).isNotNull();
      assertThat(ssr.getClientAuthenticationMethods())
          .containsOnly(ClientAuthenticationMethod.CLIENT_SECRET_POST);
      assertThat(ssr.getAuthorizationGrantTypes())
          .containsOnly(
              AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN);
      assertThat(ssr.getRedirectUris()).containsOnly("http://localhost:3000/callback");
      assertThat(ssr.getPostLogoutRedirectUris()).containsOnly("http://localhost:3000");
      assertThat(ssr.getScopes())
          .containsOnly(OidcScopes.OPENID, OidcScopes.PROFILE, OidcScopes.EMAIL, "Claims.Write");
    }

    @Test
    void machineClientHasCorrectConfig() {
      RegisteredClient machine = registeredClientRepository.findByClientId("machine");

      assertThat(machine).isNotNull();
      assertThat(machine.getClientId()).isEqualTo("machine");
      assertThat(machine.getClientSecret()).isNotNull();
      assertThat(machine.getClientAuthenticationMethods())
          .containsOnly(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
      assertThat(machine.getAuthorizationGrantTypes())
          .containsOnly(AuthorizationGrantType.CLIENT_CREDENTIALS);
      assertThat(machine.getScopes()).containsOnly("Claims.Write");
    }
  }

  @Nested
  class TestProfilesTest {

    @Autowired private Map<String, TestUser> testProfiles;

    @Test
    void testProfilesArePresent() {
      assertThat(testProfiles).hasSize(2);
      assertThat(testProfiles.get("alice")).isNotNull();
      assertThat(testProfiles.get("bob")).isNotNull();
    }
  }

  @Nested
  class AuthorizationServerSettingsTests {

    @Autowired private AuthorizationServerSettings authorizationServerSettings;

    @Test
    void testAuthorizationServerSettingsArePresent() {
      assertThat(authorizationServerSettings).isNotNull();
      assertThat(authorizationServerSettings.getIssuer()).isEqualTo("http://localhost:8091");
    }
  }
}
