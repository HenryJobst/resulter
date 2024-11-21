package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.application.config.OnH2DatabaseCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.relational.core.mapping.NamingStrategy;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Conditional(OnH2DatabaseCondition.class)
@Primary
public class JdbcConfigurationForH2 {

    @Bean
    public JdbcCustomConversions jdbcCustomConversions() {
        List<Converter<?, ?>> converterList = new ArrayList<>();
        converterList.add(new TimestampToLocalDateConverter());
        converterList.add(new LocalDateToTimestampConverter());
        converterList.add(new TimestampToOffsetDateTimeConverter());
        converterList.add(new OffsetDateTimeToTimestampConverter());
        converterList.add(new YearToIntegerConverter());
        converterList.add(new IntegerToYearConverter());
        converterList.add(new YearToLocalDateConverter());
        return new JdbcCustomConversions(converterList);
    }

    @Bean
    public NamingStrategy namingStrategy() {
        return new LowerCaseNamingStrategy();
    }
}
