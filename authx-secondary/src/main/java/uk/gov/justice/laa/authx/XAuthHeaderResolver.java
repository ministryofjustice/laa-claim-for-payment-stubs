package uk.gov.justice.laa.authx;

import java.util.Optional;

/**
 * Resolves the X-Auth header from the incoming request.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface XAuthHeaderResolver {
  Optional<String> resolveXAuthHeader();
}
