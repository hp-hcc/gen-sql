package com.gensql.generator.model.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

import java.io.Serializable;

@Data
public class FieldConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "字段名不能为空")
    private String fieldName;

    @NotBlank(message = "值来源不能为空")
    private String valueSource;

    private String value;

    private String prefix;

    private Long startValue;

    private Long incrementStep;

    public FieldConfig() {
        this.incrementStep = 1L;
    }
}

