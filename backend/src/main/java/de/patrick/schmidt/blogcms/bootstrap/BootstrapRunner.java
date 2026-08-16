package de.patrick.schmidt.blogcms.bootstrap;

import de.patrick.schmidt.blogcms.config.BootstrapProperties;
import de.patrick.schmidt.blogcms.user.Role;
import de.patrick.schmidt.blogcms.user.User;
import de.patrick.schmidt.blogcms.user.UserRepository;
import de.patrick.schmidt.blogcms.user.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) return;
        if (userRepository.existsByRole(Role.ADMIN)) {
            log.debug("Bootstrap: Admin already exists, skipping.");
            return;
        }

        User admin = User.builder()
                .username(properties.getAdmin().getUsername())
                .email(properties.getAdmin().getEmail())
                .password(passwordEncoder.encode(properties.getAdmin().getPassword()))
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(admin);
        log.info("Bootstrap: Initial admin user '{}' created.", admin.getUsername());
    }
}
