package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.model.Claim;

/** Configuration for external files read at service startup. */
@Configuration
@ConfigurationProperties(prefix = "claims")
@Getter
@Setter
public class ClaimsConfig {
  private List<Claim> claims = List.of();
}
