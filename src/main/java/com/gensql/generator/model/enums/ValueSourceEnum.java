package com.gensql.generator.model.enums;

import lombok.Getter;

@Getter
public enum ValueSourceEnum {
    SNOWFLAKE("snowflake", "雪花ID"),
    MANUAL("manual", "手动输入"),
    SERIAL("serial", "流水号");

    private final String code;
    private final String description;

    ValueSourceEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ValueSourceEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ValueSourceEnum source : values()) {
            if (source.code.equalsIgnoreCase(code)) {
                return source;
            }
        }
        return null;
    }
}

