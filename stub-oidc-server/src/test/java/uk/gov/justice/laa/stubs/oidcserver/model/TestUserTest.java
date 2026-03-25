package uk.gov.justice.laa.stubs.oidcserver.model;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TestUserTest {

  private PasswordEncoder encoder;

  @BeforeEach
  public void setUp() {
    encoder = mock(PasswordEncoder.class);
  }

  @Test
  void toUserDetails_createsUserDetails_whenNoRoles() {
    TestUser user =
        new TestUser(
            "joe",
            "Joe Bloggs",
            "joe.bloggs@example.test",
            "prov-123",
            UUID.fromString("1faf90d6-e969-4d8e-beba-0e081ea62c60"),
            "password123",
            List.of());

    when(encoder.encode(any())).thenReturn("password456");

    UserDetails result = user.toUserDetails(encoder);

    Assertions.assertThat(result.getUsername()).isEqualTo("joe");
    Assertions.assertThat(result.getPassword()).isEqualTo("password456");
    Assertions.assertThat(result.getAuthorities()).isEmpty();
  }

  @Test
  void toUserDetails_createsUserDetails_whenOneRole() {
    TestUser user =
        new TestUser(
            "joe",
            "Joe Bloggs",
            "joe.bloggs@example.test",
            "prov-123",
            UUID.fromString("1faf90d6-e969-4d8e-beba-0e081ea62c60"),
            "password123",
            List.of("role1"));

    when(encoder.encode(any())).thenReturn("password456");

    UserDetails result = user.toUserDetails(encoder);

    Assertions.assertThat(result.getUsername()).isEqualTo("joe");
    Assertions.assertThat(result.getPassword()).isEqualTo("password456");
    Assertions.assertThat(result.getAuthorities().toArray())
        .containsExactly(new SimpleGrantedAuthority("ROLE_role1"));
  }

  @Test
  void toUserDetails_createsUserDetails_whenMultipleRoles() {
    TestUser user =
        new TestUser(
            "joe",
            "Joe Bloggs",
            "joe.bloggs@example.test",
            "prov-123",
            UUID.fromString("1faf90d6-e969-4d8e-beba-0e081ea62c60"),
            "password123",
            List.of("role1", "role2"));

    when(encoder.encode(any())).thenReturn("password456");

    UserDetails result = user.toUserDetails(encoder);

    Assertions.assertThat(result.getUsername()).isEqualTo("joe");
    Assertions.assertThat(result.getPassword()).isEqualTo("password456");
    Assertions.assertThat(result.getAuthorities().toArray())
        .containsExactly(
            new SimpleGrantedAuthority("ROLE_role1"), new SimpleGrantedAuthority("ROLE_role2"));
  }
}
