package de.patrick.schmidt.blogcms.media;

import de.patrick.schmidt.blogcms.common.exception.BusinessException;
import de.patrick.schmidt.blogcms.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    private final StorageProperties properties;
    private Path rootLocation;

    @PostConstruct
    public void init() throws IOException {
        rootLocation = Paths.get(properties.getLocation()).toAbsolutePath().normalize();
        Files.createDirectories(rootLocation);
    }

    @Override
    public String store(MultipartFile file) {
        validateFile(file);
        String extension = getExtension(file.getOriginalFilename());
        LocalDate now = LocalDate.now();
        String relativePath = "media/%d/%02d/%s.%s"
                .formatted(now.getYear(), now.getMonthValue(), UUID.randomUUID(), extension);
        Path target = rootLocation.resolve(relativePath);
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw BusinessException.of("error.media.storeFailed", e.getMessage());
        }
        return relativePath;
    }

    @Override
    public Resource load(String relativePath) {
        try {
            Path file = rootLocation.resolve(relativePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw BusinessException.of("error.media.fileNotFound", relativePath);
        } catch (MalformedURLException e) {
            throw BusinessException.of("error.media.invalidPath", relativePath);
        }
    }

    @Override
    public void delete(String relativePath) {
        try {
            Files.deleteIfExists(rootLocation.resolve(relativePath).normalize());
        } catch (IOException e) {
            log.warn("Could not delete file: {}", relativePath, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw BusinessException.of("error.media.emptyFile");
        }
        String contentType = file.getContentType();
        if (contentType == null || !properties.getAllowedTypes().contains(contentType)) {
            throw BusinessException.of("error.media.typeNotAllowed", contentType);
        }
        if (file.getSize() > properties.getMaxFileSize().toBytes()) {
            throw BusinessException.of("error.media.tooLarge", properties.getMaxFileSize().toMegabytes());
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
