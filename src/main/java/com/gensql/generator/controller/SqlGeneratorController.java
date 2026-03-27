package com.gensql.generator.controller;

import com.gensql.generator.model.dto.SqlGenerateRequest;
import com.gensql.generator.model.dto.SqlGenerateResponse;
import com.gensql.generator.service.SqlGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/sql")
@Tag(name = "SQL Generator", description = "SQL generator API")
public class SqlGeneratorController {

    private final SqlGeneratorService sqlGeneratorService;

    public SqlGeneratorController(SqlGeneratorService sqlGeneratorService) {
        this.sqlGeneratorService = sqlGeneratorService;
    }

    @PostMapping("/generate")
    @Operation(
            summary = "Generate SQL",
            description = "Generate INSERT or UPDATE SQL from JSON input",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "SQL generation request",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SqlGenerateRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "INSERT By Camel Data",
                                            value = "{\n"
                                                    + "  \"tableName\": \"user\",\n"
                                                    + "  \"sqlType\": \"insert\",\n"
                                                    + "  \"schema\": \"mydb\",\n"
                                                    + "  \"data\": {\n"
                                                    + "    \"userId\": 10001,\n"
                                                    + "    \"orderNo\": \"ORD202603270001\",\n"
                                                    + "    \"userName\": \"张三\"\n"
                                                    + "  }\n"
                                                    + "}"
                                    ),
                                    @ExampleObject(
                                            name = "UPDATE By Camel Data",
                                            value = "{\n"
                                                    + "  \"tableName\": \"user\",\n"
                                                    + "  \"sqlType\": \"update\",\n"
                                                    + "  \"schema\": \"mydb\",\n"
                                                    + "  \"data\": {\n"
                                                    + "    \"userName\": \"李四\",\n"
                                                    + "    \"updatedAt\": \"2024-01-01 12:00:00\"\n"
                                                    + "  },\n"
                                                    + "  \"whereData\": {\n"
                                                    + "    \"userId\": 123456789\n"
                                                    + "  }\n"
                                                    + "}"
                                    ),
                                    @ExampleObject(
                                            name = "Disable Camel Conversion",
                                            value = "{\n"
                                                    + "  \"tableName\": \"user\",\n"
                                                    + "  \"sqlType\": \"insert\",\n"
                                                    + "  \"camelToSnake\": false,\n"
                                                    + "  \"data\": {\n"
                                                    + "    \"user_name\": \"王五\"\n"
                                                    + "  }\n"
                                                    + "}"
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "SQL generated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SqlGenerateResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request")
            }
    )
    public ResponseEntity<SqlGenerateResponse> generateSql(@Valid @RequestBody SqlGenerateRequest request) {
        SqlGenerateResponse response = sqlGeneratorService.generate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/insert")
    @Operation(
            summary = "Generate INSERT",
            description = "Shortcut endpoint for generating INSERT SQL",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "INSERT request",
                    required = true
            )
    )
    public ResponseEntity<SqlGenerateResponse> generateInsert(@Valid @RequestBody SqlGenerateRequest request) {
        request.setSqlType("insert");
        SqlGenerateResponse response = sqlGeneratorService.generate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    @Operation(
            summary = "Generate UPDATE",
            description = "Shortcut endpoint for generating UPDATE SQL",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "UPDATE request",
                    required = true
            )
    )
    public ResponseEntity<SqlGenerateResponse> generateUpdate(@Valid @RequestBody SqlGenerateRequest request) {
        request.setSqlType("update");
        SqlGenerateResponse response = sqlGeneratorService.generate(request);
        return ResponseEntity.ok(response);
    }
}

