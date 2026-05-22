package uk.gov.justice.laa.stubs.oidcserver.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Properties for configuring the authorization server clients. */
@Data
@ConfigurationProperties(prefix = "spring.security.oauth2.authorizationserver")
public class AuthorizationServerClientsProperties {

  private Map<String, Client> client = new LinkedHashMap<>();

  /** Converts a Client configuration to a RegisteredClient instance. */
  @Data
  public static class Client {
    private Registration registration;
    private Settings settings = new Settings();
  }

  /** Registration properties for each client. */
  @Data
  public static class Registration {
    private String clientId;
    private String clientSecret;
    private List<String> clientAuthenticationMethods = new ArrayList<>();
    private List<String> authorizationGrantTypes = new ArrayList<>();
    private List<String> redirectUris = new ArrayList<>();
    private List<String> postLogoutRedirectUris = new ArrayList<>();
    private List<String> scopes = new ArrayList<>();
  }

  /** Settings properties for each client. */
  @Data
  public static class Settings {
    private ClientSettings client = new ClientSettings();
  }

  /** Client settings properties. */
  @Data
  public static class ClientSettings {
    private boolean requireProofKey;
  }
}
