package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.authx;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.CivilClaimsStubApplication;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.authx.support.TestAuthController;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.config.TestJwtConfig;



// src/test/java/.../XAuthIntegrationTest.java

@SpringBootTest(classes = CivilClaimsStubApplication.class, properties = "security.enabled=true")
@AutoConfigureMockMvc
@SuppressWarnings({
  "checkstyle:AbbreviationAsWordInName",
  "checkstyle:LocalVariableName",
  "checkstyle:MethodName"
})
@ActiveProfiles("test")
@Import({ TestAuthController.class, TestJwtConfig.class })
class XAuthIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private JwtEncoder jwtEncoder;

  @Test
  void request_with_x_auth_claim_allows_access() throws Exception {
    String accessToken = accessToken();
    String xAuthToken = xAuthTokenWithEnrichedFirm();

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/test-auth")
                .header("Authorization", "Bearer " + accessToken)
                .header("X-Auth", xAuthToken))
        .andExpect(status().isOk())
        .andExpect(content().string("ok"));
  }

  private String accessToken() {
    return encode(
        Map.of(
            "sub", "test-user",
            "scope", "read"));
  }

  private String xAuthTokenWithEnrichedFirm() {
    return encode(Map.of("FIRM_CODE", "firm1234"));
  }

  private String encode(Map<String, Object> claims) {
    Instant now = Instant.now();

    JwtClaimsSet jwtClaims =
        JwtClaimsSet.builder()
            .issuer("https://issuer.test")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(60))
            .claims(c -> c.putAll(claims))
            .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(jwtClaims)).getTokenValue();
  }
}
