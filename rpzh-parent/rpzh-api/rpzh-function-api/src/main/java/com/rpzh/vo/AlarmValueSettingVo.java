package com.rpzh.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author zyq
 * 预警信息设置
 */
@Data
@ApiModel(description = "预警数值返回数据")
public class AlarmValueSettingVo {

    @ApiModelProperty(notes = "垛内氧气上限一级")
    public Double stackOxygenUpperOne;
    @ApiModelProperty(notes = "垛内氧气上限二级")
    private Double stackOxygenUpperTwo;
    @ApiModelProperty(notes = "垛内氧气上限三级")
    private Double stackOxygenUpperThree;
    @ApiModelProperty(notes = "垛内氧气下限")
    private Double stackOxygenLower;

    @ApiModelProperty(notes = "仓间氧气上限一级")
    private Double warehouseOxygenUpperOne;
    @ApiModelProperty(notes = "仓间氧气上限二级")
    private Double warehouseOxygenUpperTwo;
    @ApiModelProperty(notes = "仓间氧气上限三级")
    private Double warehouseOxygenUpperThree;
    @ApiModelProperty(notes = "仓间氧气下限")
    private Double warehouseOxygenLower;

    @ApiModelProperty(notes = "包芯温度上限一级")
    private Double tempCoreChipUpperOne;
    @ApiModelProperty(notes = "包芯温度上限二级")
    private Double tempCoreChipUpperTwo;
    @ApiModelProperty(notes = "包芯温度上限三级")
    private Double tempCoreChipUpperThree;
    @ApiModelProperty(notes = "包芯温度下限")
    private Double tempCoreChipLower;

    @ApiModelProperty(notes = "垛内温度上限一级")
    private Double stackTempUpperOne;
    @ApiModelProperty(notes = "垛内温度上限二级")
    private Double stackTempUpperTwo;
    @ApiModelProperty(notes = "垛内温度上限三级")
    private Double stackTempUpperThree;
    @ApiModelProperty(notes = "垛内温度下限")
    private Double stackTempLower;

    @ApiModelProperty(notes = "垛内水分上限一级")
    private Double stackMoistureUpperOne;
    @ApiModelProperty(notes = "垛内水分上限二级")
    private Double stackMoistureUpperTwo;
    @ApiModelProperty(notes = "垛内水分上限三级")
    private Double stackMoistureUpperThree;
    @ApiModelProperty(notes = "垛内水分下限")
    private Double stackMoistureLower;

    @ApiModelProperty(notes = "仓间温度上限一级")
    private Double warehouseTempUpperOne;
    @ApiModelProperty(notes = "仓间温度上限二级")
    private Double warehouseTempUpperTwo;
    @ApiModelProperty(notes = "仓间温度上限三级")
    private Double warehouseTempUpperThree;
    @ApiModelProperty(notes = "仓间温度下限")
    private Double warehouseTempLower;

    @ApiModelProperty(notes = "仓间水分上限一级")
    private Double warehouseMoistureUpperOne;
    @ApiModelProperty(notes = "仓间水分上限二级")
    private Double warehouseMoistureUpperTwo;
    @ApiModelProperty(notes = "仓间水分上限三级")
    private Double warehouseMoistureUpperThree;
    @ApiModelProperty(notes = "仓间水分下限")
    private Double warehouseMoistureLower;

    @ApiModelProperty(notes = "平均包芯高于环境一级")
    private Double coreTempHighEnvTempOne;
    @ApiModelProperty(notes = "平均包芯高于环境二级")
    private Double coreTempHighEnvTempTwo;
    @ApiModelProperty(notes = "平均包芯高于环境三级")
    private Double coreTempHighEnvTempThree;
    @ApiModelProperty(notes = "平均包芯低于环境")
    private Double coreTempLowEnvTemp;

    @ApiModelProperty(notes = "预警设置表id")
    private Integer alarmValueSettingId;

    @ApiModelProperty(notes = "预警规则名称")
    private String earlyWarningRuleName;

    private String materialCodes;
    private String maintenceStyle;


    @ApiModelProperty(notes = "预警设置id")
    private Integer id;
    @ApiModelProperty(notes = "制定人")
    private String createUser;
    @ApiModelProperty(notes = "状态(0:停用,1:启用)")
    private String status;
    @ApiModelProperty(notes = "最后更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedTime;
    @ApiModelProperty(notes = "养护阶段")
    private String phases;

    //位置类型,1:仓库, 2: 楼号, 3: 层, 4: 区
    private Integer siteType;
    //采集id
    private Integer siteId;

    public static String[] getColumn(String type,String siteType) {
        if ("04".equals(siteType)){
            switch (type){
                case "01":
                    return new String[]{"warehouseOxygenUpperThree","warehouseOxygenUpperTwo","warehouseOxygenUpperOne","warehouseOxygenLower"};
                case "03":
                    return new String[]{"warehouseTempUpperThree","warehouseTempUpperTwo","warehouseTempUpperOne","warehouseTempLower"
                            ,"warehouseMoistureUpperThree","warehouseMoistureUpperTwo","warehouseMoistureUpperOne","warehouseMoistureLower"};
            }
        }else {
            switch (type){
                case "01":
                    return new String[]{"stackOxygenUpperThree","stackOxygenUpperTwo","stackOxygenUpperOne","stackOxygenLower",};
                case "03":
                    return new String[]{"stackTempUpperThree","stackTempUpperTwo","stackTempUpperOne","stackTempLower"
                            ,"stackMoistureUpperThree","stackMoistureUpperTwo","stackMoistureUpperOne", "stackMoistureLower"};
                case "04":
                    return new String[]{"stackTempUpperThree","stackTempUpperTwo","tempCoreChipUpperOne","tempCoreChipLower"};
            }
        }

        return new String[0];
    }
    public static String[] getStackColumn() {
        return new String[]{"coreTempHighEnvTempThree","coreTempHighEnvTempTwo","coreTempHighEnvTempOne","coreTempLowEnvTemp"};
    }
}
