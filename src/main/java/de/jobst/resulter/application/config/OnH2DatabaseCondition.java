package de.jobst.resulter.application.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnH2DatabaseCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String jdbcUrl = context.getEnvironment().getProperty("jdbc.url");
        return jdbcUrl != null && jdbcUrl.startsWith("jdbc:h2");
    }
}
