package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.core.convert.converter.Converter;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

public class OffsetDateTimeToTimestampConverter implements Converter<OffsetDateTime, Timestamp> {

    @Override
    public Timestamp convert(OffsetDateTime source) {
        return Timestamp.from(source.toInstant());
    }
}
