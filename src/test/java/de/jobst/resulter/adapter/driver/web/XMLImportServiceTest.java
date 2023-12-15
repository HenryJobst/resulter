package de.jobst.resulter.adapter.driver.web;

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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.test.database.replace=NONE",
        "resulter.repository.inmemory=false"})
@ContextConfiguration(
        classes = {TestConfig.class},
        loader = AnnotationConfigContextLoader.class)
@ComponentScan(basePackages = {"de.jobst.resulter.application",
        "de.jobst.resulter.adapter.driver.web",
        "de.jobst.resulter.adapter.driven.jpa"})
@EntityScan(basePackages = {
        "de.jobst.resulter.adapter.driver.web",
        "de.jobst.resulter.adapter.driven.jpa"})
@EnableJpaRepositories(basePackages = {"de.jobst.resulter.adapter.driven.jpa"})
class XMLImportServiceTest {

    @Autowired
    XMLImportService importService;

    @Test
    @Transactional
    void importFile() throws Exception {
        String filePath = "import_files/XMLImportServiceTest.xml";

        FileSystemResource fileSystemResource = new FileSystemResource(new File(filePath));

        Event event =
                importService.importFile(fileSystemResource.getInputStream());

        assertThat(event).isNotNull();
        assertThat(Objects.requireNonNull(event.getId()).value()).isGreaterThanOrEqualTo(1L);
        assertThat(event.getName().value()).isEqualTo("Test-Event");

        assertThat(Objects.requireNonNull(event.getClassResults()).value()).hasSize(1);
        Collection<ClassResult> classResults = event.getClassResults().value();
        Optional<ClassResult>
                classResultBK =
                classResults.parallelStream().filter(it -> it.classResultName().value().contains("BK")).findAny();
        assertThat(classResultBK).isPresent();
        assertThat(classResultBK.get().classResultName().value()).isEqualTo("BK (Beginner Kurz)");
        assertThat(classResultBK.get().classResultShortName().value()).isEqualTo("BK");
        assertThat(classResultBK.get().gender()).isEqualTo(Gender.of("M"));

        assertThat(classResultBK.get().personResults().value()).hasSize(2);
        Optional<PersonResult>
                firstPersonResult =
                classResultBK.get()
                        .personResults()
                        .value()
                        .stream()
                        .filter(it -> it.person().getPersonName().familyName().value().contains("Mustermann"))
                        .findAny();
        assertThat(firstPersonResult).isPresent();

        assertThat(Objects.requireNonNull(firstPersonResult.get().person().getId())
                .value()).isGreaterThanOrEqualTo(1);
        assertThat(firstPersonResult.get()
                .person()
                .getPersonName()
                .familyName()).isEqualTo(FamilyName.of("Mustermann"));
        assertThat(firstPersonResult.get().person().getPersonName().givenName()).isEqualTo(GivenName.of("Max"));
        assertThat(firstPersonResult.get().person().getBirthDate().value()).isEqualTo(LocalDate.of(1960, 10, 11));
        assertThat(firstPersonResult.get().person().getGender()).isEqualTo(Gender.M);

        assertThat(Objects.requireNonNull(firstPersonResult.get().organisation().getId())
                .value()).isGreaterThanOrEqualTo(1);
        assertThat(firstPersonResult.get().organisation().getName().value()).isEqualTo("OLV Berlin");
        assertThat(firstPersonResult.get()
                .organisation()
                .getShortName()
                .value()).isEqualTo("OLV");

        assertThat(firstPersonResult.get().personRaceResults().value()).hasSize(1);
        Optional<PersonRaceResult>
                firstPersonRaceResult =
                firstPersonResult.get().personRaceResults().value().stream().findFirst();
        assertThat(firstPersonRaceResult).isPresent();

        assertThat(firstPersonRaceResult.get().raceNumber().value()).isEqualTo(1);
        assertThat(firstPersonRaceResult.get().positon().value()).isEqualTo(1);
        assertThat(firstPersonRaceResult.get().startTime()).isEqualTo(DateTime.of(LocalDateTime.of(LocalDate.of(2020,
                        1,
                        18),
                LocalTime.of(11, 30, 5))));
        assertThat(firstPersonRaceResult.get().finishTime()).isEqualTo(DateTime.of(LocalDateTime.of(LocalDate.of(2020,
                        1,
                        18),
                LocalTime.of(11, 49, 6))));
        assertThat(firstPersonRaceResult.get().runtime().value()).isEqualTo(1141.0);
        assertThat(firstPersonRaceResult.get().state()).isEqualTo(ResultStatus.OK);

        assertThat(firstPersonRaceResult.get().splitTimes().value()).hasSize(6);
        Optional<SplitTime> firstSplittime =
                firstPersonRaceResult.get().splitTimes().value().stream().findFirst();
        assertThat(firstSplittime).isPresent();
        assertThat(firstSplittime.get().controlCode().value()).isEqualTo("134");
        assertThat(firstSplittime.get().punchTime().value()).isEqualTo(212.0);
    }
}