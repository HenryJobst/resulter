package de.jobst.resulter.springapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Value("#{'${openapi.config.title}'}")
    private String title;
    @Value("#{'${openapi.config.description}'}")
    private String description;
    @Value("#{'${openapi.config.version}'}")
    private String version;
    @Value("#{'${openapi.config.contact.name}'}")
    private String contact_name;
    @Value("#{'${openapi.config.contact.email}'}")
    private String contact_email;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI().info(apiInfo())
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components().addSecuritySchemes(securitySchemeName,
                new io.swagger.v3.oas.models.security.SecurityScheme().name(securitySchemeName)
                    .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }

    private Info apiInfo() {
        return new Info().title(title)
            .description(description)
            .version(version)
            .contact(new Contact().name(contact_name).email(contact_email));
    }

}
