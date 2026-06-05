package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEvidenceEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.LineItemEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.LineItem;

/** Maps between LineItemEntity and LineItem. */
@Mapper(componentModel = "spring")
public interface LineItemMapper {

  LineItem toLineItem(LineItemEntity lineItemEntity);

  /** Maps a list of ClaimEvidenceEntity to a list of just the identifiers. */
  default List<Long> mapEvidenceItems(Set<ClaimEvidenceEntity> evidenceItems) {
    if (evidenceItems == null) {
      return List.of();
    }

    return evidenceItems.stream()
        .map(ClaimEvidenceEntity::getId)
        .sorted()
        .toList();
  }
}
