package com.rpzh.enums;

/**
 * @author wbl
 * [检测的表名]
 */
public enum GatherTables {
    /**
     * 部门
     */
    TB_DEPT("部门"),
    /**
     * 菜单
     */
    TB_MENU("菜单"),
    /**
     * 角色
     */
    TB_ROLE("角色"),
    /**
     * 用户
     */
    TB_USER("用户");
    private final String explain;

    GatherTables(String explain) {
        this.explain = explain;
    }

    public String getExplain() {
        return explain;
    }
}
