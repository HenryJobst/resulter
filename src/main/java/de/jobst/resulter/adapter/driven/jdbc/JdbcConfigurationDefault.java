package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.relational.core.mapping.NamingStrategy;

import java.util.Arrays;

@Configuration
@Primary
public class JdbcConfigurationDefault {

    @Bean
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(Arrays.asList(
            new TimestampToLocalDateConverter(),
            new LocalDateToTimestampConverter(),
            new TimestampToOffsetDateTimeConverter(),
            new OffsetDateTimeToTimestampConverter()));
    }

    @Bean
    public NamingStrategy namingStrategy() {
        return new LowerCaseNamingStrategy();
    }

}
