package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.sql.Timestamp;

public class TimestampToTimestampConverter implements Converter<Timestamp, Timestamp> {

    @Override
    public Timestamp convert(@NonNull Timestamp source) {
        return source;
    }
}
