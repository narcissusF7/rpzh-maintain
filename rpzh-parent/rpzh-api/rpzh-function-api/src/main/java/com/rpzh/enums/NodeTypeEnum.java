package com.rpzh.enums;

import com.rpzh.constant.NodeConstant;
import lombok.Getter;

@Getter
public enum NodeTypeEnum {
    OXYGEN_NODE(NodeConstant.OXYGEN_NODE_TYPE_VALUE, "oxygen", "无线氧气传感器", "tb_node_o2_history", "oxygen"),
    CARBON_DIOXIDE_NODE(NodeConstant.CARBON_DIOXIDE_NODE_TYPE_VALUE, "carbonDioxide", "无线二氧化碳传感器", "tb_node_co2_history", "concentration_co2"),
    HUMITURE_NODE(NodeConstant.HUMITURE_NODE_TYPE_VALUE, "humiture", "无线温湿度传感器", "tb_node_env_history", "humiture as data2 , temp"),
    TEMPERATURE_CORE_CHIP_NODE(NodeConstant.TEMPERATURE_CORE_CHIP_NODE_TYPE_VALUE, "tempCoreChip", "无线包芯温度传感器", "tb_node_temp_core_chip_history", "temp"),
    WET_CORE_CHIP_NODE(NodeConstant.WET_CORE_CHIP_NODE_TYPE_VALUE, "wetCoreChip", "无线包芯水分传感器", "tb_node_wet_core_chip_history", "moist"),
    PHOSPHINE_NODE(NodeConstant.PHOSPHINE_NODE_TYPE_VALUE, "phosphine", "磷化氢传感器", "tb_node_phosphine_history", "phosphine");

    private final String code;
    private final String type;
    private final String name;
    private final String tableName;
    private final String tableColumn;

    private NodeTypeEnum(String code, String type, String name, String tableName, String tableColumn) {
        this.code = code;
        this.type = type;
        this.name = name;
        this.tableName = tableName;
        this.tableColumn = tableColumn;
    }

    public static NodeTypeEnum getNodeType(String code) {
        NodeTypeEnum[] values = values();
        for (NodeTypeEnum value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
