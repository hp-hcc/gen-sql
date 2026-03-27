package com.gensql.generator.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Data
public class WhereCondition implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "条件字段不能为空")
    private String fieldName;

    @NotBlank(message = "条件操作符不能为空")
    private String operator;

    private Object value;

    private List<Object> values;
}

