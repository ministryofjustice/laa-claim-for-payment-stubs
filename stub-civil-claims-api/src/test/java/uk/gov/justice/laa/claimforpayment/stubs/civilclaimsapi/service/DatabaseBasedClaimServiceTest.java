package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
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
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.ClaimEvidenceNotFoundException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.ClaimNotFoundException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.LineItemNotFoundException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.StubException;
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

    UUID firstClaimId = UUID.randomUUID();
    UUID secondClaimId = UUID.randomUUID();

    ClaimEntity firstClaimEntity =
        ClaimEntity.builder()
            .id(firstClaimId)
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
            .id(secondClaimId)
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
            .id(firstClaimId)
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
            .id(secondClaimId)
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

    UUID firstClaimId = UUID.randomUUID();
    UUID secondClaimId = UUID.randomUUID();

    ClaimEntity firstClaimEntity =
        ClaimEntity.builder()
            .id(firstClaimId)
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
            .id(secondClaimId)
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
            .id(firstClaimId)
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
            .id(secondClaimId)
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

    UUID claimId = UUID.randomUUID();

    UUID claimEvidence1Id = UUID.randomUUID();
    UUID claimEvidence2Id = UUID.randomUUID();
    UUID claimEvidence3Id = UUID.randomUUID();

    UUID lineItem1Id = UUID.randomUUID();
    UUID lineItem2Id = UUID.randomUUID();

    ClaimEvidence claimEvidence1 =
        ClaimEvidence.builder().id(claimEvidence1Id).fileKey("fileKey1").fileSize(1000L).build();
    ClaimEvidence claimEvidence2 =
        ClaimEvidence.builder().id(claimEvidence2Id).fileKey("fileKey2").fileSize(2000L).build();
    ClaimEvidence claimEvidence3 =
        ClaimEvidence.builder().id(claimEvidence3Id).fileKey("fileKey3").fileSize(3000L).build();
    LineItem lineItem1 =
        LineItem.builder()
            .id(lineItem1Id)
            .evidenceItems(List.of(claimEvidence1Id, claimEvidence2Id))
            .build();
    LineItem lineItem2 =
        LineItem.builder().id(lineItem2Id).evidenceItems(List.of(claimEvidence3Id)).build();

    ClaimEvidenceEntity claimEvidenceEntity1 =
        ClaimEvidenceEntity.builder().id(claimEvidence1Id).fileKey("fileKey1").build();
    ClaimEvidenceEntity claimEvidenceEntity2 =
        ClaimEvidenceEntity.builder().id(claimEvidence2Id).fileKey("fileKey2").build();
    ClaimEvidenceEntity claimEvidenceEntity3 =
        ClaimEvidenceEntity.builder().id(claimEvidence3Id).fileKey("fileKey3").build();
    LineItemEntity lineItemEntity1 =
        LineItemEntity.builder()
            .id(lineItem1Id)
            .evidenceItems(Set.of(claimEvidenceEntity1, claimEvidenceEntity2))
            .build();
    LineItemEntity lineItemEntity2 =
        LineItemEntity.builder()
            .id(lineItem2Id)
            .evidenceItems(Set.of(claimEvidenceEntity3))
            .build();

    ClaimEntity claimEntity =
        ClaimEntity.builder()
            .id(claimId)
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
            .id(claimId)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .lineItems(List.of(lineItem1, lineItem2))
            .evidence(List.of(claimEvidence1, claimEvidence2, claimEvidence3))
            .build();

    when(mockClaimRepository.findById(claimId)).thenReturn(Optional.of(claimEntity));
    when(mockClaimMapper.toClaim(claimEntity)).thenReturn(claim);

    Claim result = claimService.getClaim(claimId);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(claimId);
    assertThat(result.getClient()).isEqualTo("John Doe");
    assertThat(result.getClaimed()).isEqualTo(new BigDecimal(1000.0));
    assertThat(result.getLineItems()).hasSize(2).contains(lineItem1, lineItem2);
    assertThat(result.getEvidence())
        .hasSize(3)
        .contains(claimEvidence1, claimEvidence2, claimEvidence3);
  }

  @Test
  void shouldNotGetClaimById_whenClaimNotFoundThenThrowsException() {
    UUID id = UUID.randomUUID();
    when(mockClaimRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ClaimNotFoundException.class, () -> claimService.getClaim(id));

    verify(mockClaimMapper, never()).toClaim(any(ClaimEntity.class));
  }

  @Test
  void shouldCreateClaim() {

    UUID firstClaimId = UUID.randomUUID();

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
            .id(firstClaimId)
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

    UUID result = claimService.createClaim(claimRequestBody, UUID.randomUUID());

    assertThat(result).isNotNull().isEqualTo(firstClaimId);
  }

  @Test
  void shouldUpdateClaim() {
    UUID id = UUID.randomUUID();
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
    UUID id = UUID.randomUUID();
    ClaimRequestBody claimRequestBody =
        ClaimRequestBody.builder().ufn("UFN000").client("Non-existent Client").build();

    when(mockClaimRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(
        ClaimNotFoundException.class, () -> claimService.updateClaim(id, claimRequestBody));

    verify(mockClaimRepository, never()).save(any(ClaimEntity.class));
  }

  @Test
  void shouldDeleteClaim() {
    UUID id = UUID.randomUUID();
    ClaimEntity claimEntity = ClaimEntity.builder().id(id).ufn("UFN123").client("John Doe").build();

    when(mockClaimRepository.findById(id)).thenReturn(Optional.of(claimEntity));

    claimService.deleteClaim(id);

    verify(mockClaimRepository).deleteById(id);
  }

  /** Should not delete a claim when it does not exist. */
  @Test
  void shouldNotDeleteClaim_whenClaimNotFoundThenThrowsException() {
    UUID id = UUID.randomUUID();
    when(mockClaimRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ClaimNotFoundException.class, () -> claimService.deleteClaim(id));

    verify(mockClaimRepository, never()).deleteById(id);
  }

  @Test
  void shouldAddEvidenceToClaim() {

    UUID claimId = UUID.randomUUID();

    ClaimEvidenceRequestBody claimEvidenceRequestBody =
        ClaimEvidenceRequestBody.builder().fileKey("Claim evidence file key").build();

    ClaimEntity savedClaimEntity =
        ClaimEntity.builder()
            .id(claimId)
            .ufn("UFN789")
            .client("Alice Example")
            .category("Category C")
            .concluded(LocalDate.of(2025, 7, 3))
            .feeType("Capped")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1500.0))
            .build();

    when(mockClaimRepository.findById(claimId)).thenReturn(Optional.of(savedClaimEntity));

    when(mockClaimEvidenceRepository.save(any(ClaimEvidenceEntity.class)))
        .thenAnswer(
            invocation -> {
              ClaimEvidenceEntity claimEvidenceEntity = invocation.getArgument(0);
              claimEvidenceEntity.setId(claimId);
              return claimEvidenceEntity;
            });

    UUID result = claimService.addEvidenceToClaim(claimId, claimEvidenceRequestBody);

    assertThat(result).isNotNull().isEqualTo(claimId);
  }

  @Test
  void shouldFailToAddEvidenceToClaimForSpecificFileName() {

    UUID claimId = UUID.randomUUID();

    ClaimEvidenceRequestBody claimEvidenceRequestBody =
        ClaimEvidenceRequestBody.builder().fileKey("upload_failure.pdf").build();

    assertThrows(
        StubException.class,
        () -> claimService.addEvidenceToClaim(claimId, claimEvidenceRequestBody));
  }

  @Test
  void shouldAddLineItemToClaim() {

    UUID claimId = UUID.randomUUID();

    LineItemRequestBody lineItemRequestBody =
        LineItemRequestBody.builder()
            .title("Line item title")
            .category("Line item category")
            .build();

    ClaimEntity savedClaimEntity =
        ClaimEntity.builder()
            .id(claimId)
            .ufn("UFN789")
            .client("Alice Example")
            .category("Category C")
            .concluded(LocalDate.of(2025, 7, 3))
            .feeType("Capped")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1500.0))
            .build();

    when(mockClaimRepository.findById(claimId)).thenReturn(Optional.of(savedClaimEntity));

    when(mockLineItemRepository.save(any(LineItemEntity.class)))
        .thenAnswer(
            invocation -> {
              LineItemEntity lineItemEntity = invocation.getArgument(0);
              lineItemEntity.setId(claimId);
              return lineItemEntity;
            });

    UUID result = claimService.addLineItemToClaim(claimId, lineItemRequestBody);

    assertThat(result).isNotNull().isEqualTo(claimId);
  }

  @Test
  void shouldDeleteEvidenceFromClaim() {

    UUID claimId = UUID.randomUUID();
    UUID lineItemId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();

    ClaimEntity claimEntity =
        ClaimEntity.builder()
            .id(claimId)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    LineItemEntity lineItemEntity =
        LineItemEntity.builder().id(lineItemId).claim(claimEntity).build();

    ClaimEvidenceEntity claimEvidenceEntity =
        ClaimEvidenceEntity.builder().id(evidenceId).claim(claimEntity).build();

    Set<ClaimEvidenceEntity> claimEvidenceEntities = new HashSet<>();
    claimEvidenceEntities.add(claimEvidenceEntity);
    lineItemEntity.setEvidenceItems(claimEvidenceEntities);

    List<LineItemEntity> lineItemEntities = new ArrayList<>();
    lineItemEntities.add(lineItemEntity);
    claimEntity.setLineItems(lineItemEntities);

    when(mockClaimRepository.findById(claimId)).thenReturn(Optional.of(claimEntity));
    when(mockClaimEvidenceRepository.findById(evidenceId))
        .thenReturn(Optional.of(claimEvidenceEntity));

    claimService.deleteEvidenceFromClaim(claimId, evidenceId);

    assertThat(claimEntity.getEvidence()).hasSize(0);
    assertThat(lineItemEntity.getEvidenceItems()).hasSize(0);
  }

  @Test
  void shouldFailToDeleteEvidenceFromClaimWhenClaimNotFound() {

    UUID claimId = UUID.randomUUID();

    when(mockClaimRepository.findById(claimId)).thenReturn(Optional.empty());

    assertThrows(
        ClaimNotFoundException.class,
        () -> claimService.deleteEvidenceFromClaim(claimId, UUID.randomUUID()));
  }

  @Test
  void shouldFailToDeleteEvidenceFromClaimWhenEvidenceNotFound() {

    UUID claimId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();

    ClaimEntity claimEntity =
        ClaimEntity.builder()
            .id(claimId)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    when(mockClaimRepository.findById(claimId)).thenReturn(Optional.of(claimEntity));
    when(mockClaimEvidenceRepository.findById(evidenceId)).thenReturn(Optional.empty());

    assertThrows(
        ClaimEvidenceNotFoundException.class,
        () -> claimService.deleteEvidenceFromClaim(claimId, evidenceId));
  }

  @Test
  void shouldFailToDeleteEvidenceFromClaimWhenEvidenceIsNotAlreadyUploadedToClaim() {

    UUID claimId1 = UUID.randomUUID();
    UUID claimId2 = UUID.randomUUID();
    UUID lineItemId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();

    ClaimEntity claimEntity1 =
        ClaimEntity.builder()
            .id(claimId1)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    when(mockClaimRepository.findById(claimId1)).thenReturn(Optional.of(claimEntity1));

    ClaimEntity claimEntity2 =
        ClaimEntity.builder()
            .id(claimId2)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    LineItemEntity lineItemEntity =
        LineItemEntity.builder().id(lineItemId).claim(claimEntity2).build();

    ClaimEvidenceEntity claimEvidenceEntity =
        ClaimEvidenceEntity.builder().id(evidenceId).claim(claimEntity2).build();

    Set<ClaimEvidenceEntity> claimEvidenceEntities = new HashSet<>();
    claimEvidenceEntities.add(claimEvidenceEntity);
    lineItemEntity.setEvidenceItems(claimEvidenceEntities);

    List<LineItemEntity> lineItemEntities = new ArrayList<>();
    lineItemEntities.add(lineItemEntity);
    claimEntity2.setLineItems(lineItemEntities);

    when(mockClaimEvidenceRepository.findById(evidenceId))
        .thenReturn(Optional.of(claimEvidenceEntity));

    assertThrows(
        ClaimEvidenceNotFoundException.class,
        () -> claimService.deleteEvidenceFromClaim(claimId1, evidenceId));
  }

  @Test
  void shouldFailToDeleteEvidenceFromClaimForSpecificFileName() {

    UUID claimId = UUID.randomUUID();
    UUID lineItemId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();

    ClaimEntity claimEntity =
        ClaimEntity.builder()
            .id(claimId)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    LineItemEntity lineItemEntity =
        LineItemEntity.builder().id(lineItemId).claim(claimEntity).build();

    ClaimEvidenceEntity claimEvidenceEntity =
        ClaimEvidenceEntity.builder()
            .id(evidenceId)
            .fileKey("deletion_failure.pdf")
            .claim(claimEntity)
            .build();

    Set<ClaimEvidenceEntity> claimEvidenceEntities = new HashSet<>();
    claimEvidenceEntities.add(claimEvidenceEntity);
    lineItemEntity.setEvidenceItems(claimEvidenceEntities);

    List<LineItemEntity> lineItemEntities = new ArrayList<>();
    lineItemEntities.add(lineItemEntity);
    claimEntity.setLineItems(lineItemEntities);

    when(mockClaimRepository.findById(claimId)).thenReturn(Optional.of(claimEntity));
    when(mockClaimEvidenceRepository.findById(evidenceId))
        .thenReturn(Optional.of(claimEvidenceEntity));

    assertThrows(
        StubException.class, () -> claimService.deleteEvidenceFromClaim(claimId, evidenceId));
  }

  @Test
  void shouldLinkEvidenceToLineItem() {

    UUID claimId1 = UUID.randomUUID();
    UUID lineItemId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();

    ClaimEntity claimEntity =
        ClaimEntity.builder()
            .id(claimId1)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    LineItemEntity lineItemEntity =
        LineItemEntity.builder().id(lineItemId).claim(claimEntity).build();

    ClaimEvidenceEntity claimEvidenceEntity =
        ClaimEvidenceEntity.builder().id(evidenceId).claim(claimEntity).build();

    when(mockClaimRepository.findById(claimId1)).thenReturn(Optional.of(claimEntity));
    when(mockLineItemRepository.findById(lineItemId)).thenReturn(Optional.of(lineItemEntity));
    when(mockClaimEvidenceRepository.findById(evidenceId))
        .thenReturn(Optional.of(claimEvidenceEntity));

    claimService.linkEvidenceToLineItem(claimId1, lineItemId, List.of(evidenceId));

    assertThat(lineItemEntity.getEvidenceItems()).contains(claimEvidenceEntity);
  }

  @Test
  void shouldFailToLinkEvidenceToLineItemWhenEvidenceIsNotAlreadyUploadedToClaim() {

    UUID claimId1 = UUID.randomUUID();
    UUID claimId2 = UUID.randomUUID();
    UUID lineItemId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();

    ClaimEntity claimEntity1 =
        ClaimEntity.builder()
            .id(claimId1)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    ClaimEntity claimEntity2 =
        ClaimEntity.builder()
            .id(claimId2)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    LineItemEntity lineItemEntity =
        LineItemEntity.builder().id(lineItemId).claim(claimEntity1).build();

    ClaimEvidenceEntity claimEvidenceEntity =
        ClaimEvidenceEntity.builder().id(evidenceId).claim(claimEntity2).build();

    when(mockClaimRepository.findById(claimId1)).thenReturn(Optional.of(claimEntity1));
    when(mockLineItemRepository.findById(lineItemId)).thenReturn(Optional.of(lineItemEntity));
    when(mockClaimEvidenceRepository.findById(evidenceId))
        .thenReturn(Optional.of(claimEvidenceEntity));

    assertThrows(
        ClaimEvidenceNotFoundException.class,
        () -> claimService.linkEvidenceToLineItem(claimId1, lineItemId, List.of(evidenceId)));
  }

  @Test
  void shouldLinkMultipleEvidenceToLineItem() {

    UUID claimId1 = UUID.randomUUID();
    UUID lineItemId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();
    UUID evidenceId2 = UUID.randomUUID();

    ClaimEntity claimEntity =
        ClaimEntity.builder()
            .id(claimId1)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    LineItemEntity lineItemEntity =
        LineItemEntity.builder().id(lineItemId).claim(claimEntity).build();

    ClaimEvidenceEntity claimEvidenceEntity1 =
        ClaimEvidenceEntity.builder().id(evidenceId).claim(claimEntity).build();

    ClaimEvidenceEntity claimEvidenceEntity2 =
        ClaimEvidenceEntity.builder().id(evidenceId2).claim(claimEntity).build();

    when(mockClaimRepository.findById(claimId1)).thenReturn(Optional.of(claimEntity));
    when(mockLineItemRepository.findById(lineItemId)).thenReturn(Optional.of(lineItemEntity));
    when(mockClaimEvidenceRepository.findById(evidenceId))
        .thenReturn(Optional.of(claimEvidenceEntity1));
    when(mockClaimEvidenceRepository.findById(evidenceId2))
        .thenReturn(Optional.of(claimEvidenceEntity2));

    claimService.linkEvidenceToLineItem(claimId1, lineItemId, List.of(evidenceId, evidenceId2));

    assertThat(lineItemEntity.getEvidenceItems()).contains(claimEvidenceEntity1);
  }

  @Test
  void shouldUnlinkEvidenceFromLineItem() {

    UUID claimId1 = UUID.randomUUID();
    UUID lineItemId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();

    ClaimEntity claimEntity =
        ClaimEntity.builder()
            .id(claimId1)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    ClaimEvidenceEntity claimEvidenceEntity =
        ClaimEvidenceEntity.builder().id(evidenceId).claim(claimEntity).build();

    Set<ClaimEvidenceEntity> claimEvidenceEntities = new HashSet<>();
    claimEvidenceEntities.add(claimEvidenceEntity);

    LineItemEntity lineItemEntity =
        LineItemEntity.builder()
            .id(lineItemId)
            .claim(claimEntity)
            .evidenceItems(claimEvidenceEntities)
            .build();

    when(mockClaimRepository.findById(claimId1)).thenReturn(Optional.of(claimEntity));
    when(mockLineItemRepository.findById(lineItemId)).thenReturn(Optional.of(lineItemEntity));
    when(mockClaimEvidenceRepository.findById(evidenceId))
        .thenReturn(Optional.of(claimEvidenceEntity));

    claimService.unlinkEvidenceFromLineItem(claimId1, lineItemId, evidenceId);

    assertThat(lineItemEntity.getEvidenceItems()).hasSize(0);
  }

  @Test
  void shouldFailToUnlinkEvidenceFromLineItemWhenClaimNotFound() {

    UUID claimId1 = UUID.randomUUID();
    UUID lineItemId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();

    when(mockClaimRepository.findById(claimId1)).thenReturn(Optional.empty());

    assertThrows(
        ClaimNotFoundException.class,
        () -> claimService.unlinkEvidenceFromLineItem(claimId1, lineItemId, evidenceId));
  }

  @Test
  void shouldFailToUnlinkEvidenceFromLineItemWhenLineItemNotFound() {

    UUID claimId1 = UUID.randomUUID();
    UUID lineItemId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();

    ClaimEntity claimEntity =
        ClaimEntity.builder()
            .id(claimId1)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    when(mockClaimRepository.findById(claimId1)).thenReturn(Optional.of(claimEntity));
    when(mockLineItemRepository.findById(lineItemId)).thenReturn(Optional.empty());

    assertThrows(
        LineItemNotFoundException.class,
        () -> claimService.unlinkEvidenceFromLineItem(claimId1, lineItemId, evidenceId));
  }

  @Test
  void shouldFailToUnlinkEvidenceFromLineItemWhenEvidenceNotFound() {

    UUID claimId1 = UUID.randomUUID();
    UUID lineItemId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();

    ClaimEntity claimEntity =
        ClaimEntity.builder()
            .id(claimId1)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    LineItemEntity lineItemEntity =
        LineItemEntity.builder().id(lineItemId).claim(claimEntity).build();

    when(mockClaimRepository.findById(claimId1)).thenReturn(Optional.of(claimEntity));
    when(mockLineItemRepository.findById(lineItemId)).thenReturn(Optional.of(lineItemEntity));
    when(mockClaimEvidenceRepository.findById(evidenceId)).thenReturn(Optional.empty());

    assertThrows(
        ClaimEvidenceNotFoundException.class,
        () -> claimService.unlinkEvidenceFromLineItem(claimId1, lineItemId, evidenceId));
  }

  @Test
  void shouldFailToUnlinkEvidenceFromLineItemWhenLineItemDoesNotBelongToClaim() {

    UUID claimId1 = UUID.randomUUID();
    UUID claimId2 = UUID.randomUUID();
    UUID lineItemId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();

    ClaimEntity claimEntity1 =
        ClaimEntity.builder()
            .id(claimId1)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    ClaimEntity claimEntity2 =
        ClaimEntity.builder()
            .id(claimId2)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    ClaimEvidenceEntity claimEvidenceEntity =
        ClaimEvidenceEntity.builder().id(evidenceId).claim(claimEntity1).build();

    Set<ClaimEvidenceEntity> claimEvidenceEntities = new HashSet<>();
    claimEvidenceEntities.add(claimEvidenceEntity);

    LineItemEntity lineItemEntity =
        LineItemEntity.builder()
            .id(lineItemId)
            .claim(claimEntity2)
            .evidenceItems(claimEvidenceEntities)
            .build();

    when(mockClaimRepository.findById(claimId1)).thenReturn(Optional.of(claimEntity1));
    when(mockLineItemRepository.findById(lineItemId)).thenReturn(Optional.of(lineItemEntity));
    when(mockClaimEvidenceRepository.findById(evidenceId))
        .thenReturn(Optional.of(claimEvidenceEntity));

    assertThrows(
        LineItemNotFoundException.class,
        () -> claimService.unlinkEvidenceFromLineItem(claimId1, lineItemId, evidenceId));
  }

  @Test
  void shouldFailToUnlinkEvidenceFromLineItemWhenEvidenceDoesNotBelongToLineItem() {

    UUID claimId1 = UUID.randomUUID();
    UUID lineItemId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();

    ClaimEntity claimEntity =
        ClaimEntity.builder()
            .id(claimId1)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    ClaimEvidenceEntity claimEvidenceEntity =
        ClaimEvidenceEntity.builder().id(evidenceId).claim(claimEntity).build();

    LineItemEntity lineItemEntity =
        LineItemEntity.builder().id(lineItemId).claim(claimEntity).build();

    when(mockClaimRepository.findById(claimId1)).thenReturn(Optional.of(claimEntity));
    when(mockLineItemRepository.findById(lineItemId)).thenReturn(Optional.of(lineItemEntity));
    when(mockClaimEvidenceRepository.findById(evidenceId))
        .thenReturn(Optional.of(claimEvidenceEntity));

    assertThrows(
        ClaimEvidenceNotFoundException.class,
        () -> claimService.unlinkEvidenceFromLineItem(claimId1, lineItemId, evidenceId));
  }

  @Test
  void shouldFailToUnlinkEvidenceFromLineItemWhenEvidenceDoesNotBelongToClaim() {

    UUID claimId1 = UUID.randomUUID();
    UUID claimId2 = UUID.randomUUID();
    UUID lineItemId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();

    ClaimEntity claimEntity1 =
        ClaimEntity.builder()
            .id(claimId1)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    ClaimEntity claimEntity2 =
        ClaimEntity.builder()
            .id(claimId2)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    ClaimEvidenceEntity claimEvidenceEntity =
        ClaimEvidenceEntity.builder().id(evidenceId).claim(claimEntity2).build();

    Set<ClaimEvidenceEntity> claimEvidenceEntities = new HashSet<>();
    claimEvidenceEntities.add(claimEvidenceEntity);

    LineItemEntity lineItemEntity =
        LineItemEntity.builder()
            .id(lineItemId)
            .claim(claimEntity1)
            .evidenceItems(claimEvidenceEntities)
            .build();

    when(mockClaimRepository.findById(claimId1)).thenReturn(Optional.of(claimEntity1));
    when(mockLineItemRepository.findById(lineItemId)).thenReturn(Optional.of(lineItemEntity));
    when(mockClaimEvidenceRepository.findById(evidenceId))
        .thenReturn(Optional.of(claimEvidenceEntity));

    assertThrows(
        ClaimEvidenceNotFoundException.class,
        () -> claimService.unlinkEvidenceFromLineItem(claimId1, lineItemId, evidenceId));
  }

  @Test
  void shouldFailToUnlinkEvidenceFromLineItemForSpecificFileName() {

    UUID claimId1 = UUID.randomUUID();
    UUID lineItemId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();

    ClaimEntity claimEntity =
        ClaimEntity.builder()
            .id(claimId1)
            .ufn("UFN123")
            .client("John Doe")
            .category("Category A")
            .concluded(LocalDate.of(2025, 7, 1))
            .feeType("Fixed")
            .escaped(false)
            .counselPayment("Paid and Reconciled")
            .claimed(new BigDecimal(1000.0))
            .build();

    ClaimEvidenceEntity claimEvidenceEntity =
        ClaimEvidenceEntity.builder()
            .id(evidenceId)
            .fileKey("deletion_failure.pdf")
            .claim(claimEntity)
            .build();

    Set<ClaimEvidenceEntity> claimEvidenceEntities = new HashSet<>();
    claimEvidenceEntities.add(claimEvidenceEntity);

    LineItemEntity lineItemEntity =
        LineItemEntity.builder()
            .id(lineItemId)
            .claim(claimEntity)
            .evidenceItems(claimEvidenceEntities)
            .build();

    when(mockClaimRepository.findById(claimId1)).thenReturn(Optional.of(claimEntity));
    when(mockLineItemRepository.findById(lineItemId)).thenReturn(Optional.of(lineItemEntity));
    when(mockClaimEvidenceRepository.findById(evidenceId))
        .thenReturn(Optional.of(claimEvidenceEntity));

    assertThrows(
        StubException.class,
        () -> claimService.unlinkEvidenceFromLineItem(claimId1, lineItemId, evidenceId));
  }
}
