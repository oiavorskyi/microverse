package com.jugglinhats.microverse.info;

import com.jugglinhats.microverse.restricted.RestrictedApplication;
import com.jugglinhats.microverse.test.BaseSecurityTest;
import com.jugglinhats.microverse.test.UaaJwtToken;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringApplicationConfiguration( classes = RestrictedApplication.class )
public class RestrictedApplicationTests extends BaseSecurityTest {

    private UaaJwtToken DEFAULT_TOKEN     = UaaJwtToken.builderWithDefaults()
                                                       .scope("microverse.restricted")
                                                       .build();
    private UaaJwtToken WRONG_SCOPE_TOKEN = UaaJwtToken.builderWithDefaults()
                                                       .scope("microverse.wrong")
                                                       .build();

    @Test
    public void rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/data"))
               .andExpect(status().isUnauthorized())
               .andExpect(header().string("WWW-Authenticate", startsWith("Bearer")));
    }

    @Test
    public void acceptsAuthenticatedRequest() throws Exception {
        mockMvc.perform(get("/data").with(trustedBearerToken(DEFAULT_TOKEN)))
               .andExpect(status().isOk());
    }

    @Test
    public void rejectsRequestsSignedByWrongKey() throws Exception {
        mockMvc.perform(get("/data").with(untrustedBearerToken(DEFAULT_TOKEN)))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void rejectsRequestsWithoutProperScope() throws Exception {
        mockMvc.perform(get("/data").with(trustedBearerToken(WRONG_SCOPE_TOKEN)))
               .andExpect(status().isForbidden());
    }

}