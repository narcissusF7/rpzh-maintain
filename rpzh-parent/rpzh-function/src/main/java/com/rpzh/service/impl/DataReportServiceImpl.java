package com.rpzh.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.DataReportDao;
import com.rpzh.dao.NodeDao;
import com.rpzh.dto.DataReportDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.NodeTypeEnum;
import com.rpzh.pojo.NodeExportData;
import com.rpzh.service.DataReportService;
import com.rpzh.util.EasyPoiUtil;
import com.rpzh.util.PageDtoUtil;
import com.rpzh.util.SiteRouteUtil;
import com.rpzh.vo.DataReportVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author wbl
 * @description 数据报表服务层实现类
 * @createTime 2022-6-23 17:22
 */
@Service("DataReportService")
public class DataReportServiceImpl implements DataReportService {

    @Resource
    private DataReportDao dataReportDao;

    @Resource
    NodeDao nodeDao;

    /**
     * 查询每个垛的设备信息
     *
     * @param dataReportDto 前端传入的参数
     * @return Data
     * @createTime 2022-6-23 17:24
     */
    @Override
    public Result<DataReportVo> stackComparison(DataReportDto dataReportDto) {
        PageHelper.startPage(dataReportDto.getPage(), dataReportDto.getSize());
        if (dataReportDto.getAcquisitionTimeOne() != null) {
            dataReportDto.setAcquisitionTimeOne(DateUtil.beginOfDay(DateUtil.parse(dataReportDto.getAcquisitionTimeOne())).toString());
        }
        if (dataReportDto.getAcquisitionTimeTwo() != null) {
            dataReportDto.setAcquisitionTimeTwo(DateUtil.endOfDay(DateUtil.parse(dataReportDto.getAcquisitionTimeTwo())).toString());
        }
        dataReportDto.setGatherSiteRoute(SiteRouteUtil.route(dataReportDto.getGatherSiteRoute(), dataReportDto.getWarehouseId()));
        List<DataReportVo> dataReportVos = dataReportDao.findStackComparison(dataReportDto);
        List<DataReportVo> dataReportNowVos = dataReportDao.findStackComparisonNow(dataReportDto);
        Iterator<DataReportVo> iterator = dataReportNowVos.iterator();
        for (DataReportVo dataReportVo : dataReportVos) {
            while (iterator.hasNext()) {
                DataReportVo next = iterator.next();
                // 如果地址相同就赋值nowData数据
                if (next.getGatherSite().equals(dataReportVo.getGatherSite())) {
                    dataReportVo.setNowData(next.getNowData());
                    iterator.remove();
                    break;
                }
            }
        }
        return new Result<>(true, StatusCode.OK, "查询成功!", new PageInfo<>(dataReportVos));
    }

    /**
     * @param dataReportDto
     * @return 数据
     * @description 设备的详细数据
     * @createTime 2022-6-24 16:29
     */
    @Override
    public Result<DataReportVo> findDeviceParticularsInfo(DataReportDto dataReportDto) {
        PageHelper.startPage(dataReportDto.getPage(), dataReportDto.getSize());
        if (dataReportDto.getAcquisitionTimeOne() != null) {
            dataReportDto.setAcquisitionTimeOne(DateUtil.beginOfDay(DateUtil.parse(dataReportDto.getAcquisitionTimeOne())).toString());
        }
        if (dataReportDto.getAcquisitionTimeTwo() != null) {
            dataReportDto.setAcquisitionTimeTwo(DateUtil.endOfDay(DateUtil.parse(dataReportDto.getAcquisitionTimeTwo())).toString());
        }
        // 如果设备类型等于1 2表示为温湿度  3 表示为氧气
        if (dataReportDto.getNodeType() == null) {
            return new Result<>(false, StatusCode.ERROR, "未指定设备类型!", null);
        }
        List<DataReportVo> dataReportVos;
        if (dataReportDto.getNodeType().equals(1) || dataReportDto.getNodeType().equals(2)) {
            dataReportVos = dataReportDao.findEnvDeviceParticularsInfo(dataReportDto);
        } else if (dataReportDto.getNodeType().equals(3)) {
            dataReportVos = dataReportDao.findO2DeviceParticularsInfo(dataReportDto);
        } else {
            return new Result<>(false, StatusCode.ERROR, "未知的设备类型!", null);
        }
        return new Result<>(true, StatusCode.OK, "查询成功!", new PageInfo<>(dataReportVos));
    }

