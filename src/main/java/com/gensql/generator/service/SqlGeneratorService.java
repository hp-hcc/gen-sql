package com.gensql.generator.service;

import com.gensql.generator.generator.ValueGeneratorFactory;
import com.gensql.generator.model.dto.FieldConfig;
import com.gensql.generator.model.dto.SqlGenerateRequest;
import com.gensql.generator.model.dto.SqlGenerateResponse;
import com.gensql.generator.model.dto.WhereCondition;
import com.gensql.generator.model.enums.ConditionOperatorEnum;
import com.gensql.generator.model.enums.SqlTypeEnum;
import com.gensql.generator.model.enums.ValueSourceEnum;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SqlGeneratorService {
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private final ValueGeneratorFactory valueGeneratorFactory;

    public SqlGeneratorService(ValueGeneratorFactory valueGeneratorFactory) {
        this.valueGeneratorFactory = valueGeneratorFactory;
    }

    public SqlGenerateResponse generate(SqlGenerateRequest request) {
        try {
            SqlTypeEnum sqlType = SqlTypeEnum.fromCode(request.getSqlType());
            if (sqlType == null) {
                return SqlGenerateResponse.builder()
                        .success(false)
                        .errorMessage("不支持的 SQL 类型: " + request.getSqlType())
                        .build();
            }

            validateRequest(request, sqlType);
            List<SqlGenerateResponse.FieldValue> generatedValues = resolveFieldValues(request);

            String sql;
            switch (sqlType) {
                case INSERT:
                    sql = buildInsertSql(request, generatedValues);
                    break;
                case UPDATE:
                    sql = buildUpdateSql(request, generatedValues);
                    break;
                default:
                    return SqlGenerateResponse.builder()
                            .success(false)
                            .errorMessage("未知的 SQL 类型: " + request.getSqlType())
                            .build();
            }

            return SqlGenerateResponse.builder()
                    .success(true)
                    .sql(sql)
                    .generatedValues(generatedValues)
                    .build();
        } catch (Exception e) {
            return SqlGenerateResponse.builder()
                    .success(false)
                    .errorMessage("生成 SQL 失败: " + e.getMessage())
                    .build();
        }
    }

    private void validateRequest(SqlGenerateRequest request, SqlTypeEnum sqlType) {
        if (!hasFieldConfigs(request) && !hasDataObject(request)) {
            throw new IllegalArgumentException("fields 和 data 不能同时为空");
        }
        if (sqlType == SqlTypeEnum.UPDATE && !hasWhereConditions(request) && !hasWhereData(request)) {
            throw new IllegalArgumentException("UPDATE 请求必须提供 whereConditions 或 whereData");
        }
    }

    private List<SqlGenerateResponse.FieldValue> resolveFieldValues(SqlGenerateRequest request) {
        if (hasFieldConfigs(request)) {
            return generateFieldValues(request);
        }
        return generateFieldValuesFromData(request);
    }

    private List<SqlGenerateResponse.FieldValue> generateFieldValues(SqlGenerateRequest request) {
        return request.getFields().stream()
                .map(field -> {
                    ValueSourceEnum valueSource = ValueSourceEnum.fromCode(field.getValueSource());
                    if (valueSource == null) {
                        throw new IllegalArgumentException("不支持的值来源类型: " + field.getValueSource());
                    }
                    Object value = valueGeneratorFactory.generateValue(valueSource, field);
                    return SqlGenerateResponse.FieldValue.builder()
                            .fieldName(normalizeFieldName(request, field.getFieldName()))
                            .value(value)
                            .valueSource(field.getValueSource())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<SqlGenerateResponse.FieldValue> generateFieldValuesFromData(SqlGenerateRequest request) {
        Map<String, Object> data = request.getData();
        if (data == null || data.isEmpty()) {
            return Collections.emptyList();
        }

        List<SqlGenerateResponse.FieldValue> values = new ArrayList<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            values.add(SqlGenerateResponse.FieldValue.builder()
                    .fieldName(normalizeFieldName(request, entry.getKey()))
                    .value(entry.getValue())
                    .valueSource("manual")
                    .build());
        }
        return values;
    }

    private String buildInsertSql(SqlGenerateRequest request, List<SqlGenerateResponse.FieldValue> values) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");

        if (StringUtils.hasText(request.getSchema())) {
            sql.append(escapeIdentifier(request.getSchema())).append(".");
        }
        sql.append(escapeIdentifier(request.getTableName()));

        String columns = values.stream()
                .map(v -> escapeIdentifier(v.getFieldName()))
                .collect(Collectors.joining(", "));

        String valueStr = values.stream()
                .map(v -> formatValue(v.getValue()))
                .collect(Collectors.joining(", "));

        sql.append(" (").append(columns).append(") VALUES (").append(valueStr).append(");");
        return sql.toString();
    }

    private String buildUpdateSql(SqlGenerateRequest request, List<SqlGenerateResponse.FieldValue> values) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ");

        if (StringUtils.hasText(request.getSchema())) {
            sql.append(escapeIdentifier(request.getSchema())).append(".");
        }
        sql.append(escapeIdentifier(request.getTableName()));

        StringJoiner setClause = new StringJoiner(", ");
        for (SqlGenerateResponse.FieldValue value : values) {
            setClause.add(escapeIdentifier(value.getFieldName()) + " = " + formatValue(value.getValue()));
        }
        sql.append(" SET ").append(setClause);

        List<WhereCondition> whereConditions = resolveWhereConditions(request);
        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ");
            StringJoiner whereClause = new StringJoiner(" AND ");
            for (WhereCondition condition : whereConditions) {
                whereClause.add(buildWhereCondition(request, condition));
            }
            sql.append(whereClause);
        }

        sql.append(";");
        return sql.toString();
    }

    private List<WhereCondition> resolveWhereConditions(SqlGenerateRequest request) {
        if (hasWhereConditions(request)) {
            return request.getWhereConditions();
        }
        if (!hasWhereData(request)) {
            return Collections.emptyList();
        }

        List<WhereCondition> conditions = new ArrayList<>();
        for (Map.Entry<String, Object> entry : request.getWhereData().entrySet()) {
            WhereCondition condition = new WhereCondition();
            condition.setFieldName(normalizeFieldName(request, entry.getKey()));
            condition.setOperator(ConditionOperatorEnum.EQ.getCode());
            condition.setValue(entry.getValue());
            conditions.add(condition);
        }
        return conditions;
    }

    private boolean hasFieldConfigs(SqlGenerateRequest request) {
        return request.getFields() != null && !request.getFields().isEmpty();
    }

    private boolean hasDataObject(SqlGenerateRequest request) {
        return request.getData() != null && !request.getData().isEmpty();
    }

    private boolean hasWhereConditions(SqlGenerateRequest request) {
        return request.getWhereConditions() != null && !request.getWhereConditions().isEmpty();
    }

    private boolean hasWhereData(SqlGenerateRequest request) {
        return request.getWhereData() != null && !request.getWhereData().isEmpty();
    }

    private String buildWhereCondition(SqlGenerateRequest request, WhereCondition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("条件配置不能为空");
        }

        String fieldName = escapeIdentifier(normalizeFieldName(request, condition.getFieldName()));
        ConditionOperatorEnum operator = ConditionOperatorEnum.fromCode(condition.getOperator());
        if (operator == null) {
            throw new IllegalArgumentException("不支持的条件操作符: " + condition.getOperator());
        }

        switch (operator) {
            case IS_NULL:
            case IS_NOT_NULL:
                return fieldName + " " + operator.getSqlOperator();
            case IN:
            case NOT_IN:
                if (condition.getValues() == null || condition.getValues().isEmpty()) {
                    throw new IllegalArgumentException(operator.getCode() + " 条件必须提供非空 values");
                }
                return fieldName + " " + operator.getSqlOperator() + " ("
                        + condition.getValues().stream().map(this::formatValue).collect(Collectors.joining(", "))
                        + ")";
            default:
                if (condition.getValue() == null) {
                    throw new IllegalArgumentException(operator.getCode() + " 条件必须提供 value");
                }
                return fieldName + " " + operator.getSqlOperator() + " " + formatValue(condition.getValue());
        }
    }

    private String normalizeFieldName(SqlGenerateRequest request, String fieldName) {
        if (!Boolean.FALSE.equals(request.getCamelToSnake())) {
            return toSnakeCase(fieldName);
        }
        return fieldName;
    }

    private String toSnakeCase(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        if (value.indexOf('_') >= 0) {
            return value.toLowerCase();
        }

        StringBuilder result = new StringBuilder();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char current = chars[i];
            char previous = i > 0 ? chars[i - 1] : '\0';
            boolean upperCase = Character.isUpperCase(current);
            boolean digit = Character.isDigit(current);

            if (upperCase && i > 0) {
                boolean previousLowerCaseOrDigit = Character.isLowerCase(previous) || Character.isDigit(previous);
                boolean nextLowerCase = i + 1 < chars.length && Character.isLowerCase(chars[i + 1]);
                if (previousLowerCaseOrDigit || nextLowerCase) {
                    result.append('_');
                }
            }

            if (digit && i > 0 && Character.isLetter(previous) && previous != '_') {
                result.append('_');
            }

            if (!digit && !upperCase && i > 0 && Character.isDigit(previous) && current != '_') {
                result.append('_');
            }

            result.append(Character.toLowerCase(current));
        }
        return result.toString();
    }

    private String escapeIdentifier(String identifier) {
        if (!StringUtils.hasText(identifier)) {
            throw new IllegalArgumentException("标识符不能为空");
        }
        validateIdentifier(identifier);
        return "`" + identifier.replace("`", "``") + "`";
    }

    private void validateIdentifier(String identifier) {
        if (!IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            throw new IllegalArgumentException("非法标识符: " + identifier);
        }
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? "TRUE" : "FALSE";
        }
        String strValue = value.toString();
        return "'" + strValue.replace("'", "''") + "'";
    }
}

