package de.jobst.resulter.application;

import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.domain.ResultList;
import de.jobst.resulter.domain.ResultListId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResultListService {

    private final ResultListRepository resultListRepository;

    public ResultListService(ResultListRepository resultListRepository) {
        this.resultListRepository = resultListRepository;
    }

    public ResultList findOrCreate(ResultList resultList) {
        return resultListRepository.findOrCreate(resultList);
    }

    public Optional<ResultList> findById(ResultListId resultListId) {
        return resultListRepository.findById(resultListId);
    }

    public List<ResultList> findAll() {
        return resultListRepository.findAll();
    }
}
