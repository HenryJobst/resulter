package de.jobst.resulter.adapter.driven.inmemory;

import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.domain.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

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
    public ResultList findOrCreate(ResultList resultList) {
        return resultLists.values().stream()
                .filter(it -> Objects.equals(it.getDomainKey(), resultList.getDomainKey()))
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
        return resultLists.values().stream()
                .filter(it -> Objects.equals(it.getEventId(), id))
                .toList();
    }

    @Override
    public Collection<ResultList> findAllByEventIds(Collection<EventId> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return List.of();
        }

        Set<EventId> eventIdSet = new HashSet<>(eventIds);
        return resultLists.values().stream()
                .filter(it -> eventIdSet.contains(it.getEventId()))
                .toList();
    }

    @Override
    public Optional<ResultList> findById(ResultListId resultListId) {
        return resultLists.values().stream()
                .filter(it -> Objects.equals(it.getId(), resultListId))
                .findAny();
    }

    @Override
    public ResultList findByResultListIdAndClassResultShortNameAndPersonId(
            ResultListId resultListId, ClassResultShortName classResultShortName, PersonId personId) {
        return resultLists.values().stream()
                .filter(it -> Objects.equals(it.getId(), resultListId))
                .filter(it -> it.getClassResults().stream()
                        .anyMatch(
                                classResult -> Objects.equals(classResult.classResultShortName(), classResultShortName)
                                        && classResult.personResults().value().stream()
                                                .anyMatch(result -> Objects.equals(result.personId(), personId))))
                .findAny()
                .orElse(null);
    }

    @Override
    public void replacePersonId(PersonId oldPersonId, PersonId newPersonId) {
        throw new UnsupportedOperationException("Not implemented yet");
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
