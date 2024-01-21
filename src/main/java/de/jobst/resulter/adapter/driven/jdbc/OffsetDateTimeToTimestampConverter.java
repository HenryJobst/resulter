package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

@Component
public class OffsetDateTimeToTimestampConverter implements Converter<OffsetDateTime, Timestamp> {

    @Override
    public Timestamp convert(@NonNull OffsetDateTime source) {
        return Timestamp.from(source.toInstant());
    }
}
