package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class JdbcConfiguration {

    @Bean
    public JdbcCustomConversions jdbcCustomConversions() {
        List<Converter<?, ?>> converterList = new ArrayList<>();
        converterList.add(new TimestampToOffsetDateTimeConverter());
        converterList.add(new OffsetDateTimeToTimestampConverter());
        return new JdbcCustomConversions(converterList);
    }
}
