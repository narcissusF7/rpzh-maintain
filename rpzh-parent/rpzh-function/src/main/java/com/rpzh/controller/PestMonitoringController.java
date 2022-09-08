package com.rpzh.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.constant.NodeConstant;
import com.rpzh.dao.EarlyWarningDao;
import com.rpzh.dto.PestBoardDto;
import com.rpzh.dto.PestDataDto;
import com.rpzh.dto.PestMonitoringRuleDto;
import com.rpzh.entity.ExcelUtils;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.EarlyWarningService;
import com.rpzh.service.PestMonitoringService;
import com.rpzh.util.MonitorCondition;
import com.rpzh.vo.PestBoardVo;
import com.rpzh.vo.PestDataVo;
import com.rpzh.vo.PestMonitoringRuleVo;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Autowired
    EarlyWarningDao earlyWarningDao;

    @ApiOperation("添加虫情板")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pestBoardId", value = "虫情板Id", dataType = "String", required = true),
            @ApiImplicitParam(name = "siteRoute", value = "位置路由", dataType = "String", required = true),
            @ApiImplicitParam(name = "siteName", value = "位置名称", dataType = "String", required = true),
            @ApiImplicitParam(name = "siteId", value = "位置id", dataType = "Integer", required = true),
            @ApiImplicitParam(name = "principalId", value = "负责人Id", dataType = "Integer", required = true),
            @ApiImplicitParam(name = "principalName", value = "负责人", dataType = "String", required = true),
            @ApiImplicitParam(name = "rfid", value = "rfid", dataType = "String", required = true),
            @ApiImplicitParam(name = "remark", value = "备注", dataType = "String")
    })
    @PostMapping("/addPestBoard")
    @PreAuthorize("hasAnyAuthority('f:p:add')")
    public Result addPestBoard(@ApiIgnore PestBoardDto pestBoardDto){
        if (ToolUtil.isEmpty(pestBoardDto.getPestBoardId())) {
            return new Result<>(false, StatusCode.ERROR, "虫情板Id不能为空");
        }
        int count = pestMonitoringService.findCountByPestBoradId(pestBoardDto.getPestBoardId());
        if (count>0){
            return new Result<>(false, StatusCode.ERROR, "虫情点位id已存在");
        }
        if (ToolUtil.isEmpty(pestBoardDto.getSiteRoute())) {
            return new Result<>(false, StatusCode.ERROR, "位置路由不能为空");
        }
        if (ToolUtil.isEmpty(pestBoardDto.getSiteName())) {
            return new Result<>(false, StatusCode.ERROR, "位置名称不能为空");
        }
        if (ToolUtil.isEmpty(pestBoardDto.getSiteId())) {
            return new Result<>(false, StatusCode.ERROR, "位置id不能为空");
        }
        if (ToolUtil.isEmpty(pestBoardDto.getPrincipalUser())) {
            return new Result<>(false, StatusCode.ERROR, "负责人id不能为空");
        }
        if (ToolUtil.isEmpty(pestBoardDto.getPrincipalName())) {
            return new Result<>(false, StatusCode.ERROR, "负责人姓名不能为空");
        }
        if (ToolUtil.isEmpty(pestBoardDto.getRfid())) {
            return new Result<>(false, StatusCode.ERROR, "rfid不能为空");
        }

        return pestMonitoringService.addPestBoard(pestBoardDto);
    }
    @ApiOperation("删除虫情板")
    @ApiImplicitParam(name = "ids", value = "id集", dataType = "Integer", required = true,allowMultiple = true)
    @PostMapping("/deletePestBoard")
    @PreAuthorize("hasAnyAuthority('f:p:del')")
    public Result deletePestBoard(Integer [] ids){
        if (ToolUtil.isEmpty(ids)) {
            return new Result<>(false, StatusCode.ERROR, "id集不能为空");
        }
        int count = earlyWarningDao.findPestWarningCount(ids);
        if (count>0){
            return new Result(false, StatusCode.ERROR,"所选虫情点位存在未处理预警，删除失败");
        }
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
            @ApiImplicitParam(name = "specialStack", value = "特殊垛位集合",dataType = "String")
    })
    @PostMapping("/updatePestRule")
    @PreAuthorize("hasAnyAuthority('f:p:ur')")
    public Result updatePestRule(PestMonitoringRuleDto pestMonitoringRuleDto){
        if (ToolUtil.isEmpty(pestMonitoringRuleDto.getWeekPatrol())) {
            return new Result<>(false, StatusCode.ERROR, "巡查时间不能为空");
        }
        if (ToolUtil.isEmpty(pestMonitoringRuleDto.getValueOne())) {
            return new Result<>(false, StatusCode.ERROR, "一级预警值不能为空");
        }
        if (ToolUtil.isEmpty(pestMonitoringRuleDto.getValueTwo())) {
            return new Result<>(false, StatusCode.ERROR, "二级预警值不能为空");
        }
        if (ToolUtil.isEmpty(pestMonitoringRuleDto.getValueThree())) {
            return new Result<>(false, StatusCode.ERROR, "三级预警值不能为空");
        }
        if (pestMonitoringRuleDto.getValueOne()>=pestMonitoringRuleDto.getValueTwo()){
            return new Result<>(false, StatusCode.ERROR, "二级预警值不能小于等于一级预警值");
        }
        if (pestMonitoringRuleDto.getValueTwo()>=pestMonitoringRuleDto.getValueThree()){
            return new Result<>(false, StatusCode.ERROR, "三级预警值不能小于等于二级预警值");
        }
        if (ToolUtil.isEmpty(pestMonitoringRuleDto.getColorOne())) {
            return new Result<>(false, StatusCode.ERROR, "一级预警颜色不能为空");
        }
        if (ToolUtil.isEmpty(pestMonitoringRuleDto.getColorTwo())) {
            return new Result<>(false, StatusCode.ERROR, "二级预警颜色不能为空");
        }
        if (ToolUtil.isEmpty(pestMonitoringRuleDto.getColorThree())) {
            return new Result<>(false, StatusCode.ERROR, "三级预警颜色不能为空");
        }
        if (ToolUtil.isEmpty(pestMonitoringRuleDto.getReplacePeriod())) {
            return new Result<>(false, StatusCode.ERROR, "替换周期不能为空");
        }
        pestMonitoringService.updatePestRule(pestMonitoringRuleDto);
        return new Result(true, StatusCode.OK,"保存成功");
    }

    @ApiOperation("查询虫情监控规则")
    @GetMapping("/findPestRule")
    public Result<PestMonitoringRuleVo> findPestRule(){
        PestMonitoringRuleVo pestMonitoringRuleVo =pestMonitoringService.findPestRule();
        return new Result(true, StatusCode.OK,"查询成功",pestMonitoringRuleVo);
    }

    @ApiOperation("虫情导入模板下载")
    @GetMapping("/downloadTemplate")
    @ApiImplicitParam(name = "siteType", value = "位置类型仓间04 垛内05", dataType = "String", required = true)
    public Result downloadStackTemplate(HttpServletResponse response,String siteType) {
        if (ToolUtil.isEmpty(siteType)) {
            return new Result<>(false, StatusCode.ERROR, "位置类型不能为空");
        }
        String downloadFilePath = "";
        if (NodeConstant.NODE_WARE_AREA.equals(siteType)) {
            downloadFilePath = "/template/pestBoard/ware_pestBoard.xlsx";
        }else {
            downloadFilePath = "/template/pestBoard/stack_pestBoard.xlsx";
        }
        //被下载的文件在服务器中的路径,
        String fileName = "虫情导入模板";//被下载文件的名称
        InputStream inputStream = getClass().getResourceAsStream(downloadFilePath);
        ExcelUtils.outFile(inputStream, response, fileName, "xlsx");
        return new Result<>(true, StatusCode.OK, "下载成功");
    }

    @ApiOperation("虫情数据导入")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件", dataType = "MultipartFile", required = true),
            @ApiImplicitParam(name = "siteType", value = "位置类型仓间04 垛内05", dataType = "String", required = true)
    })
    @PostMapping("/exportPestData")
    @PreAuthorize("hasAnyAuthority('f:p:fp')")
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
                pestData = ExcelUtils.importExcel(file, 1, 0, 4);
            }else {
                pestData = ExcelUtils.importExcel(file, 1, 0, 5);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new Result<>(false, StatusCode.ERROR, "导入失败");
        }

        return pestMonitoringService.exportPestData(pestData,siteType);
    }

    @ApiOperation("虫情数据一键导入")
    @PostMapping("/fullExport")
    @PreAuthorize("hasAnyAuthority('f:p:fp')")
    public Result fullExport(){
        return pestMonitoringService.fullExport();
    }



    @ApiOperation("虫情点位信息查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pestBoardId", value = "虫情点位id", dataType = "String"),
            @ApiImplicitParam(name = "siteRoute", value = "位置路由", dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "位置id", dataType = "Integer"),
            @ApiImplicitParam(name = "principalUser", value = "负责人账号", dataType = "String"),
            @ApiImplicitParam(name = "page", value = "当前页", dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "每页长度", dataType = "Integer")
    })
    @PostMapping("/findPestBoard")
    public Result<List<PestBoardVo>> findPestBoard(@ApiIgnore PestBoardDto pestBoardDto){

        if (!ToolUtil.isEmpty(pestBoardDto.getPage())&&!ToolUtil.isEmpty(pestBoardDto.getSize())){
            //分页
            PageHelper.startPage(pestBoardDto.getPage(),pestBoardDto.getSize());
        }
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
    @PostMapping("/findPestDataHistory")
    public Result<List<PestDataVo>>  findPestDataHistory(@ApiIgnore MonitorCondition monitorCondition){
        PageInfo<PestDataVo> pageInfo = pestMonitoringService.findPestDataHistory(monitorCondition);
        return new Result<>(true,StatusCode.OK,"查询成功",pageInfo);
    }


    @ApiOperation("查询单个点位虫情数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pestBoardId", value = "虫情点位id", dataType = "String" ,required = true),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "String"),
            @ApiImplicitParam(name = "siteType", value = "位置类型 04仓间05垛位", dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "String"),
            @ApiImplicitParam(name = "page", value = "当前页", dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "每页长度", dataType = "Integer")
    })
    @GetMapping("/findPestDataHistoryByOne")
    public Result<List<PestDataVo>> findPestDataHistoryByOne(@ApiIgnore PestDataDto pestDataDto){
        if (ToolUtil.isEmpty(pestDataDto.getPestBoardId())){
            return new Result(false,StatusCode.ERROR,"虫情点位id不能为空");
        }
        if (!ToolUtil.isEmpty(pestDataDto.getPage())&&!ToolUtil.isEmpty(pestDataDto.getSize())){
            //分页
            PageHelper.startPage(pestDataDto.getPage(),pestDataDto.getSize());
        }
        List<PestDataVo> pestDataVos = pestMonitoringService.findPestDataHistoryByOne(pestDataDto);
        return new Result<>(true,StatusCode.OK,"查询成功",new PageInfo<>(pestDataVos));
    }

    //    @ApiOperation("添加虫情数据")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "pestBoardId", value = "虫情点位id", dataType = "String"),
