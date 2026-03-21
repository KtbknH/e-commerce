package com.yoteh.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI yotehOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Yoteh E-Commerce API")
                .description("API REST pour la plateforme e-commerce Yoteh. "
                    + "Catégories : FEMME, HOMME, Tech/Gadgets. "
                    + "Paiements : Orange Money, MTN Money.")
                .version("0.1.0")
                .contact(new Contact()
                    .name("Yoteh Team")
                    .email("contact@yoteh.com")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer JWT"))
            .components(new Components()
                .addSecuritySchemes("Bearer JWT",
                    new SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Entrez votre token JWT")));
    }
}