    /**
     * @param dataReportDto
     * @return 数据
     * @description 两个垛位之间的对比
     * @createTime 2022-6-24 16:29
     */
    @Override
    public Result<DataReportVo> findStackComparisonData(DataReportDto dataReportDto) {
        if (dataReportDto.getPaging() != null
                && !("y".equals(dataReportDto.getPaging())
                || "n".equals(dataReportDto.getPaging()) || "".equals(dataReportDto.getPaging()))) {
            return new Result<>(false, StatusCode.ERROR, "是否分页错误");
        }
        if (dataReportDto.getAcquisitionTimeOne() != null) {
            dataReportDto.setAcquisitionTimeOne(DateUtil.beginOfDay(DateUtil.parse(dataReportDto.getAcquisitionTimeOne())).toString());
        }
        if (dataReportDto.getAcquisitionTimeTwo() != null) {
            dataReportDto.setAcquisitionTimeTwo(DateUtil.endOfDay(DateUtil.parse(dataReportDto.getAcquisitionTimeTwo())).toString());
        }
//        // 如果类型等于1 表示为温湿度  2 表示为氧气
//        if (dataReportDto.getNodeType() == null || (dataReportDto.getNodeType() != null && dataReportDto.getNodeType() < 1 || dataReportDto.getNodeType() > 2)) {
//            return new Result<>(false, StatusCode.ERROR, "未指定设备类型!", null);
//        }
        // 默认为2小时的间隔
        if (dataReportDto.getSamplingFrequency() == null) {
            dataReportDto.setSamplingFrequency(2);
        }
        if (dataReportDto.getSamplingFrequencyUnit() == null) {
            dataReportDto.setSamplingFrequencyUnit("h");
        }
        // 查第一个地址的数据
        dataReportDto.setGatherSiteRoute(dataReportDto.getGatherSiteRouteComparisonOne());
        List<DataReportVo> dataReportVosOne = new ArrayList<>();
        List<DataReportVo> dataReportVosTwo = new ArrayList<>();
        if (dataReportDto.getType() == 1 || dataReportDto.getType() == 2) {
            dataReportVosOne = dataReportDao.findEnvStackComparisonData(dataReportDto);
            // 查第二个地址的数据
            dataReportDto.setGatherSiteRoute(dataReportDto.getGatherSiteRouteComparisonTwo());
            dataReportVosTwo = dataReportDao.findEnvStackComparisonData(dataReportDto);
        } else if (dataReportDto.getType() == 3) {
            dataReportVosOne = dataReportDao.findO2StackComparisonData(dataReportDto);
            // 查第二个地址的数据
            dataReportDto.setGatherSiteRoute(dataReportDto.getGatherSiteRouteComparisonTwo());
            dataReportVosTwo = dataReportDao.findO2StackComparisonData(dataReportDto);
        }
        // 如果第一个集合的长度小于第二个就补齐
        int abs = Math.abs(dataReportVosOne.size() - dataReportVosTwo.size());
        if (dataReportVosOne.size() < dataReportVosTwo.size()) {
            for (int i = 0; i < abs; i++) {
                dataReportVosOne.add(new DataReportVo());
            }
        } else if (dataReportVosOne.size() > dataReportVosTwo.size()) {
            for (int i = 0; i < abs; i++) {
                dataReportVosTwo.add(new DataReportVo());
            }
        }

        Iterator<DataReportVo> iterator = dataReportVosTwo.iterator();
        for (DataReportVo dataReportVo : dataReportVosOne) {
            dataReportVo.setTempDataTwo(new BigDecimal("0"));
            dataReportVo.setHumitureDataTwo(new BigDecimal("0"));
            dataReportVo.setO2DataTwo(new BigDecimal("0"));
            dataReportVo.setNodeIdTwo("");
            dataReportVo.setGatherSiteTwo("");
            //dataReportVo.
            while (iterator.hasNext()) {
                DataReportVo next = iterator.next();
                // 将两个时间一样的存到一个新的list中
                String createTimeOne = dataReportVo.getCreateTime();
                String createTimeTwo = next.getCreateTime();

                if (createTimeOne == null) {
                    dataReportVo.setCreateTime(createTimeTwo);
                    dataReportVo.setTempDataTwo(next.getTempDataOne());
                    dataReportVo.setHumitureDataTwo(next.getHumitureDataOne());
                    dataReportVo.setO2DataTwo(next.getO2DataOne());
                    dataReportVo.setNodeIdTwo(next.getNodeIdOne());
                    dataReportVo.setGatherSiteTwo(next.getGatherSiteOne());
                    break;
                }
                if (createTimeOne.equals(createTimeTwo)) {
                    dataReportVo.setTempDataTwo(next.getTempDataOne());
                    dataReportVo.setHumitureDataTwo(next.getHumitureDataOne());
                    dataReportVo.setO2DataTwo(next.getO2DataOne());
                    dataReportVo.setNodeIdTwo(next.getNodeIdOne());
                    dataReportVo.setGatherSiteTwo(next.getGatherSiteOne());
                    iterator.remove();
                    break;
                }
            }
        }

        if ("y".equals(dataReportDto.getPaging()) || "".equals(dataReportDto.getPaging())) {
            PageDtoUtil pageDto = PageDtoUtil.getPageDto(dataReportVosOne, dataReportDto.getPage(), dataReportDto.getSize());
            return new Result<>(true, StatusCode.OK, "查询成功", pageDto);
        }
        return new Result<>(true, StatusCode.OK, "查询成功", dataReportVosOne);
    }

