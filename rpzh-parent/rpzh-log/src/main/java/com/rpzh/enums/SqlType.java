package com.rpzh.enums;

/**
 * @author wbl
 * [sql类型]
 */
public enum SqlType {
    /**
     * 添加
     */
    INSERT("INSERT", "新增"),
    /**
     * 更新
     */
    UPDATE("UPDATE", "修改"),
    /**
     * 删除
     */
    DELETE("DELETE", "删除");

    private final String type;
    private final String explain;

    SqlType(String type, String explain) {
        this.type = type;
        this.explain = explain;
    }

    public String getType() {
        return type;
    }

    public String getExplain() {
        return explain;
    }
}