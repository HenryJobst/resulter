package de.jobst.resulter.springapp.config;

import org.jspecify.annotations.Nullable;

import javax.sql.DataSource;

public class DataSourceContextHolder {

    private static final ThreadLocal<@Nullable DataSource> contextHolder = new ThreadLocal<>();

    public static void setDataSource(DataSource dataSource) {
        contextHolder.set(dataSource);
    }

    @SuppressWarnings("unused")
    public static @Nullable DataSource getDataSource() {
        return contextHolder.get();
    }

    public static void clearDataSource() {
        contextHolder.remove();
    }
}

