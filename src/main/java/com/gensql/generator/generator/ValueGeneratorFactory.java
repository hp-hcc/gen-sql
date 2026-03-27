package com.gensql.generator.generator;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import com.gensql.generator.model.dto.FieldConfig;
import com.gensql.generator.model.enums.ValueSourceEnum;
import org.springframework.stereotype.Component;

@Component
public class ValueGeneratorFactory {

    private final SnowflakeGenerator snowflakeGenerator;
    private final SerialGenerator serialGenerator;
    private final ManualGenerator manualGenerator;

    private final Map<ValueSourceEnum, ValueGenerator> generatorMap = new HashMap<>();

    public ValueGeneratorFactory(SnowflakeGenerator snowflakeGenerator,
                                  SerialGenerator serialGenerator,
                                  ManualGenerator manualGenerator) {
        this.snowflakeGenerator = snowflakeGenerator;
        this.serialGenerator = serialGenerator;
        this.manualGenerator = manualGenerator;
    }

    @PostConstruct
    public void init() {
        generatorMap.put(ValueSourceEnum.SNOWFLAKE, snowflakeGenerator);
        generatorMap.put(ValueSourceEnum.SERIAL, serialGenerator);
        generatorMap.put(ValueSourceEnum.MANUAL, manualGenerator);
    }

    public ValueGenerator getGenerator(ValueSourceEnum valueSource) {
        ValueGenerator generator = generatorMap.get(valueSource);
        if (generator == null) {
            throw new IllegalArgumentException("不支持的值来源类型: " + valueSource);
        }
        return generator;
    }

    public Object generateValue(ValueSourceEnum valueSource, FieldConfig config) {
        return getGenerator(valueSource).generate(config);
    }
}

