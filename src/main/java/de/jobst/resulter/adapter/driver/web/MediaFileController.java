package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.MediaFileDto;
import de.jobst.resulter.application.MediaFileService;
import de.jobst.resulter.domain.*;
import java.util.List;
import java.util.Optional;
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

    @GetMapping("/media/all")
    public ResponseEntity<List<MediaFileDto>> getAllMediaFiles() {
        List<MediaFile> mediaFiles = mediaFileService.findAll();
        return ResponseEntity.ok(mediaFiles.stream()
                .map(m -> MediaFileDto.from(m, mediaFileThumbnailsPath))
                .toList());
    }

    @GetMapping("/media")
    public ResponseEntity<Page<MediaFileDto>> searchMediaFiles(
            @RequestParam Optional<String> filter, Pageable pageable) {
        Page<MediaFile> mediaFiles = mediaFileService.findAll(
                filter.orElse(null),
                pageable != null
                        ? FilterAndSortConverter.mapOrderProperties(pageable, MediaFileDto::mapOrdersDtoToDomain)
                        : Pageable.unpaged());
        return ResponseEntity.ok(new PageImpl<>(
                mediaFiles.getContent().stream()
                        .map(x -> MediaFileDto.from(x, mediaFileThumbnailsPath))
                        .toList(),
                FilterAndSortConverter.mapOrderProperties(mediaFiles.getPageable(), MediaFileDto::mapOrdersDomainToDto),
                mediaFiles.getTotalElements()));
    }

    @GetMapping("/media/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MediaFileDto> getMediaFile(@PathVariable Long id) {
        Optional<MediaFile> mediaFile = mediaFileService.findById(MediaFileId.of(id));
        return mediaFile
                .map(value -> ResponseEntity.ok(MediaFileDto.from(value, mediaFileThumbnailsPath)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/media/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MediaFileDto> updateMedia(@PathVariable Long id, @RequestBody MediaFileDto mediaFileDto) {
        MediaFile mediaFile = mediaFileService.update(
                MediaFileId.of(id),
                MediaFileName.of(mediaFileDto.fileName()),
                MediaFileContentType.of(mediaFileDto.contentType()),
                MediaFileSize.of(mediaFileDto.fileSize()),
                MediaFileDescription.of(mediaFileDto.description()));

        if (null != mediaFile) {
            return ResponseEntity.ok(MediaFileDto.from(mediaFile, mediaFileThumbnailsPath));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/media/upload")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<String> handleMediaFileUpload(@RequestParam(FILE) MultipartFile file) {
        MediaFile mediaFile = mediaFileService.storeMediaFile(file);
        return ResponseEntity.ok(mediaFile.toString());
    }

    @DeleteMapping("/media/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> deleteMediaFile(@PathVariable Long id) {
        boolean success = mediaFileService.delete(MediaFileId.of(id));
        if (success) {
            return ResponseEntity.ok(Boolean.TRUE);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
