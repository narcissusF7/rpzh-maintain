package com.rpzh.pojo;

import com.rpzh.constant.NodeConstant;
import com.rpzh.parse.ParseData;
import com.rpzh.vo.NodeVo;
import lombok.Data;

@Data
public class NodeData {
    private String nodeId;
    private String type;
    private Integer frequency;
    private Double voltage;
    private Integer signalStrength;
    private Integer stackId;
    private Integer maintainPlanId;
    private Integer maintainStageId;
    private Double temp;
    private Double humiture;
    private Double oxygen;
    private String dateTime;



    public NodeData() {
    }

    public NodeData(ParseData parseData, NodeVo nodeVo,int count) {
        this.nodeId = parseData.getNodeId();
        this.type = parseData.getNodeType();
        this.frequency = parseData.getFrequency();
        this.voltage = parseData.getVoltage();
        this.signalStrength = parseData.getSignalStrength();
        this.stackId = nodeVo.getStackId();
        this.maintainStageId = nodeVo.getMaintainStageId();
        this.maintainPlanId = nodeVo.getMaintainPlanId();
        switch (nodeVo.getNodeType()){
            case NodeConstant.OXYGEN_NODE_TYPE_VALUE:
                this.oxygen=parseData.getData().get(count);
                break;
            case NodeConstant.HUMITURE_NODE_TYPE_VALUE:
                this.temp = parseData.getData().get(count);
                this.humiture = parseData.getData().get(count+3);
                break;
            case NodeConstant.TEMPERATURE_CORE_CHIP_NODE_TYPE_VALUE:
                this.temp = parseData.getData().get(0);

        }
    }
}
