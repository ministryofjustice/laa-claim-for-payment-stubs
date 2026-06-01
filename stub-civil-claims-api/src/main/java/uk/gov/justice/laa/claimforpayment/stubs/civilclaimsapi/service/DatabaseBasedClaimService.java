package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEvidenceEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.LineItemEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.ClaimEvidenceNotFoundException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.ClaimNotFoundException;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.exception.LineItemNotFoundException;
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
  public Claim getClaim(Long claimId) {
    ClaimEntity claimEntity = checkIfClaimExist(claimId);
    return claimMapper.toClaim(claimEntity);
  }

  /**
   * Creates a claim.
   *
   * @param claimRequestBody the claim to be created
   * @return the id of the created claim
   */
  @Override
  public Long createClaim(ClaimRequestBody claimRequestBody, UUID providerUserId) {
    ClaimEntity claimEntity = new ClaimEntity();
    claimEntity.setUfn(claimRequestBody.getUfn());
    claimEntity.setClient(claimRequestBody.getClient());
    claimEntity.setCategory(claimRequestBody.getCategory());
    claimEntity.setConcluded(claimRequestBody.getConcluded());
    claimEntity.setFeeType(claimRequestBody.getFeeType());
    claimEntity.setEscaped(claimRequestBody.getEscaped());
    claimEntity.setCounselPayment(claimRequestBody.getCounselPayment());
    claimEntity.setClaimed(claimRequestBody.getClaimed());
    claimEntity.setProviderUserId(providerUserId);
    claimEntity.setSubmissionId(claimRequestBody.getSubmissionId());

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
  public void updateClaim(Long id, ClaimRequestBody claimRequestBody) {
    ClaimEntity claimEntity = checkIfClaimExist(id);
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
  public void deleteClaim(Long id) {
    checkIfClaimExist(id);
    claimRepository.deleteById(id);
  }

  private ClaimEntity checkIfClaimExist(Long id) throws ClaimNotFoundException {
    return claimRepository
        .findById(id)
        .orElseThrow(
            () -> new ClaimNotFoundException(String.format("No claim found with id: %s", id)));
  }

  private LineItemEntity checkIfLineItemExist(Long id) throws LineItemNotFoundException {
    return lineItemRepository
        .findById(id)
        .orElseThrow(
            () ->
                new LineItemNotFoundException(String.format("No line item found with id: %s", id)));
  }

  private ClaimEvidenceEntity checkIfClaimEvidenceExists(Long id)
      throws ClaimEvidenceNotFoundException {
    return claimEvidenceRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ClaimEvidenceNotFoundException(
                    String.format("No claim evidence found with id: %s", id)));
  }

  @Override
  public Page<Claim> getAllClaimsForProvider(UUID providerUserId, int pageNumber, int pageSize) {
    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    Page<ClaimEntity> claimPage = claimRepository.findByProviderUserId(providerUserId, pageable);
    return claimPage.map(claimMapper::toClaim);
  }

  @Override
  public Long addLineItemToClaim(Long claimId, LineItemRequestBody lineItemRequestBody) {
    ClaimEntity claimEntity = checkIfClaimExist(claimId);
    LineItemEntity newLineItemEntity = new LineItemEntity();
    newLineItemEntity.setTitle(lineItemRequestBody.getTitle());
    newLineItemEntity.setClaim(claimEntity);

    LineItemEntity savedItem = lineItemRepository.save(newLineItemEntity);

    return savedItem.getId();
  }

  @Override
  public Long addEvidenceToClaim(Long claimId, ClaimEvidenceRequestBody claimEvidenceRequestBody) {
    ClaimEntity claimEntity = checkIfClaimExist(claimId);
    ClaimEvidenceEntity newEvidenceEntity = new ClaimEvidenceEntity();
    newEvidenceEntity.setFileKey(claimEvidenceRequestBody.getFileKey());
    newEvidenceEntity.setFileSize(claimEvidenceRequestBody.getFileSize());
    newEvidenceEntity.setClaim(claimEntity);
    ClaimEvidenceEntity savedEvidence = claimEvidenceRepository.save(newEvidenceEntity);
    return savedEvidence.getId();
  }

  @Override
  @Transactional
  public void deleteEvidenceFromClaim(Long claimId, Long evidenceId) {
    ClaimEntity claimEntity = checkIfClaimExist(claimId);
    ClaimEvidenceEntity evidenceEntity = checkIfClaimEvidenceExists(evidenceId);
    if (!evidenceEntity.getClaim().getId().equals(claimEntity.getId())) {
      throw new ClaimEvidenceNotFoundException(
          String.format(
              "Evidence with id: %s has not been uploaded to claim with id: %s",
              evidenceEntity, claimId));
    }
    for (LineItemEntity lineItemEntity : claimEntity.getLineItems()) {
      lineItemEntity.getEvidenceItems().remove(evidenceEntity);
    }
    claimEntity.getEvidence().remove(evidenceEntity);
    claimEvidenceRepository.deleteById(evidenceId);
  }

  @Override
  public void linkEvidenceToLineItem(Long claimId, Long lineItemId, List<Long> evidenceIds) {
    ClaimEntity claimEntity = checkIfClaimExist(claimId);
    LineItemEntity lineItemEntity = checkIfLineItemExist(lineItemId);
    List<ClaimEvidenceEntity> evidenceEntities = evidenceIds.stream().map(
        this::checkIfClaimEvidenceExists).toList();

    if (!lineItemEntity.getClaim().getId().equals(claimEntity.getId())) {
      throw new LineItemNotFoundException(
          String.format(
              "Line item with id: %s does not belong to claim with id: %s", lineItemId, claimId));
    }
    lineItemEntity.getEvidenceItems().addAll(evidenceEntities);
    lineItemRepository.save(lineItemEntity);
  }

  @Override
  public void unlinkEvidenceFromLineItem(Long claimId, Long lineItemId, Long evidenceId) {
    ClaimEntity claimEntity = checkIfClaimExist(claimId);
    LineItemEntity lineItemEntity = checkIfLineItemExist(lineItemId);
    ClaimEvidenceEntity evidenceEntity = checkIfClaimEvidenceExists(evidenceId);

    if (!lineItemEntity.getClaim().getId().equals(claimEntity.getId())) {
      throw new LineItemNotFoundException(
          String.format(
              "Line item with id: %s does not belong to claim with id: %s", lineItemId, claimId));
    }

    if (!lineItemEntity.getEvidenceItems().stream()
        .map(ClaimEvidenceEntity::getId)
        .toList()
        .contains(evidenceEntity.getId())) {
      throw new ClaimEvidenceNotFoundException(
          String.format(
              "Evidence with id: %s does not belong to line item with id: %s",
              evidenceEntity, lineItemId));
    }

    lineItemEntity.getEvidenceItems().remove(evidenceEntity);
    lineItemRepository.save(lineItemEntity);
  }
}