    /**
     * 查找仓间和垛位信息
     *
     * @param dataReportDto
     * @return
     */
    @Override
    public Result<DataReportVo> findStackAndWarehouseInfo(DataReportDto dataReportDto) {
        PageHelper.startPage(dataReportDto.getPage(), dataReportDto.getSize());
        if (dataReportDto.getAcquisitionTimeOne() != null) {
            dataReportDto.setAcquisitionTimeOne(DateUtil.beginOfDay(DateUtil.parse(dataReportDto.getAcquisitionTimeOne())).toString());
        }
        if (dataReportDto.getAcquisitionTimeTwo() != null) {
            dataReportDto.setAcquisitionTimeTwo(DateUtil.endOfDay(DateUtil.parse(dataReportDto.getAcquisitionTimeTwo())).toString());
        }
        dataReportDto.setGatherSiteRoute(SiteRouteUtil.route(dataReportDto.getGatherSiteRoute(), dataReportDto.getWarehouseId()));
        List<DataReportVo> dataReportVos = dataReportDao.findStackComparisonByWarehouse(dataReportDto);
        List<DataReportVo> dataReportNowVos = dataReportDao.findStackComparisonByWarehouseNow(dataReportDto);
        Iterator<DataReportVo> iterator = dataReportNowVos.iterator();
        for (DataReportVo dataReportVo : dataReportVos) {
            for (DataReportVo dataReportNowVo : dataReportNowVos) {
                // 如果地址相同就赋值nowData数据
                if (dataReportNowVo.getGatherSite().equals(dataReportVo.getGatherSite())) {
                    dataReportVo.setNowData(dataReportNowVo.getNowData());
                    break;
                }
            }
        }
        // 判断当前值为null的删除
        dataReportVos.removeIf(next -> next.getNowData() == null);
        return new Result<>(true, StatusCode.OK, "查询成功!", new PageInfo<>(dataReportVos));
    }

    /**
     * 查找包芯温度和垛内温度对比
     *
     * @param dataReportDto
     * @return
     */
    @Override
    public Result<DataReportVo> findStackAndCoreInfo(DataReportDto dataReportDto) {
        PageHelper.startPage(dataReportDto.getPage(), dataReportDto.getSize());
        if (dataReportDto.getAcquisitionTimeOne() != null) {
            dataReportDto.setAcquisitionTimeOne(DateUtil.beginOfDay(DateUtil.parse(dataReportDto.getAcquisitionTimeOne())).toString());
        }
        if (dataReportDto.getAcquisitionTimeTwo() != null) {
            dataReportDto.setAcquisitionTimeTwo(DateUtil.endOfDay(DateUtil.parse(dataReportDto.getAcquisitionTimeTwo())).toString());
        }
        dataReportDto.setGatherSiteRoute(SiteRouteUtil.route(dataReportDto.getGatherSiteRoute(), dataReportDto.getWarehouseId()));
        List<DataReportVo> dataReportVos = dataReportDao.findStackAndCoreInfo(dataReportDto);
        return new Result<>(true, StatusCode.OK, "查询成功!", new PageInfo<>(dataReportVos));
    }

