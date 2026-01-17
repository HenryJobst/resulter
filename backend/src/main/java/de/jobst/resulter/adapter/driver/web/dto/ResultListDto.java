package de.jobst.resulter.adapter.driver.web.dto;

import java.util.Collection;

public record ResultListDto(
        Long id,
        Long eventId,
        Long raceId,
        String creator,
        String createTime,
        String status,
        Collection<ClassResultDto> classResults,
        Boolean isCertificateAvailable,
        Boolean isCupScoreAvailable,
        Boolean isSplitTimeAvailable) {}
