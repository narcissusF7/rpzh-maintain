package com.rpzh.controller;

import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.EarlyWarningService;
import com.rpzh.vo.RepairDataVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author: zyq
 * @Date: 2022/05/20/17:34
 * @Description: 功能
 */
@RestController
@CrossOrigin
@RequestMapping("/maintain/appAlarmWarning")
@Api(value = "APP预警管理类", tags = "APP预警管理类", description = "APP预警管理类")
public class AppEarlyWarningController {
    @Resource
    private EarlyWarningService earlyWarningService;

    @PostMapping("/findAppWarningIndex")
    @ApiOperation(value = "APP预警管理首页")
    public Result findAppWarningIndex(){
        Map appMap = earlyWarningService.findAppWarningIndex();
        return new Result<>(true, StatusCode.OK, "查询成功",appMap);
    }

    @PostMapping("/findAppWarningInfoByWarningType")
    @ApiOperation(value = "APP根据预警类型查询预警信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "warningType", value = "预警类型:01:设备故障,04:垛内甲虫预警,05:仓间甲虫预警,06:垛内氧气预警," +
                "07:仓间氧气预警,08:垛内温度预警,09:仓间温度预警,10:垛内湿度预警,11:仓间湿度预警,12:包芯温度预警,13:平均包芯高于环境预警,14:仓间粉螟预警，19:平均包芯低于环境预警", required = true,dataType = "String"),
            @ApiImplicitParam(name = "warningGrade", value = "预警等级:1:一级等级,2:二级等级,3:三级等级,4:全部", dataType = "String",required = true)
    })
    public Result findAppWarningInfoByWarningType(@RequestParam(name =  "warningType",required = false) String warningType, @RequestParam(name = "warningGrade",required = false)String warningGrade){
        if(ToolUtil.isEmpty(warningType)){
            return new Result<>(true, StatusCode.OK, "预警类型不能为空");
        }
        Map appMap = earlyWarningService.findAppWarningInfoByWarningType(warningType,warningGrade);
        return new Result<>(true, StatusCode.OK, "查询成功",appMap);
    }

    @PostMapping("/appMisreport")
    @ApiOperation(value = "误报")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id",dataType = "int",required = true)
    })
    public Result appMisreport(Integer id){
        if(ToolUtil.isEmpty(id)){
            return new Result<>(false, StatusCode.ERROR, "预警id不能为空");
        }
        earlyWarningService.misreport(id);
        return new Result<>(true, StatusCode.OK, "处理误报成功");
    }

    @PostMapping("/findAppWarningInfoBySiteRoute")
    @ApiOperation(value = "APP根据预警类型和位置查询预警详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "warningType", value = "预警类型:01:设备故障,04:垛内甲虫预警,05:仓间甲虫预警,06:垛内氧气预警," +
                    "07:仓间氧气预警,08:垛内温度预警,09:仓间温度预警,10:垛内湿度预警,11:仓间湿度预警,12:包芯温度预警,13:平均包芯高于环境预警,14:仓间粉螟预警，19:平均包芯低于环境预警", required = true,dataType = "String"),
            @ApiImplicitParam(name = "warningGrade", value = "预警等级:1:一级等级,2:二级等级,3:三级等级", dataType = "String",required = true),
            @ApiImplicitParam(name = "siteRoute", value = "位置路由", required = true,dataType = "String"),
            @ApiImplicitParam(name = "nodeType", value = "设备类型", dataType = "String",required = true),
            @ApiImplicitParam(name = "id", value = "预警id", dataType = "String",required = true)
    })
    public Result findAppWarningInfoBySiteRoute(String siteRoute,String warningType,String warningGrade,String nodeType,String id){
        Map mapInfos = earlyWarningService.findAppWarningInfoBySiteRoute(siteRoute, warningType, warningGrade, nodeType,  id);
        return new Result<>(true, StatusCode.OK, "查询成功",mapInfos);
    }
}
