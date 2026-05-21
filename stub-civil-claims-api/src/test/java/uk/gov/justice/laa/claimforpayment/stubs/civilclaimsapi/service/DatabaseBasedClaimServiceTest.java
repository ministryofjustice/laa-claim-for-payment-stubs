package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEvidenceEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.LineItemEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.ClaimNotFoundException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper.ClaimMapper;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.Claim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.ClaimEvidence;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.ClaimEvidenceRequestBody;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.ClaimRequestBody;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.LineItem;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.LineItemRequestBody;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.repository.ClaimEvidenceRepository;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.repository.ClaimRepository;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.repository.LineItemRepository;

@ExtendWith(MockitoExtension.class)
class DatabaseBasedClaimServiceTest {

  @Mock private ClaimRepository mockClaimRepository;
  @Mock private LineItemRepository mockLineItemRepository;
  @Mock private ClaimEvidenceRepository mockClaimEvidenceRepository;

  @Mock private ClaimMapper mockClaimMapper;

  @InjectMocks private DatabaseBasedClaimService claimService;

  @Test
  void shouldGetAllClaims() {

    ClaimEntity firstClaimEntity =
        ClaimEntity.builder()
            .id(1L)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    ClaimEntity secondClaimEntity =
        ClaimEntity.builder()
            .id(2L)
            .ufn("UFN456")
            .client("Jane Smith")
            .category("Category B")
            .concluded(LocalDate.of(2025, 7, 2))
            .feeType("Hourly")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(2000.0))
            .build();

