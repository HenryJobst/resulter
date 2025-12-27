package de.jobst.resulter.adapter.driven.jdbc;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;

import java.sql.Timestamp;

public class TimestampToTimestampConverter implements Converter<Timestamp, Timestamp> {

    @Override
    public Timestamp convert(@NonNull Timestamp source) {
        return source;
    }
}
