package com.gensql.generator.model.enums;

import lombok.Getter;

@Getter
public enum ConditionOperatorEnum {
    EQ("eq", "="),
    NE("ne", "<>"),
    GT("gt", ">"),
    GTE("gte", ">="),
    LT("lt", "<"),
    LTE("lte", "<="),
    LIKE("like", "LIKE"),
    IN("in", "IN"),
    NOT_IN("not_in", "NOT IN"),
    IS_NULL("is_null", "IS NULL"),
    IS_NOT_NULL("is_not_null", "IS NOT NULL");

    private final String code;
    private final String sqlOperator;

    ConditionOperatorEnum(String code, String sqlOperator) {
        this.code = code;
        this.sqlOperator = sqlOperator;
    }

    public static ConditionOperatorEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ConditionOperatorEnum operator : values()) {
            if (operator.code.equalsIgnoreCase(code)) {
                return operator;
            }
        }
        return null;
    }
}

