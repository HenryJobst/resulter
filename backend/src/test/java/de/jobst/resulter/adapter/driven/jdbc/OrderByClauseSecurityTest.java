package de.jobst.resulter.adapter.driven.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.simple.JdbcClient;

class OrderByClauseSecurityTest {

    private static final String MALICIOUS_PROPERTY = "id DESC; DROP TABLE event; --";

    @Test
    void cupRepository_shouldFallbackToIdForUnknownSortProperty() throws Exception {
        CupJdbcRepositoryImpl repository = new CupJdbcRepositoryImpl(mock(JdbcClient.class));

        String mappedProperty = (String) invokePrivate(repository, "mapSortProperty", MALICIOUS_PROPERTY);
        String orderBy = (String) invokePrivate(
                repository,
                "buildOrderByClause",
                Sort.by(Sort.Order.asc(MALICIOUS_PROPERTY)));

        assertThat(mappedProperty).isEqualTo("id");
        assertThat(orderBy).isEqualTo("ORDER BY id ASC");
    }

    @Test
    void eventRepository_shouldFallbackToIdForUnknownSortProperty() throws Exception {
        EventJdbcRepositoryImpl repository = new EventJdbcRepositoryImpl(mock(JdbcClient.class));

        String mappedProperty = (String) invokePrivate(repository, "mapSortProperty", MALICIOUS_PROPERTY);
        String orderBy = (String) invokePrivate(
                repository,
                "buildOrderByClause",
                Sort.by(Sort.Order.desc(MALICIOUS_PROPERTY)));

        assertThat(mappedProperty).isEqualTo("id");
        assertThat(orderBy).isEqualTo("ORDER BY id DESC");
    }

    @Test
    void organisationRepository_shouldFallbackToIdForUnknownSortProperty() throws Exception {
        OrganisationJdbcRepositoryImpl repository = new OrganisationJdbcRepositoryImpl(mock(JdbcClient.class));

        String mappedProperty = (String) invokePrivate(repository, "mapSortProperty", MALICIOUS_PROPERTY);
        String orderBy = (String) invokePrivate(
                repository,
                "buildOrderByClause",
                Sort.by(Sort.Order.asc(MALICIOUS_PROPERTY)));

        assertThat(mappedProperty).isEqualTo("id");
        assertThat(orderBy).isEqualTo("ORDER BY id ASC");
    }

    private static Object invokePrivate(Object target, String methodName, Object argument) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, argument.getClass());
        method.setAccessible(true);
        return method.invoke(target, argument);
    }
}
