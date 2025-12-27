package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.core.convert.converter.Converter;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TimestampToOffsetDateTimeConverter implements Converter<Timestamp, OffsetDateTime> {

    @Override
    public OffsetDateTime convert(Timestamp source) {
        return source.toInstant().atOffset(ZoneOffset.UTC);
    }
}
