package uk.gov.justice.laa.authx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/** Tests for {@link XAuthHeaderResolver}. */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class ServletXAuthHeaderResolverTest {
  @Test
  void resolver_returnsXAuthHeaderValue() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Auth", "x-auth-token");

    XAuthHeaderResolver resolver = new ServletXAuthHeaderResolver(() -> request);

    Optional<String> result = resolver.resolveXAuthHeader();

    assertThat(result).contains("x-auth-token");
  }

  @Test
  void returnsEmptyWhenXAuthHeaderIsMissing() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    XAuthHeaderResolver resolver = new ServletXAuthHeaderResolver(() -> request);

    Optional<String> result = resolver.resolveXAuthHeader();

    assertThat(result).isEmpty();
  }

  @Test
  void returnsEmptyWhenRequestIsNull() {
    XAuthHeaderResolver resolver = new ServletXAuthHeaderResolver(() -> null);

    Optional<String> result = resolver.resolveXAuthHeader();

    assertThat(result).isEmpty();
  }
}
