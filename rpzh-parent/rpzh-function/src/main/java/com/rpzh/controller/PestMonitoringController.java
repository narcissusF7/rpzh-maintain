package com.rpzh.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.constant.NodeConstant;
import com.rpzh.dto.PestBoardDto;
import com.rpzh.dto.PestDataDto;
import com.rpzh.dto.PestMonitoringRuleDto;
import com.rpzh.entity.ExcelUtils;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.pojo.PestData;
import com.rpzh.service.PestMonitoringService;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.PestBoardVo;
import com.rpzh.vo.PestDataVo;
import com.rpzh.vo.PestMonitoringRuleVo;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 虫情监控中心
 * @Author liujj
 * @create 2022-06-08 11:31
 * @Description: good good study,day day up
 */
@RestController
@RequestMapping("/maintain/pestMonitoring")
@CrossOrigin
@Api(value = "虫情监控中心", tags = "虫情监控中心")
public class PestMonitoringController {

    @Autowired
    PestMonitoringService pestMonitoringService;

    @ApiOperation("添加虫情板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pestBoardId", value = "虫情板Id", dataType = "String", required = true),
            @ApiImplicitParam(name = "siteRoute", value = "位置路由", dataType = "String", required = true),
            @ApiImplicitParam(name = "siteId", value = "位置id", dataType = "Integer", required = true),
            @ApiImplicitParam(name = "principalId", value = "负责人Id", dataType = "Integer", required = true),
            @ApiImplicitParam(name = "principalName", value = "负责人", dataType = "String", required = true),
            @ApiImplicitParam(name = "rfid", value = "rfid", dataType = "String", required = true),
            @ApiImplicitParam(name = "remark", value = "备注", dataType = "String")
    })
    @PostMapping("/addPestBoard")
    public Result addPestBoard(@ApiIgnore PestBoardDto pestBoardDto){
        pestMonitoringService.addPestBoard(pestBoardDto);
        return new Result(true, StatusCode.OK,"添加成功");
    }
    @ApiOperation("删除虫情板")
    @ApiImplicitParam(name = "ids", value = "id集", dataType = "Integer", required = true,allowMultiple = true)
    @PostMapping("/deletePestBoard")
    public Result deletePestBoard(Integer [] ids){
        pestMonitoringService.deletePestBoard(ids);
        return new Result(true, StatusCode.OK,"删除成功");
    }

    @ApiOperation("虫情监控规则配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "weekPatrol", value = "巡查时间(0/1)",dataType = "Integer", required = true, allowMultiple = true),
            @ApiImplicitParam(name = "valueOne", value = "一级预警值",dataType = "Integer", required = true),
            @ApiImplicitParam(name = "valueTwo", value = "二级预警值",dataType = "Integer", required = true),
            @ApiImplicitParam(name = "valueThree", value = "三级预警值",dataType = "Integer", required = true),
            @ApiImplicitParam(name = "colorOne", value = "一级预警颜色",dataType = "String", required = true),
            @ApiImplicitParam(name = "colorTwo", value = "二级预警颜色",dataType = "String", required = true),
            @ApiImplicitParam(name = "colorThree", value = "三级预警颜色",dataType = "String", required = true),
            @ApiImplicitParam(name = "replacePeriod", value = "替换周期",dataType = "Integer", required = true),
            @ApiImplicitParam(name = "siteRoutes", value = "位置路由集合（需要前端把原本的pidRoute中的，替换成|）",dataType = "String",allowMultiple = true),
            @ApiImplicitParam(name = "siteIds", value = "位置id集合",dataType = "Integer",allowMultiple = true),
            @ApiImplicitParam(name = "siteNames", value = "位置名称集合",dataType = "String",allowMultiple = true),
            @ApiImplicitParam(name = "stackWarningValues", value = "垛位预警值集合（每个垛位的预警值用|拼接）",dataType = "String", allowMultiple = true),
            @ApiImplicitParam(name = "delIds", value = "需要删除的特殊垛位的主键id集",dataType = "Integer", allowMultiple = true)
    })
    @PostMapping("/updatePestRule")
    public Result updatePestRule(PestMonitoringRuleDto pestMonitoringRuleDto){
        pestMonitoringService.updatePestRule(pestMonitoringRuleDto);
        return new Result(true, StatusCode.OK,"修改成功");
    }

    @ApiOperation("查询虫情监控规则")
    @GetMapping("/findPestRule")
    public Result<PestMonitoringRuleVo> findPestRule(){
        PestMonitoringRuleVo pestMonitoringRuleVo =pestMonitoringService.findPestRule();
        return new Result(true, StatusCode.OK,"查询成功",pestMonitoringRuleVo);
    }

    @ApiOperation("虫情导入模板下载")
    @GetMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) {
        String downloadFilePath = "/template/pestBoard/pestBoard.xlsx";//被下载的文件在服务器中的路径,
        String fileName = "虫情导入模板";//被下载文件的名称
        InputStream inputStream = getClass().getResourceAsStream(downloadFilePath);
        ExcelUtils.outFile(inputStream, response, fileName, "xlsx");
    }

    @ApiOperation("虫情数据导入")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件", dataType = "MultipartFile", required = true),
            @ApiImplicitParam(name = "siteType", value = "位置类型仓间04 垛内05", dataType = "String", required = true)
    })
    @PostMapping("/exportPestData")
    public Result exportPestData(MultipartFile file,String siteType){
        if (ToolUtil.isEmpty(file)) {
            return new Result<>(false, StatusCode.ERROR, "请选择一个文件");
        }

        if (ToolUtil.isEmpty(siteType)) {
            return new Result<>(false, StatusCode.ERROR, "请确定位置类型（）仓间/垛位");
        }

        //读取表格中的数据，file 文件，startRow:开始的行数   startCloumn:开始的列数，endCloumn:结束的列数
        List<Map<Integer, String>> pestData;
        try {
            //垛内只计算甲虫
            if (NodeConstant.NODE_STACK.equals(siteType)){
                pestData = ExcelUtils.importExcel(file, 1, 0, 3);
            }else {
                pestData = ExcelUtils.importExcel(file, 1, 0, 4);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new Result<>(false, StatusCode.ERROR, "导入失败");
        }

        return pestMonitoringService.exportPestData(pestData,siteType);
    }

    @ApiOperation("虫情数据一键导入")
    @PostMapping("/fullExport")
    public Result fullExport(){
        return pestMonitoringService.fullExport();
    }



    @ApiOperation("虫情点位信息查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pestBoardId", value = "虫情点位id", dataType = "String"),
            @ApiImplicitParam(name = "pidRoute", value = "位置路由", dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "位置id", dataType = "Integer"),
            @ApiImplicitParam(name = "principalId", value = "负责人id", dataType = "Integer"),
            @ApiImplicitParam(name = "page", value = "当前页", dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "每页长度", dataType = "Integer")
    })
    @GetMapping("/findPestBoard")
    public Result<List<PestBoardVo>> findPestBoard(@ApiIgnore PestBoardDto pestBoardDto){
        //分页
        PageHelper.startPage(pestBoardDto.getPage(),pestBoardDto.getSize());

        List<PestBoardVo> pestBoard = pestMonitoringService.findPestBoard(pestBoardDto);

        return new Result<>(true,StatusCode.OK,"查询成功",new PageInfo<>(pestBoard));
    }
    @ApiOperation("虫情数据实时监控")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "monitorStatus", value = "监控状态 0 全部 1 预警 2 正常", dataType = "Integer"),
            @ApiImplicitParam(name = "pidRoute", value = "位置路由", dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "位置id", dataType = "Integer"),
            @ApiImplicitParam(name = "siteType", value = "位置类型 04仓间05垛位", dataType = "String"),
            @ApiImplicitParam(name = "page", value = "当前页", dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "每页长度", dataType = "Integer"),
            @ApiImplicitParam(name = "sortCondition", value = "排序规则 0 位置排序 1 预警排序", dataType = "Integer")
    })
    @GetMapping("/findPestDataHistory")
    public Result<List<PestDataVo>>  findPestDataHistory(@ApiIgnore MonitorCondition monitorCondition){
        PageInfo<PestDataVo> pageInfo = pestMonitoringService.findPestDataHistory(monitorCondition);
        return new Result<>(true,StatusCode.OK,"查询成功",pageInfo);
    }


    @ApiOperation("查询单个点位虫情数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pestBoardId", value = "虫情点位id", dataType = "String"),
            @ApiImplicitParam(name = "beginTime", value = "开始时间", dataType = "String"),
            @ApiImplicitParam(name = "siteType", value = "位置类型 04仓间05垛位", dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "String"),
            @ApiImplicitParam(name = "page", value = "当前页", dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "每页长度", dataType = "Integer")
    })
    @GetMapping("/findPestDataHistoryByOne")
    public Result<List<PestDataVo>> findPestDataHistoryByOne(@ApiIgnore PestDataDto pestDataDto){
        PageHelper.startPage(pestDataDto.getPage(),pestDataDto.getSize());
        List<PestDataVo> pestDataVos = pestMonitoringService.findPestDataHistoryByOne(pestDataDto);
        return new Result<>(true,StatusCode.OK,"查询成功",new PageInfo<>(pestDataVos));
    }

    @ApiOperation("添加虫情数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pestBoardId", value = "虫情点位id", dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "位置标识", dataType = "String"),
            @ApiImplicitParam(name = "beetleCount", value = "甲虫数量", dataType = "Integer"),
            @ApiImplicitParam(name = "mealwormCount", value = "粉螟数量", dataType = "Integer"),
            @ApiImplicitParam(name = "gatherSite", value = "采集位置", dataType = "String")
    })
    @PostMapping("/addPestHistory")
    public Result addPestHistory(@ApiIgnore PestDataDto pestDataDto){
        pestMonitoringService.addPestHistory(pestDataDto);
        return new Result(true,StatusCode.OK,"添加成功");
    }
    //TODO 扫描RFID获取位置 物联网上做还是养护上做
}
