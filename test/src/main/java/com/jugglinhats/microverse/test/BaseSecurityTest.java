package com.jugglinhats.microverse.test;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static com.jugglinhats.microverse.test.JwtUtils.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@RunWith( SpringJUnit4ClassRunner.class )
@WebAppConfiguration
@ContextConfiguration( initializers = BaseSecurityTest.JwtKeyPropertyInjector.class )
public abstract class BaseSecurityTest {

    protected UaaJwtToken DEFAULT_TOKEN     = UaaJwtToken.builderWithDefaults()
                                                         .scope("microverse.info")
                                                         .build();
    protected UaaJwtToken WRONG_SCOPE_TOKEN = UaaJwtToken.builderWithDefaults()
                                                         .scope("microverse.wrong")
                                                         .build();

    @Autowired
    protected WebApplicationContext webApplicationContext;

    protected MockMvc mockMvc;

    @Before
    public void setUpMockMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .alwaysDo(print())
                .build();
    }

    protected RequestPostProcessor trustedBearerToken( UaaJwtToken token ) {
        return bearerToken(trustedToken(token));
    }

    protected RequestPostProcessor untrustedBearerToken( UaaJwtToken token ) {
        return bearerToken(untrustedToken(token));
    }

    private RequestPostProcessor bearerToken( String token ) {
        return mockRequest -> {
            mockRequest.addHeader("Authorization", "Bearer " + token);
            return mockRequest;
        };
    }

    public static class JwtKeyPropertyInjector implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize( ConfigurableApplicationContext applicationContext ) {
            final ConfigurableEnvironment env = applicationContext.getEnvironment();
            Map<String, Object> props = new HashMap<>();
            props.put("security.oauth2.resource.jwt.key-value", trustedVerifierKey());
            env.getPropertySources().addFirst(new MapPropertySource("test-runtime", props));
        }
    }
}
