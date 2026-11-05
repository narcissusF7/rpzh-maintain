package com.rpzh.enums;

import lombok.Getter;

/**
 * 仓库路径标识（枚举值在各 api 模块中完全一致，仅注释略有差异，统一收敛到 rpzh-common-api）
 *
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
