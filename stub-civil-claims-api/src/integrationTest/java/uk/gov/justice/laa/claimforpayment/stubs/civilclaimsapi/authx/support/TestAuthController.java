package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.authx.support;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** A controller for testing authentication. */
@RestController
@RequestMapping("/test-auth")
public class TestAuthController {

  /** Returns a secured response. */
  @GetMapping
  @PreAuthorize("principal.claims['FIRM_CODE'] == 'firm1234'")
  public ResponseEntity<String> secured(JwtAuthenticationToken auth) {

    Object v = auth.getToken().getClaims().get("FIRM_CODE");
    System.out.println(v);
    System.out.println(v.getClass());

    return ResponseEntity.ok("ok");
  }
}