    /**
     * 查找包芯温度和垛内温度对比列表
     *
     * @param dataReportDto
     * @return
     */
    @Override
    public Result<DataReportVo> findStackAndCoreInfoList(DataReportDto dataReportDto) {
        if (dataReportDto.getAcquisitionTimeOne() != null) {
            dataReportDto.setAcquisitionTimeOne(DateUtil.beginOfDay(DateUtil.parse(dataReportDto.getAcquisitionTimeOne())).toString());
        }
        if (dataReportDto.getAcquisitionTimeTwo() != null) {
            dataReportDto.setAcquisitionTimeTwo(DateUtil.endOfDay(DateUtil.parse(dataReportDto.getAcquisitionTimeTwo())).toString());
        }
        // 查第一个地址的数据
        dataReportDto.setGatherSiteRoute(dataReportDto.getGatherSiteRouteComparisonOne());
        List<DataReportVo> dataReportVos = dataReportDao.findEnvStackComparisonData(dataReportDto);
        // 查找包芯温度数据
        // 查第二个地址的数据
        dataReportDto.setGatherSiteRoute(dataReportDto.getGatherSiteRouteComparisonTwo());
        List<DataReportVo> dataReportTempVos = dataReportDao.findStackAndCoreInfoList(dataReportDto);
        // 如果第一个集合的长度小于第二个就补齐
        int abs = Math.abs(dataReportVos.size() - dataReportTempVos.size());
        if (dataReportVos.size() < dataReportTempVos.size()) {
            for (int i = 0; i < abs; i++) {
                dataReportVos.add(new DataReportVo());
            }
        } else if (dataReportVos.size() > dataReportTempVos.size()) {
            for (int i = 0; i < abs; i++) {
                dataReportTempVos.add(new DataReportVo());
            }
        }
        Iterator<DataReportVo> iterator = dataReportTempVos.iterator();
        for (DataReportVo dataReportVo : dataReportVos) {
            dataReportVo.setNodeIdTwo("");
            dataReportVo.setGatherSiteTwo("");
            dataReportVo.setCoreTempDataTwo(new BigDecimal("0"));
            while (iterator.hasNext()) {
                DataReportVo next = iterator.next();
                // 将两个时间一样的存到一个新的list中
                String createTimeOne = dataReportVo.getCreateTime();
                String createTimeTwo = next.getCreateTime();
                if (createTimeOne == null) {
                    dataReportVo.setCreateTime(createTimeTwo);
                    dataReportVo.setCoreTempDataTwo(next.getCoreTempDataOne());
                    break;
                }
                if (createTimeOne.equals(createTimeTwo)) {
                    dataReportVo.setCoreTempDataTwo(next.getCoreTempDataOne());
                    iterator.remove();
                    break;
                }
            }
        }
        if ("y".equals(dataReportDto.getPaging()) || "".equals(dataReportDto.getPaging())) {
            PageDtoUtil pageDto = PageDtoUtil.getPageDto(dataReportVos, dataReportDto.getPage(), dataReportDto.getSize());
            return new Result<>(true, StatusCode.OK, "查询成功", pageDto);
        }
        return new Result<>(true, StatusCode.OK, "查询成功", new PageInfo<>(dataReportVos));
    }