//            @ApiImplicitParam(name = "siteId", value = "位置标识", dataType = "String"),
//            @ApiImplicitParam(name = "beetleCount", value = "甲虫数量", dataType = "Integer"),
//            @ApiImplicitParam(name = "mealwormCount", value = "粉螟数量", dataType = "Integer"),
//            @ApiImplicitParam(name = "gatherSite", value = "采集位置", dataType = "String")
//    })
//    @PostMapping("/addPestHistory")
//    public Result addPestHistory(@ApiIgnore PestDataDto pestDataDto){
//        pestMonitoringService.addPestHistory(pestDataDto);
//        return new Result(true,StatusCode.OK,"添加成功");
//    }
    @ApiOperation("通过rfid查询虫情板数据")
    @GetMapping("/findPestBoardByRfid")
    @ApiImplicitParam(name = "rfid", value = "rfid", dataType = "String", required = true)
    public Result findPestBoardByRfid(String rfid){
        if (ToolUtil.isEmpty(rfid)){
            return new Result(false,StatusCode.ERROR,"rfid不能为空");
        }
        return pestMonitoringService.findPestBoardByRfid(rfid);
    }


    @ApiOperation("查询虫情点位预警值")
    @GetMapping("/findPestWarningValue")
    @ApiImplicitParam(name = "pestBoardId", value = "虫情点位id", dataType = "String", required = true)
    public Result<Map<String,Object>> findPestWarningValue(String pestBoardId){
        if (ToolUtil.isEmpty(pestBoardId)){
            return new Result(false,StatusCode.ERROR,"虫情点位id不能为空");
        }
        Map<String,Object> map = pestMonitoringService.findPestWarningValue(pestBoardId);
        return new Result(true,StatusCode.OK,"查询成功",map);
    }



}
