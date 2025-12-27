package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.core.convert.converter.Converter;

import java.sql.Timestamp;
import java.time.LocalDate;

public class LocalDateToTimestampConverter implements Converter<LocalDate, Timestamp> {

    @Override
    public Timestamp convert(LocalDate source) {
        return Timestamp.valueOf(source.atStartOfDay());
    }
}
