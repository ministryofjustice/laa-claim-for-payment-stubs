package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.config.TestJwtConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestJwtConfig.class})
class CivilClaimsStubApplicationTests {

  @Test
  void contextLoads() {
    // empty due to only testing context load
  }
}
