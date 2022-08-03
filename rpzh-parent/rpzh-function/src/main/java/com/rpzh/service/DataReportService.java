package com.rpzh.service;

import com.rpzh.dto.DataReportDto;
import com.rpzh.entity.Result;
import com.rpzh.vo.DataReportVo;

/**
 * @author wbl
 * @version 1.0
 * @description 数据报表服务层
 * @createTime 2022-6-23 17:21
 */
public interface DataReportService {
    /**
     * 查询每个垛的设备信息
     *
     * @param dataReportDto 前端传入的参数
     * @return Data
     * @createTime 2022-6-23 17:24
     */
    Result<DataReportVo> stackComparison(DataReportDto dataReportDto);

    /**
     * @return 数据
     * @description 设备的详细数据
     * @createTime 2022-6-24 16:29
     */
    Result<DataReportVo> findDeviceParticularsInfo(DataReportDto dataReportDto);

    /**
     * @return 数据
     * @description 两个垛位之间的对比
     * @createTime 2022-6-24 16:29
     */
    Result<DataReportVo> findStackComparisonData(DataReportDto dataReportDto);
    /**
     * 查找仓间和垛位信息
     * @return
     */
    Result<DataReportVo> findStackAndWarehouseInfo(DataReportDto dataReportDto);
    /**
     * 查找包芯温度和垛内温度对比
     *
     * @param dataReportDto
     * @return
     */
    Result<DataReportVo> findStackAndCoreInfo(DataReportDto dataReportDto);
    /**
     * 查找包芯温度和垛内温度对比列表
     *
     * @param dataReportDto
     * @return
     */
    Result<DataReportVo> findStackAndCoreInfoList(DataReportDto dataReportDto);

    /**
     * 综合汇总报表
     * @param dataReportDto
     * @return
     */
    Result<DataReportVo> findSummaryReport(DataReportDto dataReportDto);
    /**
     * 综合汇总详情
     * @param dataReportDto
     * @return
     */
    Result<DataReportVo> findSummaryReportDetails(DataReportDto dataReportDto);

    Result exportComparisonData(String[] pidRoutes,String comparisonType ,String dateType,String acquisitionTimeOne,String acquisitionTimeTwo);
}