    Claim firstClaim =
        Claim.builder()
            .id(1L)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    Claim secondClaim =
        Claim.builder()
            .id(2L)
            .ufn("UFN456")
            .client("Jane Smith")
            .category("Category B")
            .concluded(LocalDate.of(2025, 7, 2))
            .feeType("Hourly")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(2000.0))
            .build();

    when(mockClaimRepository.findAll()).thenReturn(List.of(firstClaimEntity, secondClaimEntity));
    when(mockClaimMapper.toClaim(firstClaimEntity)).thenReturn(firstClaim);
    when(mockClaimMapper.toClaim(secondClaimEntity)).thenReturn(secondClaim);

    List<Claim> result = claimService.getClaims();

    assertThat(result).hasSize(2).contains(firstClaim, secondClaim);
  }

  @Test
  void shouldGetAllClaimsForProviderUser() {
    UUID providerUserId = UUID.randomUUID();

    ClaimEntity firstClaimEntity =
        ClaimEntity.builder()
            .id(1L)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .providerUserId(providerUserId)
            .build();

    ClaimEntity secondClaimEntity =
        ClaimEntity.builder()
            .id(2L)
            .ufn("UFN456")
            .client("Jane Smith")
            .category("Category B")
            .concluded(LocalDate.of(2025, 7, 2))
            .feeType("Hourly")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(2000.0))
            .providerUserId(providerUserId)
            .build();

    Claim firstClaim =
        Claim.builder()
            .id(1L)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .providerUserId(providerUserId)
            .build();

    Claim secondClaim =
        Claim.builder()
            .id(2L)
            .ufn("UFN456")
            .client("Jane Smith")
            .category("Category B")
            .concluded(LocalDate.of(2025, 7, 2))
            .feeType("Hourly")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(2000.0))
            .providerUserId(providerUserId)
            .build();

    Pageable pageable = PageRequest.of(1, 1);
    Page<ClaimEntity> page =
        new PageImpl<ClaimEntity>(List.of(firstClaimEntity, secondClaimEntity));

    when(mockClaimRepository.findByProviderUserId(providerUserId, pageable)).thenReturn(page);
    when(mockClaimMapper.toClaim(firstClaimEntity)).thenReturn(firstClaim);
    when(mockClaimMapper.toClaim(secondClaimEntity)).thenReturn(secondClaim);

    Page<Claim> result = claimService.getAllClaimsForProvider(providerUserId, 1, 1);

    assertThat(result).hasSize(2).contains(firstClaim, secondClaim);
  }

  @Test
  void shouldGetClaimById() {

    Long id = 1L;

    ClaimEvidence claimEvidence1 = ClaimEvidence.builder().id(1L).fileKey("fileKey1").fileSize(1000L).build();
    ClaimEvidence claimEvidence2 = ClaimEvidence.builder().id(2L).fileKey("fileKey2").fileSize(2000L).build();
    ClaimEvidence claimEvidence3 = ClaimEvidence.builder().id(3L).fileKey("fileKey3").fileSize(3000L).build();
    LineItem lineItem1 =
        LineItem.builder().id(1L).evidenceItems(List.of(claimEvidence1, claimEvidence2)).build();
    LineItem lineItem2 = LineItem.builder().id(2L).evidenceItems(List.of(claimEvidence3)).build();

    ClaimEvidenceEntity claimEvidenceEntity1 =
        ClaimEvidenceEntity.builder().id(1L).fileKey("fileKey1").build();
    ClaimEvidenceEntity claimEvidenceEntity2 =
        ClaimEvidenceEntity.builder().id(2L).fileKey("fileKey2").build();
    ClaimEvidenceEntity claimEvidenceEntity3 =
        ClaimEvidenceEntity.builder().id(3L).fileKey("fileKey3").build();
    LineItemEntity lineItemEntity1 =
        LineItemEntity.builder()
            .id(1L)
            .evidenceItems(Set.of(claimEvidenceEntity1, claimEvidenceEntity2))
            .build();
    LineItemEntity lineItemEntity2 =
        LineItemEntity.builder().id(2L).evidenceItems(Set.of(claimEvidenceEntity3)).build();

    ClaimEntity claimEntity =
        ClaimEntity.builder()
            .id(id)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .lineItems(List.of(lineItemEntity1, lineItemEntity2))
            .evidence(List.of(claimEvidenceEntity1, claimEvidenceEntity2, claimEvidenceEntity3))
            .build();

    Claim claim =
        Claim.builder()
            .id(id)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .lineItems(List.of(lineItem1, lineItem2))
            .evidenceItems(List.of(claimEvidence1, claimEvidence2, claimEvidence3))
            .build();

    when(mockClaimRepository.findById(id)).thenReturn(Optional.of(claimEntity));
    when(mockClaimMapper.toClaim(claimEntity)).thenReturn(claim);

    Claim result = claimService.getClaim(id);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getClient()).isEqualTo("John Doe");
    assertThat(result.getClaimed()).isEqualTo(new BigDecimal(1000.0));
    assertThat(result.getLineItems()).hasSize(2).contains(lineItem1, lineItem2);
  }

  @Test
  void shouldNotGetClaimById_whenClaimNotFoundThenThrowsException() {
    Long id = 5L;
    when(mockClaimRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ClaimNotFoundException.class, () -> claimService.getClaim(id));

    verify(mockClaimMapper, never()).toClaim(any(ClaimEntity.class));
  }

  @Test
  void shouldCreateClaim() {

    ClaimRequestBody claimRequestBody =
        ClaimRequestBody.builder()
            .ufn("UFN789")
            .client("Alice Example")
            .category("Category C")
            .concluded(LocalDate.of(2025, 7, 3))
            .feeType("Capped")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1500.0))
            .build();

    ClaimEntity savedClaimEntity =
        ClaimEntity.builder()
            .id(3L)
            .ufn("UFN789")
            .client("Alice Example")
            .category("Category C")
            .concluded(LocalDate.of(2025, 7, 3))
            .feeType("Capped")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1500.0))
            .build();

    when(mockClaimRepository.save(any(ClaimEntity.class))).thenReturn(savedClaimEntity);

    Long result = claimService.createClaim(claimRequestBody, UUID.randomUUID());

    assertThat(result).isNotNull().isEqualTo(3L);
  }

  @Test
  void shouldUpdateClaim() {
    Long id = 1L;
    ClaimRequestBody claimRequestBody =
        ClaimRequestBody.builder()
            .ufn("UFN999")
            .client("Updated Client")
            .category("Updated Category")
            .concluded(LocalDate.of(2025, 7, 4))
            .feeType("Revised")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(2500.0))
            .build();

    ClaimEntity claimEntity =
        ClaimEntity.builder()
            .id(id)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    when(mockClaimRepository.findById(id)).thenReturn(Optional.of(claimEntity));

    claimService.updateClaim(id, claimRequestBody);

    assertThat(claimEntity.getUfn()).isEqualTo("UFN999");
    assertThat(claimEntity.getClient()).isEqualTo("Updated Client");
    assertThat(claimEntity.getCategory()).isEqualTo("Updated Category");
    assertThat(claimEntity.getConcluded()).isEqualTo(LocalDate.of(2025, 7, 4));
    assertThat(claimEntity.getFeeType()).isEqualTo("Revised");
    assertThat(claimEntity.getEscaped()).isEqualTo(false);
    assertThat(claimEntity.getCounselPayment()).isEqualTo("Paid and Reconciled");
    assertThat(claimEntity.getClaimed()).isEqualTo(new BigDecimal(2500.0));

    verify(mockClaimRepository).save(claimEntity);
  }

  @Test
  void shouldNotUpdateClaim_whenClaimNotFoundThenThrowsException() {
    Long id = 5L;
    ClaimRequestBody claimRequestBody =
        ClaimRequestBody.builder().ufn("UFN000").client("Non-existent Client").build();

    when(mockClaimRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(
        ClaimNotFoundException.class, () -> claimService.updateClaim(id, claimRequestBody));

    verify(mockClaimRepository, never()).save(any(ClaimEntity.class));
  }

  @Test
  void shouldDeleteClaim() {
    Long id = 1L;
    ClaimEntity claimEntity = ClaimEntity.builder().id(id).ufn("UFN123").client("John Doe").build();

    when(mockClaimRepository.findById(id)).thenReturn(Optional.of(claimEntity));

    claimService.deleteClaim(id);

    verify(mockClaimRepository).deleteById(id);
  }

  /** Should not delete a claim when it does not exist. */
  @Test
  void shouldNotDeleteClaim_whenClaimNotFoundThenThrowsException() {
    Long id = 5L;
    when(mockClaimRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ClaimNotFoundException.class, () -> claimService.deleteClaim(id));

    verify(mockClaimRepository, never()).deleteById(id);
  }

  @Test
  void shouldAddEvidenceToClaim() {

    ClaimEvidenceRequestBody claimEvidenceRequestBody =
        ClaimEvidenceRequestBody.builder().fileKey("Claim evidence file key").build();

    ClaimEntity savedClaimEntity =
        ClaimEntity.builder()
            .id(3L)
            .ufn("UFN789")
            .client("Alice Example")
            .category("Category C")
            .concluded(LocalDate.of(2025, 7, 3))
            .feeType("Capped")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1500.0))
            .build();

    when(mockClaimRepository.findById(3L)).thenReturn(Optional.of(savedClaimEntity));

    when(mockClaimEvidenceRepository.save(any(ClaimEvidenceEntity.class)))
        .thenAnswer(
            invocation -> {
              ClaimEvidenceEntity claimEvidenceEntity = invocation.getArgument(0);
              claimEvidenceEntity.setId(3L);
              return claimEvidenceEntity;
            });

    Long result = claimService.addEvidenceToClaim(3L, claimEvidenceRequestBody);

    assertThat(result).isNotNull().isEqualTo(3L);
  }

  @Test
  void shouldAddLineItemToClaim() {

    LineItemRequestBody lineItemRequestBody =
        LineItemRequestBody.builder()
            .title("Line item title")
            .category("Line item category")
            .build();

    ClaimEntity savedClaimEntity =
        ClaimEntity.builder()
            .id(3L)
            .ufn("UFN789")
            .client("Alice Example")
            .category("Category C")
            .concluded(LocalDate.of(2025, 7, 3))
            .feeType("Capped")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1500.0))
            .build();

    when(mockClaimRepository.findById(3L)).thenReturn(Optional.of(savedClaimEntity));

    when(mockLineItemRepository.save(any(LineItemEntity.class)))
        .thenAnswer(
            invocation -> {
              LineItemEntity lineItemEntity = invocation.getArgument(0);
              lineItemEntity.setId(3L);
              return lineItemEntity;
            });

    Long result = claimService.addLineItemToClaim(3L, lineItemRequestBody);

    assertThat(result).isNotNull().isEqualTo(3L);
  }

  @Test
  void shouldLinkEvidenceToLineItem() {

    ClaimEntity claimEntity =
        ClaimEntity.builder()
            .id(1L)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    LineItemEntity lineItemEntity = LineItemEntity.builder().id(1L).claim(claimEntity).build();

    ClaimEvidenceEntity claimEvidenceEntity =
        ClaimEvidenceEntity.builder().id(1L).claim(claimEntity).build();

    when(mockClaimRepository.findById(1L)).thenReturn(Optional.of(claimEntity));
    when(mockLineItemRepository.findById(1L)).thenReturn(Optional.of(lineItemEntity));
    when(mockClaimEvidenceRepository.findById(1L)).thenReturn(Optional.of(claimEvidenceEntity));

    claimService.linkEvidenceToLineItem(1L, 1L, List.of(1L));

    assertThat(lineItemEntity.getEvidenceItems()).contains(claimEvidenceEntity);
  }

  @Test
  void shouldLinkMultipleEvidenceToLineItem() {

    ClaimEntity claimEntity =
      ClaimEntity.builder()
        .id(1L)
        .ufn("UFN123")
        .client("John Doe")
        .category("Category A")
        .concluded(LocalDate.of(2025, 7, 1))
        .feeType("Fixed")
        .escaped(false)
        .counselPayment("Paid and Reconciled")
        .claimed(new BigDecimal(1000.0))
        .build();

    LineItemEntity lineItemEntity = LineItemEntity.builder().id(1L).claim(claimEntity).build();

    ClaimEvidenceEntity claimEvidenceEntity1 =
      ClaimEvidenceEntity.builder().id(1L).claim(claimEntity).build();

    ClaimEvidenceEntity claimEvidenceEntity2 =
      ClaimEvidenceEntity.builder().id(2L).claim(claimEntity).build();

    when(mockClaimRepository.findById(1L)).thenReturn(Optional.of(claimEntity));
    when(mockLineItemRepository.findById(1L)).thenReturn(Optional.of(lineItemEntity));
    when(mockClaimEvidenceRepository.findById(1L)).thenReturn(Optional.of(claimEvidenceEntity1));
    when(mockClaimEvidenceRepository.findById(2L)).thenReturn(Optional.of(claimEvidenceEntity2));

    claimService.linkEvidenceToLineItem(1L, 1L, List.of(1L, 2L));

    assertThat(lineItemEntity.getEvidenceItems()).contains(claimEvidenceEntity1);
  }
}
