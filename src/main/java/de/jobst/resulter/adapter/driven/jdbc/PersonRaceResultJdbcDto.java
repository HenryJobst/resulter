package de.jobst.resulter.adapter.driven.jdbc;

import java.time.OffsetDateTime;

public record PersonRaceResultJdbcDto(Long resultListId, OffsetDateTime createTime, String createTimeZone,
                                      String resultListStatus, String classListShortName, String classListName,
                                      Long courseId, Long personId, Long organisationId, OffsetDateTime startTime,
                                      String startTimeZone, Double punchTime, Long raceNumber, Integer position,
                                      String state) {}
