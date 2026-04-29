package org.antredesloutres.papi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilsTest {

    private static final String VALID_SECRET =
            Base64.getEncoder().encodeToString(new byte[] {
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
                    17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32
            });

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", VALID_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3_600_000L);
        jwtUtils.validateAndCacheKey();
    }

    @Test
    void validateAndCacheKey_throwsWhenSecretBlank() {
        // arrange
        JwtUtils utils = new JwtUtils();
        ReflectionTestUtils.setField(utils, "jwtSecret", "");
        ReflectionTestUtils.setField(utils, "jwtExpirationMs", 3_600_000L);

        // act + assert
        assertThatThrownBy(utils::validateAndCacheKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be set");
    }

    @Test
    void validateAndCacheKey_throwsWhenSecretTooShort() {
        // arrange
        JwtUtils utils = new JwtUtils();
        ReflectionTestUtils.setField(utils, "jwtSecret", Base64.getEncoder().encodeToString("short".getBytes()));
        ReflectionTestUtils.setField(utils, "jwtExpirationMs", 3_600_000L);

        // act + assert
        assertThatThrownBy(utils::validateAndCacheKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    void validateAndCacheKey_throwsWhenSecretNotBase64() {
        // arrange
        JwtUtils utils = new JwtUtils();
        ReflectionTestUtils.setField(utils, "jwtSecret", "###not-base64###");
        ReflectionTestUtils.setField(utils, "jwtExpirationMs", 3_600_000L);

        // act + assert
        assertThatThrownBy(utils::validateAndCacheKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Base64");
    }

    @Test
    void validateAndCacheKey_throwsWhenExpirationNotPositive() {
        // arrange
        JwtUtils utils = new JwtUtils();
        ReflectionTestUtils.setField(utils, "jwtSecret", VALID_SECRET);
        ReflectionTestUtils.setField(utils, "jwtExpirationMs", 0L);

        // act + assert
        assertThatThrownBy(utils::validateAndCacheKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expiration");
    }

    @Test
    void generateToken_includesUsernameAndRoles() {
        // arrange
        List<GrantedAuthority> auths = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER"));

        // act
        String token = jwtUtils.generateToken("alice", auths);

        // assert
        assertThat(token).isNotBlank();
        assertThat(jwtUtils.validateToken(token)).isTrue();
        assertThat(jwtUtils.getUsernameFromToken(token)).isEqualTo("alice");
    }

    @Test
    void validateToken_returnsFalseForGarbage() {
        // arrange + act + assert
        assertThat(jwtUtils.validateToken("not.a.token")).isFalse();
    }

    @Test
    void validateToken_returnsFalseForEmpty() {
        // arrange + act + assert
        assertThat(jwtUtils.validateToken("")).isFalse();
    }

    @Test
    void validateToken_returnsFalseForTokenSignedWithDifferentKey() {
        // arrange
        JwtUtils otherUtils = new JwtUtils();
        String otherSecret = Base64.getEncoder().encodeToString(new byte[] {
                99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,
                99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99
        });
        ReflectionTestUtils.setField(otherUtils, "jwtSecret", otherSecret);
        ReflectionTestUtils.setField(otherUtils, "jwtExpirationMs", 3_600_000L);
        otherUtils.validateAndCacheKey();
        String foreign = otherUtils.generateToken("alice", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // act + assert
        assertThat(jwtUtils.validateToken(foreign)).isFalse();
    }
}
