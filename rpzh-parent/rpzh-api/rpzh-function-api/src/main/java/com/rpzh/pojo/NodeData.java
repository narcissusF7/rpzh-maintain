package com.rpzh.pojo;

import com.rpzh.constant.NodeConstant;
import com.rpzh.parse.ParseData;
import com.rpzh.vo.NodeVo;
import lombok.Data;
/**
 * @Author: ljj
 * @Date: 2022/05/18/15:30
 * @Description: 设备数据实体类
 */
@Data
public class NodeData{
    //主键id
    private Integer id;
    //设备id
    private String nodeId;
    //设备类型
    private String type;
    //频率
    private Integer frequency;
    //电压
    private Double voltage;
    //信号强度
    private Integer signalStrength;
    //垛位id
    private Integer stackId;
    //养护计划id
    private Integer maintainPlanId;
    //养护阶段id
    private Integer maintainStageId;
    //温度
    private Double temp=0d;
    //湿度
    private Double humiture=0d;
    //氧气浓度
    private Double oxygen=0d;
    //传输时间
    private String transferTime;
    //采集位置
    private String gatherSite;
    //位置路由
    private String pidRoute;
    //位置类型
    private String siteType;
    //预警等级
    private Integer alertLevel;
    private String siteX;
    private String siteY;
    private String siteZ;
    //物料等级
    private String materialCode;
    //预警类型
    private String warningType;
    //温度预警等级
    private Integer tempLevel;
    //湿度预警等级
    private Integer humidityLevel;




    public NodeData() {
    }

    public NodeData(String type, Integer maintainStageId, String pidRoute, String siteType) {
        this.type = type;
        this.maintainStageId = maintainStageId;
        this.pidRoute = pidRoute;
        this.siteType = siteType;
    }

    public NodeData(ParseData parseData, NodeVo nodeVo, int count) {
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
        this.materialCode = nodeVo.getMaterialCode();
        this.transferTime = parseData.getDateTime().get(count);
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
                this.tempLevel = nodeVo.getTempLevel();
                this.humidityLevel = nodeVo.getHumidityLevel();
                break;
            case NodeConstant.TEMPERATURE_CORE_CHIP_NODE_TYPE_VALUE:
                this.temp = parseData.getData().get(0);

        }
    }
}
