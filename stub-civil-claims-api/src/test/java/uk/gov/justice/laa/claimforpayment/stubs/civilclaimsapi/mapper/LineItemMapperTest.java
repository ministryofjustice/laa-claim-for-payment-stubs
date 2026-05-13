package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.LineItemEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.LineItem;

class LineItemMapperTest {

  @InjectMocks private LineItemMapper lineItemMapper = new LineItemMapperImpl();

  @Test
  void shouldMapToLineItemEntity() {

    LineItem lineItem = LineItem.builder().title("Test line item").category("category1").build();
    LineItemEntity lineItemEntity = lineItemMapper.toLineItemEntity(lineItem);

    assertThat(lineItemEntity).isNotNull();
    assertThat(lineItemEntity.getTitle()).isEqualTo(lineItem.getTitle());
    assertThat(lineItemEntity.getCategory()).isEqualTo(lineItem.getCategory());
    assertThat(lineItemEntity.getDate()).isEqualTo(lineItem.getDate());
  }

  @Test
  void shouldMapToLineItem() {
    LineItemEntity lineItemEntity =
        LineItemEntity.builder().title("Test line item").category("category1").build();
    LineItem lineItem = lineItemMapper.toLineItem(lineItemEntity);

    assertThat(lineItem).isNotNull();
    assertThat(lineItem.getTitle()).isEqualTo(lineItemEntity.getTitle());
    assertThat(lineItem.getCategory()).isEqualTo(lineItemEntity.getCategory());
    assertThat(lineItem.getDate()).isEqualTo(lineItemEntity.getDate());
  }
}
