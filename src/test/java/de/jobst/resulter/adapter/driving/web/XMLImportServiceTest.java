package de.jobst.resulter.adapter.driving.web;

import de.jobst.resulter.adapter.TestConfig;
import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.test.database.replace=NONE",
        "resulter.repository.inmemory=true"})
@ContextConfiguration(
        classes = {TestConfig.class},
        loader = AnnotationConfigContextLoader.class)
@ComponentScan(basePackages = {"de.jobst.resulter.application",
        "de.jobst.resulter.adapter.driving.web",
        "de.jobst.resulter.adapter.driven.jpa"})
@EntityScan(basePackages = {
        "de.jobst.resulter.adapter.driving.web",
        "de.jobst.resulter.adapter.driven.jpa"})
@EnableJpaRepositories(basePackages = {"de.jobst.resulter.adapter.driven.jpa"})
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
        Collection<ClassResult> classResults = event.getClassResults().value();
        assertThat(classResults).element(0).extracting(ClassResult::classResultName).isEqualTo(
                ClassResultName.of("BK (Beginner Kurz)"));
        assertThat(classResults).element(0).extracting(ClassResult::classResultShortName).isEqualTo(
                ClassResultShortName.of("BK"));
        assertThat(classResults).element(0).extracting(ClassResult::gender).isEqualTo(
                Gender.of("M"));
        Optional<ClassResult> first = classResults.stream().findFirst();
        assertThat(first).isPresent();
        assertThat(first.get().personResults().value()).hasSize(13);
        Optional<PersonResult> firstPersonResult = first.get().personResults().value().stream().findFirst();
        assertThat(firstPersonResult).isPresent();
        assertThat(firstPersonResult.get().person().personName().familyName()).isEqualTo(FamilyName.of("Graumann"));
        assertThat(firstPersonResult.get().person().personName().givenName()).isEqualTo(GivenName.of("Bernd"));
        assertThat(firstPersonResult.get().person().birthDate().value()).isEqualTo(LocalDate.of(1961, 1, 1));
        assertThat(firstPersonResult.get().person().gender()).isEqualTo(Gender.M);
    }
}