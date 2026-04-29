package org.antredesloutres.papi;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Base64;

/**
 * One-shot helper to print a fresh Base64-encoded HS256 secret.
 */
public final class GenerateSecret {

    private GenerateSecret() {}

    public static void main(String[] args) {
        var key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        System.out.println(Base64.getEncoder().encodeToString(key.getEncoded()));
    }
}
