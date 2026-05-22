package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import org.mapstruct.Mapper;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.LineItemEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.LineItem;

/** Maps between LineItem and LineItemEntity. */
@Mapper(componentModel = "spring")
public interface LineItemMapper {

  LineItem toLineItem(LineItemEntity lineItemEntity);

  LineItemEntity toLineItemEntity(LineItem lineItem);
}
