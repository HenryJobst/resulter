package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TimestampToOffsetDateTimeConverter implements Converter<Timestamp, OffsetDateTime> {

    @Override
    public OffsetDateTime convert(@NonNull Timestamp source) {
        return source.toInstant().atOffset(ZoneOffset.UTC);
    }
}
