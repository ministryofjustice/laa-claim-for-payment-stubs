package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEvidenceEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.LineItemEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.ClaimEvidenceNotFoundException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.ClaimNotFoundException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.LineItemNotFoundException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.StubException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper.ClaimMapper;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.Claim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.ClaimEvidenceRequestBody;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.ClaimRequestBody;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.LineItemRequestBody;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.repository.ClaimEvidenceRepository;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.repository.ClaimRepository;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.repository.LineItemRepository;

/** Service class for handling claims requests. */
@RequiredArgsConstructor
@Service
public class DatabaseBasedClaimService implements ClaimServiceInterface {

  private final ClaimRepository claimRepository;
  private final LineItemRepository lineItemRepository;
  private final ClaimMapper claimMapper;
  private final ClaimEvidenceRepository claimEvidenceRepository;

  private static final String ADD_EVIDENCE_FAILURE_FILE_KEY = "upload_failure.pdf";
  private static final String DELETE_EVIDENCE_FAILURE_FILE_KEY = "deletion_failure.pdf";

  /**
   * Gets all claims.
   *
   * @return the list of claims
   */
  @Override
  public List<Claim> getClaims() {
    return claimRepository.findAll().stream().map(claimMapper::toClaim).toList();
  }

  /**
   * Gets a claim for a given id.
   *
   * @param claimId the claim id
   * @return the requested claim
   */
  @Override
  public Claim getClaim(UUID claimId) {
    ClaimEntity claimEntity = checkIfClaimExists(claimId);
    return claimMapper.toClaim(claimEntity);
  }

  /**
   * Creates a claim.
   *
   * @param claimRequestBody the claim to be created
   * @return the id of the created claim
   */
  @Override
  public UUID createClaim(ClaimRequestBody claimRequestBody, UUID providerUserId) {
    ClaimEntity claimEntity = new ClaimEntity();
    claimEntity.setId(claimRequestBody.getId());
    claimEntity.setUfn(claimRequestBody.getUfn());
    claimEntity.setClient(claimRequestBody.getClient());
    claimEntity.setCategory(claimRequestBody.getCategory());
    claimEntity.setConcluded(claimRequestBody.getConcluded());
    claimEntity.setFeeType(claimRequestBody.getFeeType());
    claimEntity.setEscaped(claimRequestBody.getEscaped());
    claimEntity.setCounselPayment(claimRequestBody.getCounselPayment());
    claimEntity.setClaimed(claimRequestBody.getClaimed());
    claimEntity.setProviderUserId(providerUserId);

    ClaimEntity createdClaimEntity = claimRepository.save(claimEntity);
    return createdClaimEntity.getId();
  }

  /**
   * Updates a claim.
   *
   * @param id the id of the claim to be updated
   * @param claimRequestBody the updated claim
   */
  @Override
  public void updateClaim(UUID id, ClaimRequestBody claimRequestBody) {
    ClaimEntity claimEntity = checkIfClaimExists(id);
    claimEntity.setUfn(claimRequestBody.getUfn());
    claimEntity.setClient(claimRequestBody.getClient());
    claimEntity.setCategory(claimRequestBody.getCategory());
    claimEntity.setConcluded(claimRequestBody.getConcluded());
    claimEntity.setFeeType(claimRequestBody.getFeeType());
    claimEntity.setEscaped(claimRequestBody.getEscaped());
    claimEntity.setCounselPayment(claimRequestBody.getCounselPayment());
    claimEntity.setClaimed(claimRequestBody.getClaimed());
    claimRepository.save(claimEntity);
  }

  /**
   * Deletes a claim.
   *
   * @param id the id of the claim to be deleted
   */
  @Override
  public void deleteClaim(UUID id) {
    checkIfClaimExists(id);
    claimRepository.deleteById(id);
  }

  private ClaimEntity checkIfClaimExists(UUID id) throws ClaimNotFoundException {
    return claimRepository
        .findById(id)
        .orElseThrow(
            () -> new ClaimNotFoundException(String.format("No claim found with id: %s", id)));
  }

  private LineItemEntity checkIfLineItemExists(UUID id) throws LineItemNotFoundException {
    return lineItemRepository
        .findById(id)
        .orElseThrow(
            () ->
                new LineItemNotFoundException(String.format("No line item found with id: %s", id)));
  }

