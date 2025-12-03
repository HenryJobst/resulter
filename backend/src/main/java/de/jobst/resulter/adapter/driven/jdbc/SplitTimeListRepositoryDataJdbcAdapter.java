package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.SplitTimeList;
import de.jobst.resulter.domain.SplitTimeListId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
@Slf4j
public class SplitTimeListRepositoryDataJdbcAdapter implements SplitTimeListRepository {

    private final SplitTimeListJdbcRepository splitTimeListJdbcRepository;

    public SplitTimeListRepositoryDataJdbcAdapter(SplitTimeListJdbcRepository splitTimeListJdbcRepository) {
        this.splitTimeListJdbcRepository = splitTimeListJdbcRepository;
    }

    @Override
    public SplitTimeList save(SplitTimeList splitTimeList) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setSplitTimeListDboResolver(id -> splitTimeListJdbcRepository.findById(id.value()).orElseThrow());
        SplitTimeListDbo splitTimeListEntity = SplitTimeListDbo.from(splitTimeList, dboResolvers);
        SplitTimeListDbo savedSplitTimeListEntity = splitTimeListJdbcRepository.save(splitTimeListEntity);
        return SplitTimeListDbo.asSplitTimeLists(List.of(savedSplitTimeListEntity)).stream().findFirst().orElse(null);
    }

    @Override
    public List<SplitTimeList> findAll() {
        return SplitTimeListDbo.asSplitTimeLists(splitTimeListJdbcRepository.findAll()).stream().sorted().toList();
    }

    @Override
    public Optional<SplitTimeList> findById(SplitTimeListId splitTimeListId) {
        Optional<SplitTimeListDbo> splitTimeListEntity = splitTimeListJdbcRepository.findById(splitTimeListId.value());
        return splitTimeListEntity.isPresent() ?
               SplitTimeListDbo.asSplitTimeLists(List.of(splitTimeListEntity.orElse(null))).stream().findFirst() :
               Optional.empty();
    }

    @Override
    public SplitTimeList findOrCreate(SplitTimeList splitTimeList) {
        Optional<SplitTimeListDbo> splitTimeListEntity =
            splitTimeListJdbcRepository.findByEventIdAndResultListIdAndClassResultShortNameAndPersonIdAndRaceNumber(
                AggregateReference.to(splitTimeList.getEventId().value()),
                AggregateReference.to(splitTimeList.getResultListId().value()),
                splitTimeList.getClassResultShortName().value(),
                AggregateReference.to(splitTimeList.getPersonId().value()),
                splitTimeList.getRaceNumber().value());
        if (splitTimeListEntity.isEmpty()) {
            return save(splitTimeList);
        }
        SplitTimeListDbo entity = splitTimeListEntity.get();
        return SplitTimeListDbo.asSplitTimeLists(List.of(entity)).stream().findFirst().orElse(null);
    }

    @Override
    @Transactional
    public Collection<SplitTimeList> findOrCreate(Collection<SplitTimeList> splitTimeLists) {
        return splitTimeLists.stream().map(this::findOrCreate).toList();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void replacePersonId(PersonId oldPersonId, PersonId newPersonId) {
        if (splitTimeListJdbcRepository.existsByPersonId(oldPersonId.value())) {
            int updatedRows =
                splitTimeListJdbcRepository.replacePersonIdInSplitTimeList(oldPersonId.value(), newPersonId.value());
            log.debug("Updated {} rows in split_time_list with person_id {} to person_id {}",
                updatedRows,
                oldPersonId,
                newPersonId);
        }
    }

    @Override
    public List<SplitTimeList> findByResultListId(de.jobst.resulter.domain.ResultListId resultListId) {
        Collection<SplitTimeListDbo> splitTimeListDbos =
            splitTimeListJdbcRepository.findByResultListId(AggregateReference.to(resultListId.value()));
        return SplitTimeListDbo.asSplitTimeLists(splitTimeListDbos).stream().toList();
    }

}
