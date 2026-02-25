package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.Claim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.security.NoAuthSecurityConfig;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.service.DatabaseBasedClaimService;

@WebMvcTest(controllers = ClaimController.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "security.enabled=false")
@Import({NoAuthSecurityConfig.class}) // Import security and OAuth2 config for tests
class ClaimControllerNoAuthTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private DatabaseBasedClaimService mockClaimService;

  @Test
  void getClaims_returnsOkStatusAndAllClaimsWithDefaultAuth() throws Exception {
    UUID providerUserId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID providerUserId2 = UUID.randomUUID();

    List<Claim> claims =
        List.of(
            Claim.builder()
                .id(1L)
                .category("Category 1")
                .claimed(new BigDecimal(2.2))
                .client("Smith")
                .concluded(LocalDate.now())
                .feeType("Fee type 1")
                .providerUserId(providerUserId1)
                .build(),
            Claim.builder()
                .id(2L)
                .category("Category 1")
                .claimed(new BigDecimal(2.5))
                .client("Smith")
                .concluded(LocalDate.now())
                .feeType("Fee type 2")
                .providerUserId(providerUserId2)
                .build());

    Page<Claim> claim1 = new PageImpl<>(List.of(claims.getFirst()), Pageable.ofSize(1), 1);

    int pageNumber = 1;
    int pageSize = 1;

    when(mockClaimService.getAllClaimsForProvider(providerUserId1, pageNumber, pageSize))
        .thenReturn(claim1);

    mockMvc
        .perform(
            get("/api/v1/claims")
                .param("page", String.valueOf(pageNumber))
                .param("limit", String.valueOf(pageSize)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.claims[0].id").value("1"))
        .andExpect(jsonPath("$.claims", hasSize(1)));
  }
}
