package com.jugglinhats.microverse.info;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.*;

import static java.util.Arrays.stream;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;

public final class BearerTokenRequestPostProcessors {

    private static final KeyPair TRUSTED_KEYS = createSigningKey();

    private static KeyPair createSigningKey() {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA", "BC");
        } catch ( Exception e ) {
            throw new RuntimeException("Unable to create key pair", e);
        }
        keyGen.initialize(2048);

        return keyGen.generateKeyPair();
    }

    private static JwtAccessTokenConverter accessTokenConverter() throws Exception {
        JwtAccessTokenConverter jwt = new JwtAccessTokenConverter();
        jwt.setKeyPair(TRUSTED_KEYS);
        jwt.afterPropertiesSet();
        return jwt;
    }

    private static OAuth2AccessToken createAccessToken( final OAuth2Authentication auth ) {
        DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(UUID.randomUUID().toString());
        int validitySeconds = 5;
        token.setExpiration(new Date(System.currentTimeMillis() + (validitySeconds * 1000L)));
        token.setRefreshToken(null);
        token.setScope(auth.getOAuth2Request().getScope());

        try {
            return accessTokenConverter().enhance(token, auth);
        } catch ( Exception e ) {
            throw new RuntimeException("Unable to create the JWT token converter", e);
        }
    }

    private static OAuth2Authentication createAuthentication( String userId, String clientId,
                                                              Set<String> scopes,
                                                              Set<String> resourceIds,
                                                              String... authorities ) {
        final Collection<GrantedAuthority> authoritiesList =
                stream(authorities)
                        .map(SimpleGrantedAuthority::new)
                        .collect(toList());

        // Default values for other parameters
        final Map<String, String> requestParameters = Collections.emptyMap();
        final boolean approved = true;
        final String redirectUrl = null;
        final Set<String> responseTypes = Collections.emptySet();
        final Map<String, Serializable> extensionProperties = Collections.emptyMap();

        // Create request
        OAuth2Request oAuth2Request = new OAuth2Request(requestParameters, clientId,
                authoritiesList, approved, scopes, resourceIds, redirectUrl, responseTypes,
                extensionProperties);

        // Create OAuth2AccessToken
        User userPrincipal = new User(userId, "", true, true, true, true, authoritiesList);
        UsernamePasswordAuthenticationToken authenticationToken = new
                UsernamePasswordAuthenticationToken(userPrincipal, null, authoritiesList);
        return new OAuth2Authentication(oAuth2Request, authenticationToken);
    }

    public static RequestPostProcessor bearerToken() {
        return mockRequest -> {
            final OAuth2Authentication auth = createAuthentication("oleg", "myservice",
                    singleton("myscope"), singleton("myresource"), "myauthority");
            OAuth2AccessToken token = createAccessToken(auth);
            mockRequest.addHeader("Authorization", "Bearer " + token.getValue());
            return mockRequest;
        };
    }

    public static class JwtPropertyInjector implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize( ConfigurableApplicationContext applicationContext ) {
            final ConfigurableEnvironment env = applicationContext.getEnvironment();
            Map<String, Object> props = new HashMap<>();


            StringWriter publicKeyPEM = new StringWriter();
            PEMWriter writer = new PEMWriter(publicKeyPEM);
            try {
                writer.writeObject(new PemObject("PUBLIC KEY",
                        TRUSTED_KEYS.getPublic().getEncoded()));
                writer.flush();
                props.put("security.oauth2.resource.jwt.key-value", publicKeyPEM.toString());
            } catch ( IOException e ) {
                throw new RuntimeException("Unable to setup JWT verifier key", e);
            }

            env.getPropertySources().addFirst(new MapPropertySource("test-runtime", props));
        }
    }
}
