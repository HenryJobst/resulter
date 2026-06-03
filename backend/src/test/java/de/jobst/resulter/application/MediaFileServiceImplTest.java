package de.jobst.resulter.application;

import de.jobst.resulter.application.port.MediaFileRepository;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaFileServiceImplTest {

    @Mock MediaFileRepository mediaFileRepository;

    @InjectMocks
    MediaFileServiceImpl service;

    private static MediaFile mediaFile(long id) {
        return MediaFile.of(id, "file.pdf", "thumb.jpg", "application/pdf", 1000L);
    }

    @Test
    void findAll_delegatesToRepository() {
        when(mediaFileRepository.findAll()).thenReturn(List.of(mediaFile(1L)));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findAll_paged_delegatesToRepository() {
        when(mediaFileRepository.findAll(any(), any()))
                .thenReturn(new PageImpl<>(List.of(mediaFile(1L))));
        var page = service.findAll(null, PageRequest.of(0, 10));
        assertThat(page).isNotEmpty();
    }

    @Test
    void findById_returnsPresent_whenFound() {
        when(mediaFileRepository.findById(MediaFileId.of(1L))).thenReturn(Optional.of(mediaFile(1L)));
        assertThat(service.findById(MediaFileId.of(1L))).isPresent();
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        when(mediaFileRepository.findById(MediaFileId.of(99L))).thenReturn(Optional.empty());
        assertThat(service.findById(MediaFileId.of(99L))).isEmpty();
    }

    @Test
    void getById_returns_whenFound() {
        MediaFile mf = mediaFile(1L);
        when(mediaFileRepository.findById(MediaFileId.of(1L))).thenReturn(Optional.of(mf));
        assertThat(service.getById(MediaFileId.of(1L))).isEqualTo(mf);
    }

    @Test
    void getById_throws_whenNotFound() {
        when(mediaFileRepository.findById(MediaFileId.of(99L))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(MediaFileId.of(99L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_returnsFalse_whenIdNotPersistent() {
        assertThat(service.delete(MediaFileId.empty())).isFalse();
        verify(mediaFileRepository, never()).delete(any());
    }

    @Test
    void delete_returnsTrue_whenIdPersistent() {
        assertThat(service.delete(MediaFileId.of(1L))).isTrue();
        verify(mediaFileRepository).delete(MediaFileId.of(1L));
    }

    @Test
    void findAllById_delegatesToRepository() {
        when(mediaFileRepository.findAllById(List.of(MediaFileId.of(1L)))).thenReturn(List.of(mediaFile(1L)));
        assertThat(service.findAllById(List.of(MediaFileId.of(1L)))).hasSize(1);
    }

    @Test
    void findAllByIdAsMap_returnsMapWithCorrectKey() {
        MediaFile mf = mediaFile(1L);
        when(mediaFileRepository.findAllById(Set.of(MediaFileId.of(1L)))).thenReturn(List.of(mf));
        var map = service.findAllByIdAsMap(Set.of(MediaFileId.of(1L)));
        assertThat(map).containsKey(MediaFileId.of(1L));
    }

    @Test
    void update_returnsNull_whenFileNotFound() {
        when(mediaFileRepository.findById(MediaFileId.of(99L))).thenReturn(Optional.empty());
        var result = service.update(MediaFileId.of(99L),
                MediaFileName.of("new.pdf"),
                MediaFileContentType.of("application/pdf"),
                MediaFileSize.of(500L),
                null);
        assertThat(result).isNull();
    }

    @Test
    void update_savesAndReturns_whenFound() {
        MediaFile mf = mediaFile(1L);
        when(mediaFileRepository.findById(MediaFileId.of(1L))).thenReturn(Optional.of(mf));
        when(mediaFileRepository.save(mf)).thenReturn(mf);
        var result = service.update(MediaFileId.of(1L),
                MediaFileName.of("updated.pdf"),
                MediaFileContentType.of("application/pdf"),
                MediaFileSize.of(2000L),
                null);
        assertThat(result).isNotNull();
        verify(mediaFileRepository).save(mf);
    }

    // --- storeMediaFile tests ---

    private void configureStorePaths(Path mediaDir, Path thumbDir, int thumbSize) {
        ReflectionTestUtils.setField(service, "mediaFilePath", mediaDir.toString());
        ReflectionTestUtils.setField(service, "mediaFileThumbnailsPath", thumbDir.toString());
        ReflectionTestUtils.setField(service, "mediaFileThumbnailSize", thumbSize);
    }

    /** PDF magic bytes sufficient for Tika to detect application/pdf */
    private static final byte[] PDF_MAGIC =
        "%PDF-1.4 1 0 obj<</Type /Catalog>>endobj".getBytes();

    @Test
    void storeMediaFile_throwsIllegalArgument_whenUnsupportedMimeType(@TempDir Path tempDir) throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("plain text content".getBytes()));
        configureStorePaths(tempDir, tempDir, 100);

        assertThatThrownBy(() -> service.storeMediaFile(file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported media type");
    }

    @Test
    void storeMediaFile_savesPdf_withOriginalFilename(@TempDir Path tempDir) throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(PDF_MAGIC));
        when(file.getOriginalFilename()).thenReturn("report.pdf");
        when(file.getSize()).thenReturn((long) PDF_MAGIC.length);
        doNothing().when(file).transferTo(any(File.class));

        MediaFile saved = MediaFile.of(1L, "report.pdf", "report.pdf", "application/pdf", (long) PDF_MAGIC.length);
        when(mediaFileRepository.save(any())).thenReturn(saved);

        configureStorePaths(tempDir, tempDir, 100);

        MediaFile result = service.storeMediaFile(file);

        assertThat(result).isNotNull();
        verify(mediaFileRepository).save(any());
    }

    @Test
    void storeMediaFile_generatesuuidFilename_whenOriginalFilenameIsNull(@TempDir Path tempDir) throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(PDF_MAGIC));
        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getSize()).thenReturn((long) PDF_MAGIC.length);
        doNothing().when(file).transferTo(any(File.class));

        when(mediaFileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        configureStorePaths(tempDir, tempDir, 100);

        MediaFile result = service.storeMediaFile(file);

        // UUID filename ends with ".pdf" extension
        assertThat(result.getMediaFileName().value()).endsWith(".pdf");
    }

    @Test
    void storeMediaFile_generatesuuidFilename_whenOriginalFilenameIsEmpty(@TempDir Path tempDir) throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(PDF_MAGIC));
        when(file.getOriginalFilename()).thenReturn("");
        when(file.getSize()).thenReturn((long) PDF_MAGIC.length);
        doNothing().when(file).transferTo(any(File.class));

        when(mediaFileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        configureStorePaths(tempDir, tempDir, 100);

        MediaFile result = service.storeMediaFile(file);

        assertThat(result.getMediaFileName().value()).endsWith(".pdf");
    }

    @Test
    void storeMediaFile_throwsRuntime_whenInputStreamThrowsIOException(@TempDir Path tempDir) throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException("stream error"));
        configureStorePaths(tempDir, tempDir, 100);

        assertThatThrownBy(() -> service.storeMediaFile(file))
            .isInstanceOf(RuntimeException.class)
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void storeMediaFile_throwsRuntime_whenTransferToThrowsIOException(@TempDir Path tempDir) throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(PDF_MAGIC));
        when(file.getOriginalFilename()).thenReturn("report.pdf");
        doThrow(new IOException("disk full")).when(file).transferTo(any(File.class));
        configureStorePaths(tempDir, tempDir, 100);

        assertThatThrownBy(() -> service.storeMediaFile(file))
            .isInstanceOf(RuntimeException.class)
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void storeMediaFile_throwsIllegalArgument_whenFilenameContainsDotDot(@TempDir Path tempDir) throws Exception {
        // "test..pdf" passes Paths.get().getFileName() as "test..pdf"
        // which contains ".." → sanitizeFilename throws IllegalArgumentException
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(PDF_MAGIC));
        when(file.getOriginalFilename()).thenReturn("test..pdf");
        configureStorePaths(tempDir, tempDir, 100);

        assertThatThrownBy(() -> service.storeMediaFile(file))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid filename");
    }

    @Test
    void storeMediaFile_saveJpeg_withThumbnail(@TempDir Path tempDir) throws Exception {
        // Generate a valid 1×1 JPEG in memory
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpeg", bos);
        byte[] jpegBytes = bos.toByteArray();

        Path mediaDir = tempDir.resolve("media");
        Path thumbDir = tempDir.resolve("thumbs");
        java.nio.file.Files.createDirectories(mediaDir);
        java.nio.file.Files.createDirectories(thumbDir);

        // Write a real JPEG file so Thumbnailator can process it
        Path jpegFile = mediaDir.resolve("photo.jpg");
        java.nio.file.Files.write(jpegFile, jpegBytes);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(jpegBytes));
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getSize()).thenReturn((long) jpegBytes.length);
        // transferTo copies bytes to the target file
        doAnswer(inv -> {
            File dest = inv.getArgument(0);
            java.nio.file.Files.write(dest.toPath(), jpegBytes);
            return null;
        }).when(file).transferTo(any(File.class));

        when(mediaFileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        configureStorePaths(mediaDir, thumbDir, 32);

        MediaFile result = service.storeMediaFile(file);

        assertThat(result).isNotNull();
        assertThat(result.getContentType().value()).isEqualTo("image/jpeg");
        assertThat(result.getMediaFileName().value()).isEqualTo("photo.jpg");
        verify(mediaFileRepository).save(any());
    }
}
