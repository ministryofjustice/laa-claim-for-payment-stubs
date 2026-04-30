package uk.gov.justice.laa.authx;

import java.util.Map;

/** An authentication object that is aware of the claims extracted from the X-Auth token. */
public interface ClaimsAwareAuthentication {
  Map<String, Object> getClaims();
}
