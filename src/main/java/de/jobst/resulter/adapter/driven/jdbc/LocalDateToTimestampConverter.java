package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.sql.Timestamp;
import java.time.LocalDate;

public class LocalDateToTimestampConverter implements Converter<LocalDate, Timestamp> {

    @Override
    public Timestamp convert(@NonNull LocalDate source) {
        return Timestamp.valueOf(source.atStartOfDay());
    }
}
