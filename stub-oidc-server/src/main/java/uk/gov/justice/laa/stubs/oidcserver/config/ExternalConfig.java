package uk.gov.justice.laa.stubs.oidcserver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.justice.laa.stubs.oidcserver.model.TestUser;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "external")
@Getter
@Setter
public class ExternalConfig {

    private List<TestUser> users;
}
