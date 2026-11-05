package com.rpzh.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rpzh.util.SiteRouteUtil;
import com.rpzh.vo.AlarmValueSettingVo;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @author wbl
 * @description 养护计划前端传参
 */
@Data

public class MaintainPlanDto {
    //物料代码集合
    private List<String> materialCodes;
    //物料代码
    private String materialCode;
    //养护阶段id
    private Integer stageId;
    //位置路由
    private String pidRoute;
    //垛位id
    private Integer stackId;
    /** 列表查询：传 "1" 表示只查已结束（已完成养护）的计划；不传则维持原口径只查进行中 */
    private String isEnd;
    //仓间id
    private Integer areaId;
    //楼层id
    private Integer floorId;
    //楼栋id
    private Integer buildingId;
    //仓库id
    private Integer warehouseId;
    //开始时间
    private String startTime;
    //结束时间
    private String endTime;
    //养护方式
    private String maintenceStyle;
    //养护阶段
    private String phases;
    //养护计划id
    private Integer id;
    //垛位名称
    private String stackName;
    private String principalId;
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
    //养护天数
    private Integer maintainDays;
    //是否是新建养护计划：1-是；0-否
    private Integer version = 1;
    //库龄
    private Integer storageAge;
    //调养日期
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private String aftercareDateStart;
    //转段日期
    @JsonFormat(pattern = "yyyy-MM-dd 00:00:00", timezone = "GMT+8")
    private Date aftercareDateEnd;
    //降氧完成时间
    private String oxygenReductionTime;
    //负责人id集合
    private List<Integer> principalIds;
    //负责人
    private String principalName;
    //到期提醒时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private String expireTime;
    //养护数量
    private Double maintainCount=0d;
    //预警设置表id
    private Integer alarmValueSettingId;
    //放在养护阶段表中的养护计划id
    private Integer maintainPlanId;
    private Integer page = 1;
    private Integer size = 10;
    //位置名称
    private String siteName;
    //垛内氧气上限一级
    private Double stackOxygenUpperOne=0d;
    //垛内氧气上限二级
    private Double stackOxygenUpperTwo=0d;
    //垛内氧气上限三级
    private Double stackOxygenUpperThree=0d;
    //垛内氧气下限
    private Double stackOxygenLower=0d;
    //仓间氧气上限一级
    private Double warehouseOxygenUpperOne;
    //仓间氧气上限二级
    private Double warehouseOxygenUpperTwo;
    //仓间氧气上限三级
    private Double warehouseOxygenUpperThree;
    //仓间氧气下限
    private Double warehouseOxygenLower;
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
    private Double warehouseTempUpper0ne;
    //仓间温度上限二级
    private Double warehouseTempUpperTwo;
    //仓间温度上限三级
    private Double warehouseTempUpperThree;
    //仓间温度下限
    private Double warehouseTempLower;
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
    //预警规则名称
    private String earlyWarningRuleName;
    //该养护计划下的所有设备（前端以单个 JSON 数组字符串传入，避免 Spring 对 List<String> 的逗号拆分破坏 JSON）
    String nodeDtoList;

    public MaintainPlanDto(){}

    public MaintainPlanDto(String materialCode,Double maintainCount,Integer id,Integer stackId,String pidRoute,String maintenceStyle,String phases){
        this.materialCode = materialCode;//物料代码
        this.maintainCount = maintainCount;//养护数量
        this.maintainPlanId = id;//养护计划id
        this.stackId = stackId;//垛位id
        this.areaId = SiteRouteUtil.getSiteId(pidRoute, "04");//仓间id
        this.floorId = SiteRouteUtil.getSiteId(pidRoute, "03");//层id
        this.buildingId = SiteRouteUtil.getSiteId(pidRoute, "02");//楼栋id
        this.warehouseId = SiteRouteUtil.getSiteId(pidRoute, "01");//仓库id
        this.maintenceStyle = maintenceStyle;//养护方式
        this.phases = phases;//养护阶段
    }
}
