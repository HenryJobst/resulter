package de.jobst.resulter.application;

import de.jobst.resulter.application.port.MediaFileRepository;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
}
