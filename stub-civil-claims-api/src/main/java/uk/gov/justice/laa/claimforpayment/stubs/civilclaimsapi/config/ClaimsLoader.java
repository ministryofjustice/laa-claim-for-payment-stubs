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
  return ClaimEntity.builder().id(claim.id()).providerUserId
}