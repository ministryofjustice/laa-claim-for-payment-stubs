package uk.gov.justice.laa.authx;

import java.util.Map;

/**
 * Extracts claims from the X-Auth token.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public interface XAuthClaimsExtractor {
  Map<String, Object> extractClaims(String tokenValue);
}
