package com.rpzh.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rpzh.vo.AlarmValueSettingVo;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @author wbl
 * @description [养护计划前端传入类]
 */
@Data

public class MaintainPlanDto {
    /**
     * 物料代码
     */
    private List<String> materialCodes;
    private String materialCode;
    private Integer stageId;
    private String pidRoute;
    /**
     * 垛位id
     */
    private Integer stackId;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 养护方式 (1:常规养护、2:机械调控)
     */
    private String maintenceStyle;
    /**
     * 阶段 （1:养护阶段 2:充氮杀虫阶段 3:磷化铝杀虫阶段 4:充氮保质阶段）
     */
    private String phases;

    /**
     * 养护计划id
     */
    private Integer id;

    private String stackName;
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
     * 养护天数
     */
    private Integer maintainDays;
    private Integer version = 1;
    private Integer storageAge;
    /**
     * 调养日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private String aftercareDateStart;
    @JsonFormat(pattern = "yyyy-MM-dd 00:00:00", timezone = "GMT+8")
    private Date aftercareDateEnd;
    /**
     * 降氧完成时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private String oxygenReductionTime;
    /**
     * 负责人
     */
    private List<Integer> principalIds;
    private String principalId;
    private String principalName;

    /**
     * 到期提醒时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private String expireTime;
    /**
     * 养护数量
     */
    private Double maintainCount;

    /**
     * 预警设置表id
     */
    private Integer alarmValueSettingId;
    /**
     * 放在养护阶段表中的养护计划id
     */
    private Integer maintainPlanId;
    private Date nowDate;

    /**
     * 第几页
     */
    private Integer page = 1;
    /**
     * 有几条数据
     */
    private Integer size = 10;

    private String siteName;

    private String changeStatus;

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
     * 预警规则名称
     */
    private String earlyWarningRuleName;
    /**
     * 该养护计划下的所有设备
     */
    List<String> nodeDtoList;
}