    /**
     * 综合汇总报表
     *
     * @param dataReportDto
     * @return
     */
    @Override
    public Result<DataReportVo> findSummaryReport(DataReportDto dataReportDto) {
        PageHelper.startPage(dataReportDto.getPage(), dataReportDto.getSize());
        List<DataReportVo> listEnv = dataReportDao.findEnvSummaryReport(dataReportDto);
        List<DataReportVo> listO2 = dataReportDao.findO2SummaryReport(dataReportDto);
        List<DataReportVo> listTemp = dataReportDao.findTempSummaryReport(dataReportDto);
        boolean isPrment = dataReportDto.getCoreTempOne() != null || dataReportDto.getCoreTempTwo() != null
                || dataReportDto.getTempOne() != null || dataReportDto.getTempTwo() != null
                || dataReportDto.getHumitureOne() != null || dataReportDto.getHumitureTwo() != null
                || dataReportDto.getO2One() != null || dataReportDto.getO2Two() != null;
        dataReportDto.setGatherSiteRoute(SiteRouteUtil.route(dataReportDto.getGatherSiteRoute(), dataReportDto.getWarehouseId()));
        // 判断是存为0还是删除
        // 判断温湿度数据的长度是否大于氧气数据的长度
        if (listEnv.size() >= listO2.size()) {
            // 如果大于 就判断是否大于温度数据的长度
            // 如果大于就按照最大的来循环
            if (listEnv.size() >= listTemp.size()) {
                for (DataReportVo dataReportVo : listEnv) {
                    if (isPrment) {
                        dataReportVo.setO2Data(null);
                        dataReportVo.setCoreTempData(null);
                    } else {
                        dataReportVo.setO2Data(new BigDecimal("0"));
                        dataReportVo.setCoreTempData(new BigDecimal("0"));
                    }
                    for (DataReportVo reportVo : listO2) {
                        if (dataReportVo.getGatherSite().equals(reportVo.getGatherSite())) {
                            dataReportVo.setO2Data(reportVo.getO2Data());
                            break;
                        }
                    }
                    for (DataReportVo reportVo : listTemp) {
                        if (dataReportVo.getGatherSite().equals(reportVo.getGatherSite())) {
                            dataReportVo.setCoreTempData(reportVo.getCoreTempData());
                            break;
                        }
                    }
                }
                listEnv.removeIf(item -> item.getTempData() == null
                        || item.getHumitureData() == null
                        || item.getO2Data() == null
                        || item.getCoreTempData() == null);
                return new Result<>(true, StatusCode.OK, "查询成功", new PageInfo<>(listEnv));
            }
        } else {
            if (listO2.size() >= listTemp.size()) {
                for (DataReportVo dataReportVo : listO2) {
                    if (isPrment) {
                        dataReportVo.setTempData(null);
                        dataReportVo.setHumitureData(null);
                        dataReportVo.setCoreTempData(null);
                    } else {
                        dataReportVo.setTempData(new BigDecimal("0"));
                        dataReportVo.setHumitureData(new BigDecimal("0"));
                        dataReportVo.setCoreTempData(new BigDecimal("0"));
                    }
                    for (DataReportVo reportVo : listEnv) {
                        if (reportVo.getGatherSite().equals(dataReportVo.getGatherSite())) {
                            dataReportVo.setTempData(reportVo.getTempData());
                            dataReportVo.setHumitureData(reportVo.getHumitureData());
                            break;
                        }
                    }

                    for (DataReportVo reportVo : listTemp) {
                        if (dataReportVo.getGatherSite().equals(reportVo.getGatherSite())) {
                            dataReportVo.setCoreTempData(reportVo.getCoreTempData());
                            break;
                        }
                    }
                }
                listO2.removeIf(item -> item.getTempData() == null
                        || item.getHumitureData() == null
                        || item.getO2Data() == null
                        || item.getCoreTempData() == null);
                return new Result<>(true, StatusCode.OK, "查询成功", new PageInfo<>(listO2));
            }
        }

        for (DataReportVo dataReportVo : listTemp) {
            if (isPrment) {
                dataReportVo.setTempData(null);
                dataReportVo.setHumitureData(null);
                dataReportVo.setO2Data(null);
            } else {
                dataReportVo.setTempData(new BigDecimal("0"));
                dataReportVo.setHumitureData(new BigDecimal("0"));
                dataReportVo.setO2Data(new BigDecimal("0"));
            }
            for (DataReportVo reportVo : listEnv) {
                if (reportVo.getGatherSite().equals(dataReportVo.getGatherSite())) {
                    dataReportVo.setTempData(reportVo.getTempData());
                    dataReportVo.setHumitureData(reportVo.getHumitureData());
                    break;
                }
            }
            for (DataReportVo reportVo : listO2) {
                if (reportVo.getGatherSite().equals(dataReportVo.getGatherSite())) {
                    dataReportVo.setO2Data(reportVo.getO2Data());
                    break;
                }
            }
        }
        listTemp.removeIf(item -> item.getTempData() == null
                || item.getHumitureData() == null
                || item.getO2Data() == null
                || item.getCoreTempData() == null);
        return new Result<>(true, StatusCode.OK, "查询成功", new PageInfo<>(listTemp));
    }

