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
    private Double coreTempLowEnvTemp;

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
    private String materialCode;

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

    private Integer size=10;
    private Integer page=1;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    public AlarmValueSettingDto(MaintainPlanDto maintainPlanDto){
      this.stackOxygenUpperOne =maintainPlanDto.getStackOxygenUpperOne();
      this.stackOxygenUpperTwo =maintainPlanDto.getStackOxygenUpperTwo();
      this.stackOxygenUpperThree =maintainPlanDto.getStackOxygenUpperThree();
      this.stackOxygenLower =maintainPlanDto.getStackOxygenLower();
      this.warehouseOxygenUpperOne =maintainPlanDto.getWarehouseOxygenUpperOne();
      this.warehouseOxygenUpperTwo =maintainPlanDto.getWarehouseOxygenUpperTwo();
      this.warehouseOxygenUpperThree =maintainPlanDto.getWarehouseOxygenUpperThree();
      this.warehouseOxygenLower =maintainPlanDto.getWarehouseOxygenLower();
      this.tempCoreChipUpperOne =maintainPlanDto.getTempCoreChipUpperOne();
      this.tempCoreChipUpperTwo =maintainPlanDto.getTempCoreChipUpperTwo();
      this.tempCoreChipUpperThree=maintainPlanDto.getTempCoreChipUpperThree();
      this.tempCoreChipLower=maintainPlanDto.getTempCoreChipLower();
      this.stackTempUpperOne=maintainPlanDto.getStackTempUpperOne();
      this.stackTempUpperTwo=maintainPlanDto.getStackTempUpperTwo();
      this.stackTempUpperThree=maintainPlanDto.getStackTempUpperThree();
      this.stackTempLower=maintainPlanDto.getStackTempLower();
      this.stackMoistureUpperOne=maintainPlanDto.getStackMoistureUpperOne();
      this.stackMoistureUpperTwo=maintainPlanDto.getStackMoistureUpperTwo();
      this.stackMoistureUpperThree=maintainPlanDto.getStackMoistureUpperThree();
      this.stackMoistureLower=maintainPlanDto.getStackMoistureLower();
      this.warehouseTempUpper0ne=maintainPlanDto.getWarehouseTempUpper0ne();
      this.warehouseTempUpperTwo=maintainPlanDto.getWarehouseTempUpperTwo();
      this.warehouseTempUpperThree=maintainPlanDto.getWarehouseTempUpperThree();
      this.warehouseTempLower=maintainPlanDto.getWarehouseTempLower();
      this.warehouseMoistureUpperOne=maintainPlanDto.getWarehouseMoistureUpperOne();
      this.warehouseMoistureUpperTwo=maintainPlanDto.getWarehouseMoistureUpperTwo();
      this.warehouseMoistureUpperThree=maintainPlanDto.getWarehouseMoistureUpperThree();
      this.warehouseMoistureLower=maintainPlanDto.getWarehouseMoistureLower();
      this.coreTempHighEnvTempOne=maintainPlanDto.getCoreTempHighEnvTempOne();
      this.coreTempHighEnvTempTwo=maintainPlanDto.getCoreTempHighEnvTempTwo();
      this.coreTempHighEnvTempThree=maintainPlanDto.getCoreTempHighEnvTempThree();
      this.earlyWarningRuleName=maintainPlanDto.getEarlyWarningRuleName();
      this.maintenceStyle=maintainPlanDto.getMaintenceStyle();
      this.phases=maintainPlanDto.getPhases();
      this.materialCode=maintainPlanDto.getMaterialCode();
      this.alarmValueSettingId=maintainPlanDto.getAlarmValueSettingId();
      this.coreTempLowEnvTemp = maintainPlanDto.getCoreTempLowEnvTemp();
      this.updatedTime = maintainPlanDto.getUpdatedTime();
      this.updateUser = maintainPlanDto.getUpdateUser();
      this.createdTime = maintainPlanDto.getCreatedTime();
      this.createUser = maintainPlanDto.getCreateUser();
      this.alarmValueSettingId = maintainPlanDto.getAlarmValueSettingId();
    }
}
