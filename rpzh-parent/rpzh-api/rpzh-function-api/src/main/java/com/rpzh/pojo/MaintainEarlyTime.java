package com.rpzh.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 2022.09.22
 * liu
 * 养护预警时间实体类
 */
@Data
public class MaintainEarlyTime {
    /**
     * 主键id
     */
    private Integer id;
    /**
     * 养护计划id
     */
    private Integer maintainPlanId;
    /**
     * 包芯温度连续预警时间
     */
    private Double coreTempMaxEarlyTime;
    /**
     * 包芯温度累计预警时间
     */
    private Double coreTempAllEarlyTime;
    /**
     * 垛内氧气连续预警时间
     */
    private Double oxygenMaxEarlyTime;
    /**
     * 垛内氧气累计预警时间
     */
    private Double oxygenAllEarlyTime;
    /**
     * 垛内温度连续预警时间
     */
    private Double tempMaxEarlyTime;
    /**
     * 垛内温度累计预警时间
     */
    private Double tempAllEarlyTime;
    /**
     * 垛内湿度连续预警时间
     */
    private Double humidityMaxEarlyTime;
    /**
     * 垛内湿度累计预警时间
     */
    private Double humidityAllEarlyTime;
    /**
     * 创建时间
     */
    private Date createdTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 养护结果
     */
    private Integer maintainResult;
}
