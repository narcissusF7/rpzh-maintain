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
    private Integer maIntegerainPlanId;
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
    private Integer coreTempMaxEarlyTimeOne;
    /**
     * 包芯温度累计预警时间
     */
    @ApiModelProperty("包芯温度累计预警时间1")
    private Integer coreTempAllEarlyTimeOne;
    /**
     * 垛内氧气连续预警时间
     */
    @ApiModelProperty("垛内氧气连续预警时间1")
    private Integer oxygenMaxEarlyTimeOne;
    /**
     * 垛内氧气累计预警时间
     */
    @ApiModelProperty("垛内氧气累计预警时间1")
    private Integer oxygenAllEarlyTimeOne;
    /**
     * 垛内温度连续预警时间
     */
    @ApiModelProperty("垛内温度连续预警时间1")
    private Integer tempMaxEarlyTimeOne;
    /**
     * 垛内温度累计预警时间
     */
    @ApiModelProperty("垛内温度累计预警时间1")
    private Integer tempAllEarlyTimeOne;
    /**
     * 垛内湿度连续预警时间
     */
    @ApiModelProperty("垛内湿度连续预警时间1")
    private Integer humidityMaxEarlyTimeOne;
    /**
     * 垛内湿度累计预警时间
     */
    @ApiModelProperty("垛内湿度累计预警时间1")
    private Integer humidityAllEarlyTimeOne;
    /**
     * 包芯温度连续预警时间
     */
    @ApiModelProperty("包芯温度连续预警时间2")
    private Integer coreTempMaxEarlyTimeTwo;
    /**
     * 包芯温度累计预警时间
     */
    @ApiModelProperty("包芯温度累计预警时间2")
    private Integer coreTempAllEarlyTimeTwo;
    /**
     * 垛内氧气连续预警时间
     */
    @ApiModelProperty("垛内氧气连续预警时间2")
    private Integer oxygenMaxEarlyTimeTwo;
    /**
     * 垛内氧气累计预警时间
     */
    @ApiModelProperty("垛内氧气累计预警时间2")
    private Integer oxygenAllEarlyTimeTwo;
    /**
     * 垛内温度连续预警时间
     */
    @ApiModelProperty("垛内温度连续预警时间2")
    private Integer tempMaxEarlyTimeTwo;
    /**
     * 垛内温度累计预警时间
     */
    @ApiModelProperty("垛内温度累计预警时间2")
    private Integer tempAllEarlyTimeTwo;
    /**
     * 垛内湿度连续预警时间
     */
    @ApiModelProperty("垛内湿度连续预警时间2")
    private Integer humidityMaxEarlyTimeTwo;
    /**
     * 垛内湿度累计预警时间
     */
    @ApiModelProperty("垛内湿度累计预警时间2")
    private Integer humidityAllEarlyTimeTwo;
    /**
     * 养护结果
     */
    @ApiModelProperty("养护结果 1 合格 0 不合格")
    private Integer maintainResult =-1;


    private Integer page = 1;
    private Integer size = 10;
}
