package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEvidenceEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.LineItemEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.Claim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.ClaimEvidence;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.LineItem;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ClaimMapperImpl.class, LineItemMapperImpl.class})
class ClaimMapperTest {
  private static final Long CLAIM_ID = 123L;
  private static final String UFN = "UFN123";
  private static final String CLIENT = "John Doe";
  private static final String CATEGORY = "A";
  private static final LocalDate CONCLUDED = LocalDate.of(2024, 7, 7);
  private static final String FEE_TYPE = "Standard";
  private static final Boolean ESCAPED = false;
  private static final String COUNSEL_PAYMENT = "Paid and Reconciled";
  private static final BigDecimal CLAIMED = new BigDecimal(100.0);
  private static final Long LINE_ITEM_ID_1 = 1L;
  private static final Long LINE_ITEM_ID_2 = 2L;
  private static final Long EVIDENCE_ID_1 = 10L;
  private static final Long EVIDENCE_ID_2 = 20L;
  private static final ClaimEvidence CLAIM_EVIDENCE_1 =
      ClaimEvidence.builder().id(EVIDENCE_ID_1).fileKey("fileKey1").fileSize(1000L).build();
  private static final ClaimEvidence CLAIM_EVIDENCE_2 =
      ClaimEvidence.builder().id(EVIDENCE_ID_2).fileKey("fileKey2").fileSize(2000L).build();
  private static final LineItem LINE_ITEM_1 =
      LineItem.builder().id(LINE_ITEM_ID_1).evidenceItems(List.of(EVIDENCE_ID_1)).build();
  private static final LineItem LINE_ITEM_2 =
      LineItem.builder()
          .id(LINE_ITEM_ID_2)
          .evidenceItems(List.of(EVIDENCE_ID_1, EVIDENCE_ID_2))
          .build();

  @Autowired
  private ClaimMapper claimMapper;

  @Test
  void shouldMapToClaim() {
    ClaimEvidenceEntity claimEvidence1 =
        ClaimEvidenceEntity.builder().id(EVIDENCE_ID_1).fileKey("fileKey1").fileSize(1000L).build();

    ClaimEvidenceEntity claimEvidence2 =
        ClaimEvidenceEntity.builder().id(EVIDENCE_ID_2).fileKey("fileKey2").fileSize(2000L).build();

    LineItemEntity lineItem1 =
        LineItemEntity.builder().id(LINE_ITEM_ID_1).evidenceItems(Set.of(claimEvidence1)).build();

    LineItemEntity lineItem2 =
        LineItemEntity.builder().id(LINE_ITEM_ID_2).evidenceItems(Set.of(claimEvidence1, claimEvidence2)).build();

    ClaimEntity claimEntity =
        ClaimEntity.builder()
            .id(CLAIM_ID)
            .ufn(UFN)
            .client(CLIENT)
            .category(CATEGORY)
            .concluded(CONCLUDED)
            .feeType(FEE_TYPE)
            .escaped(ESCAPED)
            .counselPayment(COUNSEL_PAYMENT)
            .claimed(CLAIMED)
            .lineItems(List.of(lineItem1, lineItem2))
            .evidence(List.of(claimEvidence1, claimEvidence2))
            .build();

    Claim result = claimMapper.toClaim(claimEntity);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(CLAIM_ID);
    assertThat(result.getUfn()).isEqualTo(UFN);
    assertThat(result.getClient()).isEqualTo(CLIENT);
    assertThat(result.getCategory()).isEqualTo(CATEGORY);
    assertThat(result.getConcluded()).isEqualTo(CONCLUDED);
    assertThat(result.getFeeType()).isEqualTo(FEE_TYPE);
    assertThat(result.getEscaped()).isEqualTo(ESCAPED);
    assertThat(result.getCounselPayment()).isEqualTo(COUNSEL_PAYMENT);
    assertThat(result.getClaimed()).isEqualTo(CLAIMED);
    assertThat(result.getLineItems()).hasSize(2);
    assertThat(result.getLineItems()).containsExactly(LINE_ITEM_1, LINE_ITEM_2);
    assertThat(result.getEvidence()).hasSize(2);
    assertThat(result.getEvidence()).containsExactly(CLAIM_EVIDENCE_1, CLAIM_EVIDENCE_2);
  }
}
