package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;

public class LowerCaseNamingStrategy implements NamingStrategy {

    @Override
    public String getSchema() {
        return NamingStrategy.super.getSchema().toLowerCase();
    }

    @Override
    public String getTableName(Class<?> type) {
        return type.getSimpleName().toLowerCase();
    }

    @Override
    public String getColumnName(RelationalPersistentProperty property) {
        return NamingStrategy.super.getColumnName(property).toLowerCase();
    }

    @Override
    public String getReverseColumnName(RelationalPersistentProperty property) {
        return NamingStrategy.super.getReverseColumnName(property).toLowerCase();
    }

    @Override
    public String getReverseColumnName(RelationalPersistentEntity<?> owner) {
        return NamingStrategy.super.getReverseColumnName(owner).toLowerCase();
    }

    @Override
    public String getKeyColumn(RelationalPersistentProperty property) {
        return NamingStrategy.super.getKeyColumn(property).toLowerCase();
    }
}
