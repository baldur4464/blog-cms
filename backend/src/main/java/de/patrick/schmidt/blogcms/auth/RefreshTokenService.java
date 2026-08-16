package de.patrick.schmidt.blogcms.auth;

import de.patrick.schmidt.blogcms.common.exception.BusinessException;
import de.patrick.schmidt.blogcms.config.SecurityProperties;
import de.patrick.schmidt.blogcms.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityProperties securityProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    /** Creates a new opaque refresh token, persists its hash, and returns the plaintext. */
    @Transactional
    public String createToken(User user) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String plaintext = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String hash = sha256(plaintext);

        Instant expiresAt = Instant.now()
                .plus(securityProperties.getJwt().getRefreshTokenTtl());

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        refreshTokenRepository.save(token);
        return plaintext;
    }

    /**
     * Validates the plaintext token. If the found token is already revoked, all tokens
     * of that user are revoked (suspected theft).
     *
     * @return the associated User if valid
     */
    @Transactional
    public User validateAndRotate(String plaintext, String newPlaintext) {
        String hash = sha256(plaintext);
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        if (token.isRevoked()) {
            // Suspected token theft — revoke everything
            refreshTokenRepository.revokeAllByUserId(token.getUser().getId());
            throw new BusinessException("Refresh token already revoked. All sessions invalidated.");
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Refresh token expired");
        }

        // Rotate: revoke old, issue new
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        User user = token.getUser();
        String newHash = sha256(newPlaintext);
        Instant expiresAt = Instant.now().plus(securityProperties.getJwt().getRefreshTokenTtl());

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(newHash)
                .expiresAt(expiresAt)
                .revoked(false)
                .build());

        return user;
    }

    @Transactional
    public void revokeByPlaintext(String plaintext) {
        String hash = sha256(plaintext);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
