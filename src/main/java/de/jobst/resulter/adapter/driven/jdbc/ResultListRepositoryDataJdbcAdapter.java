package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.domain.ResultList;
import de.jobst.resulter.domain.ResultListId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
        ResultListDbo savedResultListEntity =
            resultListJdbcRepository.save(ResultListDbo.from(resultList, dboResolvers));
        return ResultListDbo.asResultLists(List.of(savedResultListEntity)).stream().findFirst().orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultList> findAll() {
        return ResultListDbo.asResultLists(resultListJdbcRepository.findAll()).stream().sorted().toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResultList> findById(ResultListId resultListId) {
        Optional<ResultListDbo> resultListEntity = resultListJdbcRepository.findById(resultListId.value());
        return resultListEntity.isPresent() ?
               ResultListDbo.asResultLists(List.of(resultListEntity.orElse(null))).stream().findFirst() :
               Optional.empty();
    }

    @Override
    @Transactional
    public ResultList findOrCreate(ResultList resultList) {
        Optional<ResultListDbo> resultListEntity =
            resultListJdbcRepository.findByCreatorAndCreateTime(resultList.getCreator(), resultList.getCreateTime());
        if (resultListEntity.isEmpty()) {
            return save(resultList);
        }
        return ResultListDbo.asResultLists(List.of(resultListEntity.get())).stream().findFirst().orElse(null);
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
}
