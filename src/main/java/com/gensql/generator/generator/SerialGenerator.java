package com.gensql.generator.generator;

import com.gensql.generator.model.dto.FieldConfig;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class SerialGenerator implements ValueGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final int RANDOM_BOUND = 10000;

    @Override
    public String generate(FieldConfig config) {
        String prefix = config.getPrefix() != null ? config.getPrefix() : "";
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int random = ThreadLocalRandom.current().nextInt(0, RANDOM_BOUND);
        String randomStr = String.format("%04d", random);
        return prefix + timestamp + randomStr;
    }
}

