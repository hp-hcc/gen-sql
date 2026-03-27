package com.gensql.generator.model.enums;

import lombok.Getter;

@Getter
public enum SqlTypeEnum {
    INSERT("insert", "插入语句"),
    UPDATE("update", "更新语句");

    private final String code;
    private final String description;

    SqlTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SqlTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (SqlTypeEnum type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}

