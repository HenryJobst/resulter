package de.jobst.resulter.application.config;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("testcontainers")
public class DataSourceCleanupScheduler {

    private final DataSourceManager dataSourceManager;

    public DataSourceCleanupScheduler(DataSourceManager dataSourceManager) {
        this.dataSourceManager = dataSourceManager;
    }

    @Scheduled(fixedRate = 10000) // Überprüfen Sie alle 10 Sekunden
    public void cleanUp() {
        dataSourceManager.checkForInactiveDataSources();
    }
}