    /**
     * 综合汇总详情
     *
     * @param dataReportDto
     * @return
     */
    @Override
    public Result<DataReportVo> findSummaryReportDetails(DataReportDto dataReportDto) {
        if (dataReportDto.getAcquisitionTimeOne() != null) {
            dataReportDto.setAcquisitionTimeOne(DateUtil.beginOfDay(DateUtil.parse(dataReportDto.getAcquisitionTimeOne())).toString());
        }
        if (dataReportDto.getAcquisitionTimeTwo() != null) {
            dataReportDto.setAcquisitionTimeTwo(DateUtil.endOfDay(DateUtil.parse(dataReportDto.getAcquisitionTimeTwo())).toString());
        }
        List<DataReportVo> listEnv = dataReportDao.findEnvSummaryReportDetails(dataReportDto);
        List<DataReportVo> listO2 = dataReportDao.findO2SummaryReportDetails(dataReportDto);
        List<DataReportVo> listTemp = dataReportDao.findTempSummaryReportDetails(dataReportDto);
        if (listEnv.size() >= listO2.size()) {
            // 如果大于 就判断是否大于温度数据的长度
            // 如果大于就按照最大的来循环
            if (listEnv.size() >= listTemp.size()) {
                for (DataReportVo dataReportVo : listEnv) {
                    dataReportVo.setO2Data(new BigDecimal("0"));
                    dataReportVo.setCoreTempData(new BigDecimal("0"));
                    for (DataReportVo reportVo : listO2) {
                        if (dataReportVo.getCreateTime().equals(reportVo.getCreateTime())) {
                            dataReportVo.setO2Data(reportVo.getO2Data());
                            break;
                        }
                    }
                    for (DataReportVo reportVo : listTemp) {
                        if (dataReportVo.getCreateTime().equals(reportVo.getCreateTime())) {
                            dataReportVo.setCoreTempData(reportVo.getCoreTempData());
                            break;
                        }
                    }
                }
                listEnv.removeIf(item -> item.getTempData() == null
                        || item.getHumitureData() == null
                        || item.getO2Data() == null
                        || item.getCoreTempData() == null);
                return new Result<>(true, StatusCode.OK, "查询成功", PageDtoUtil.getPageDto(listEnv, dataReportDto.getPage(), dataReportDto.getSize()));
            }
        } else {
            if (listO2.size() >= listTemp.size()) {
                for (DataReportVo dataReportVo : listO2) {
                    dataReportVo.setTempData(new BigDecimal("0"));
                    dataReportVo.setHumitureData(new BigDecimal("0"));
                    dataReportVo.setCoreTempData(new BigDecimal("0"));

                    for (DataReportVo reportVo : listEnv) {
                        if (reportVo.getCreateTime().equals(dataReportVo.getCreateTime())) {
                            dataReportVo.setTempData(reportVo.getTempData());
                            dataReportVo.setHumitureData(reportVo.getHumitureData());
                            break;
                        }
                    }

                    for (DataReportVo reportVo : listTemp) {
                        if (dataReportVo.getCreateTime().equals(reportVo.getCreateTime())) {
                            dataReportVo.setCoreTempData(reportVo.getCoreTempData());
                            break;
                        }
                    }
                }
                listO2.removeIf(item -> item.getTempData() == null
                        || item.getHumitureData() == null
                        || item.getO2Data() == null
                        || item.getCoreTempData() == null);
                return new Result<>(true, StatusCode.OK, "查询成功", PageDtoUtil.getPageDto(listO2, dataReportDto.getPage(), dataReportDto.getSize()));
            }
        }

        for (DataReportVo dataReportVo : listTemp) {

            dataReportVo.setTempData(new BigDecimal("0"));
            dataReportVo.setHumitureData(new BigDecimal("0"));
            dataReportVo.setO2Data(new BigDecimal("0"));

            for (DataReportVo reportVo : listEnv) {
                if (reportVo.getCreateTime().equals(dataReportVo.getCreateTime())) {
                    dataReportVo.setTempData(reportVo.getTempData());
                    dataReportVo.setHumitureData(reportVo.getHumitureData());
                    break;
                }
            }
            for (DataReportVo reportVo : listO2) {
                if (reportVo.getCreateTime().equals(dataReportVo.getCreateTime())) {
                    dataReportVo.setO2Data(reportVo.getO2Data());
                    break;
                }
            }
        }
        return new Result<>(true, StatusCode.OK, "查询成功!", PageDtoUtil.getPageDto(listTemp, dataReportDto.getPage(), dataReportDto.getSize()));
    }


