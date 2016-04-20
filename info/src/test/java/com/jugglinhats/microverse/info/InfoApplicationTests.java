package com.jugglinhats.microverse.info;

import com.jugglinhats.microverse.info.BearerTokenRequestPostProcessors.AuthTokenSpec;
import com.jugglinhats.microverse.info.BearerTokenRequestPostProcessors.JwtPropertyInjector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.jugglinhats.microverse.info.BearerTokenRequestPostProcessors.trustedBearerToken;
import static com.jugglinhats.microverse.info.BearerTokenRequestPostProcessors.untrustedBearerToken;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
        .springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith( SpringJUnit4ClassRunner.class )
@SpringApplicationConfiguration(
        classes = InfoApplication.class,
        initializers = JwtPropertyInjector.class )
@WebAppConfiguration
public class InfoApplicationTests {

    @Value( "${security.oauth2.resource.id}" )
    private String resourceId = "default";

    private AuthTokenSpec defaultToken;
    private AuthTokenSpec wrongResourceToken;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void setUpMockMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

    }

    @Before
    public void defineTokens() {
        defaultToken = baseTokenBuilder()
                .resource(resourceId)
                .build();

        wrongResourceToken = baseTokenBuilder()
                .resource("wrong-resource")
                .build();
    }

    private AuthTokenSpec.AuthTokenSpecBuilder baseTokenBuilder() {
        return AuthTokenSpec.builder()
                            .clientId("test-client")
                            .userId("default");
    }

    @Test
    public void rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/info"))
               .andExpect(status().isUnauthorized())
               .andExpect(header().string("WWW-Authenticate", startsWith("Bearer")));
    }

    @Test
    public void acceptsAuthenticatedRequest() throws Exception {
        mockMvc.perform(get("/info").with(trustedBearerToken(defaultToken)))
               .andExpect(status().isOk());
    }

    @Test
    public void rejectsRequestsWithoutProperResourceId() throws Exception {
        mockMvc.perform(get("/info").with(trustedBearerToken(wrongResourceToken)))
               .andExpect(status().isForbidden());
    }

    @Test
    public void rejectsRequestsSignedByWrongKey() throws Exception {
        mockMvc.perform(get("/info").with(untrustedBearerToken(defaultToken)))
               .andExpect(status().isUnauthorized());
    }
}
