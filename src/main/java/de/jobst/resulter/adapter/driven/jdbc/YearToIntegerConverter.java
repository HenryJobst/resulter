package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.time.Year;

public class YearToIntegerConverter implements Converter<Year, Integer> {

    @Override
    public Integer convert(@NonNull Year source) {
        return source.getValue();
    }
}

