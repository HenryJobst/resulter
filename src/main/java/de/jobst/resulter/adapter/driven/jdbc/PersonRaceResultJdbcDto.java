package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.*;
import org.springframework.lang.NonNull;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record PersonRaceResultJdbcDto(Long eventId, Long resultListId, OffsetDateTime createTime, String createTimeZone,
                                      String resultListStatus, String classListShortName, String classListName,
                                      String classGender, Long courseId, Long personId, Long organisationId,
                                      OffsetDateTime startTime, String startTimeZone, Double punchTime, Long raceNumber,
                                      Integer position, String state) {

    static @NonNull List<ResultList> asResultLists(Collection<PersonRaceResultJdbcDto> personRaceResultJdbcDtos) {
        // Schritt 1: Gruppierung nach resultListId
        Map<Long, List<PersonRaceResultJdbcDto>> resultListGroups =
            personRaceResultJdbcDtos.stream().collect(Collectors.groupingBy(PersonRaceResultJdbcDto::resultListId));

        return resultListGroups.entrySet().stream().map(entry -> {
            Long resultListId1 = entry.getKey();
            List<PersonRaceResultJdbcDto> resultListDtos = entry.getValue();
            PersonRaceResultJdbcDto sampleDto = resultListDtos.getFirst(); // Ein Beispiel-DTO für
            // gemeinsame Daten

            // Schritt 2: Gruppierung nach ClassList
            Map<String, List<PersonRaceResultJdbcDto>> classResultGroups =
                resultListDtos.stream().collect(Collectors.groupingBy(PersonRaceResultJdbcDto::classListShortName));

            Collection<ClassResult> classResults = classResultGroups.values().stream().map(classDtos -> {
                PersonRaceResultJdbcDto sampleClassListDto = classDtos.getFirst();

                // Schritt 3: Gruppierung nach personId
                Map<Long, PersonResult> personResults = classDtos.stream()
                    .collect(Collectors.groupingBy(PersonRaceResultJdbcDto::personId,
                        Collectors.collectingAndThen(Collectors.toList(), personRaceResultDtos -> {
                            PersonRaceResultJdbcDto personSampleDto =
                                personRaceResultDtos.getFirst(); // Ein Beispiel-DTO für gemeinsame Daten

                            List<PersonRaceResult> raceResults = personRaceResultDtos.stream()
                                .map(dto -> PersonRaceResult.of(dto.classListShortName(),
                                    dto.personId(),
                                    dto.raceNumber(),
                                    dto.startTime() == null ?
                                    null :
                                    dto.startTime().atZoneSameInstant(ZoneId.of(dto.startTimeZone())),
                                    null,
                                    // Finish Time ist hier ausgelassen
                                    dto.punchTime(),
                                    dto.position != null ? dto.position().longValue() : null,
                                    ResultStatus.valueOf(dto.state())))
                                .collect(Collectors.toList());

                            return new PersonResult(new ClassResultShortName(personSampleDto.classListShortName()),
                                new PersonId(personSampleDto.personId()),
                                personSampleDto.organisationId() != null ?
                                new OrganisationId(personSampleDto.organisationId()) :
                                null,
                                new PersonRaceResults(raceResults));
                        })));

                return ClassResult.of(sampleClassListDto.classListName(),
                    sampleClassListDto.classListShortName(),
                    Gender.of(sampleClassListDto.classGender()),
                    personResults.values(),
                    CourseId.of(sampleClassListDto.courseId()));
            }).collect(Collectors.toList());

            return new ResultList(new ResultListId(resultListId1),
                new EventId(sampleDto.eventId()),
                "",
                sampleDto.createTime() == null ?
                null :
                sampleDto.createTime().atZoneSameInstant(ZoneId.of(sampleDto.createTimeZone())),
                sampleDto.resultListStatus(),
                classResults);
        }).toList();
    }
}
