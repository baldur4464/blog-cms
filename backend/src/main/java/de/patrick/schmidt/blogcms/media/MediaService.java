package de.patrick.schmidt.blogcms.media;

import de.patrick.schmidt.blogcms.common.PageResponse;
import de.patrick.schmidt.blogcms.common.exception.ResourceNotFoundException;
import de.patrick.schmidt.blogcms.media.dto.MediaResponse;
import de.patrick.schmidt.blogcms.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public PageResponse<MediaResponse> findAll(Pageable pageable) {
        return PageResponse.from(mediaRepository.findAll(pageable), MediaResponse::from);
    }

    @Transactional
    public MediaResponse upload(MultipartFile file, User uploader) {
        String relativePath = storageService.store(file);
        String filename = relativePath.substring(relativePath.lastIndexOf('/') + 1);

        Media media = Media.builder()
                .filename(filename)
                .originalName(file.getOriginalFilename())
                .mimeType(file.getContentType())
                .size(file.getSize())
                .url("/" + relativePath)
                .uploadedBy(uploader)
                .build();

        return MediaResponse.from(mediaRepository.save(media));
    }

    @Transactional
    public void delete(UUID id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("error.media.notFound", id));
        storageService.delete("media/" + media.getFilename());
        mediaRepository.deleteById(id);
    }
}
