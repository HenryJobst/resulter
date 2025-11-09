package de.jobst.resulter.springapp.config;

import javax.sql.DataSource;

public class DataSourceContextHolder {

    private static final ThreadLocal<DataSource> contextHolder = new ThreadLocal<>();

    public static void setDataSource(DataSource dataSource) {
        contextHolder.set(dataSource);
    }

    @SuppressWarnings("unused")
    public static DataSource getDataSource() {
        return contextHolder.get();
    }

    public static void clearDataSource() {
        contextHolder.remove();
    }
}

