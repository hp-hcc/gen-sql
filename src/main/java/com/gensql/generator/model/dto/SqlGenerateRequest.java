package com.gensql.generator.model.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class SqlGenerateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "表名不能为空")
    private String tableName;

    private String sqlType;

    @Valid
    private List<FieldConfig> fields;

    @Valid
    private List<WhereCondition> whereConditions;

    private Map<String, Object> data;

    private Map<String, Object> whereData;

    /**
     * Whether request field names should be converted from camelCase to snake_case.
     * Defaults to true.
     */
    private Boolean camelToSnake = true;

    private String schema;
}

