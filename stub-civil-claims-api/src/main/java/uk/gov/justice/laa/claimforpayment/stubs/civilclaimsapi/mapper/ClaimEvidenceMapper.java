package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEvidenceEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.ClaimEvidence;

/** Maps between ClaimEvidence and ClaimEvidenceEntity. */
@Mapper(componentModel = "spring")
public interface ClaimEvidenceMapper {

  ClaimEvidence toClaimEvidence(ClaimEvidenceEntity claimEvidenceEntity);

  @Mapping(target = "lineItems", ignore = true)
  ClaimEvidenceEntity toClaimEvidenceEntity(ClaimEvidence claimEvidence);
}