  private ClaimEvidenceEntity checkIfEvidenceExists(UUID id) throws ClaimEvidenceNotFoundException {
    return claimEvidenceRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ClaimEvidenceNotFoundException(
                    String.format("No claim evidence found with id: %s", id)));
  }

  private void checkIfEvidenceExistsForClaim(
      ClaimEvidenceEntity evidenceEntity, ClaimEntity claimEntity) throws ClaimNotFoundException {
    UUID claimId = claimEntity.getId();
    if (!evidenceEntity.getClaim().getId().equals(claimId)) {
      throw new ClaimEvidenceNotFoundException(
          String.format(
              "Evidence with id: %s does not belong to claim with id: %s",
              evidenceEntity.getId(), claimId));
    }
  }

  private void checkIfLineItemExistsForClaim(LineItemEntity lineItemEntity, ClaimEntity claimEntity)
      throws ClaimNotFoundException {
    UUID claimId = claimEntity.getId();
    if (!lineItemEntity.getClaim().getId().equals(claimEntity.getId())) {
      throw new LineItemNotFoundException(
          String.format(
              "Line item with id: %s does not belong to claim with id: %s",
              lineItemEntity.getId(), claimId));
    }
  }

  private void checkIfEvidenceExistsForLineItem(
      ClaimEvidenceEntity evidenceEntity, LineItemEntity lineItemEntity)
      throws ClaimNotFoundException {
    UUID evidenceId = evidenceEntity.getId();
    if (!lineItemEntity.getEvidenceItems().stream()
        .map(ClaimEvidenceEntity::getId)
        .toList()
        .contains(evidenceId)) {
      throw new ClaimEvidenceNotFoundException(
          String.format(
              "Evidence with id: %s does not belong to line item with id: %s",
              evidenceId, lineItemEntity.getId()));
    }
  }

  @Override
  public Page<Claim> getAllClaimsForProvider(UUID providerUserId, int pageNumber, int pageSize) {
    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    Page<ClaimEntity> claimPage = claimRepository.findByProviderUserId(providerUserId, pageable);
    return claimPage.map(claimMapper::toClaim);
  }

  @Override
  public UUID addLineItemToClaim(UUID claimId, LineItemRequestBody lineItemRequestBody) {
    ClaimEntity claimEntity = checkIfClaimExists(claimId);
    LineItemEntity newLineItemEntity = new LineItemEntity();
    newLineItemEntity.setId(lineItemRequestBody.getId());
    newLineItemEntity.setTitle(lineItemRequestBody.getTitle());
    newLineItemEntity.setClaim(claimEntity);

    LineItemEntity savedItem = lineItemRepository.save(newLineItemEntity);

    return savedItem.getId();
  }

  @Override
  public UUID addEvidenceToClaim(UUID claimId, ClaimEvidenceRequestBody claimEvidenceRequestBody) {
    if (ADD_EVIDENCE_FAILURE_FILE_KEY.equals(claimEvidenceRequestBody.getFileKey())) {
      throw new StubException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Simulating an add evidence failure");
    }
    ClaimEntity claimEntity = checkIfClaimExists(claimId);
    ClaimEvidenceEntity newEvidenceEntity = new ClaimEvidenceEntity();
    newEvidenceEntity.setId(claimEvidenceRequestBody.getId());
    newEvidenceEntity.setFileKey(claimEvidenceRequestBody.getFileKey());
    newEvidenceEntity.setFileSize(claimEvidenceRequestBody.getFileSize());
    newEvidenceEntity.setSubmittedOn(claimEvidenceRequestBody.getSubmittedOn());
    newEvidenceEntity.setClaim(claimEntity);
    ClaimEvidenceEntity savedEvidence = claimEvidenceRepository.save(newEvidenceEntity);
    return savedEvidence.getId();
  }

  @Override
  @Transactional
  public void deleteEvidenceFromClaim(UUID claimId, UUID evidenceId) {
    ClaimEntity claimEntity = checkIfClaimExists(claimId);
    ClaimEvidenceEntity evidenceEntity = checkIfEvidenceExists(evidenceId);
    checkIfEvidenceExistsForClaim(evidenceEntity, claimEntity);
    if (DELETE_EVIDENCE_FAILURE_FILE_KEY.equals(evidenceEntity.getFileKey())) {
      throw new StubException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Simulating a delete evidence failure");
    }
    for (LineItemEntity lineItemEntity : claimEntity.getLineItems()) {
      lineItemEntity.getEvidenceItems().remove(evidenceEntity);
    }
    claimEntity.getEvidence().remove(evidenceEntity);
    claimEvidenceRepository.deleteById(evidenceId);
  }

  @Override
  public void linkEvidenceToLineItem(UUID claimId, UUID lineItemId, List<UUID> evidenceIds) {
    ClaimEntity claimEntity = checkIfClaimExists(claimId);
    LineItemEntity lineItemEntity = checkIfLineItemExists(lineItemId);
    checkIfLineItemExistsForClaim(lineItemEntity, claimEntity);
    List<ClaimEvidenceEntity> evidenceEntities =
        evidenceIds.stream().map(this::checkIfEvidenceExists).toList();
    evidenceEntities.forEach(
        evidenceEntity -> checkIfEvidenceExistsForClaim(evidenceEntity, claimEntity));
    lineItemEntity.getEvidenceItems().addAll(evidenceEntities);
    lineItemRepository.save(lineItemEntity);
  }

  @Override
  public void unlinkEvidenceFromLineItem(UUID claimId, UUID lineItemId, UUID evidenceId) {
    ClaimEntity claimEntity = checkIfClaimExists(claimId);
    LineItemEntity lineItemEntity = checkIfLineItemExists(lineItemId);
    ClaimEvidenceEntity evidenceEntity = checkIfEvidenceExists(evidenceId);
    checkIfLineItemExistsForClaim(lineItemEntity, claimEntity);
    checkIfEvidenceExistsForLineItem(evidenceEntity, lineItemEntity);
    checkIfEvidenceExistsForClaim(evidenceEntity, claimEntity);
    if (DELETE_EVIDENCE_FAILURE_FILE_KEY.equals(evidenceEntity.getFileKey())) {
      throw new StubException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Simulating an unlink evidence failure");
    }
    lineItemEntity.getEvidenceItems().remove(evidenceEntity);
    lineItemRepository.save(lineItemEntity);
  }
}
