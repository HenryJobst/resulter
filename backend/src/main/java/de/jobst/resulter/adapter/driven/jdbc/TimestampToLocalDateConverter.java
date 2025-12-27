package de.jobst.resulter.adapter.driven.jdbc;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;

import java.sql.Timestamp;
import java.time.LocalDate;

public class TimestampToLocalDateConverter implements Converter<Timestamp, LocalDate> {

    @Override
    public LocalDate convert(@NonNull Timestamp source) {
        return source.toLocalDateTime().toLocalDate();
    }
}
