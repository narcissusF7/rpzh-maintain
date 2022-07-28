package com.rpzh.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DataReportDto {
    /**
     * 对比的采集位置的id路线1 示例：[00:0],[01:1],[02:1],[03:1],[04:1],[05:1]
     */
    private String gatherSiteRouteComparisonOne;
    /**
     * 对比的采集位置的id路线2 示例：[00:0],[01:1],[02:1],[03:1],[04:1],[05:1]
     */
    private String gatherSiteRouteComparisonTwo;
    /**
     * 采集时间1
     */
    private String acquisitionTimeOne;
    /**
     * 采集时间2
     */
    private String acquisitionTimeTwo;
    /**
     * 采集频率 默认为2
     */
    private Integer samplingFrequency = 2;
    /**
     * 采集频率单位 默认为小时 h = 小时 , d = 日 , w = 周 , m = 月 , y = 年
     */
    private String samplingFrequencyUnit = "h";
    /**
     * 类型 (1 => 温度 | 2 => 湿度 | 3 => 氧气)
     */
    private Integer type;
    /**
     * 数据类型 (1 => 当前 | 2 => 最大 | 3 => 最小 | 4 = 平均)
     */
    private Integer dateType;
    /**
     * 位置类型id 示例：[00:0],[01:1],[02:1],[03:1],[04:1],[05:2]
     */
    private String gatherSiteRoute;
    /**
     * 数值范围1
     */
    private BigDecimal valOne;
    /**
     * 数值范围2
     */
    private BigDecimal valTwo;
    /**
     * 设备类型 1 = 温湿度 , 2 = 氧气
     */
    private Integer nodeType;
    /**
     * 起始页
     */
    private Integer page = 1;
    /**
     * 多少数据
     */
    private Integer size = 10;
    /**
     * 包芯温度1
     */
    private BigDecimal coreTempOne;
    /**
     * 包芯温度2
     */
    private BigDecimal coreTempTwo;
    /**
     * 温度1
     */
    private BigDecimal tempOne;
    /**
     * 温度2
     */
    private BigDecimal tempTwo;
    /**
     * 湿度1
     */
    private BigDecimal humitureOne;
    /**
     * 湿度2
     */
    private BigDecimal humitureTwo;
    /**
     * 氧气1
     */
    private BigDecimal o2One;
    /**
     * 氧气2
     */
    private BigDecimal o2Two;
    /**
     * 仓库id
     */
    private String warehouseId;
    /**
     * 是否分页
     */
    private String paging;

    /**
     * 导出类型  1 日统计 2 周统计
     */
    private Integer exportType;

}
