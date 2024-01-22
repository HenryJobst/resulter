package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.lang.NonNull;

public class LowerCaseNamingStrategy implements NamingStrategy {

    @NonNull
    @Override
    public String getSchema() {
        return NamingStrategy.super.getSchema().toLowerCase();
    }

    @NonNull
    @Override
    public String getTableName(Class<?> type) {
        return type.getSimpleName().toLowerCase();
    }

    @NonNull
    @Override
    public String getColumnName(@NonNull RelationalPersistentProperty property) {
        return NamingStrategy.super.getColumnName(property).toLowerCase();
    }

    @NonNull
    @Override
    public String getReverseColumnName(@NonNull RelationalPersistentProperty property) {
        return NamingStrategy.super.getReverseColumnName(property).toLowerCase();
    }

    @NonNull
    @Override
    public String getReverseColumnName(@NonNull RelationalPersistentEntity<?> owner) {
        return NamingStrategy.super.getReverseColumnName(owner).toLowerCase();
    }

    @NonNull
    @Override
    public String getKeyColumn(@NonNull RelationalPersistentProperty property) {
        return NamingStrategy.super.getKeyColumn(property).toLowerCase();
    }
}
