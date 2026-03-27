package com.gensql.generator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GenSQL API")
                        .version("1.0.0")
                        .description("GenSQL is a lightweight tool for generating SQL INSERT and UPDATE statements from JSON input.\n\n"
                                + "Supported value sources:\n"
                                + "- `snowflake`: distributed unique ID\n"
                                + "- `manual`: caller-provided value\n"
                                + "- `serial`: timestamp-based serial string\n\n"
                                + "Where conditions are structured and safely assembled with whitelisted operators.\n\n"
                                + "Example request:\n"
                                + "```json\n"
                                + "{\n"
                                + "  \"tableName\": \"user\",\n"
                                + "  \"sqlType\": \"update\",\n"
                                + "  \"schema\": \"mydb\",\n"
                                + "  \"fields\": [\n"
                                + "    {\"fieldName\": \"name\", \"valueSource\": \"manual\", \"value\": \"Alice\"}\n"
                                + "  ],\n"
                                + "  \"whereConditions\": [\n"
                                + "    {\"fieldName\": \"id\", \"operator\": \"eq\", \"value\": 1},\n"
                                + "    {\"fieldName\": \"status\", \"operator\": \"in\", \"values\": [\"INIT\", \"READY\"]}\n"
                                + "  ]\n"
                                + "}\n"
                                + "```")
                        .contact(new Contact()
                                .name("GenSQL Team")
                                .email("support@gensql.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}

