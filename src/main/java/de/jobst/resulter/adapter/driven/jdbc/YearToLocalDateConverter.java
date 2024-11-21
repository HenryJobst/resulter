package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.time.Year;

public class YearToLocalDateConverter implements Converter<Year, LocalDate> {

    @Override
    public LocalDate convert(@NonNull Year source) {
        return java.time.LocalDate.of(source.getValue(), 1, 1);
    }
}

