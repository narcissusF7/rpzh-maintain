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
    private Double stackOxygenUpperOne;
    //垛内氧气上限二级
    private Double stackOxygenUpperTwo;
    //垛内氧气上限三级
    private Double stackOxygenUpperThree;
    //垛内氧气下限
    private Double stackOxygenLower;

    //仓间氧气上限一级
    private Double warehouseOxygenUpperOne;
    //仓间氧气上限二级
    private Double warehouseOxygenUpperTwo;
    //仓间氧气上限三级
    private Double warehouseOxygenUpperThree;
    //仓间氧气下限
    private Double warehouseOxygenLower;
    //包芯温度上限一级
    private Double tempCoreChipUpperOne;
    //包芯温度上限二级
    private Double tempCoreChipUpperTwo;
    //包芯温度上限三级
    private Double tempCoreChipUpperThree;
    //包芯温度下限
    private Double tempCoreChipLower;

    //垛内温度上限一级
    private Double stackTempUpperOne;
    //垛内温度上限二级
    private Double stackTempUpperTwo;
    //垛内温度上限三级
    private Double stackTempUpperThree;
    //垛内温度下限
    private Double stackTempLower;

    //垛内水分上限一级
    private Double stackMoistureUpperOne;
    //垛内水分上限二级
    private Double stackMoistureUpperTwo;
    //垛内水分上限三级
    private Double stackMoistureUpperThree;
    //垛内水分下限
    private Double stackMoistureLower;

    //仓间温度上限一级
    private Double warehouseTempUpperOne;
    //仓间温度上限二级
    private Double warehouseTempUpperTwo;
    //仓间温度上限三级
    private Double warehouseTempUpperThree;
    //仓间温度下限
    private Double warehouseTempLower;

    //仓间水分上限一级
    private Double warehouseMoistureUpperOne;
    //仓间水分上限二级
    private Double warehouseMoistureUpperTwo;
    //仓间水分上限三级
    private Double warehouseMoistureUpperThree;
    //仓间水分下限
    private Double warehouseMoistureLower;

    //平均包芯高于环境上限一级
    private Double coreTempHighEnvTempOne;
    //平均包芯高于环境上限二级
    private Double coreTempHighEnvTempTwo;
    //平均包芯高于环境上限三级
    private Double coreTempHighEnvTempThree;
    //平均包芯高于环境下限
    private Double coreTempLowEnvTemp;

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
    private Integer flag=1;
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
      this.updateUser = maintainPlanDto.getUpdateUser();
      this.createUser = maintainPlanDto.getCreateUser();
      this.alarmValueSettingId = maintainPlanDto.getAlarmValueSettingId();
    }
    public AlarmValueSettingDto(String earlyWarningRuleName,String maintenceStyle,String createUser,Integer flag){
        this.earlyWarningRuleName=earlyWarningRuleName;
        this.maintenceStyle=maintenceStyle;
        this.createUser = createUser;
        this.flag = flag;
    }
    public AlarmValueSettingDto(Double stackOxygenUpperOne,Double stackOxygenUpperTwo,Double stackOxygenUpperThree,Double stackOxygenLower,
                                Double stackMoistureUpperOne,Double stackMoistureUpperTwo,Double stackMoistureUpperThree,Double stackMoistureLower,
                                Double tempCoreChipUpperOne,Double tempCoreChipUpperTwo,Double tempCoreChipUpperThree,Double tempCoreChipLower,
                                Double stackTempUpperOne,Double stackTempUpperTwo,Double stackTempUpperThree,Double stackTempLower,
                                Double coreTempHighEnvTempOne,Double coreTempHighEnvTempTwo,Double coreTempHighEnvTempThree,Double coreTempLowEnvTemp, String phases,String updateUser,Integer alarmValueSettingId){
        this.stackOxygenUpperOne =stackOxygenUpperOne;
        this.stackOxygenUpperTwo =stackOxygenUpperTwo;
        this.stackOxygenUpperThree =stackOxygenUpperThree;
        this.stackOxygenLower =stackOxygenLower;
        this.tempCoreChipUpperOne =tempCoreChipUpperOne;
        this.tempCoreChipUpperTwo =tempCoreChipUpperTwo;
        this.tempCoreChipUpperThree=tempCoreChipUpperThree;
        this.tempCoreChipLower=tempCoreChipLower;
        this.stackTempUpperOne=stackTempUpperOne;
        this.stackTempUpperTwo=stackTempUpperTwo;
        this.stackTempUpperThree=stackTempUpperThree;
        this.stackTempLower=stackTempLower;
        this.stackMoistureUpperOne=stackMoistureUpperOne;
        this.stackMoistureUpperTwo=stackMoistureUpperTwo;
        this.stackMoistureUpperThree=stackMoistureUpperThree;
        this.stackMoistureLower=stackMoistureLower;
        this.coreTempHighEnvTempOne=coreTempHighEnvTempOne;
        this.coreTempHighEnvTempTwo=coreTempHighEnvTempTwo;
        this.coreTempHighEnvTempThree=coreTempHighEnvTempThree;
        this.coreTempLowEnvTemp = coreTempLowEnvTemp;
        this.phases=phases;
        this.updateUser = updateUser;
        this.alarmValueSettingId = alarmValueSettingId;
    }
}
