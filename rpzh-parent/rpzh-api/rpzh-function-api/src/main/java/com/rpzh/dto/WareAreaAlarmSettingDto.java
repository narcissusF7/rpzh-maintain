package com.rpzh.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rpzh.dto.MaintainPlanDto;
import lombok.Data;

import java.util.Date;

/**
 * @author zyq
 * 预警信息设置
 */
@Data
public class WareAreaAlarmSettingDto {

    //仓间水分上限一级
    private Double warehouseMoistureUpperOne;
    //仓间水分上限二级
    private Double warehouseMoistureUpperTwo;
    //仓间水分上限三级
    private Double warehouseMoistureUpperThree;
    //仓间水分下限
    private Double warehouseMoistureLower;
    //仓间温度上限一级
    private Double warehouseTempUpperOne;
    //仓间温度上限二级
    private Double warehouseTempUpperTwo;
    //仓间温度上限三级
    private Double warehouseTempUpperThree;
    //仓间温度下限
    private Double warehouseTempLower;
    //仓间氧气上限一级
    private Double warehouseOxygenUpperOne;
    //仓间氧气上限二级
    private Double warehouseOxygenUpperTwo;
    //仓间氧气上限三级
    private Double warehouseOxygenUpperThree;
    //仓间氧气下限
    private Double warehouseOxygenLower;
    //位置类型 01:仓库, 02: 楼号, 03: 层, 04: 区, 05: 垛位
    private String siteType;
    //位置id
    private Integer stackId;
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
    //仓间数值设置表id
    private Integer id;
    //位置名称
    private String siteName;
    private Integer size=10;
    private Integer page=1;
    //开始时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String startTime;
    //结束时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String endTime;

    public WareAreaAlarmSettingDto(){}

    public WareAreaAlarmSettingDto(Double warehouseMoistureUpperOne,Double warehouseMoistureUpperTwo,Double warehouseMoistureUpperThree,Double warehouseMoistureLower,
                                   Double warehouseTempUpperOne,Double warehouseTempUpperTwo,Double warehouseTempUpperThree,Double warehouseTempLower,
                                   Double warehouseOxygenUpperOne,Double warehouseOxygenUpperTwo,Double warehouseOxygenUpperThree,Double warehouseOxygenLower,
                                   String siteType,Integer stackId,String createUser,String updateUser){
        this.warehouseMoistureUpperOne = warehouseMoistureUpperOne;
        this.warehouseMoistureUpperTwo = warehouseMoistureUpperTwo;
        this.warehouseMoistureUpperThree = warehouseMoistureUpperThree;
        this.warehouseMoistureLower = warehouseMoistureLower;
        this.warehouseTempUpperOne = warehouseTempUpperOne;
        this.warehouseTempUpperTwo = warehouseTempUpperTwo;
        this.warehouseTempUpperThree = warehouseTempUpperThree;
        this.warehouseTempLower = warehouseTempLower;
        this.warehouseOxygenUpperOne = warehouseOxygenUpperOne;
        this.warehouseOxygenUpperTwo = warehouseOxygenUpperTwo;
        this.warehouseOxygenUpperThree = warehouseOxygenUpperThree;
        this.warehouseOxygenLower = warehouseOxygenLower;
        this.siteType = siteType;
        this.stackId = stackId;
        this.pidRoute = "[00:0],[01:" + stackId + "]";
        this.createUser = createUser;
        this.updateUser = updateUser;
    }
}
