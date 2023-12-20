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
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

        assertThat(event.getClassResults().isLoaded()).isTrue();
        assertThat(Objects.requireNonNull(event.getClassResults()).get().value()).hasSize(1);
        Collection<ClassResult> classResults = event.getClassResults().get().value();
        Optional<ClassResult>
                classResultBK =
                classResults.stream().filter(it -> it.getClassResultName().value().contains("BK")).findAny();
        assertThat(classResultBK).isPresent();
        assertThat(classResultBK.get().getClassResultName().value()).isEqualTo("BK (Beginner Kurz)");
        assertThat(classResultBK.get().getClassResultShortName().value()).isEqualTo("BK");
        assertThat(classResultBK.get().getGender()).isEqualTo(Gender.of("M"));

        assertThat(classResultBK.get().getPersonResults().isLoaded()).isTrue();
        assertThat(classResultBK.get().getPersonResults().get().value()).hasSize(2);
        Optional<PersonResult>
                firstPersonResult =
                classResultBK.get()
                        .getPersonResults()
                        .get()
                        .value()
                        .stream()
                        .filter(it -> it.person().isPresent())
                        .filter(it -> it.person().get().getPersonName().familyName().value().contains("Mustermann"))
                        .findAny();
        assertThat(firstPersonResult).isPresent();

        assertThat(Objects.requireNonNull(firstPersonResult.get().person())).isPresent();
        assertThat(Objects.requireNonNull(firstPersonResult.get().person().get().getId())
                .value()).isGreaterThanOrEqualTo(1);
        assertThat(firstPersonResult.get()
                .person().get()
                .getPersonName()
                .familyName()).isEqualTo(FamilyName.of("Mustermann"));
        assertThat(firstPersonResult.get().person().get().getPersonName().givenName()).isEqualTo(GivenName.of("Max"));
        assertThat(firstPersonResult.get().person().get().getBirthDate().value()).isEqualTo(LocalDate.of(1960, 10, 11));
        assertThat(firstPersonResult.get().person().get().getGender()).isEqualTo(Gender.M);

        assertThat(Objects.requireNonNull(firstPersonResult.get().organisation())).isPresent();
        assertThat(Objects.requireNonNull(firstPersonResult.get().organisation().get().getId())
                .value()).isGreaterThanOrEqualTo(1);
        assertThat(firstPersonResult.get().organisation().get().getName().value()).isEqualTo("OLV Berlin");
        assertThat(firstPersonResult.get()
                .organisation().get()
                .getShortName()
                .value()).isEqualTo("OLV");

        assertThat(firstPersonResult.get().personRaceResults()).isPresent();
        assertThat(firstPersonResult.get().personRaceResults().get().value()).hasSize(1);
        Optional<PersonRaceResult>
                firstPersonRaceResult =
                firstPersonResult.get().personRaceResults().get().value().stream().findFirst();
        assertThat(firstPersonRaceResult).isPresent();

        assertThat(firstPersonRaceResult.get().raceNumber().value()).isEqualTo(1);
        assertThat(firstPersonRaceResult.get().positon().value()).isEqualTo(1);
        assertThat(firstPersonRaceResult.get().startTime()).isEqualTo(DateTime.of(ZonedDateTime.of(LocalDate.of(2020,
                        1,
                        18),
                LocalTime.of(11, 30, 5), ZoneId.systemDefault())));
        assertThat(firstPersonRaceResult.get().finishTime()).isEqualTo(DateTime.of(ZonedDateTime.of(LocalDate.of(2020,
                        1,
                        18),
                LocalTime.of(11, 49, 6), ZoneId.systemDefault())));
        assertThat(firstPersonRaceResult.get().runtime().value()).isEqualTo(1141.0);
        assertThat(firstPersonRaceResult.get().state()).isEqualTo(ResultStatus.OK);

        assertThat(firstPersonRaceResult.get().splitTimes()).isPresent();
        assertThat(firstPersonRaceResult.get().splitTimes().get().value()).hasSize(6);
        Optional<SplitTime> firstSplittime =
                firstPersonRaceResult.get().splitTimes().get().value().stream().findFirst();
        assertThat(firstSplittime).isPresent();
        assertThat(firstSplittime.get().controlCode().value()).isEqualTo("134");
        assertThat(firstSplittime.get().punchTime().value()).isEqualTo(212.0);
    }
}