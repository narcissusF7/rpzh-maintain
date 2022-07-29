package com.rpzh.vo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.rpzh.constant.NodeConstant;
import com.rpzh.entity.ToolUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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
    @ApiModelProperty(notes = "设备类别id  01  03  04")
    private String type;
    @Excel(
            name = "设备类别",
            orderNum = "2"
    )
    @ApiModelProperty(notes = "设备类别 01 氧气  03 温湿度  04 包芯温度")
    private String typeName;
    @Excel(
            name = "数据上传时间",
            orderNum = "9"
    )
    @ApiModelProperty(notes = "数据上传时间")
    private String transferTime;
    @Excel(
            name = "氧气浓度",
            orderNum = "3",
            isColumnHidden = true
    )
    @ApiModelProperty(notes = "氧气浓度")
    private Double oxygen;
    @Excel(
            name = "二氧化碳浓度",
            orderNum = "3",
            isColumnHidden = true
    )
    @ApiModelProperty(notes = "二氧化碳浓度")
    private Double concentrationCo2;
    @Excel(
            name = "温度",
            orderNum = "3",
            isColumnHidden = true
    )
    @ApiModelProperty(notes = "温度")
    private Double temp;
    @Excel(
            name = "水份含量",
            orderNum = "3",
            isColumnHidden = true
    )
    @ApiModelProperty(notes = "水份含量")
    private Double moist;
    @Excel(
            name = "磷化氢含量",
            orderNum = "3",
            isColumnHidden = true
    )
    @ApiModelProperty(notes = "磷化氢含量")
    private Double phosphine;
    @Excel(
            name = "湿度",
            orderNum = "4",
            isColumnHidden = true
    )
    @ApiModelProperty(notes = "湿度")
    private Double humiture;
    @Excel(
            name = "采集位置",
            orderNum = "8"
    )
    @ApiModelProperty(notes = "采集位置")
    private String gatherSite;
    @Excel(
            name = "坐标X",
            orderNum = "5"
    )
    @ApiModelProperty(notes = "坐标X")
    private String siteX;
    @Excel(
            name = "坐标Y",
            orderNum = "6"
    )
    @ApiModelProperty(notes = "坐标Y")
    private String siteY;
    @Excel(
            name = "坐标Z",
            orderNum = "7"
    )
    @ApiModelProperty(notes = "坐标Z")
    private String siteZ;
    @ApiModelProperty(notes = "位置路由")
    private String siteRoute;
    @ApiModelProperty(notes = "垛位Id")
    private String stackId;
    @ApiModelProperty(notes = "垛位名称")
    private String stack;
    @ApiModelProperty(notes = "预警等级 0 正常 >0 预警")
    private Integer alertLevel;
    //物料代码
    @ApiModelProperty(notes = "物料代码")
    private String materialCode;
    @ApiModelProperty(notes = "养护计划id")
    private Integer maintainPlanId;
    @ApiModelProperty(notes = "养护计划阶段id")
    private Integer maintainStageId;
    @ApiModelProperty(notes = "预警颜色")
    private String warningColor;

    @ApiModelProperty(notes = "预警类型:01:设备电量故障,02:设备信号强度故障,03:设备丢包故障,04:垛内甲虫预警,05:仓间甲虫预警,06:垛内氧气预警,07:仓间氧气预警,08:垛内温度预警,09:仓间温度预警,10:垛内湿度预警,11:仓间湿度预警,12:包芯温度预警,13:平均包芯高于环境预警,14:仓间粉螟预警")
    private String warningType;

    @ApiModelProperty(notes = "预警类型名称")
    private String warningName;

    @ApiModelProperty(notes = "位置类型")
    private String siteType;

    @ApiModelProperty(notes = "温度预警等级")
    private Integer tempLevel;
    @ApiModelProperty(notes = "温度预警颜色")
    private String tempColor;

    @ApiModelProperty(notes = "湿度预警等级")
    private Integer humidityLevel;
    @ApiModelProperty(notes = "湿度预警颜色")
    private String humidityColor;


    public void setType(String type){
        this.type = type;
        switch (type){
            case "01":
                this.typeName = "氧气设备";
                break;
            case "03":
                this.typeName = "温湿度设备";
                break;
            case "04":
                this.typeName = "包芯温度设备";
                break;
        }
    }

    public Double getData() {
        if (!ToolUtil.isEmpty(this.type)){
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
        return null;
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

    public void setWarningName(){
        String str1 = "01,02,03";
        String str2 = "04,05,06,07,08,09,10,11,12,13,14";
        String warningTypeName ="-";
        if (!ToolUtil.isEmpty(this.warningType)){
            String[] split = this.warningType.split(",");

            for (int i = 0; i <split.length ; i++) {
                if (str2.contains(split[i])){
                    warningTypeName+="预警-";
                    break;
                }
            }

            for (int i = 0; i <split.length ; i++) {
                if (str1.contains(split[i])){
                    warningTypeName+="故障";
                    break;
                }
            }
            if (warningTypeName.length()>1&&warningTypeName.length()>5){
                this.warningName =warningTypeName.substring(1);
            }else if (warningTypeName.length()>1){
                this.warningName =warningTypeName.substring(1,3);
            }else {
                this.warningName ="";
            }
        }else {
            this.warningType = "";
            this.warningName = "";
        }

    }
}

