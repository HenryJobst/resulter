package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.time.Year;

public class IntegerToYearConverter implements Converter<Integer, Year> {

    @Override
    public Year convert(@NonNull Integer source) {
        return Year.of(source);
    }
}
