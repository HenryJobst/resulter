package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.port.DashboardRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of Dashboard Repository.
 * Uses native SQL queries for counting entities.
 */
@Repository
public class DashboardJdbcRepository implements DashboardRepository {

    private final JdbcTemplate jdbcTemplate;

    public DashboardJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long countEvents() {
        String sql = "SELECT COUNT(*) FROM event";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public long countCups() {
        String sql = "SELECT COUNT(*) FROM cup";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public long countPersons() {
        String sql = "SELECT COUNT(*) FROM person";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public long countOrganisationsExcludingIndividuals() {
        // Count organisations excluding type OTHER,
        // but include OTHER if they have a parent organisation (real clubs with incorrect type)
        String sql = """
            SELECT COUNT(*) FROM organisation o
            WHERE o.type <> 'OTHER'
               OR (o.type = 'OTHER'
                   AND EXISTS (SELECT 1 FROM organisation_organisation oo
                               WHERE oo.organisation_id = o.id))
            """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public long countSplitTimes() {
        String sql = "SELECT COUNT(*) FROM split_time";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public long countRaces() {
        String sql = "SELECT COUNT(*) FROM race";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public long countResultLists() {
        String sql = "SELECT COUNT(*) FROM result_list";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public long countCertificates() {
        String sql = "SELECT COUNT(*) FROM event_certificate_stat";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
