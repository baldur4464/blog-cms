package de.patrick.schmidt.blogcms.media;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String store(MultipartFile file);
    Resource load(String relativePath);
    void delete(String relativePath);
}
