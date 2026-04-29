package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.entity.ClaimEntity;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.Claim;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.repository.ClaimRepository;

/** Load claims into database at runtime. */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClaimsLoader implements ApplicationRunner {

  private final ClaimsConfig claimsConfig;
  private final ClaimRepository repository;

  @Override
  public void run(ApplicationArguments args) {

    repository.deleteAll();

    List<ClaimEntity> claims = claimsConfig.getClaims().stream().map(this::toEntity).toList();

    repository.saveAll(claims);

    log.info("Loaded {} claims from config", repository.count());
  }

  private ClaimEntity toEntity(Claim claim) {
    return ClaimEntity.builder()
        .providerUserId(claim.getProviderUserId())
        .ufn(claim.getUfn())
        .client(claim.getClient())
        .category(claim.getCategory())
        .concluded(claim.getConcluded())
        .feeType(claim.getFeeType())
        .claimed(claim.getClaimed())
        .submissionId(claim.getSubmissionId())
        .build();
  }
}