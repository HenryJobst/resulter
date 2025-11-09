package de.jobst.resulter.application;

import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.application.port.SplitTimeListService;
import de.jobst.resulter.domain.SplitTimeList;
import de.jobst.resulter.domain.SplitTimeListId;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class SplitTimeListServiceImpl implements SplitTimeListService {

    private final SplitTimeListRepository splitTimeListRepository;

    public SplitTimeListServiceImpl(SplitTimeListRepository splitTimeListRepository) {
        this.splitTimeListRepository = splitTimeListRepository;
    }

    @Override
    public Collection<SplitTimeList> findOrCreate(Collection<SplitTimeList> splitTimeLists) {
        return splitTimeListRepository.findOrCreate(splitTimeLists);
    }

    @Override
    public Optional<SplitTimeList> findById(SplitTimeListId splitTimeListId) {
        return splitTimeListRepository.findById(splitTimeListId);
    }

    @Override
    public List<SplitTimeList> findAll() {
        return splitTimeListRepository.findAll();
    }
}
