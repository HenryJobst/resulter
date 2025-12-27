package de.jobst.resulter.adapter.driven.jdbc;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

public class OffsetDateTimeToTimestampConverter implements Converter<OffsetDateTime, Timestamp> {

    @Override
    public Timestamp convert(@NonNull OffsetDateTime source) {
        return Timestamp.from(source.toInstant());
    }
}
