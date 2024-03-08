package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "true")
public class InMemoryResultListRepository implements ResultListRepository {

    private final Map<ResultListId, ResultList> resultLists = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final List<ResultList> savedResultLists = new ArrayList<>();

    @Override
    public ResultList save(ResultList resultList) {
        if (ObjectUtils.isEmpty(resultList.getId()) || resultList.getId().value() == 0) {
            resultList.setId(ResultListId.of(sequence.incrementAndGet()));
        }
        resultLists.put(resultList.getId(), resultList);
        savedResultLists.add(resultList);
        return resultList;
    }


    @Override
    public List<ResultList> findAll() {
        return List.copyOf(resultLists.values());
    }

    @Override
    public Optional<ResultList> findById(ResultListId ResultListId) {
        return Optional.ofNullable(resultLists.get(ResultListId));
    }

    @Override
    public ResultList findOrCreate(ResultList resultList) {
        return resultLists.values()
            .stream()
            .filter(it -> Objects.equals(it.getCreator(), resultList.getCreator()) &&
                          Objects.equals(it.getCreateTime(), resultList.getCreateTime()))
            .findAny()
            .orElseGet(() -> save(resultList));
    }

    @Override
    public Collection<ResultList> findOrCreate(Collection<ResultList> resultLists) {
        return resultLists.stream().map(this::findOrCreate).toList();
    }

    @Override
    public ResultList update(ResultList resultList) {
        return save(resultList);
    }

    @Override
    public Collection<ResultList> findByEventId(EventId id) {
        return resultLists.values().stream().filter(it -> Objects.equals(it.getEventId(), id)).toList();
    }

    @Override
    public ResultList findByResultListIdAndClassResultShortNameAndPersonId(ResultListId resultListId,
                                                                           ClassResultShortName classResultShortName,
                                                                           PersonId personId) {
        return resultLists.values()
            .stream()
            .filter(it -> Objects.equals(it.getId(), resultListId))
            .filter(it -> it.getClassResults()
                .stream()
                .anyMatch(classResult -> Objects.equals(classResult.classResultShortName(), classResultShortName) &&
                                         classResult.personResults()
                                             .value()
                                             .stream()
                                             .anyMatch(result -> Objects.equals(result.personId(), personId))))
            .findAny()
            .orElse(null);
    }

    @SuppressWarnings("unused")
    public List<ResultList> savedResultLists() {
        return savedResultLists;
    }

    @SuppressWarnings("unused")
    public int saveCount() {
        return savedResultLists.size();
    }

    @SuppressWarnings("unused")
    public void resetSaveCount() {
        savedResultLists.clear();
    }

}
