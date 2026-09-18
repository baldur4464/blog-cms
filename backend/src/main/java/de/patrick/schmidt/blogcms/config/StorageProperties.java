package de.patrick.schmidt.blogcms.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    @NotBlank
    @Pattern(regexp = "local|s3", message = "storage backend must be 'local' or 's3'")
    private String backend = "local";

    @NotBlank
    private String location = "./storage";

    @NotNull
    private DataSize maxFileSize = DataSize.ofMegabytes(10);

    @NotEmpty
    private List<String> allowedTypes = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "application/pdf"
    );
}
