package com.rpzh.vo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.rpzh.constant.NodeConstant;
import lombok.Data;

@Data
public class NodeDataVo {
    private Integer id;
    @Excel(
            name = "传感器id",
            orderNum = "1"
    )
    private Integer NodeId;
    private String type;
    @Excel(
            name = "设备类别",
            orderNum = "2"
    )
    private String typeName;
    @Excel(
            name = "数据上传时间",
            orderNum = "3"
    )
    private String transferTime;
    @Excel(
            name = "氧气浓度",
            orderNum = "4",
            isColumnHidden = true
    )
    private Double oxygen;
    @Excel(
            name = "二氧化碳浓度",
            orderNum = "4",
            isColumnHidden = true
    )
    private Double concentrationCo2;
    @Excel(
            name = "温度",
            orderNum = "4",
            isColumnHidden = true
    )
    private Double temp;
    @Excel(
            name = "水份含量",
            orderNum = "4",
            isColumnHidden = true
    )
    private Double moist;
    @Excel(
            name = "磷化氢含量",
            orderNum = "4",
            isColumnHidden = true
    )
    private Double phosphine;
    @Excel(
            name = "湿度",
            orderNum = "5",
            isColumnHidden = true
    )
    private Double humiture;
    @Excel(
            name = "采集位置",
            orderNum = "6"
    )
    private String gatherSite;
    @Excel(
            name = "坐标X",
            orderNum = "7"
    )
    private Integer siteX;
    @Excel(
            name = "坐标Y",
            orderNum = "8"
    )
    private Integer siteY;
    @Excel(
            name = "坐标Z",
            orderNum = "9"
    )
    private Integer siteZ;
    private String siteRoute;
    private String stack;
    private Integer alertLevel;
    //物料代码
    private String materialCode;



    public Double getData() {
        switch(this.type) {
            case NodeConstant.CARBON_DIOXIDE_NODE_TYPE_VALUE:
                return this.getConcentrationCo2();
            case NodeConstant.HUMITURE_NODE_TYPE_VALUE:
            case NodeConstant.TEMPERATURE_CORE_CHIP_NODE_TYPE_VALUE:
                return this.getTemp();
            case NodeConstant.WET_CORE_CHIP_NODE_TYPE_VALUE:
                return this.getMoist();
            case NodeConstant.PHOSPHINE_NODE_TYPE_VALUE:
                return this.getPhosphine();
            case NodeConstant.OXYGEN_NODE_TYPE_VALUE:
            default:
                return this.getOxygen();
        }
    }

    public static String getColumn(String type) {
        switch(type) {
            case NodeConstant.CARBON_DIOXIDE_NODE_TYPE_VALUE:
                return "concentrationCo2";
            case NodeConstant.HUMITURE_NODE_TYPE_VALUE:
            case NodeConstant.TEMPERATURE_CORE_CHIP_NODE_TYPE_VALUE:
                return "temp";
            case NodeConstant.WET_CORE_CHIP_NODE_TYPE_VALUE:
                return "moist";
            case NodeConstant.PHOSPHINE_NODE_TYPE_VALUE:
                return "phosphine";
            case NodeConstant.OXYGEN_NODE_TYPE_VALUE:
            default:
                return "oxygen";
        }
    }
}