    @Override
    public Result exportComparisonData(List<String> pidRoutes, String comparisonType , Integer exportType, String acquisitionTimeOne, String acquisitionTimeTwo, HttpServletResponse response) {
        //包芯温度数据
        List<Map> coreTimeData;
        //垛位温湿度数据
        List<Map> starckHumitureData;
        //垛位氧气数据
        List<Map> starckOxygenData;
        //垛位温湿度数据
        List<Map> wareHumitureData;
        //垛位氧气数据
        List<Map> wareOxygenData;
        String dateType ="day";
        if (exportType.equals(2)){
            dateType = "week";
        }
        //如果结束时间为空
        if (ToolUtil.isEmpty(acquisitionTimeTwo)||"undefined".equals(acquisitionTimeTwo)){
            SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            acquisitionTimeTwo = sm.format(new Date());
        }
        acquisitionTimeTwo = acquisitionTimeTwo.split(" ")[0]+" 23:59:59";
        //如果开始时间为空 默认结束时间前的一个月
        if (ToolUtil.isEmpty(acquisitionTimeOne)||"undefined".equals(acquisitionTimeOne)){
            SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date()); // 设置为当前时间
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1); // 设置为上一个月
            acquisitionTimeOne = sm.format(calendar.getTime());
        }
        long beginDate = localDateUtil(acquisitionTimeOne).toEpochDay();
        long endDate = localDateUtil(acquisitionTimeTwo).toEpochDay();

        List<NodeExportData> list = new ArrayList<>();
        EasyPoiUtil easyPoiUtil = new EasyPoiUtil();

        String title = "设备数据";

        switch (comparisonType){
            case "1":
                //垛内  包芯对比
                //包芯温度数据
                coreTimeData = nodeDao.findStatistics(pidRoutes,NodeTypeEnum.TEMPERATURE_CORE_CHIP_NODE.getTableName(),dateType,acquisitionTimeOne,acquisitionTimeTwo);

                //垛位温湿度数据
                starckHumitureData = nodeDao.findStatistics(pidRoutes,NodeTypeEnum.HUMITURE_NODE.getTableName(),dateType,acquisitionTimeOne,acquisitionTimeTwo);

                comparsionData(beginDate,endDate,list,coreTimeData,starckHumitureData,dateType);

                title ="垛内包芯温度与温湿度数据";
                break;

            case "2":
                //垛位氧气数据
                starckOxygenData = nodeDao.findStatistics(pidRoutes,NodeTypeEnum.OXYGEN_NODE.getTableName(),dateType,acquisitionTimeOne,acquisitionTimeTwo);

                //垛位温湿度数据
                starckHumitureData = nodeDao.findStatistics(pidRoutes,NodeTypeEnum.HUMITURE_NODE.getTableName(),dateType,acquisitionTimeOne,acquisitionTimeTwo);

                comparsionData(beginDate,endDate,list,starckOxygenData,starckHumitureData,dateType);

                title ="垛位氧气与温湿度数据";

                break;
            case "3":
                //仓间氧气数据
                wareOxygenData = nodeDao.findStatistics(pidRoutes,NodeTypeEnum.OXYGEN_NODE.getTableName(),dateType,acquisitionTimeOne,acquisitionTimeTwo);

                //仓间温湿度数据
                wareHumitureData = nodeDao.findStatistics(pidRoutes,NodeTypeEnum.HUMITURE_NODE.getTableName(),dateType,acquisitionTimeOne,acquisitionTimeTwo);

                comparsionData(beginDate,endDate,list,wareOxygenData,wareHumitureData,dateType);
                title = "仓间氧气与温湿度数据";
                break;

            default:
        }

        DecimalFormat df = new DecimalFormat("#.00");
        if (list.size()>0){
            for (int i = 0; i < list.size(); i++) {

                easyPoiUtil.t = list.get(i);
                switch (comparisonType){
                    case "1":
                        Double avgCoreTemp = list.get(i).getAvgCoreTemp();
                        Double avgTemp = list.get(i).getAvgTemp();
                        if (ToolUtil.isNotEmpty(avgCoreTemp)&&ToolUtil.isNotEmpty(avgTemp)){
                            String s = String.format("%.2f", list.get(i).getAvgCoreTemp() - list.get(i).getAvgTemp());
                            list.get(i).setTempReduce(Double.parseDouble(s));
                        }
                        try {
                            easyPoiUtil.hihdColumn("avgOxygen", true);
                            easyPoiUtil.hihdColumn("maxOxygen", true);
                            easyPoiUtil.hihdColumn("minOxygen", true);
                        } catch (Exception e) {
                            System.out.println("列隐藏转换失败：{}"+ e.getMessage());
                            e.printStackTrace();
                        }
                        break;
                    case "2":
                        try {
                            easyPoiUtil.hihdColumn("avgCoreTemp", true);
                            easyPoiUtil.hihdColumn("maxCoreTemp", true);
                            easyPoiUtil.hihdColumn("minCoreTemp", true);
                            easyPoiUtil.hihdColumn("tempReduce", true);
                        } catch (Exception e) {
                            System.out.println("列隐藏转换失败：{}"+ e.getMessage());
                            e.printStackTrace();
                        }
                        break;
                    case "3":
                        try {
                            easyPoiUtil.hihdColumn("avgCoreTemp", true);
                            easyPoiUtil.hihdColumn("maxCoreTemp", true);
                            easyPoiUtil.hihdColumn("minCoreTemp", true);
                            easyPoiUtil.hihdColumn("tempReduce", true);
                        } catch (Exception e) {
                            System.out.println("列隐藏转换失败：{}"+ e.getMessage());
                            e.printStackTrace();
                        }
                        break;
                }

            }
        }

        EasyPoiUtil.exportExcel(list,title,title+".xls",NodeExportData.class,title+".xls",response);
        return new Result(true,StatusCode.OK,"查询成功");
    }

    public LocalDate localDateUtil(String str){
        String[] s = (str.split(" ")[0]).split("-");
        return LocalDate.of(Integer.parseInt(s[0]),Integer.parseInt(s[1]),Integer.parseInt(s[2]));
    }

    public void comparsionData(long beginDate,long endDate ,List list ,List<Map> list1,List<Map> list2,String dateType){


        //key  时间  value 那个时间的map1里面数据的下标
        Map<String, List<Map>> dateTimeList1 = getDateTimeList(list1);
        Map<String, List<Map>> dateTimeList2 = getDateTimeList(list2);

        List<Map>  dataList  = new ArrayList<>();

        for (long i = beginDate; i <= endDate; i++) {
            //如果是按周统计 就从开始时间的周日算起
            if (dateType.equals("week")){
                i = LocalDate.ofEpochDay(i).with(DayOfWeek.SUNDAY).toEpochDay();
            }
            //获取这一天每个位置产生的数据
            List<Map> list3,list4;
            if (dateType.equals("week")){
                list3 = dateTimeList1.get(LocalDate.ofEpochDay(i).toString());

                list4 = dateTimeList2.get(LocalDate.ofEpochDay(i).toString());
            }else {
                list3 = dateTimeList1.get(LocalDate.ofEpochDay(i) + " 00:00:00");

                list4 = dateTimeList2.get(LocalDate.ofEpochDay(i) + " 00:00:00");
            }

            //如果当天没有数据
            if (ToolUtil.isEmpty(list3) && ToolUtil.isEmpty(list4)) {
                Map map = new HashMap<>();
                map.put("dateTime", LocalDate.ofEpochDay(i));
                dataList.add(map);
            }
            //同一时间不同位置
            Map<String, Map> map = new HashMap<>();
            if (ToolUtil.isNotEmpty(list3)) {
                for (int j = 0; j < list3.size(); j++) {
                    Map map1 = new HashMap<>();
                    map1.put("dateTime", i);
                    //数据
                    map1.putAll(list3.get(j));
                    map.put(map1.get("pidRoute").toString(), map1);
                }
            }
            if (ToolUtil.isNotEmpty(list4)) {
                for (int j = 0; j < list4.size(); j++) {

                    String pidRoute = list4.get(j).get("pidRoute").toString();
                    if (ToolUtil.isNotEmpty(map.get(pidRoute))) {
                        //上面有同样的位置路由  即同一天  同一个位置的数据存到一起
                        Map data = list4.get(j);
                        data.putAll(map.get(pidRoute));
                        dataList.add(data);
                        map.remove(pidRoute);
                    } else {
                        dataList.add(list4.get(j));
                    }
                }
            }

            //把剩下的都放到dataList
            if (map.size() > 0) {
                map.forEach((k, v) -> {
                    dataList.add(v);
                });
            }
            if (dateType.equals("week")){
                i=i+6;
            }
        }
        dataList.forEach(map1 -> map1.put("dateTime", map1.get("dateTime").toString().split(" ")[0]));
        dataList.forEach(map -> list.add(JSONObject.parseObject(JSONObject.toJSONString(map),NodeExportData.class)));



    }
    //key  时间  value 那个时间的map1里面数据的下标
    public Map<String,List<Map>> getDateTimeList(List<Map> list){
        Map<String,List<Map>> map = new HashMap<>();
        Set set1 = new HashSet();
        for (int i = 0; i < list.size(); i++) {
            //获取时间
            String dateTime = list.get(i).get("dateTime").toString();
            List<Map> list1;
            //如果set中存在重复元素
            if (set1.contains(dateTime)){
                list1 = map.get(dateTime);
            }else {
                list1 = new ArrayList<>();
            }
            set1.add(dateTime);
            list1.add(list.get(i));
            map.put(dateTime,list1);
        }
        return map;
    }

}
