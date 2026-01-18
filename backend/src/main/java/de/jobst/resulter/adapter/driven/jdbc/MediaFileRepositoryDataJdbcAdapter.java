package de.jobst.resulter.adapter.driven.jdbc;

import com.turkraft.springfilter.converter.FilterStringConverter;
import com.turkraft.springfilter.parser.node.FilterNode;
import com.turkraft.springfilter.transformer.FilterNodeTransformer;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformResult;
import de.jobst.resulter.adapter.driven.jdbc.transformer.MappingFilterNodeTransformer;
import de.jobst.resulter.application.port.MediaFileRepository;
import de.jobst.resulter.application.util.FilterAndSortConverter;
import de.jobst.resulter.domain.MediaFile;
import de.jobst.resulter.domain.MediaFileId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
@Slf4j
public class MediaFileRepositoryDataJdbcAdapter implements MediaFileRepository {

    private final MediaFileJdbcRepository mediaFileJdbcRepository;
    private final FilterStringConverter filterStringConverter;
    private final FilterNodeTransformer<MappingFilterNodeTransformResult> filterNodeTransformer;

    public MediaFileRepositoryDataJdbcAdapter(
            MediaFileJdbcRepository mediaFileJdbcRepository, FilterStringConverter filterStringConverter) {
        this.mediaFileJdbcRepository = mediaFileJdbcRepository;
        this.filterStringConverter = filterStringConverter;
        this.filterNodeTransformer = new MappingFilterNodeTransformer(new DefaultConversionService());
    }

    @Transactional
    public DboResolver<MediaFileId, MediaFileDbo> getIdResolver() {
        return (MediaFileId id) -> findDboById(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Optional<MediaFileDbo> findDboById(MediaFileId mediaFileId) {
        return mediaFileJdbcRepository.findById(mediaFileId.value());
    }

    @Override
    @Transactional
    public MediaFile save(MediaFile mediaFile) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setMediaFileDboResolver(getIdResolver());
        MediaFileDbo mediaFileDbo = MediaFileDbo.from(mediaFile, dboResolvers);
        MediaFileDbo savedMediaFileEntity = mediaFileJdbcRepository.save(mediaFileDbo);
        return savedMediaFileEntity.asMediaFile();
    }

    @Override
    public void delete(MediaFileId mediaFileId) {
        mediaFileJdbcRepository.deleteById(mediaFileId.value());
    }

    @Override
    public List<MediaFile> findAll() {
        return mediaFileJdbcRepository.findAll().stream()
                .map(x -> x.asMediaFile())
                .sorted()
                .toList();
    }

    @Override
    public Page<MediaFile> findAll(@Nullable String filter, Pageable pageable) {
        Page<MediaFileDbo> page;
        if (filter != null) {
            MediaFileDbo cupDbo = new MediaFileDbo();
            AtomicReference<ExampleMatcher> matcher = new AtomicReference<>(
                    ExampleMatcher.matching()
                    // .withIgnorePaths("")
                    );
            FilterNode filterNode = filterStringConverter.convert(filter);
            log.info("FilterNode: {}", filterNode);
            MappingFilterNodeTransformResult transformResult = filterNodeTransformer.transform(filterNode);
            transformResult.filterMap().forEach((key, value) -> {
                String unquotedValue = value.value().replace("'", "");
                switch (key) {
                    case "fileName" -> {
                        cupDbo.setFileName(unquotedValue);
                        matcher.set(matcher.get().withMatcher("fileName", m -> m.stringMatcher(value.matcher())));
                    }
                    case "description" -> {
                        cupDbo.setDescription(unquotedValue);
                        matcher.set(matcher.get().withMatcher("description", m -> m.stringMatcher(value.matcher())));
                    }
                    case "contentType" -> {
                        cupDbo.setContentType(unquotedValue);
                        matcher.set(matcher.get().withMatcher("contentType", m -> m.stringMatcher(value.matcher())));
                    }
                    case "id" -> {
                        cupDbo.setId(Long.parseLong(unquotedValue));
                        matcher.set(matcher.get().withMatcher("id", ExampleMatcher.GenericPropertyMatcher::exact));
                    }
                }
            });

            page = mediaFileJdbcRepository.findAll(
                    Example.of(cupDbo, matcher.get()),
                    FilterAndSortConverter.mapOrderProperties(pageable, MediaFileDbo::mapOrdersDomainToDbo));

        } else {
            page = mediaFileJdbcRepository.findAll(
                    FilterAndSortConverter.mapOrderProperties(pageable, MediaFileDbo::mapOrdersDomainToDbo));
        }
        return new PageImpl<>(
                page.stream().map(x -> MediaFileDbo.asMediaFile(x)).toList(),
                FilterAndSortConverter.mapOrderProperties(page.getPageable(), PersonDbo::mapOrdersDboToDomain),
                page.getTotalElements());
    }

    @Override
    public Optional<MediaFile> findById(MediaFileId mediaFileId) {
        return mediaFileJdbcRepository.findById(mediaFileId.value()).map(x -> x.asMediaFile());
    }

    @Override
    public List<MediaFile> findAllById(Collection<MediaFileId> mediaFileIds) {
        List<Long> ids = mediaFileIds.stream().map(MediaFileId::value).collect(Collectors.toList());
        Iterable<MediaFileDbo> dbos = mediaFileJdbcRepository.findAllById(ids);
        return StreamSupport.stream(dbos.spliterator(), false)
                .map(dbo -> dbo.asMediaFile())
                .toList();
    }
}
