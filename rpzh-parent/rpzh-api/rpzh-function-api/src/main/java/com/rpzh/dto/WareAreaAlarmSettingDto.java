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

    /**
     * 仓间氧气
     */
    private Double warehouseOxygenUpperOne;
    private Double warehouseOxygenUpperTwo;
    private Double warehouseOxygenUpperThree;
    private Double warehouseOxygenLower;

    /**
     * 仓间温度
     */
    private Double warehouseTempUpperOne;
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

    private String siteType;
    private Integer level;
    private Integer stackId;
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

    private Integer id;
    private String siteName;

    private Integer size=10;
    private Integer page=1;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String endTime;

}
