package de.patrick.schmidt.blogcms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.bootstrap")
public class BootstrapProperties {

    private boolean enabled = true;
    private Admin admin = new Admin();

    @Getter
    @Setter
    public static class Admin {
        private String username = "admin";
        private String email = "admin@example.com";
        private String password = "changeme";
    }
}
