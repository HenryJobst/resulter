package de.jobst.resulter.adapter.driven.inmemory;

import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.SplitTimeList;
import de.jobst.resulter.domain.SplitTimeListId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "resulter.repository.inmemory", havingValue = "true")
public class InMemorySplitTimeListRepository implements SplitTimeListRepository {

    private final Map<SplitTimeListId, SplitTimeList> splitTimeLists = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private final List<SplitTimeList> savedSplitTimeLists = new ArrayList<>();

    @Override
    public SplitTimeList save(SplitTimeList splitTimeList) {
        if (ObjectUtils.isEmpty(splitTimeList.getId()) || splitTimeList.getId().value() == 0) {
            splitTimeList.setId(SplitTimeListId.of(sequence.incrementAndGet()));
        }
        splitTimeLists.put(splitTimeList.getId(), splitTimeList);
        savedSplitTimeLists.add(splitTimeList);
        return splitTimeList;
    }

    @Override
    public List<SplitTimeList> findAll() {
        return List.copyOf(splitTimeLists.values());
    }

    @Override
    public Optional<SplitTimeList> findById(SplitTimeListId SplitTimeListId) {
        return Optional.ofNullable(splitTimeLists.get(SplitTimeListId));
    }

    @Override
    public SplitTimeList findOrCreate(SplitTimeList splitTimeList) {
        return splitTimeLists.values().stream()
                .filter(it -> Objects.equals(it.getDomainKey(), splitTimeList.getDomainKey()))
                .findAny()
                .orElseGet(() -> save(splitTimeList));
    }

    @Override
    public Collection<SplitTimeList> findOrCreate(Collection<SplitTimeList> splitTimeLists) {
        return splitTimeLists.stream().map(this::findOrCreate).toList();
    }

    @Override
    public void replacePersonId(PersonId oldPersonId, PersonId newPersonId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<SplitTimeList> findByResultListId(ResultListId resultListId) {
        return splitTimeLists.values().stream()
                .filter(stl -> Objects.equals(stl.getResultListId(), resultListId))
                .toList();
    }

    @Override
    public Set<ResultListId> existsByResultListIds(Collection<ResultListId> resultListIds) {
        if (resultListIds == null || resultListIds.isEmpty()) {
            return Set.of();
        }

        Set<ResultListId> queryIds = new HashSet<>(resultListIds);
        return splitTimeLists.values().stream()
                .map(SplitTimeList::getResultListId)
                .filter(queryIds::contains)
                .collect(java.util.stream.Collectors.toSet());
    }

    @SuppressWarnings("unused")
    public List<SplitTimeList> savedSplitTimeLists() {
        return savedSplitTimeLists;
    }

    @SuppressWarnings("unused")
    public int saveCount() {
        return savedSplitTimeLists.size();
    }

    @SuppressWarnings("unused")
    public void resetSaveCount() {
        savedSplitTimeLists.clear();
    }
}
