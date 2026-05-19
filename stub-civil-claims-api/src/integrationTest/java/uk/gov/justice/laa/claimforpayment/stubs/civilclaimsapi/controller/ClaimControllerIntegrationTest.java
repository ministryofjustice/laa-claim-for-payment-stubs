package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.CivilClaimsStubApplication;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.config.TestJwtConfig;

@SpringBootTest(classes = CivilClaimsStubApplication.class, properties = "security.enabled=true")
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import({TestJwtConfig.class})
@SuppressWarnings({
  "checkstyle:MemberNameCheck",
  "checkstyle:ParameterNameCheck",
  "checkstyle:AbbreviationAsWordInName",
  "checkstyle:LocalVariableName",
  "checkstyle:MethodName"
})
class ClaimControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  private UUID providerUserId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  @Autowired private JwtEncoder jwtEncoder;

  @Value("${app.security.authorities.claims-write}")
  private String claimsWriteScope;

  @Test
  void shouldGetAllClaimsForUser() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/claims")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId1.toString()))
                        .authorities(() -> "SCOPE_Claims.Write")))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.claims", hasSize(11)));
  }

  @Test
  void shouldGetAllClaimsForUserInXAuthOBOFlow() throws Exception {

    mockMvc
        .perform(
            get("/api/v1/claims")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
                .header("X-Auth", xAuthTokenWithUserId(providerUserId1.toString())))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.claims", hasSize(11)));
  }

  @Test
  void shouldGetClaim() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/claims/{claimId}", 1)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId1.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.ufn").value("121120/467"))
        .andExpect(jsonPath("$.client").value("Giordano"))
        .andExpect(jsonPath("$.category").value("Family"))
        .andExpect(jsonPath("$.concluded").value("2025-03-18"))
        .andExpect(jsonPath("$.feeType").value("Escape"))
        .andExpect(jsonPath("$.escaped").value(false))
        .andExpect(jsonPath("$.counselPayment").value("Paid and Reconciled"))
        .andExpect(jsonPath("$.claimed").value(234.56))
        .andExpect(jsonPath("$.lineItems", hasSize(0)));
  }

  @Test
  void shouldGetClaimWithLineItems() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/claims/{claimId}", 2)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId1.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(2))
        .andExpect(jsonPath("$.ufn").value("100323/098"))
        .andExpect(jsonPath("$.client").value("Amoto"))
        .andExpect(jsonPath("$.lineItems", hasSize(1)))
        .andExpect(jsonPath("$.lineItems[0].id").value(1))
        .andExpect(jsonPath("$.lineItems[0].title").value("Interim hearing"))
        .andExpect(jsonPath("$.lineItems[0].category").value("Work Item"))
        .andExpect(jsonPath("$.lineItems[0].date").value("2023-12-20"))
        .andExpect(jsonPath("$.lineItems[0].evidenceItems", hasSize(1)))
        .andExpect(jsonPath("$.lineItems[0].evidenceItems[0].id").value(1))
        .andExpect(
            jsonPath("$.lineItems[0].evidenceItems[0].fileKey").value("amoto-invoice-001.pdf"))
        .andExpect(jsonPath("$.lineItems[0].id").value(1));
  }

  @Test
  void shouldCreateClaim() throws Exception {
    String requestBody =
        """
        {
          "ufn": "NEW/999",
          "client": "New Client",
          "category": "Family",
          "concluded": "2025-07-09",
          "feeType": "Hourly",
          "escaped": false,
          "counselPayment": "Paid and Reconciled",
          "claimed": 123.45
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/claims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId1.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isCreated());
  }

  @Test
  void shouldUpdateClaim() throws Exception {
    String requestBody =
        """
        {
          "ufn": "UPDATED/123",
          "client": "Updated Client",
          "category": "Immigration and Asylum",
          "concluded": "2025-07-10",
          "feeType": "Fixed",
          "escaped": false,
          "counselPayment": "Paid and Reconciled",
          "claimed": 999.99
        }
        """;

    mockMvc
        .perform(
            put("/api/v1/claims/{claimId}", 2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId1.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldDeleteClaim() throws Exception {
    mockMvc
        .perform(
            delete("/api/v1/claims/{claimId}", 3)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId1.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isNoContent());
  }

  @Test
  void addLineItemToClaim_returnsCreatedStatusAndLocationHeader() throws Exception {
    String requestBody =
        """
        {
          "title": "New Line Item",
          "category": "New Line Item Category"
        }
        """;
    mockMvc
        .perform(
            patch("/api/v1/claims/{claimId}/line-items", 3)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId1.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isCreated())
        .andExpect(
            header().string("Location", matchesPattern(".*/api/v1/claims/3/line-items/\\d+$")));
  }

  @Test
  void addEvidenceToClaim_returnsCreatedStatusAndLocationHeader() throws Exception {
    String requestBody =
        """
        {
          "fileKey": "New file key for evidence"
        }
        """;
    mockMvc
        .perform(
            patch("/api/v1/claims/{claimId}/evidence", 3)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId1.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isCreated())
        .andExpect(
            header().string("Location", matchesPattern(".*/api/v1/claims/3/evidence/\\d+$")));
  }

  @Test
  void addEvidenceToLineItem_returnsNoContentStatus() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/claims/3/line-items/2/evidence/1")
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId1.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            get("/api/v1/claims/{claimId}", 3)
                .with(
                    jwt()
                        .jwt(jwt -> jwt.claim("USER_NAME", providerUserId1.toString()))
                        .authorities(() -> "SCOPE_" + claimsWriteScope)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(3))
        .andExpect(jsonPath("$.lineItems", hasSize(7)))
        .andDo(print())
        .andExpect(jsonPath("$.lineItems[0].evidenceItems", hasSize(1)));
  }

  private String xAuthTokenWithUserId(String userId) {
    return encode(Map.of("USER_NAME", userId, "oid", "test-user"));
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

  private String accessToken() {
    return encode(Map.of("oid", "test-user", "scope", claimsWriteScope));
  }
}
