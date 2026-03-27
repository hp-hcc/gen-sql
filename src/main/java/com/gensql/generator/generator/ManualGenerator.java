package com.gensql.generator.generator;

import com.gensql.generator.model.dto.FieldConfig;
import org.springframework.stereotype.Component;

@Component
public class ManualGenerator implements ValueGenerator {

    @Override
    public Object generate(FieldConfig config) {
        return config.getValue();
    }
}

