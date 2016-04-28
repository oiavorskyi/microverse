package com.jugglinhats.microverse.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.Signer;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;

final class JwtUtils {

    private static final KeyPair TRUSTED_KEYS   = createSigningKey();
    private static final KeyPair UNTRUSTED_KEYS = createSigningKey();

    private static final Signer TRUSTED_SIGNER =
            new RsaSigner((RSAPrivateKey) TRUSTED_KEYS.getPrivate());

    private static final Signer UNTRUSTED_SIGNER =
            new RsaSigner((RSAPrivateKey) UNTRUSTED_KEYS.getPrivate());

    private static ObjectMapper objectMapper = new ObjectMapper();

    static String trustedVerifierKey() {
        StringWriter verifierKeyPEM = new StringWriter();
        PEMWriter writer = new PEMWriter(verifierKeyPEM);
        try {
            writer.writeObject(new PemObject("PUBLIC KEY",
                    TRUSTED_KEYS.getPublic().getEncoded()));
            writer.flush();
        } catch ( IOException e ) {
            throw new RuntimeException("Unable to encode JWT verifier key", e);
        }

        return verifierKeyPEM.toString();
    }

    static String trustedToken( UaaJwtToken token ) {
        return encodeToken(token, TRUSTED_SIGNER);
    }

    static String untrustedToken( UaaJwtToken token ) {
        return encodeToken(token, UNTRUSTED_SIGNER);
    }

    private static String encodeToken( UaaJwtToken token, Signer signer ) {
        try {
            return JwtHelper.encode(objectMapper.writeValueAsString(token), signer).getEncoded();
        } catch ( JsonProcessingException e ) {
            throw new RuntimeException("Unable to encode JWT token", e);
        }
    }

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

}
