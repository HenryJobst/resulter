package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.SplitTimeList;
import de.jobst.resulter.domain.SplitTimeListId;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

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
        return splitTimeLists.values()
            .stream()
            .filter(it -> Objects.equals(it.getEventId(), splitTimeList.getEventId()))
            .filter(it -> Objects.equals(it.getResultListId(), splitTimeList.getResultListId()))
            .filter(it -> Objects.equals(it.getClassResultShortName(), splitTimeList.getClassResultShortName()))
            .filter(it -> Objects.equals(it.getPersonId(), splitTimeList.getPersonId()))
            .findAny()
            .orElseGet(() -> save(splitTimeList));
    }

    @Override
    public Collection<SplitTimeList> findOrCreate(Collection<SplitTimeList> splitTimeLists) {
        return null;
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
