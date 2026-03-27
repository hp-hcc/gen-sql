package com.gensql.generator.generator;

import com.gensql.generator.model.dto.FieldConfig;

public interface ValueGenerator {
    Object generate(FieldConfig config);
    
    default String generateAsString(FieldConfig config) {
        Object value = generate(config);
        return value != null ? value.toString() : null;
    }
}

