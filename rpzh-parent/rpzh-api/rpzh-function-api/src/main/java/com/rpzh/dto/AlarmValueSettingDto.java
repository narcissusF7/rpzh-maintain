package com.rpzh.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author zyq
 * 预警信息设置
 */
@Data
public class AlarmValueSettingDto {

    /**
     * 垛内氧气
     */
    private Double stackOxygenUpperOne;
    private Double stackOxygenUpperTwo;
    private Double stackOxygenUpperThree;
    private Double stackOxygenLower;

    /**
     * 仓间氧气
     */
    private Double warehouseOxygenUpperOne;
    private Double warehouseOxygenUpperTwo;
    private Double warehouseOxygenUpperThree;
    private Double warehouseOxygenLower;

    /**
     * 包芯温度
     */
    private Double tempCoreChipUpperOne;
    private Double tempCoreChipUpperTwo;
    private Double tempCoreChipUpperThree;
    private Double tempCoreChipLower;

    /**
     * 垛内温度
     */
    private Double stackTempUpperOne;
    private Double stackTempUpperTwo;
    private Double stackTempUpperThree;
    private Double stackTempLower;

    /**
     * 垛内水分
     */
    private Double stackMoistureUpperOne;
    private Double stackMoistureUpperTwo;
    private Double stackMoistureUpperThree;
    private Double stackMoistureLower;

    /**
     * 仓间温度
     */
    private Double warehouseTempUpper0ne;
    private Double warehouseTempUpperTwo;
    private Double warehouseTempUpperThree;
    private Double warehouseTempLower;

    /**
     * 垛内水分
     */
    private Double warehouseMoistureUpperOne;
    private Double warehouseMoistureUpperTwo;
    private Double warehouseMoistureUpperThree;
    private Double warehouseMoistureLower;

    /**
     * 平均包芯高于环境
     */
    private Double coreTempHighEnvTempOne;
    private Double coreTempHighEnvTempTwo;
    private Double coreTempHighEnvTempThree;

    /**
     * 预警颜色
     */
    private String colorOne;
    private String colorTwo;
    private String colorThree;

    /**
     * 预警升级天数
     */
    private String oneToTwoDay;
    private String twoToThreeDay;

    /**
     * 预警规则名称
     */
    private String earlyWarningRuleName;

    private String phases;
    private String maintenceStyle;
    private String tobaccoYear;
    private String materialCode;
    private String materialBatchNum;

    /**
     * 制定时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdTime;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedTime;
    private String createUser;
    private String updateUser;

    /**
     * 物料代码与烟叶年份
     */
    private String materialCodeYear;

    private Integer alarmValueSettingId;
}
