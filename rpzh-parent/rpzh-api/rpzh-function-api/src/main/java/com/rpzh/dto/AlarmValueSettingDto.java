package com.rpzh.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author zyq
 * 预警规则前端传参
 */
@Data
public class AlarmValueSettingDto {

    //垛内氧气上限一级
    private Double stackOxygenUpperOne=0d;
    //垛内氧气上限二级
    private Double stackOxygenUpperTwo=0d;
    //垛内氧气上限三级
    private Double stackOxygenUpperThree=0d;
    //垛内氧气下限
    private Double stackOxygenLower=0d;

    //仓间氧气上限一级
    private Double warehouseOxygenUpperOne=0d;
    //仓间氧气上限二级
    private Double warehouseOxygenUpperTwo=0d;
    //仓间氧气上限三级
    private Double warehouseOxygenUpperThree=0d;
    //仓间氧气下限
    private Double warehouseOxygenLower=0d;

    //包芯温度上限一级
    private Double tempCoreChipUpperOne=0d;
    //包芯温度上限二级
    private Double tempCoreChipUpperTwo=0d;
    //包芯温度上限三级
    private Double tempCoreChipUpperThree=0d;
    //包芯温度下限
    private Double tempCoreChipLower=0d;

    //垛内温度上限一级
    private Double stackTempUpperOne=0d;
    //垛内温度上限二级
    private Double stackTempUpperTwo=0d;
    //垛内温度上限三级
    private Double stackTempUpperThree=0d;
    //垛内温度下限
    private Double stackTempLower=0d;

    //垛内水分上限一级
    private Double stackMoistureUpperOne=0d;
    //垛内水分上限二级
    private Double stackMoistureUpperTwo=0d;
    //垛内水分上限三级
    private Double stackMoistureUpperThree=0d;
    //垛内水分下限
    private Double stackMoistureLower=0d;

    //仓间温度上限一级
    private Double warehouseTempUpperOne=0d;
    //仓间温度上限二级
    private Double warehouseTempUpperTwo=0d;
    //仓间温度上限三级
    private Double warehouseTempUpperThree=0d;
    //仓间温度下限
    private Double warehouseTempLower=0d;

    //仓间水分上限一级
    private Double warehouseMoistureUpperOne=0d;
    //仓间水分上限二级
    private Double warehouseMoistureUpperTwo=0d;
    //仓间水分上限三级
    private Double warehouseMoistureUpperThree=0d;
    //仓间水分下限
    private Double warehouseMoistureLower=0d;

    //平均包芯高于环境上限一级
    private Double coreTempHighEnvTempOne=0d;
    //平均包芯高于环境上限二级
    private Double coreTempHighEnvTempTwo=0d;
    //平均包芯高于环境上限三级
    private Double coreTempHighEnvTempThree=0d;
    //平均包芯高于环境下限
    private Double coreTempLowEnvTemp=0d;

    //位置类型 1:仓库, 2: 楼号, 3: 层, 4: 区, 5: 垛位
    private String siteType;
    //位置id
    private Integer stackId;

    //预警规则名称
    private String earlyWarningRuleName;
    //养护阶段
    private String phases;
    //养护方式
    private String maintenceStyle;
    //物料代码
    private String materialCode;
    //物料代码集合
    private List<String> materialCodes;
    //位置路由
    private String pidRoute;
    //制定时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdTime;
    //更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedTime;
    //创建人
    private String createUser;
    //更新人
    private String updateUser;
    //预警规则表id
    private Integer alarmValueSettingId;

    private Integer size=10;
    private Integer page=1;
    //开始时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    //结束时间
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
