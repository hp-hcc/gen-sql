package com.gensql.generator.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlGenerateResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String sql;
    private List<FieldValue> generatedValues;
    private String errorMessage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldValue implements Serializable {
        private static final long serialVersionUID = 1L;
        private String fieldName;
        private Object value;
        private String valueSource;
    }
}

