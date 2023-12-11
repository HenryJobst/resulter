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
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        assertThat(Objects.requireNonNull(event.getId()).value()).isGreaterThanOrEqualTo(1L);
        assertThat(event.getName().value()).isEqualTo("Winter-OL 2023");

        assertThat(Objects.requireNonNull(event.getClassResults()).value()).hasSize(35);
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

        assertThat(Objects.requireNonNull(firstPersonResult.get().person().getId())
                .value()).isGreaterThanOrEqualTo(1);
        assertThat(firstPersonResult.get().person().getPersonName().familyName()).isEqualTo(FamilyName.of("Graumann"));
        assertThat(firstPersonResult.get().person().getPersonName().givenName()).isEqualTo(GivenName.of("Bernd"));
        assertThat(firstPersonResult.get().person().getBirthDate().value()).isEqualTo(LocalDate.of(1961, 1, 1));
        assertThat(firstPersonResult.get().person().getGender()).isEqualTo(Gender.M);

        assertThat(Objects.requireNonNull(firstPersonResult.get().organisation().getId())
                .value()).isGreaterThanOrEqualTo(1);
        assertThat(firstPersonResult.get().organisation().getName().value()).isEqualTo("ESV Lok Berlin-Schöneweide");
        assertThat(firstPersonResult.get()
                .organisation()
                .getShortName()
                .value()).isEqualTo("ESV Lok Berlin-Schöneweide");

        assertThat(firstPersonResult.get().personRaceResults().value()).hasSize(1);
        Optional<PersonRaceResult>
                firstPersonRaceResult =
                firstPersonResult.get().personRaceResults().value().stream().findFirst();
        assertThat(firstPersonRaceResult).isPresent();

        assertThat(firstPersonRaceResult.get().raceNumber().value()).isEqualTo(1);
        assertThat(firstPersonRaceResult.get().positon().value()).isEqualTo(1);
        assertThat(firstPersonRaceResult.get().startTime()).isEqualTo(DateTime.of(LocalDateTime.of(LocalDate.of(2023,
                        3,
                        18),
                LocalTime.of(11, 30, 5))));
        assertThat(firstPersonRaceResult.get().finishTime()).isEqualTo(DateTime.of(LocalDateTime.of(LocalDate.of(2023,
                        3,
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