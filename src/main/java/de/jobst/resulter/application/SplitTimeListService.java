package de.jobst.resulter.application;

import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.SplitTimeList;
import de.jobst.resulter.domain.SplitTimeListId;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class SplitTimeListService {

    private final SplitTimeListRepository splitTimeListRepository;

    public SplitTimeListService(SplitTimeListRepository splitTimeListRepository) {
        this.splitTimeListRepository = splitTimeListRepository;
    }

    public Collection<SplitTimeList> findOrCreate(Collection<SplitTimeList> splitTimeLists) {
        return splitTimeListRepository.findOrCreate(splitTimeLists);
    }

    public Optional<SplitTimeList> findById(SplitTimeListId splitTimeListId) {
        return splitTimeListRepository.findById(splitTimeListId);
    }

    public List<SplitTimeList> findAll() {
        return splitTimeListRepository.findAll();
    }
}
