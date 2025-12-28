package de.jobst.resulter.springapp.config;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

/**
 * Dynamic routing DataSource that routes to different databases based on ThreadLocal context.
 * <p>
 * This is used for E2E test database isolation - each test can specify a unique database
 * identifier via the X-DB-Identifier header/cookie, and this router will direct all
 * database operations to that isolated test database.
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    private static final Logger log = LoggerFactory.getLogger(DynamicRoutingDataSource.class);

    private final DataSource defaultDataSource;

    public DynamicRoutingDataSource(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
        setDefaultTargetDataSource(defaultDataSource);
        // AbstractRoutingDataSource requires targetDataSources to be set
        // We use an empty map since we override determineTargetDataSource() directly
        setTargetDataSources(java.util.Collections.emptyMap());
    }

    @Nullable
    @Override
    protected Object determineCurrentLookupKey() {
        // This method is called by Spring for every database operation
        // We return null here because we override determineTargetDataSource() directly
        return null;
    }

    @Override
    protected DataSource determineTargetDataSource() {
        // Get the DataSource from ThreadLocal (set by DataSourceInterceptor)
        DataSource contextDataSource = DataSourceContextHolder.getDataSource();

        if (contextDataSource != null) {
            log.trace("Using context DataSource for current request");
            return contextDataSource;
        }

        // Fall back to default DataSource if no context DataSource is set
        log.trace("Using default DataSource (no context DataSource set)");
        return defaultDataSource;
    }
}
