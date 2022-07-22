package com.rpzh.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rpzh.entity.AlertSettingSetDefault;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author zyq
 * 预警规则数据返回
 */
@Data
@ApiModel(description = "预警规则数据返回")
public class AlarmValueSettingVo {
    @ApiModelProperty(notes = "垛内氧气上限一级")
    public Double stackOxygenUpperOne= AlertSettingSetDefault.stackOxygenUpperOne;
    @ApiModelProperty(notes = "垛内氧气上限二级")
    private Double stackOxygenUpperTwo=AlertSettingSetDefault.stackOxygenUpperTwo;
    @ApiModelProperty(notes = "垛内氧气上限三级")
    private Double stackOxygenUpperThree=AlertSettingSetDefault.stackOxygenUpperThree;
    @ApiModelProperty(notes = "垛内氧气下限")
    private Double stackOxygenLower=AlertSettingSetDefault.stackOxygenLower;
    @ApiModelProperty(notes = "仓间氧气上限一级")
    private Double warehouseOxygenUpperOne=AlertSettingSetDefault.warehouseOxygenUpperOne;
    @ApiModelProperty(notes = "仓间氧气上限二级")
    private Double warehouseOxygenUpperTwo=AlertSettingSetDefault.warehouseOxygenUpperTwo;
    @ApiModelProperty(notes = "仓间氧气上限三级")
    private Double warehouseOxygenUpperThree=AlertSettingSetDefault.warehouseOxygenUpperThree;
    @ApiModelProperty(notes = "仓间氧气下限")
    private Double warehouseOxygenLower=AlertSettingSetDefault.warehouseOxygenLower;
    @ApiModelProperty(notes = "包芯温度上限一级")
    private Double tempCoreChipUpperOne=AlertSettingSetDefault.tempCoreChipUpperOne;
    @ApiModelProperty(notes = "包芯温度上限二级")
    private Double tempCoreChipUpperTwo=AlertSettingSetDefault.tempCoreChipUpperTwo;
    @ApiModelProperty(notes = "包芯温度上限三级")
    private Double tempCoreChipUpperThree=AlertSettingSetDefault.tempCoreChipUpperThree;
    @ApiModelProperty(notes = "包芯温度下限")
    private Double tempCoreChipLower=AlertSettingSetDefault.tempCoreChipLower;
    @ApiModelProperty(notes = "垛内温度上限一级")
    private Double stackTempUpperOne=AlertSettingSetDefault.stackTempUpperOne;
    @ApiModelProperty(notes = "垛内温度上限二级")
    private Double stackTempUpperTwo=AlertSettingSetDefault.stackTempUpperTwo;
    @ApiModelProperty(notes = "垛内温度上限三级")
    private Double stackTempUpperThree=AlertSettingSetDefault.stackTempUpperThree;
    @ApiModelProperty(notes = "垛内温度下限")
    private Double stackTempLower=AlertSettingSetDefault.stackTempLower;
    @ApiModelProperty(notes = "垛内水分上限一级")
    private Double stackMoistureUpperOne=AlertSettingSetDefault.stackMoistureUpperOne;
    @ApiModelProperty(notes = "垛内水分上限二级")
    private Double stackMoistureUpperTwo=AlertSettingSetDefault.stackMoistureUpperTwo;
    @ApiModelProperty(notes = "垛内水分上限三级")
    private Double stackMoistureUpperThree=AlertSettingSetDefault.stackMoistureUpperThree;
    @ApiModelProperty(notes = "垛内水分下限")
    private Double stackMoistureLower=AlertSettingSetDefault.stackMoistureLower;
    @ApiModelProperty(notes = "仓间温度上限一级")
    private Double warehouseTempUpperOne=AlertSettingSetDefault.warehouseTempUpperOne;
    @ApiModelProperty(notes = "仓间温度上限二级")
    private Double warehouseTempUpperTwo=AlertSettingSetDefault.warehouseTempUpperTwo;
    @ApiModelProperty(notes = "仓间温度上限三级")
    private Double warehouseTempUpperThree=AlertSettingSetDefault.warehouseTempUpperThree;
    @ApiModelProperty(notes = "仓间温度下限")
    private Double warehouseTempLower=AlertSettingSetDefault.warehouseTempLower;
    @ApiModelProperty(notes = "仓间水分上限一级")
    private Double warehouseMoistureUpperOne=AlertSettingSetDefault.warehouseMoistureUpperOne;
    @ApiModelProperty(notes = "仓间水分上限二级")
    private Double warehouseMoistureUpperTwo=AlertSettingSetDefault.warehouseMoistureUpperTwo;
    @ApiModelProperty(notes = "仓间水分上限三级")
    private Double warehouseMoistureUpperThree=AlertSettingSetDefault.warehouseMoistureUpperThree;
    @ApiModelProperty(notes = "仓间水分下限")
    private Double warehouseMoistureLower=AlertSettingSetDefault.warehouseMoistureLower;
    @ApiModelProperty(notes = "平均包芯高于环境一级")
    private Double coreTempHighEnvTempOne=AlertSettingSetDefault.coreTempHighEnvTempOne;
    @ApiModelProperty(notes = "平均包芯高于环境二级")
    private Double coreTempHighEnvTempTwo=AlertSettingSetDefault.coreTempHighEnvTempTwo;
    @ApiModelProperty(notes = "平均包芯高于环境三级")
    private Double coreTempHighEnvTempThree=AlertSettingSetDefault.coreTempHighEnvTempThree;
    @ApiModelProperty(notes = "平均包芯低于环境")
    private Double coreTempLowEnvTemp=AlertSettingSetDefault.coreTempLowEnvTemp;
    @ApiModelProperty(notes = "预警设置表id")
    private Integer alarmValueSettingId;
    @ApiModelProperty(notes = "预警规则名称")
    private String earlyWarningRuleName="通用规则名称";
    @ApiModelProperty(notes = "物料代码集合")
    private List<String> materialCodes;
    @ApiModelProperty(notes = "物料代码")
    private String materialCode;
    @ApiModelProperty(notes = "养护方式")
    private String maintenceStyle;
    @ApiModelProperty(notes = "预警设置id")
    private Integer id;
    @ApiModelProperty(notes = "制定人")
    private String createUser;
    @ApiModelProperty(notes = "最后更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedTime;
    @ApiModelProperty(notes = "养护阶段")
    private String phases;

    @ApiModelProperty(notes = "位置类型,1:仓库, 2: 楼号, 3: 层, 4: 区")
    private Integer siteType;
    @ApiModelProperty(notes = "位置id")
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
                    return new String[]{"tempCoreChipUpperThree","tempCoreChipUpperTwo","tempCoreChipUpperOne","tempCoreChipLower"};
            }
        }

        return new String[0];
    }
    public static String[] getStackColumn() {
        return new String[]{"coreTempHighEnvTempThree","coreTempHighEnvTempTwo","coreTempHighEnvTempOne","coreTempLowEnvTemp"};
    }
}
