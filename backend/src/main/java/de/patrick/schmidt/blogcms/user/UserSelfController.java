package de.patrick.schmidt.blogcms.user;

import de.patrick.schmidt.blogcms.user.dto.PreferredLocaleRequest;
import de.patrick.schmidt.blogcms.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Selbstverwaltung des angemeldeten Benutzers (ADR-005 Patch).
 * Nicht auf ADMIN beschränkt — jeder authentifizierte Nutzer setzt nur seine eigene Präferenz.
 */
@RestController
@RequestMapping("/api/admin/v1/users/me")
@RequiredArgsConstructor
public class UserSelfController {

    private final UserService userService;

    @PutMapping("/preferred-locale")
    public ResponseEntity<UserResponse> setPreferredLocale(
            @Valid @RequestBody PreferredLocaleRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.setPreferredLocale(currentUser.getId(), request.preferredLocale()));
    }
}
