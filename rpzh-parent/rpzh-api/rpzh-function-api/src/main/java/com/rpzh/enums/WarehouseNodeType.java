package com.rpzh.enums;

import lombok.Getter;

/**
 * 仓库路径标识
 * 例如:[00:0],[01:1],[02:17],[03:19],[04:38],[05:272]
 * 每段前面两位代表仓库标识,后面一位代表相应标识的id
 * 根节点id为0,仓库id为1,楼号id为17,层号id为19,区id为38,垛位id为272
 * @Author liubc
 * @create 2022/2/23 10:20
 * @Description: good good study,day day up
 */
@Getter
public enum WarehouseNodeType {

    BASIC_NODE("00", "根节点"),

    WAREHOUSE_NODE("01", "仓库"),

    BUILDING_NODE("02", "楼号"),

    FLOOR_NODE("03", "层"),

    WAREAREA_NODE("04", "区"),

    STACK_NODE("05", "垛位");
    private String type;

    private String name;

    WarehouseNodeType(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public static WarehouseNodeType getType(String type) {
        for (WarehouseNodeType value : WarehouseNodeType.values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }
        return null;
    }
}
