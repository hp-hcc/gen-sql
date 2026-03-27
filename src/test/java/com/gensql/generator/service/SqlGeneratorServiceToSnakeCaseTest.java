package com.gensql.generator.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SqlGeneratorServiceToSnakeCaseTest {

    private final SqlGeneratorService service = new SqlGeneratorService(null);

    @Test
    void shouldConvertSimpleCamelCase() throws Exception {
        assertEquals("user_name", invokeToSnakeCase("userName"));
        assertEquals("updated_at", invokeToSnakeCase("updatedAt"));
        assertEquals("phone_number", invokeToSnakeCase("phoneNumber"));
    }

    @Test
    void shouldConvertAcronymAndMixedCaseNames() throws Exception {
        assertEquals("user_id", invokeToSnakeCase("userId"));
        assertEquals("user_id_number", invokeToSnakeCase("userIDNumber"));
        assertEquals("url_value", invokeToSnakeCase("URLValue"));
        assertEquals("http_status_code", invokeToSnakeCase("HTTPStatusCode"));
    }

    @Test
    void shouldHandleDigits() throws Exception {
        assertEquals("user_2_name", invokeToSnakeCase("user2Name"));
        assertEquals("version_2_value", invokeToSnakeCase("version2Value"));
        assertEquals("order_2024_no", invokeToSnakeCase("order2024No"));
    }

    @Test
    void shouldKeepUnderscoreNamesAndLowercaseThem() throws Exception {
        assertEquals("user_name", invokeToSnakeCase("user_name"));
        assertEquals("user_name", invokeToSnakeCase("USER_NAME"));
        assertEquals("mixed_case_name", invokeToSnakeCase("Mixed_Case_Name"));
    }

    @Test
    void shouldHandleSingleWordAndEdgeValues() throws Exception {
        assertEquals("username", invokeToSnakeCase("username"));
        assertEquals("username", invokeToSnakeCase("Username"));
        assertEquals("x", invokeToSnakeCase("x"));
        assertEquals("", invokeToSnakeCase(""));
        assertNull(invokeToSnakeCase(null));
    }

    private String invokeToSnakeCase(String value) throws Exception {
        Method method = SqlGeneratorService.class.getDeclaredMethod("toSnakeCase", String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, value);
    }
}

