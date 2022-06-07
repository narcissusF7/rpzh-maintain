package com.rpzh.pojo;

import com.rpzh.constant.NodeConstant;
import com.rpzh.parse.ParseData;
import com.rpzh.vo.NodeVo;
import lombok.Data;

import java.io.Serializable;

@Data
public class NodeData{
    private Integer id;
    private String nodeId;
    private String type;
    private Integer frequency;
    private Double voltage;
    private Integer signalStrength;
    private Integer stackId;
    private Integer maintainPlanId;
    private Integer maintainStageId;
    private Double temp=0d;
    private Double humiture=0d;
    private Double oxygen=0d;
    private String dateTime;
    private String gatherSite;
    private String pidRoute;
    private String siteType;
    private Integer alertLevel;
    private String siteX;
    private String siteY;
    private String siteZ;



    public NodeData() {
    }

    public NodeData(ParseData parseData, NodeVo nodeVo,int count) {
        this.id = nodeVo.getId();
        this.nodeId = parseData.getNodeId();
        this.type = parseData.getNodeType();
        this.frequency = parseData.getFrequency();
        this.voltage = parseData.getVoltage();
        this.signalStrength = parseData.getSignalStrength();
        this.stackId = nodeVo.getStackId();
        this.maintainStageId = nodeVo.getMaintainStageId();
        this.maintainPlanId = nodeVo.getMaintainPlanId();
        this.gatherSite = nodeVo.getGatherSite();
        this.siteType = nodeVo.getSiteType();
        this.alertLevel = nodeVo.getAlertLevel();
        this.pidRoute =nodeVo.getSiteRoute();
        if (NodeConstant.NODE_STACK.equals(nodeVo.getSiteType())){
            this.siteX = nodeVo.getSiteX();
            this.siteY = nodeVo.getSiteY();
            this.siteZ = nodeVo.getSiteZ();
        }
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
