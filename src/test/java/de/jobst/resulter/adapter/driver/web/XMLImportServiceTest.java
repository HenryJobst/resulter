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

@DataJpaTest(properties = {"spring.test.database.replace=NONE", "resulter.repository.inmemory=false"})
@ContextConfiguration(classes = {TestConfig.class}, loader = AnnotationConfigContextLoader.class)
@ComponentScan(basePackages = {"de.jobst.resulter.application", "de.jobst.resulter.adapter.driver.web",
    "de.jobst.resulter.adapter.driven.jpa"})
@EntityScan(basePackages = {"de.jobst.resulter.adapter.driver.web", "de.jobst.resulter.adapter.driven.jpa"})
@EnableJpaRepositories(basePackages = {"de.jobst.resulter.adapter.driven.jpa"})
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
        Person firstPerson =
            importResult.personMap().get(new Person.DomainKey("Mustermann", "Max", LocalDate.of(1960, 10, 11)));

        assertThat(event).isNotNull();
        assertThat(Objects.requireNonNull(event.getId()).value()).isGreaterThanOrEqualTo(1L);
        assertThat(event.getName().value()).isEqualTo("Test-Event");

        assertThat(event.getClassResults().isLoaded()).isTrue();
        assertThat(Objects.requireNonNull(event.getClassResults()).get().value()).hasSize(1);
        Collection<ClassResult> classResults = event.getClassResults().get().value();
        Optional<ClassResult> classResultBK =
            classResults.stream().filter(it -> it.getClassResultName().value().contains("BK")).findAny();
        assertThat(classResultBK).isPresent();
        assertThat(classResultBK.get().getClassResultName().value()).isEqualTo("BK (Beginner Kurz)");
        assertThat(classResultBK.get().getClassResultShortName().value()).isEqualTo("BK");
        assertThat(classResultBK.get().getGender()).isEqualTo(Gender.of("M"));

        assertThat(classResultBK.get().getPersonResults().isLoaded()).isTrue();
        assertThat(classResultBK.get().getPersonResults().get().value()).hasSize(2);
        Optional<PersonResult> firstPersonResult = classResultBK.get()
            .getPersonResults()
            .get()
            .value()
            .stream()
            .filter(it -> Objects.nonNull(it.getPersonId()))
            .filter(it -> it.getPersonId().value() == 1)
            .findAny();
        assertThat(firstPersonResult).isPresent();

        assertThat(firstPerson).isNotNull();
        assertThat(firstPerson.getId().value()).isGreaterThanOrEqualTo(1);
        assertThat(firstPerson.getPersonName().familyName()).isEqualTo(FamilyName.of("Mustermann"));
        assertThat(firstPerson.getPersonName().givenName()).isEqualTo(GivenName.of("Max"));
        assertThat(firstPerson.getBirthDate().value()).isEqualTo(LocalDate.of(1960, 10, 11));
        assertThat(firstPerson.getGender()).isEqualTo(Gender.M);

        assertThat(firstPersonResult.get().getOrganisationId()).isNotNull();
        assertThat(Objects.requireNonNull(firstPersonResult.get().getOrganisationId())
            .value()).isGreaterThanOrEqualTo(1);

        assertThat(firstPersonResult.get().getPersonRaceResults().isLoaded()).isTrue();
        assertThat(firstPersonResult.get().getPersonRaceResults().get().value()).hasSize(1);
        Optional<PersonRaceResult> firstPersonRaceResult =
            firstPersonResult.get().getPersonRaceResults().get().value().stream().findFirst();
        assertThat(firstPersonRaceResult).isPresent();

        assertThat(firstPersonRaceResult.get().getRaceNumber().value()).isEqualTo(1);
        assertThat(firstPersonRaceResult.get().getPosition().value()).isEqualTo(1);
        assertThat(firstPersonRaceResult.get().getStartTime()).isEqualTo(DateTime.of(ZonedDateTime.of(LocalDate.of(2020,
            1,
            18), LocalTime.of(11, 30, 5), ZoneId.systemDefault())));
        assertThat(firstPersonRaceResult.get()
            .getFinishTime()).isEqualTo(DateTime.of(ZonedDateTime.of(LocalDate.of(2020, 1, 18),
            LocalTime.of(11, 49, 6),
            ZoneId.systemDefault())));
        assertThat(firstPersonRaceResult.get().getRuntime().value()).isEqualTo(1141.0);
        assertThat(firstPersonRaceResult.get().getState()).isEqualTo(ResultStatus.OK);

        assertThat(firstPersonRaceResult.get().getSplitTimes().isLoaded()).isTrue();
        assertThat(firstPersonRaceResult.get().getSplitTimes().get().value()).hasSize(6);
        Optional<SplitTime> firstSplittime =
            firstPersonRaceResult.get().getSplitTimes().get().value().stream().findFirst();
        assertThat(firstSplittime).isPresent();
        assertThat(firstSplittime.get().getControlCode().value()).isEqualTo("134");
        assertThat(firstSplittime.get().getPunchTime().value()).isEqualTo(212.0);
    }
}
