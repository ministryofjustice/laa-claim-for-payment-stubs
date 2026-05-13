package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEvidenceEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.ClaimEvidence;

class ClaimEvidenceMapperTest {

  @InjectMocks private ClaimEvidenceMapper claimEvidenceMapper = new ClaimEvidenceMapperImpl();

  @Test
  void shouldMapToClaimEvidenceEntity() {
    ClaimEvidence claimEvidence = ClaimEvidence.builder().fileKey("fileKey").build();

    var claimEvidenceEntity = claimEvidenceMapper.toClaimEvidenceEntity(claimEvidence);

    assertThat(claimEvidenceEntity).isNotNull();
    assertThat(claimEvidenceEntity.getFileKey()).isEqualTo(claimEvidence.getFileKey());
  }

  @Test
  void shouldMapToClaimEvidence() {
    var claimEvidenceEntity = ClaimEvidenceEntity.builder().fileKey("fileKey").build();

    var claimEvidence = claimEvidenceMapper.toClaimEvidence(claimEvidenceEntity);

    assertThat(claimEvidence).isNotNull();
    assertThat(claimEvidence.getFileKey()).isEqualTo(claimEvidenceEntity.getFileKey());
  }
}
