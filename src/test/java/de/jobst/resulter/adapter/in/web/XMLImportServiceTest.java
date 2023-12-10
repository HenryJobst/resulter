package de.jobst.resulter.adapter.in.web;

import de.jobst.resulter.ResulterApplication;
import de.jobst.resulter.adapter.TestConfig;
import de.jobst.resulter.domain.Event;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.test.database.replace=NONE")
@ContextConfiguration(
        classes = {TestConfig.class, ResulterApplication.class},
        loader = AnnotationConfigContextLoader.class)
@EntityScan(basePackages = {"de.jobst.resulter"})
class XMLImportServiceTest {

    @Autowired
    XMLImportService importService;

    @Test
    @Transactional
    void importFile() throws Exception {
        String filePath = "import_files/Zwischenzeiten_IOFv3_WinterOL.xml";

        FileSystemResource fileSystemResource = new FileSystemResource(new File(filePath));

        Event event =
                importService.importFile(fileSystemResource.getInputStream());

        assertThat(event).isNotNull();
        assertThat(Objects.requireNonNull(event.getId()).value())
                .isGreaterThanOrEqualTo(1L);
        assertThat(event.getName().value())
                .isEqualTo("Winter-OL 2023");
        assertThat(Objects.requireNonNull(event.getClassResults()).value())
                .hasSize(35);
    }
}