package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.TestConfig;
import de.jobst.resulter.domain.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest(properties = {"spring.test.database.replace=NONE", "resulter.repository.inmemory=false"})
@ContextConfiguration(classes = {TestConfig.class}, loader = AnnotationConfigContextLoader.class)
@ComponentScan(basePackages = {"de.jobst.resulter.application", "de.jobst.resulter.adapter.driver.web",
    "de.jobst.resulter.adapter.driven.jdbc"})
@EntityScan(basePackages = {"de.jobst.resulter.adapter.driver.web", "de.jobst.resulter.adapter.driven.jdbc"})
@EnableJdbcRepositories(basePackages = {"de.jobst.resulter.adapter.driven.jdbc"})
@ExtendWith(SpringExtension.class)
class XMLImportServiceTest {

    @Autowired
    XMLImportService importService;

    @Test
    @Transactional
    void importFile() throws Exception {
        String filePath = "import_files/XMLImportServiceTest.xml";

        FileSystemResource fileSystemResource = new FileSystemResource(new File(filePath));

        XMLImportService.ImportResult importResult = importService.importFile(fileSystemResource.getInputStream());
        Event event = importResult.event();
        assertThat(event).isNotNull();
        assertThat(Objects.requireNonNull(event.getId()).value()).isGreaterThanOrEqualTo(1L);
        assertThat(event.getName().value()).isEqualTo("Test-Event");
    }
}
