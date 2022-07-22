package com.rpzh.dao;

import cn.hutool.core.date.DateTime;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.rpzh.dto.DataReportDto;
import com.rpzh.vo.DataReportVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据报表dao层
 */
@Mapper
public interface DataReportDao {
    @DS("clickhouse")
    List<DataReportVo> findStackComparison(DataReportDto dataReportDto);

    /**
     * 查询温湿度设备详细数据
     *
     * @param dataReportDto
     * @return
     */
    @DS("clickhouse")
    List<DataReportVo> findEnvDeviceParticularsInfo(DataReportDto dataReportDto);

    /**
     * 查询氧气设备详细数据
     *
     * @param dataReportDto
     * @return
     */
    @DS("clickhouse")
    List<DataReportVo> findO2DeviceParticularsInfo(DataReportDto dataReportDto);

    @DS("clickhouse")
    List<DataReportVo> findEnvStackComparisonData(DataReportDto dataReportDto);

    @DS("clickhouse")
    int insertData(@Param("id") int id,
                   @Param("type") String type,
                   @Param("doubleValue") double doubleValue,
                   @Param("doubleValue1") double doubleValue1,
                   @Param("s") String s,
                   @Param("v") double v,
                   @Param("v1") double v1,
                   @Param("i") int i,
                   @Param("i1") int i1,
                   @Param("i2") int i2,
                   @Param("i3") int i3,
                   @Param("address") String address,
                   @Param("c") String c,
                   @Param("parse") DateTime parse,
                   @Param("i4") int i4,
                   @Param("s1") String s1,
                   @Param("parse1") DateTime parse1,
                   @Param("s2") String s2,
                   @Param("s3") String s3,
                   @Param("s4") String s4,
                   @Param("s5") String s5);

    @DS("clickhouse")
    List<DataReportVo> findStackComparisonByWarehouse(DataReportDto dataReportDto);
    @DS("clickhouse")
    List<DataReportVo> findStackComparisonByWarehouseNow(DataReportDto dataReportDto);

    @DS("clickhouse")
    List<DataReportVo> findStackAndCoreInfo(DataReportDto dataReportDto);

    @DS("clickhouse")
    List<DataReportVo> findStackAndCoreInfoList(DataReportDto dataReportDto);

    @DS("clickhouse")
    List<DataReportVo> findEnvSummaryReport(DataReportDto dataReportDto);
    @DS("clickhouse")
    List<DataReportVo> findO2SummaryReport(DataReportDto dataReportDto);
    @DS("clickhouse")
    List<DataReportVo> findTempSummaryReport(DataReportDto dataReportDto);

    @DS("clickhouse")
    List<DataReportVo> findO2StackComparisonData(DataReportDto dataReportDto);
    @DS("clickhouse")
    List<DataReportVo> findStackComparisonNow(DataReportDto dataReportDto);

    @DS("clickhouse")
    List<DataReportVo> findEnvSummaryReportDetails(DataReportDto dataReportDto);
    @DS("clickhouse")
    List<DataReportVo> findO2SummaryReportDetails(DataReportDto dataReportDto);
    @DS("clickhouse")
    List<DataReportVo> findTempSummaryReportDetails(DataReportDto dataReportDto);
}
