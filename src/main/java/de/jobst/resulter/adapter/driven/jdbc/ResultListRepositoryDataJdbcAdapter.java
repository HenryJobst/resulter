package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
@Slf4j
public class ResultListRepositoryDataJdbcAdapter implements ResultListRepository {

    private final ResultListJdbcRepository resultListJdbcRepository;

    public ResultListRepositoryDataJdbcAdapter(ResultListJdbcRepository resultListJdbcRepository) {
        this.resultListJdbcRepository = resultListJdbcRepository;
    }

    @NonNull
    private static Throwable getRootCause(@NonNull Throwable t) {
        Throwable rootCause = NestedExceptionUtils.getRootCause(t);
        return rootCause != null ? rootCause : t;
    }

    @Override
    @Transactional
    public ResultList save(ResultList resultList) {
        DboResolvers dboResolvers = DboResolvers.empty();
        dboResolvers.setResultListDboResolver(id -> resultListJdbcRepository.findById(id.value()).orElseThrow());
        ResultListDbo resultListDbo = ResultListDbo.from(resultList, dboResolvers);
        ResultListDbo savedResultListEntity = resultListJdbcRepository.save(resultListDbo);
        return ResultListDbo.asResultLists(List.of(savedResultListEntity)).stream().findFirst().orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultList> findAll() {
        return ResultListDbo.asResultLists(resultListJdbcRepository.findAll()).stream().sorted().toList();
    }

    @Override
    @Transactional
    public ResultList findOrCreate(ResultList resultList) {
        Optional<ResultListId> resultListId =
            resultListJdbcRepository.findResultListIdByDomainKey(resultList.getEventId().value(),
                resultList.getRaceId().value(),
                resultList.getCreator(),
                resultList.getCreateTime() != null ?
                Timestamp.from(resultList.getCreateTime().toOffsetDateTime().toInstant()) :
                null,
                resultList.getCreateTime() != null ? resultList.getCreateTime().getZone().getId() : null);
        return resultListId.map(listId -> findById(listId).orElseThrow()).orElseGet(() -> save(resultList));
    }

    @Override
    @Transactional
    public Collection<ResultList> findOrCreate(Collection<ResultList> resultLists) {
        return resultLists.stream().map(this::findOrCreate).toList();
    }

    @Override
    @Transactional
    public ResultList update(ResultList resultList) {
        return save(resultList);
    }

    @Override
    public Collection<ResultList> findByEventId(EventId id) {
        Collection<PersonRaceResultJdbcDto> personRaceResultJdbcDtos =
            resultListJdbcRepository.findPersonRaceResultsByEventId(id.value());

        return PersonRaceResultJdbcDto.asResultLists(personRaceResultJdbcDtos);
    }

    @Override
    public Optional<ResultList> findById(ResultListId resultListId) {
        Collection<PersonRaceResultJdbcDto> personRaceResultJdbcDtos =
            resultListJdbcRepository.findPersonRaceResultsByResultListId(resultListId.value());
        if (personRaceResultJdbcDtos.isEmpty()) {
            return Optional.empty();
        }
        return PersonRaceResultJdbcDto.asResultLists(personRaceResultJdbcDtos)
            .stream()
            .findFirst();
    }

    @Override
    public ResultList findByResultListIdAndClassResultShortNameAndPersonId(ResultListId resultListId,
                                                                           ClassResultShortName classResultShortName,
                                                                           PersonId personId) {
        List<PersonRaceResultJdbcDto> personRaceResults =
            resultListJdbcRepository.findPersonRaceResultByResultListIdAndClassResultShortNameAndPersonId(resultListId.value(),
                classResultShortName.value(),
                personId.value());
        if (personRaceResults.isEmpty()) {
            return null;
        }
        return Optional.of(personRaceResults.getFirst())
            .flatMap(personRaceResultJdbcDto -> PersonRaceResultJdbcDto.asResultLists(List.of(personRaceResultJdbcDto))
                .stream()
                .findFirst())
            .orElse(null);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void replacePersonId(PersonId oldPersonId, PersonId newPersonId) {
        // 1. Clone parent rows (person_result) for the new person
        long clonedRows = resultListJdbcRepository.cloneResultsForPerson(oldPersonId.value(), newPersonId.value());
        log.debug("Cloned {} rows in person_result from person_id {} to {}", clonedRows, oldPersonId, newPersonId);

        // 2. Update child rows (person_race_result) to point to new person
        long updatedChildRows = resultListJdbcRepository.replacePersonIdInPersonRaceResult(oldPersonId.value(),
            newPersonId.value());
        log.debug("Updated {} rows in person_race_result from person_id {} to {}", updatedChildRows, oldPersonId, newPersonId);

        // 3. Delete old parent rows
        long deletedRows = resultListJdbcRepository.deleteByPersonId(oldPersonId.value());
        log.debug("Deleted {} old rows in person_result with person_id {}", deletedRows, oldPersonId);

    }
}
