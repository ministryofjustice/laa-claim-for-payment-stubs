package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import org.mapstruct.Mapper;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEvidenceEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.ClaimEvidence;

/** Maps between ClaimEvidenceEntity and ClaimEvidence. */
@Mapper(componentModel = "spring")
public interface ClaimEvidenceMapper {

  ClaimEvidence toClaimEvidence(ClaimEvidenceEntity claimEvidenceEntity);
}
