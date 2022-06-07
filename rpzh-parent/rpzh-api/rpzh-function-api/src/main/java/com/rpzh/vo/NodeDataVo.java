package com.rpzh.vo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.rpzh.constant.NodeConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "设备数据返回类")
public class NodeDataVo {
    @ApiModelProperty(notes = "主键Id")
    private Integer id;
    @Excel(
            name = "传感器id",
            orderNum = "1"
    )
    @ApiModelProperty(notes = "传感器id")
    private String nodeId;
    @ApiModelProperty(notes = "设备类别id")
    private String type;
    @Excel(
            name = "设备类别",
            orderNum = "2"
    )
    @ApiModelProperty(notes = "设备类别")
    private String typeName;
    @Excel(
            name = "数据上传时间",
            orderNum = "3"
    )
    @ApiModelProperty(notes = "数据上传时间")
    private String transferTime;
    @Excel(
            name = "氧气浓度",
            orderNum = "4",
            isColumnHidden = true
    )
    @ApiModelProperty(notes = "氧气浓度")
    private Double oxygen;
    @Excel(
            name = "二氧化碳浓度",
            orderNum = "4",
            isColumnHidden = true
    )
    @ApiModelProperty(notes = "二氧化碳浓度")
    private Double concentrationCo2;
    @Excel(
            name = "温度",
            orderNum = "4",
            isColumnHidden = true
    )
    @ApiModelProperty(notes = "温度")
    private Double temp;
    @Excel(
            name = "水份含量",
            orderNum = "4",
            isColumnHidden = true
    )
    @ApiModelProperty(notes = "水份含量")
    private Double moist;
    @Excel(
            name = "磷化氢含量",
            orderNum = "4",
            isColumnHidden = true
    )
    @ApiModelProperty(notes = "磷化氢含量")
    private Double phosphine;
    @Excel(
            name = "湿度",
            orderNum = "5",
            isColumnHidden = true
    )
    @ApiModelProperty(notes = "湿度")
    private Double humiture;
    @Excel(
            name = "采集位置",
            orderNum = "6"
    )
    @ApiModelProperty(notes = "采集位置")
    private String gatherSite;
    @Excel(
            name = "坐标X",
            orderNum = "7"
    )
    @ApiModelProperty(notes = "坐标X")
    private Integer siteX;
    @Excel(
            name = "坐标Y",
            orderNum = "8"
    )
    @ApiModelProperty(notes = "坐标Y")
    private Integer siteY;
    @Excel(
            name = "坐标Z",
            orderNum = "9"
    )
    @ApiModelProperty(notes = "坐标Z")
    private Integer siteZ;
    @ApiModelProperty(notes = "位置路由")
    private String siteRoute;
    @ApiModelProperty(notes = "垛位名称")
    private String stack;
    @ApiModelProperty(notes = "预警等级")
    private Integer alertLevel;
    //物料代码
    @ApiModelProperty(notes = "物料代码")
    private String materialCode;
    @ApiModelProperty(notes = "养护计划id")
    private Integer maintainPlanId;
    @ApiModelProperty(notes = "养护计划阶段id")
    private Integer maintainStageId;



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

