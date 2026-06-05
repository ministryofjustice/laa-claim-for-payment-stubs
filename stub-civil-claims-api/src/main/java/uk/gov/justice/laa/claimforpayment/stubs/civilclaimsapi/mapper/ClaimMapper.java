package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import org.mapstruct.Mapper;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.Claim;

/** The mapper between ClaimEntity and Claim. */
@Mapper(
    componentModel = "spring",
    uses = {LineItemMapper.class}
)
public interface ClaimMapper {

  /**
   * Maps the given claim entity to a claim.
   *
   * @param claimEntity the claim entity
   * @return the claim
   */
  Claim toClaim(ClaimEntity claimEntity);
}
