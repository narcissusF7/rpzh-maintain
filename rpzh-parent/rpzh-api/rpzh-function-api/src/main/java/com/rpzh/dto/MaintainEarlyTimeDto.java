package com.rpzh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 2022/09/22
 *  ljj
 */
@Data
@ApiModel(value = "预警超出时间交互类")
public class MaintainEarlyTimeDto {

    /**
     * 养护计划id
     */
    @ApiModelProperty("养护计划id")
    private Integer maintainPlanId;
    /**
     * 位置路由
     */
    @ApiModelProperty("位置路由")
    private String siteRoute;
    /**
     * 位置id
     */
    @ApiModelProperty("位置id")
    private Integer siteId;
    /**
     * 包芯温度连续预警时间
     */
    @ApiModelProperty("包芯温度连续预警时间1")
    private Double coreTempMaxEarlyTimeOne;
    /**
     * 包芯温度累计预警时间
     */
    @ApiModelProperty("包芯温度累计预警时间1")
    private Double coreTempAllEarlyTimeOne;
    /**
     * 垛内氧气连续预警时间
     */
    @ApiModelProperty("垛内氧气连续预警时间1")
    private Double oxygenMaxEarlyTimeOne;
    /**
     * 垛内氧气累计预警时间
     */
    @ApiModelProperty("垛内氧气累计预警时间1")
    private Double oxygenAllEarlyTimeOne;
    /**
     * 垛内温度连续预警时间
     */
    @ApiModelProperty("垛内温度连续预警时间1")
    private Double tempMaxEarlyTimeOne;
    /**
     * 垛内温度累计预警时间
     */
    @ApiModelProperty("垛内温度累计预警时间1")
    private Double tempAllEarlyTimeOne;
    /**
     * 垛内湿度连续预警时间
     */
    @ApiModelProperty("垛内湿度连续预警时间1")
    private Double humidityMaxEarlyTimeOne;
    /**
     * 垛内湿度累计预警时间
     */
    @ApiModelProperty("垛内湿度累计预警时间1")
    private Double humidityAllEarlyTimeOne;
    /**
     * 包芯温度连续预警时间
     */
    @ApiModelProperty("包芯温度连续预警时间2")
    private Double coreTempMaxEarlyTimeTwo;
    /**
     * 包芯温度累计预警时间
     */
    @ApiModelProperty("包芯温度累计预警时间2")
    private Double coreTempAllEarlyTimeTwo;
    /**
     * 垛内氧气连续预警时间
     */
    @ApiModelProperty("垛内氧气连续预警时间2")
    private Double oxygenMaxEarlyTimeTwo;
    /**
     * 垛内氧气累计预警时间
     */
    @ApiModelProperty("垛内氧气累计预警时间2")
    private Double oxygenAllEarlyTimeTwo;
    /**
     * 垛内温度连续预警时间
     */
    @ApiModelProperty("垛内温度连续预警时间2")
    private Double tempMaxEarlyTimeTwo;
    /**
     * 垛内温度累计预警时间
     */
    @ApiModelProperty("垛内温度累计预警时间2")
    private Double tempAllEarlyTimeTwo;
    /**
     * 垛内湿度连续预警时间
     */
    @ApiModelProperty("垛内湿度连续预警时间2")
    private Double humidityMaxEarlyTimeTwo;
    /**
     * 垛内湿度累计预警时间
     */
    @ApiModelProperty("垛内湿度累计预警时间2")
    private Double humidityAllEarlyTimeTwo;
    /**
     * 养护结果
     */
    @ApiModelProperty("养护结果 1 合格 0 不合格")
    private int maintainResult;


    private int page = 1;
    private int size = 10;
}
