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
    private Integer coreTempMaxEarlyTime;
    /**
     * 包芯温度累计预警时间
     */
    private Integer coreTempAllEarlyTime;
    /**
     * 垛内氧气连续预警时间
     */
    private Integer oxygenMaxEarlyTime;
    /**
     * 垛内氧气累计预警时间
     */
    private Integer oxygenAllEarlyTime;
    /**
     * 垛内温度连续预警时间
     */
    private Integer tempMaxEarlyTime;
    /**
     * 垛内温度累计预警时间
     */
    private Integer tempAllEarlyTime;
    /**
     * 垛内湿度连续预警时间
     */
    private Integer humidityMaxEarlyTime;
    /**
     * 垛内湿度累计预警时间
     */
    private Integer humidityAllEarlyTime;
    /**
     * 创建时间
     */
    private Date createdTime;
    /**
     * 更新时间
     */
    private Date updateTime;
}
