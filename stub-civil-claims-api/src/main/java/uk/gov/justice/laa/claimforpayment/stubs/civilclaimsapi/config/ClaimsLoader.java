package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.config;

@Component
@RequiredArgsConstructor
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

private ClaimEntity toEntity(ConfigClaim claim) {
  return ClaimEntity.builder()
  .id(claim.id())
  .provider_user_id(claim.provider_user_id())
  .ufn(claim.ufn())
  .client(claim.client())
  .category(claim.category())
  .concluded(claim.concluded())
  .fee_type(claim.fee_type())
  .claimed(claim.claimed())
  .submission_id(claim.submission_id())
  .build();
}