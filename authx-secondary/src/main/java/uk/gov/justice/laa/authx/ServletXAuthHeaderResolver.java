package uk.gov.justice.laa.authx;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.function.Supplier;

/** A resolver for extracting the X-Auth header from a servlet request. */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class ServletXAuthHeaderResolver implements XAuthHeaderResolver {

  private static final String HEADER_NAME = "X-Auth";

  private final Supplier<HttpServletRequest> requestSupplier;

  public ServletXAuthHeaderResolver(Supplier<HttpServletRequest> requestSupplier) {
    this.requestSupplier = requestSupplier;
  }

  @Override
  public Optional<String> resolveXAuthHeader() {

    HttpServletRequest request = requestSupplier.get();
    if (request == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(request.getHeader(HEADER_NAME));
  }
}
