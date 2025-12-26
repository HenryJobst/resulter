package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.aggregations.PersonRaceResults;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

public record PersonRaceResultJdbcDto(
        Long eventId,
        Long resultListId,
        Long raceId,
        @Nullable OffsetDateTime createTime,
        @Nullable String createTimeZone,
        String resultListStatus,
        String classListShortName,
        String classListName,
        String classGender,
        @Nullable Long courseId,
        Long personId,
        @Nullable Long organisationId,
        @Nullable OffsetDateTime startTime,
        @Nullable String startTimeZone,
        Double punchTime,
        @Nullable Integer position,
        @Nullable Byte raceNumber,
        @Nullable String state) {

    static List<ResultList> asResultLists(Collection<PersonRaceResultJdbcDto> personRaceResultJdbcDtos) {
        record ResultListRaceNumber(Long resultListId, @Nullable Byte raceNumber) {}
        // Schritt 1: Gruppierung nach resultListId und raceNumber
        Map<ResultListRaceNumber, List<PersonRaceResultJdbcDto>> resultListGroups = personRaceResultJdbcDtos.stream()
                .collect(Collectors.groupingBy(personRaceResultJdbcDto -> new ResultListRaceNumber(
                        personRaceResultJdbcDto.resultListId(), personRaceResultJdbcDto.raceNumber())));

        return resultListGroups.entrySet().stream()
                .map(entry -> {
                    ResultListRaceNumber resultListRaceNumber = entry.getKey();
                    Long resultListId1 = resultListRaceNumber.resultListId();
                    List<PersonRaceResultJdbcDto> resultListDtos = entry.getValue();
                    PersonRaceResultJdbcDto sampleDto = resultListDtos.getFirst(); // Ein Beispiel-DTO für
                    // gemeinsame Daten

                    // Schritt 2: Gruppierung nach ClassList
                    Map<String, List<PersonRaceResultJdbcDto>> classResultGroups = resultListDtos.stream()
                            .collect(Collectors.groupingBy(PersonRaceResultJdbcDto::classListShortName));

                    Collection<ClassResult> classResults = classResultGroups.values().stream()
                            .map(classDtos -> {
                                PersonRaceResultJdbcDto sampleClassListDto = classDtos.getFirst();

                                // Schritt 3: Gruppierung nach personId
                                Map<Long, PersonResult> personResults = classDtos.stream()
                                        .collect(Collectors.groupingBy(
                                                PersonRaceResultJdbcDto::personId,
                                                Collectors.collectingAndThen(
                                                        Collectors.toList(), personRaceResultDtos -> {
                                                            PersonRaceResultJdbcDto personSampleDto =
                                                                    personRaceResultDtos
                                                                            .getFirst(); // Ein Beispiel-DTO für
                                                            // gemeinsame Daten

                                                            List<PersonRaceResult> raceResults =
                                                                    personRaceResultDtos.stream()
                                                                            .map(dto -> PersonRaceResult.of(
                                                                                    dto.classListShortName(),
                                                                                    dto.personId(),
                                                                                    dto.startTime() == null || dto.startTimeZone() == null
                                                                                            ? null
                                                                                            : dto.startTime()
                                                                                                    .atZoneSameInstant(
                                                                                                            ZoneId.of(
                                                                                                                    dto
                                                                                                                            .startTimeZone())),
                                                                                    null,
                                                                                    // Finish Time ist hier ausgelassen
                                                                                    dto.punchTime(),
                                                                                    dto.position != null
                                                                                            ? dto.position()
                                                                                                    .longValue()
                                                                                            : null,
                                                                                    dto.raceNumber != null
                                                                                            ? dto.raceNumber()
                                                                                            : (byte) 1,
                                                                                    dto.state != null
                                                                                            ? ResultStatus.valueOf(
                                                                                                    dto.state())
                                                                                            : ResultStatus
                                                                                                    .DID_NOT_START))
                                                                            .collect(Collectors.toList());

                                                            return new PersonResult(
                                                                    new ClassResultShortName(
                                                                            personSampleDto.classListShortName()),
                                                                    new PersonId(personSampleDto.personId()),
                                                                    personSampleDto.organisationId() != null
                                                                            ? new OrganisationId(
                                                                                    personSampleDto.organisationId())
                                                                            : null,
                                                                    new PersonRaceResults(raceResults));
                                                        })));

                                return ClassResult.of(
                                        sampleClassListDto.classListName(),
                                        sampleClassListDto.classListShortName(),
                                        Gender.of(sampleClassListDto.classGender()),
                                        personResults.values(),
                                        sampleClassListDto.courseId() != null ?
                                        CourseId.of(sampleClassListDto.courseId()) : null);
                            })
                            .collect(Collectors.toList());

                    return new ResultList(
                            ResultListId.of(resultListId1),
                            EventId.of(sampleDto.eventId()),
                            RaceId.of(sampleDto.raceId()),
                            "",
                            sampleDto.createTime() == null || sampleDto.createTimeZone() == null
                                    ? null
                                    : sampleDto.createTime().atZoneSameInstant(ZoneId.of(sampleDto.createTimeZone())),
                            sampleDto.resultListStatus(),
                            classResults);
                })
                .toList();
    }
}
