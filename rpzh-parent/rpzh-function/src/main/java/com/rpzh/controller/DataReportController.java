package com.rpzh.controller;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.rpzh.dao.DataReportDao;
import com.rpzh.dto.DataReportDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.DataReportService;
import com.rpzh.vo.DataReportVo;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author wbl
 * @version 1.0
 * @createTime 2022-6-23 10:24
 * @description 数据报表模块
 */
@RestController
@CrossOrigin
@Api(value = "数据报表", tags = "数据报表")
@RequestMapping("/dataReport")
public class DataReportController {

    @Resource
    private DataReportService dataReportService;

    @Resource
    private DataReportDao dataReportDao;


    /**
     * @return 数据
     * @description 查看每个垛的数据
     * @createTime 2022-6-23 13:30
     */
    @ApiOperation(
            value = "查看每个垛的数据"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "gatherSiteRoute",
            value = "位置类型id 示例：[00:0],[01:1],[02:1],[03:1],[04:1],[05:2]",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "type",
            value = "类型 (1 => 温度 | 2 => 湿度 | 3 => 氧气)",
            dataType = "Integer",
            required = true
    ), @ApiImplicitParam(
            name = "dateType",
            value = "数据类型 (1 => 当前 | 2 => 最大 | 3 => 最小 | 4 = 平均)",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "valOne",
            value = "数值范围1",
            dataType = "BigDecimal"
    ), @ApiImplicitParam(
            name = "valTwo",
            value = "数值范围2",
            dataType = "BigDecimal"
    ), @ApiImplicitParam(
            name = "acquisitionTimeOne",
            value = "采集时间1",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "acquisitionTimeTwo",
            value = "采集时间2",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "warehouseId",
            value = "仓库id",
            dataType = "String"
    )})
    @PostMapping("/stackComparison")
    public Result<DataReportVo> findAllStackData(@ApiIgnore DataReportDto dataReportDto) {
        if (dataReportDto.getType() == null || (dataReportDto.getType() != null &&
                (dataReportDto.getType() < 1 || dataReportDto.getType() > 3))) {
            return new Result<>(false, StatusCode.ERROR, "未知类型!");
        }
        if (dataReportDto.getDateType() != null &&
                (dataReportDto.getDateType() < 1 || dataReportDto.getDateType() > 4)) {
            return new Result<>(false, StatusCode.ERROR, "未知数据类型!");
        }
        return dataReportService.stackComparison(dataReportDto);
    }

    /**
     * @return 数据
     * @description 垛位的详细数据
     * @createTime 2022-6-24 16:29
     */
    @ApiOperation(
            value = "垛位的详细数据"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "gatherSiteRoute",
            value = "位置类型id 示例：[00:0],[01:1],[02:1],[03:1],[04:1],[05:2]",
            dataType = "String",
            required = true
    ), @ApiImplicitParam(
            name = "nodeType",
            value = "设备类型  1 = 温湿度 , 3 = 氧气",
            dataType = "Integer",
            required = true
    ), @ApiImplicitParam(
            name = "acquisitionTimeOne",
            value = "采集时间1",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "acquisitionTimeTwo",
            value = "采集时间2",
            dataType = "String"
    )})
    @PostMapping("/findDeviceParticularsInfo")
    public Result<DataReportVo> findDeviceParticularsInfo(@ApiIgnore DataReportDto dataReportDto) {
        if (dataReportDto.getGatherSiteRoute() == null) {
            return new Result<>(false, StatusCode.ERROR, "地址为空!", null);
        }
        return dataReportService.findDeviceParticularsInfo(dataReportDto);
    }


    /**
     * @return 数据
     * @description 两个垛位之间的对比
     * @createTime 2022-6-24 16:29
     */
    @ApiOperation(
            value = "两个垛位之间的对比"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "gatherSiteRouteComparisonOne",
            value = "对比的采集位置的id路线1 示例：[00:0],[01:1],[02:1],[03:1],[04:1],[05:1]",
            dataType = "String",
            required = true
    ), @ApiImplicitParam(
            name = "gatherSiteRouteComparisonTwo",
            value = "对比的采集位置的id路线2 示例：[00:0],[01:1],[02:1],[03:1],[04:1],[05:2]",
            dataType = "String",
            required = true
    ), @ApiImplicitParam(
            name = "type",
            value = "类型 (1 => 温度 | 2 => 湿度 | 3 => 氧气)",
            dataType = "Integer",
            required = true
    ), @ApiImplicitParam(
            name = "acquisitionTimeOne",
            value = "采集时间1",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "acquisitionTimeTwo",
            value = "采集时间2",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "samplingFrequency",
            value = "采集频率 默认为2",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "samplingFrequencyUnit",
            value = "采集频率单位 默认为小时  h = 小时 , d = 日 , w = 周 , m = 月 , y = 年",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "paging",
            value = "是否分页 y = yes , n = no , 默认为y",
            dataType = "String"
    )})
    @PostMapping("/findStackComparisonData")
    public Result<DataReportVo> findStackComparisonData(@ApiIgnore DataReportDto dataReportDto) {
        if (dataReportDto.getType() == null || (dataReportDto.getType() != null &&
                (dataReportDto.getType() < 1 || dataReportDto.getType() > 3))) {
            return new Result<>(false, StatusCode.ERROR, "未知类型!");
        }
        return dataReportService.findStackComparisonData(dataReportDto);
    }


    /**
     * 查找仓间和垛位信息
     *
     * @return
     */
    @ApiOperation(
            value = "查找仓间和垛位的数据"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "gatherSiteRoute",
            value = "位置类型id 示例：[00:0],[01:1],[02:1],[03:1],[04:1],[05:2]",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "type",
            value = "类型 (1 => 温度 | 2 => 湿度 )",
            dataType = "Integer",
            required = true
    ), @ApiImplicitParam(
            name = "dateType",
            value = "数据类型 (1 => 当前 | 2 => 最大 | 3 => 最小 | 4 = 平均)",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "valOne",
            value = "数值范围1",
            dataType = "BigDecimal"
    ), @ApiImplicitParam(
            name = "valTwo",
            value = "数值范围2",
            dataType = "BigDecimal"
    ), @ApiImplicitParam(
            name = "acquisitionTimeOne",
            value = "采集时间1",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "acquisitionTimeTwo",
            value = "采集时间2",
            dataType = "String"
    )})
    @PostMapping("/findStackAndWarehouseInfo")
    public Result<DataReportVo> findStackAndWarehouseInfo(@ApiIgnore DataReportDto dataReportDto) {
        if (dataReportDto.getType() == null || (dataReportDto.getType() != null &&
                (dataReportDto.getType() < 1 || dataReportDto.getType() > 2))) {
            return new Result<>(false, StatusCode.ERROR, "未知类型!");
        }
        if (dataReportDto.getDateType() != null &&
                (dataReportDto.getDateType() < 1 || dataReportDto.getDateType() > 4)) {
            return new Result<>(false, StatusCode.ERROR, "未知数据类型!");
        }
        return dataReportService.findStackAndWarehouseInfo(dataReportDto);
    }

    /**
     * 查找包芯温度和垛内温度对比
     *
     * @param dataReportDto
     * @return
     */
    @ApiOperation(
            value = "查找包芯温度和垛内温度对比"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "gatherSiteRoute",
            value = "位置类型id 示例：[00:0],[01:1],[02:1],[03:1],[04:1],[05:2]",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "dateType",
            value = "数据类型 ( 5 = 垛内温度 | 6 = 平均包芯温度 | 7 = 差值)",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "valOne",
            value = "数值范围1",
            dataType = "BigDecimal"
    ), @ApiImplicitParam(
            name = "valTwo",
            value = "数值范围2",
            dataType = "BigDecimal"
    ), @ApiImplicitParam(
            name = "warehouseId",
            value = "仓库id",
            dataType = "String"
    )})
    @PostMapping("/findStackAndCoreInfo")
    public Result<DataReportVo> findStackAndCoreInfo(@ApiIgnore DataReportDto dataReportDto) {
        if (dataReportDto.getDateType() != null &&
                (dataReportDto.getDateType() < 5 || dataReportDto.getDateType() > 7)) {
            return new Result<>(false, StatusCode.ERROR, "未知数据类型!");
        }
        return dataReportService.findStackAndCoreInfo(dataReportDto);
    }

    /**
     * 查找包芯温度和垛内温度对比列表
     *
     * @param dataReportDto
     * @return
     */
    @ApiOperation(
            value = "查找包芯温度和垛内温度对比列表"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "gatherSiteRoute",
            value = "对比的采集位置的id路线 示例：[00:0],[01:1],[02:1],[03:1],[04:1],[05:1]",
            dataType = "String",
            required = true
    ), @ApiImplicitParam(
            name = "acquisitionTimeOne",
            value = "采集时间1",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "acquisitionTimeTwo",
            value = "采集时间2",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "samplingFrequency",
            value = "采集频率 默认为2",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "samplingFrequencyUnit",
            value = "采集频率单位 默认为小时  h = 小时 , d = 日 , w = 周 , m = 月 , y = 年",
            dataType = "String"
    )})
    @PostMapping("/findStackAndCoreInfoList")
    public Result<DataReportVo> findStackAndCoreInfoList(@ApiIgnore DataReportDto dataReportDto) {
        return dataReportService.findStackAndCoreInfoList(dataReportDto);
    }


    /**
     * 综合汇总报表
     *
     * @param dataReportDto
     * @return
     */
    @ApiOperation(
            value = "综合汇总报表"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "gatherSiteRoute",
            value = "位置类型id 示例：[00:0],[01:1],[02:1],[03:1],[04:1],[05:2]",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "coreTempOne",
            value = "包芯温度数值范围1",
            dataType = "BigDecimal"
    ), @ApiImplicitParam(
            name = "coreTempTwo",
            value = "包芯温度数值范围2",
            dataType = "BigDecimal"
    ), @ApiImplicitParam(
            name = "tempOne",
            value = "温度1",
            dataType = "BigDecimal"
    ), @ApiImplicitParam(
            name = "tempTwo",
            value = "温度2",
            dataType = "BigDecimal"
    ), @ApiImplicitParam(
            name = "humitureOne",
            value = "湿度1",
            dataType = "BigDecimal"
    ), @ApiImplicitParam(
            name = "humitureTwo",
            value = "湿度2",
            dataType = "BigDecimal"
    ), @ApiImplicitParam(
            name = "o2One",
            value = "氧气1",
            dataType = "BigDecimal"
    ), @ApiImplicitParam(
            name = "o2Two",
            value = "氧气2",
            dataType = "BigDecimal"
    )})
    @PostMapping("/findSummaryReport")
    public Result<DataReportVo> findSummaryReport(@ApiIgnore DataReportDto dataReportDto) {
        return dataReportService.findSummaryReport(dataReportDto);
    }

    /**
     * 综合汇报详情
     *
     * @param dataReportDto
     * @return
     */
    @ApiOperation(
            value = "综合汇报详情"
    )
    @ApiImplicitParams({@ApiImplicitParam(
            name = "gatherSiteRoute",
            value = "对比的采集位置的id路线 示例：[00:0],[01:1],[02:1],[03:1],[04:1],[05:1]",
            dataType = "String",
            required = true
    ), @ApiImplicitParam(
            name = "acquisitionTimeOne",
            value = "采集时间1",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "acquisitionTimeTwo",
            value = "采集时间2",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "samplingFrequency",
            value = "采集频率 默认为2",
            dataType = "Integer"
    ), @ApiImplicitParam(
            name = "samplingFrequencyUnit",
            value = "采集频率单位 默认为小时  h = 小时 , d = 日 , w = 周 , m = 月 , y = 年",
            dataType = "String"
    )})
    @PostMapping("/findSummaryReportDetails")
    public Result<DataReportVo> findSummaryReportDetails(@ApiIgnore DataReportDto dataReportDto) {
        return dataReportService.findSummaryReportDetails(dataReportDto);
    }


    @PostMapping("/insertData")
    public String insertData() {
        int[] a1 = new int[]{1, 2, 3, 4, 5, 6};
        int[] a2 = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        String[] nodeId = new String[]{"24DFE6AB", "CC89AEF2"};
        String[] address = new String[]{"大仓库1号楼1层1分区1号垛位",
                "大仓库1号楼1层1分区2号垛位"};
        int index = 0;
        int id = 0;
        String type = "03";
        int i = RandomUtil.randomInt(0, 2);
        String time = "2022-06-01 00:00:00";
        DateTime parse = DateUtil.parse(time);
        for (int i1 = 0; i1 < 600; i1++) {
            int c = RandomUtil.randomInt(0, 6);
            int c2 = RandomUtil.randomInt(0, 9);
            int c3 = RandomUtil.randomInt(0, 9);
            BigDecimal temp = new BigDecimal("21." + a2[c2] + "" + a2[c3]);
            c = RandomUtil.randomInt(0, 6);
            c2 = RandomUtil.randomInt(0, 9);
            c3 = RandomUtil.randomInt(0, 9);
            BigDecimal humiture = new BigDecimal("6" + a1[c] + "." + a2[c2] + "" + a2[c3]);
            id = ++index;
            int i2 = RandomUtil.randomInt(0, 2);
            dataReportDao.insertData(id + 1200,
                    type,
                    temp.doubleValue(),
                    humiture.doubleValue(),
                    "99999999",
                    470.3,
                    3.0,
                    15,
                    2,
                    32,
                    57,
                    "大仓库1号楼1层1分区2垛位",
                    "05",
                    parse,
                    0,
                    "[00:0],[01:1],[02:1],[03:1],[04:1],[05:2]",
                    parse,
                    "1",
                    "1",
                    "3",
                    "05");
            parse.offset(DateField.HOUR_OF_DAY, 2);
        }
        return "aaaa";
    }
}