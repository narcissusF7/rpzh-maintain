package com.rpzh.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author zyq
 * 预警信息设置
 */
@Data
public class AlarmValueSettingDto {

    /**
     * 垛内氧气
     */
    private Double stackOxygenUpperOne=0d;
    private Double stackOxygenUpperTwo=0d;
    private Double stackOxygenUpperThree=0d;
    private Double stackOxygenLower=0d;

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
    private Double tempCoreChipUpperOne=0d;
    private Double tempCoreChipUpperTwo=0d;
    private Double tempCoreChipUpperThree=0d;
    private Double tempCoreChipLower=0d;

    /**
     * 垛内温度
     */
    private Double stackTempUpperOne=0d;
    private Double stackTempUpperTwo=0d;
    private Double stackTempUpperThree=0d;
    private Double stackTempLower=0d;

    /**
     * 垛内水分
     */
    private Double stackMoistureUpperOne=0d;
    private Double stackMoistureUpperTwo=0d;
    private Double stackMoistureUpperThree=0d;
    private Double stackMoistureLower=0d;

    /**
     * 仓间温度
     */
    private Double warehouseTempUpperOne;
    private Double warehouseTempUpperTwo;
    private Double warehouseTempUpperThree;
    private Double warehouseTempLower;

    /**
     * 仓间水分
     */
    private Double warehouseMoistureUpperOne=0d;
    private Double warehouseMoistureUpperTwo=0d;
    private Double warehouseMoistureUpperThree=0d;
    private Double warehouseMoistureLower=0d;

    /**
     * 平均包芯高于环境
     */
    private Double coreTempHighEnvTempOne=0d;
    private Double coreTempHighEnvTempTwo=0d;
    private Double coreTempHighEnvTempThree=0d;
    private Double coreTempLowEnvTemp=0d;

    private String siteType;
    private Integer level;
    private Integer stackId;

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
    private List<String> materialCodes;
    private String pidRoute;
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

    private Integer alarmValueSettingId;

    private Integer size=10;
    private Integer page=1;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    public AlarmValueSettingDto(){}
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
      this.warehouseTempUpperOne=maintainPlanDto.getWarehouseTempUpper0ne();
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
