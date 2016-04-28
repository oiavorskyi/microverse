package com.jugglinhats.microverse.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.UUID;

@Data
@JsonInclude( Include.NON_NULL )
@AllArgsConstructor( staticName = "newInstance", access = AccessLevel.PACKAGE )
public class UaaJwtToken {

    private String             jti;
    private String             sub;
    private Collection<String> authorities;
    private Collection<String> scope;
    private String             client_id;
    private String             cid;
    private String             azp;
    private String             grant_type;
    private String             user_id;
    private String             user_name;
    private String             email;
    private long               iat;
    private long               exp;
    private String             iss;
    private String             zid;
    private Collection<String> aud;

    @Builder
    static UaaJwtToken newInstance( @Singular Collection<String> authorities,
                                    @Singular Collection<String> scopes,
                                    String client_id, GrantType grant_type, String user_id,
                                    String user_name,
                                    String email, String issuer, String zid ) {
        String jti = UUID.randomUUID().toString();
        String sub = (user_id != null ? user_id : client_id);
        String cid = client_id;
        String azp = client_id;
        long iat = System.currentTimeMillis();
        long exp = iat + 1000 * 60 * 60 * 24;
        authorities = (authorities != null ? authorities : Collections.emptySet());
        scopes = (scopes != null ? scopes : Collections.emptySet());

        LinkedHashSet<String> aud = new LinkedHashSet<>();
        if ( client_id != null ) {
            aud.add(client_id);
        }
        scopes.forEach(scope -> {
            aud.add(scope.substring(0, scope.lastIndexOf('.') > 0
                    ? scope.lastIndexOf('.')
                    : scope.length()));
        });

        if ( grant_type == null ) {
            grant_type = GrantType.authorization_code;
        }
        if ( GrantType.client_credentials == grant_type ) {
            authorities = scopes;
        }

        return newInstance(
                jti,
                sub,
                authorities,
                scopes,
                client_id,
                cid,
                azp,
                grant_type.name(),
                user_id,
                user_name,
                email,
                iat,
                exp,
                issuer,
                zid,
                aud
        );
    }

    public static UaaJwtTokenBuilder builderWithDefaults() {
        return builder()
                .grant_type(UaaJwtToken.GrantType.authorization_code)
                .client_id("ui-portal")
                .user_id("test-user")
                .user_name("Test User")
                .email("test@example.com");
    }

    public enum GrantType {
        implicit, client_credentials, authorization_code, password
    }

}