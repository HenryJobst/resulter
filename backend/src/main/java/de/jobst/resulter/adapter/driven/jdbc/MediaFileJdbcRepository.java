package de.jobst.resulter.adapter.driven.jdbc;

import java.util.Collection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaFileJdbcRepository
        extends CrudRepository<MediaFileDbo, Long>,
                PagingAndSortingRepository<MediaFileDbo, Long>,
                QueryByExampleExecutor<MediaFileDbo> {

    @NonNull
    Collection<MediaFileDbo> findAll();
}
