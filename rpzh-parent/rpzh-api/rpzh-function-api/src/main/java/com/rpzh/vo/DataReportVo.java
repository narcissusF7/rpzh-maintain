package com.rpzh.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DataReportVo {
    /**
     * 平均数据
     */
    private BigDecimal avgData;
    /**
     * 包芯温度数据
     */
    private BigDecimal coreTempData;
    /**
     * 包芯温度数据对比1
     */
    private BigDecimal coreTempDataOne = new BigDecimal("0");
    /**
     * 包芯温度数据对比2
     */
    private BigDecimal coreTempDataTwo = new BigDecimal("0");
    /**
     * 创建时间
     */
    private String createTime = "";
    /**
     * 差值
     */
    private BigDecimal differenceValue;
    /**
     * 采集位置
     */
    private String gatherSite;
    /**
     * 对比采集位置1
     */
    private String gatherSiteOne = "";
    /**
     * 对比采集位置2
     */
    private String gatherSiteTwo = "";
    /**
     * 湿度数据
     */
    private BigDecimal humitureData;
    /**
     * 湿度数据对比1
     */
    private BigDecimal humitureDataOne = new BigDecimal("0");
    /**
     * 湿度数据对比2
     */
    private BigDecimal humitureDataTwo = new BigDecimal("0");
    /**
     * id
     */
    private Integer id;
    /**
     * 最大数据
     */
    private BigDecimal maxData;
    /**
     * 最小数据
     */
    private BigDecimal minData;
    /**
     * 当前数据
     */
    private BigDecimal nowData;
    /**
     * 设备Id
     */
    private String nodeId;
    /**
     * 对比设备Id1
     */
    private String nodeIdOne = "";
    /**
     * 对比设备Id2
     */
    private String nodeIdTwo = "";
    /**
     * 氧气数据
     */
    private BigDecimal o2Data;
    /**
     * 温度数据
     */
    private BigDecimal tempData;
    /**
     * 温度数据对比1
     */
    private BigDecimal tempDataOne = new BigDecimal("0");
    /**
     * 温度数据对比2
     */
    private BigDecimal tempDataTwo = new BigDecimal("0");
    /**
     * 氧气数据对比1
     */
    private BigDecimal o2DataOne = new BigDecimal("0");
    /**
     * 氧气数据对比2
     */
    private BigDecimal o2DataTwo = new BigDecimal("0");
    /**
     * 位置路由
     */
    private String pidRoute;
}
