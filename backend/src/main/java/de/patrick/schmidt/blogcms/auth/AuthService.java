package de.patrick.schmidt.blogcms.auth;

import de.patrick.schmidt.blogcms.auth.dto.LoginRequest;
import de.patrick.schmidt.blogcms.auth.dto.LoginResponse;
import de.patrick.schmidt.blogcms.common.exception.BusinessException;
import de.patrick.schmidt.blogcms.config.SecurityProperties;
import de.patrick.schmidt.blogcms.security.JwtTokenProvider;
import de.patrick.schmidt.blogcms.user.User;
import de.patrick.schmidt.blogcms.user.UserRepository;
import de.patrick.schmidt.blogcms.user.UserStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String COOKIE_NAME = "refresh_token";
    private static final String COOKIE_PATH = "/api/auth";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final SecurityProperties securityProperties;

    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> BusinessException.of("error.auth.invalidCredentials"));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw BusinessException.of("error.auth.accountInactive");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw BusinessException.of("error.auth.invalidCredentials");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getUsername(), user.getRole().name());
        String refreshToken = refreshTokenService.createToken(user);

        addRefreshCookie(response, refreshToken);
        return new LoginResponse(accessToken);
    }

    public LoginResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String oldToken = extractRefreshToken(request);
        if (oldToken == null) {
            throw BusinessException.of("error.auth.missingRefreshToken");
        }

        String newPlaintext = generateNewPlaintext();
        User user = refreshTokenService.validateAndRotate(oldToken, newPlaintext);

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getUsername(), user.getRole().name());

        addRefreshCookie(response, newPlaintext);
        return new LoginResponse(accessToken);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String token = extractRefreshToken(request);
        if (token != null) {
            refreshTokenService.revokeByPlaintext(token);
        }
        clearRefreshCookie(response);
    }

    private void addRefreshCookie(HttpServletResponse response, String token) {
        Duration ttl = securityProperties.getJwt().getRefreshTokenTtl();
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(securityProperties.getCookie().isSecure())
                .sameSite(securityProperties.getCookie().getSameSite())
                .path(COOKIE_PATH)
                .maxAge(ttl)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(securityProperties.getCookie().isSecure())
                .sameSite(securityProperties.getCookie().getSameSite())
                .path(COOKIE_PATH)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .map(jakarta.servlet.http.Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private String generateNewPlaintext() {
        // RefreshTokenService handles secure random generation internally;
        // we need a plaintext here only to pass it for rotation — generate it there.
        // Delegating via a simple round-trip: return placeholder, rotated inside service.
        // Actually: generate plaintext here and pass both old+new to service.
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
