package uk.gov.justice.laa.stubs.oidcserver.model;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Represents a test user profile with username, display name, email, and provider ID.
 */
public record TestUser(
    String username,
    String displayName,
    String email,
    String firmId,
    UUID providerUserId,
    String password,
    List<String> roles) {

  /** Convert TestUser to UserDetails. */
  public UserDetails toUserDetails(PasswordEncoder encoder) {
    return User
        .withUsername(username)
        .password(encoder.encode(password))
        .roles(roles.toArray(String[]::new))
        .build();
  }
}
