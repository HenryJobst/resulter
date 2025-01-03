package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.MediaFileDto;
import de.jobst.resulter.application.MediaFileService;
import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;

@RestController
@Slf4j
public class MediaFileController {

    public static final String FILE = "file";

    @Value("#{'${resulter.media-file-path-thumbnails}'}")
    private String mediaFileThumbnailsPath;

    private final MediaFileService mediaFileService;

    @Autowired
    public MediaFileController(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    private static void logError(Exception e) {
        log.error(e.getMessage());
        if (Objects.nonNull(e.getCause())) {
            log.error(e.getCause().getMessage());
        }
    }

    @GetMapping("/media")
    public ResponseEntity<Page<MediaFileDto>> searchMediaFiles(@RequestParam Optional<String> filter,
                                                               Pageable pageable) {
        try {
            Page<MediaFile> mediaFiles = mediaFileService.findAll(filter.orElse(null),
                pageable != null ?
                FilterAndSortConverter.mapOrderProperties(pageable, MediaFileDto::mapOrdersDtoToDomain) :
                Pageable.unpaged());
            return ResponseEntity.ok(new PageImpl<>(mediaFiles.getContent()
                .stream()
                .map(x -> MediaFileDto.from(x, mediaFileThumbnailsPath))
                .toList(),
                FilterAndSortConverter.mapOrderProperties(mediaFiles.getPageable(), MediaFileDto::mapOrdersDomainToDto),
                mediaFiles.getTotalElements()));
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/media/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MediaFileDto> getMediaFile(@PathVariable Long id) {
        try {
            Optional<MediaFile> mediaFile = mediaFileService.findById(MediaFileId.of(id));
            return mediaFile.map(value -> ResponseEntity.ok(MediaFileDto.from(value, mediaFileThumbnailsPath)))
                .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/media/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MediaFileDto> updateMedia(@PathVariable Long id, @RequestBody MediaFileDto mediaFileDto) {
        try {
            MediaFile mediaFile = mediaFileService.update(MediaFileId.of(id),
                MediaFileName.of(mediaFileDto.fileName()),
                MediaFileContentType.of(mediaFileDto.contentType()),
                MediaFileSize.of(mediaFileDto.fileSize()),
                MediaFileDescription.of(mediaFileDto.description()));

            if (null != mediaFile) {
                return ResponseEntity.ok(MediaFileDto.from(mediaFile, mediaFileThumbnailsPath));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/media/upload")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<String> handleMediaFileUpload(@RequestParam(FILE) MultipartFile file) {
        try {
            MediaFile mediaFile = mediaFileService.storeMediaFile(file);
            return ResponseEntity.ok(mediaFile.toString());
        } catch (Exception e) {
            log.error(e.getMessage());
            if (Objects.nonNull(e.getCause())) {
                log.error(e.getCause().getMessage());
            }
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/media/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> deleteMediaFile(@PathVariable Long id) {
        try {
            boolean success = mediaFileService.delete(MediaFileId.of(id));
            if (success) {
                return ResponseEntity.ok(Boolean.TRUE);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
