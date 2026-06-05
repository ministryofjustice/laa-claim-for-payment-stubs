package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEvidenceEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.LineItemEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.LineItem;

class LineItemMapperTest {

  private final LineItemMapper lineItemMapper =
      Mappers.getMapper(LineItemMapper.class);

  @Test
  void shouldMapToLineItem() {
    Long evidenceEntity1Id = 1L;
    Long evidenceEntity2Id = 2L;

    ClaimEvidenceEntity evidenceEntity1 =
        ClaimEvidenceEntity.builder()
            .id(evidenceEntity1Id)
            .fileKey("file-1.pdf")
            .fileSize(1000L)
            .build();

    ClaimEvidenceEntity evidenceEntity2 =
        ClaimEvidenceEntity.builder()
            .id(evidenceEntity2Id)
            .fileKey("file-2.pdf")
            .fileSize(2000L)
            .build();

    LineItemEntity lineItemEntity =
        LineItemEntity.builder()
            .id(1L)
            .title("Test line item")
            .category("category1")
            .date(LocalDate.of(2020, 1, 1))
            .evidenceItems(Set.of(evidenceEntity1, evidenceEntity2))
            .build();

    LineItem result = lineItemMapper.toLineItem(lineItemEntity);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(lineItemEntity.getId());
    assertThat(result.getTitle()).isEqualTo(lineItemEntity.getTitle());
    assertThat(result.getCategory()).isEqualTo(lineItemEntity.getCategory());
    assertThat(result.getDate()).isEqualTo(lineItemEntity.getDate());
    assertThat(result.getEvidenceItems()).containsExactly(evidenceEntity1Id, evidenceEntity2Id);
  }
}
