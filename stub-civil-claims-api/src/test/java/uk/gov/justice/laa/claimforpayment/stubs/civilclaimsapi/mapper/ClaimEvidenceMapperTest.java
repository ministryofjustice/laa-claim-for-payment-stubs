package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEvidenceEntity;

class ClaimEvidenceMapperTest {

  private final ClaimEvidenceMapper claimEvidenceMapper =
      Mappers.getMapper(ClaimEvidenceMapper.class);

  @Test
  void shouldMapToClaimEvidence() {
    var claimEvidenceEntity =
        ClaimEvidenceEntity.builder()
            .id(1L)
            .fileKey("fileKey")
            .fileSize(1000L)
            .build();

    var result = claimEvidenceMapper.toClaimEvidence(claimEvidenceEntity);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(claimEvidenceEntity.getId());
    assertThat(result.getFileKey()).isEqualTo(claimEvidenceEntity.getFileKey());
    assertThat(result.getFileSize()).isEqualTo(claimEvidenceEntity.getFileSize());
  }
}
