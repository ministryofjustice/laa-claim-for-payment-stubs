package uk.gov.justice.laa.stubs.oidcserver.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OidcServerConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openidConfigurationIsExposed() throws Exception {
        mockMvc
            .perform(get("/.well-known/openid-configuration"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.issuer").value("http://localhost:8081"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"alice", "bob"})
    void userCanLoginWithCorrectPassword(String user) throws Exception {
        mockMvc.perform(formLogin("/login")
                .user(user)
                .password("password"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andDo(print());
    }

    @ParameterizedTest
    @ValueSource(strings = {"alice", "bob"})
    void userCannotLoginWithIncorrectPassword(String user) throws Exception {
        mockMvc.perform(formLogin("/login")
                .user(user)
                .password("incorrect"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?error"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"charlotte", "dave"})
    void unknownUserCannotLogin(String user) throws Exception {
        mockMvc.perform(formLogin("/login")
                .user(user)
                .password("password"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?error"));
    }
}
