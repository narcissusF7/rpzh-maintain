package com.rpzh.enums;

import com.rpzh.constant.NodeConstant;
import lombok.Getter;

@Getter
public enum NodeOperationEnum {

    ADD_NODE("新增设备","addNode", NodeConstant.ADD_NODE_OPERATE_NUMBER),
    DELETE_NODE("删除设备","deleteNode",NodeConstant.DELETE_NODE_OPERATE_NUMBER),
    UPDATE_NODE("修改设备","updateNode",NodeConstant.UPDATE_NODE_OPERATE_NUMBER),
    REPLACE_NODE("替换设备","replaceNode",NodeConstant.REPLACE_NODE_OPERATE_NUMBER),
    ENTER_STACK("设备入垛","enterStack",NodeConstant.ENTER_STACK_NODE_OPERATE_NUMBER),
    UNBIND_STACK("设备解垛","unbindStack",NodeConstant.UNBIND_STACK_NODE_OPERATE_NUMBER),
    FLIP_STACK("翻垛","flipStack",NodeConstant.FLIP_STACK_NODE_OPERATE_NUMBER),
    MOVE_STACK("移垛","moveStack",NodeConstant.MOVE_STACK_NODE_OPERATE_NUMBER);
    private final String type;
    private final String name;
    private final int typeNum;

    NodeOperationEnum(String name, String type, int typeNum) {
        this.type = type;
        this.name = name;
        this.typeNum = typeNum;
    }


    public static String getName(String type) {
        for (NodeOperationEnum nodeOperation: NodeOperationEnum.values()) {
            if (nodeOperation.getType().equals(type)){
                return nodeOperation.getName();
            }
        }
        return null;
    }
    public static Integer getTypeNum(String type) {
        for (NodeOperationEnum nodeOperation: NodeOperationEnum.values()) {
            if (nodeOperation.getType().equals(type)){
                return nodeOperation.getTypeNum();
            }
        }
        return 0;
    }
}