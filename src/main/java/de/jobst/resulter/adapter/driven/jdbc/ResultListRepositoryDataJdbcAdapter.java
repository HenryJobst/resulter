package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.domain.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "false")
public class ResultListRepositoryDataJdbcAdapter implements ResultListRepository {

    private final ResultListJdbcRepository resultListJdbcRepository;

    public ResultListRepositoryDataJdbcAdapter(ResultListJdbcRepository resultListJdbcRepository) {
        this.resultListJdbcRepository = resultListJdbcRepository;
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
                resultList.getCreateTime() != null ? Timestamp.from(resultList.getCreateTime().toOffsetDateTime().toInstant()) :
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
        return Optional.ofNullable(PersonRaceResultJdbcDto.asResultLists(personRaceResultJdbcDtos).stream().findFirst().orElse(null));
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
}
