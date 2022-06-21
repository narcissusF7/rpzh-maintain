package com.rpzh.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.rpzh.Task.SendTask;
import com.rpzh.Task.SendTwoTask;
import com.rpzh.dao.EarlyWarningDao;
import com.rpzh.dao.MaintainPlanDao;
import com.rpzh.dto.NodeDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.feign.UserFeign;
import com.rpzh.handler.DataHandler;
import com.rpzh.parse.ParseData;
import com.rpzh.service.NodeService;
import com.rpzh.vo.NodeVo;
import com.rpzh.vo.TimeVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author liubc
 * @create 2022-05-17 13:36
 * @Description: good good study,day day up
 */
@RestController
@RequestMapping("/maintain/test")
@CrossOrigin
@Api(value = "测试用例类", tags = "测试用例类", description = "测试用例类")
@Slf4j
public class TestContrller {


    @Autowired
    private NodeService nodeService;

    @Autowired
    private DataHandler dataHandler;

    @ApiOperation(value = "测试用例方法", tags = "测试用例方法")
    @PostMapping("/one")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nodeType", value = "设备类型,默认:01", required = true,dataType = "String"),
            @ApiImplicitParam(name = "mainStre", value = "养护阶段", required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "pelpcileId", value = "负责人ID", required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "jwt", value = "令牌",paramType = "请求头",dataType = "String"),
            @ApiImplicitParam(name = "stack", value = "垛位",dataType = "String"),
            @ApiImplicitParam(name = "createTime", value = "创建时间",dataType = "String")
    })
    public Result testOne(NodeDto nodeDto){
        PageInfo<NodeVo> pageInfo = nodeService.findAllNodeList(nodeDto);
        return new Result(true, StatusCode.OK, "发送成功",pageInfo);
    }

    /**
     * 参考这个方法
     * @param nodeId
     * @param nodeType
     * @param createTime
     * @param startTime
     * @param endTime
     * @return
     */
    @PostMapping("/two")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nodeId", value = "设备id", required = true,dataType = "String"),
            @ApiImplicitParam(name = "nodeType", value = "设备类型", required = true,dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "createTime", value = "创建时间",dataType = "String")
    })
    @ApiOperation(value = "测试用例方法2")
    public Result<NodeVo> two(String nodeId,String nodeType,String createTime,String startTime,String endTime){
        List<NodeVo> list = nodeService.testFindNode(nodeId,nodeType,createTime,startTime,endTime);
        return new Result<NodeVo>(true, StatusCode.OK, "测试方法2",list);
    }

    /**
     *测试用例方法3
     * @param nodeDto
     * @return
     */
    @PostMapping("/three")
    @ApiOperation(value = "测试用例方法3")
    public Result<NodeVo> three(NodeDto nodeDto){
        String nodeId = nodeDto.getNodeId();
        String nodeType = nodeDto.getNodeType();
        String createTime = nodeDto.getCreatedTime();
        String startTime = nodeDto.getStartTime();
        String endTime = nodeDto.getEndTime();
        List<NodeVo> list = nodeService.testFindNode(nodeId,nodeType,createTime,startTime,endTime);
        return new Result<NodeVo>(true, StatusCode.OK, "测试方法3",list);
    }

    /**
     *测试用例方法4
     * @param nodeDto
     * @return
     */
    @PostMapping("/four")
    @ApiOperation(value = "测试用例方法4")
    @ApiImplicitParams({@ApiImplicitParam(
            name = "nodeId",
            value = "设备id",
            required = true,
            dataType = "String"
    ), @ApiImplicitParam(
            name = "nodeType",
            value = "设备类型,默认:01",
            required = true,
            dataType = "String"
    ), @ApiImplicitParam(
            name = "createdTime()",
            value = "创建时间",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "startTime",
            value = "开始时间",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "endTime",
            value = "结束时间",
            dataType = "Integer"
    )})
    public Result<NodeVo> four(@ApiIgnore NodeDto nodeDto){
        String nodeId = nodeDto.getNodeId();
        String nodeType = nodeDto.getNodeType();
        String createTime = nodeDto.getCreatedTime();
        String startTime = nodeDto.getStartTime();
        String endTime = nodeDto.getEndTime();
        List<NodeVo> list = nodeService.testFindNode(nodeId,nodeType,createTime,startTime,endTime);
        return new Result<>(true, StatusCode.OK, "测试方法4",list);
    }


    @ApiOperation(value = "数据推送测试4")
    @ApiImplicitParams({@ApiImplicitParam(
            name = "nodeId",
            value = "设备id",
            required = true,
            dataType = "String"
    ), @ApiImplicitParam(
            name = "nodeType",
            value = "设备类型",
            required = true,
            dataType = "String"
    ), @ApiImplicitParam(
            name = "Data",
            value = "数据",
            dataType = "String",
            allowMultiple = true
    ), @ApiImplicitParam(
            name = "dateTime",
            value = "传输时间",
            dataType = "String",
            allowMultiple = true
    ), @ApiImplicitParam(
            name = "frequency",
            value = "频率",
            dataType = "Double"
    ), @ApiImplicitParam(
            name = "voltage",
            value = "电压",
            dataType = "Double"
    ), @ApiImplicitParam(
            name = "signalStrength",
            value = "信号强度",
            dataType = "Double"
    ), @ApiImplicitParam(
            name = "packetLossNum",
            value = "丢包个数",
            dataType = "Integer"
    )})
    @PostMapping("testParseData")
    public Result testParseData(@ApiIgnore ParseData parseData){
        dataHandler.handlerData(parseData);
        return new Result<>(true, StatusCode.OK, "测试方法4");
    }
    @PostMapping("five")
    public Result five(String map){
//        dataHandler.handlerData(parseData);

        JSONObject.parseObject(map,Map.class);
        return new Result<>(true, StatusCode.OK, "测试方法4");
    }

    @Autowired
    private SendTask sendTask;

    @Autowired
    private SendTwoTask sendTwoTask;
    @Resource
    private EarlyWarningDao earlyWarningDao;
    @Resource
    private RedisTemplate redisTemplate;
    @GetMapping("/updateCron")
    public String updateCron(String firstTime,String secondTime) {
        if(ToolUtil.isEmpty(firstTime) || ToolUtil.isEmpty(secondTime)) {
            return "";
        }
        TimeVo timeVo = earlyWarningDao.findTime();
        if(ToolUtil.isNotEmpty(timeVo)){
            earlyWarningDao.updateTime(firstTime,secondTime);
        }
        if(ToolUtil.isEmpty(timeVo)){
            earlyWarningDao.insertTime(firstTime,secondTime);
        }
        log.debug(firstTime);
        String[] times = firstTime.split(":");
        int hours = Integer.parseInt(times[0]);
        int minutes = Integer.parseInt(times[1]);
        int seconds = Integer.parseInt(times[2]);
        /*String cron = seconds + " " + minutes + " " + hours + " * * ?";*/
        String cron ="*/" + seconds + " * * * * ?";
        sendTask.setCron(cron);
        redisTemplate.opsForValue().set("firstTime",cron);
        times = secondTime.split(":");
        hours = Integer.parseInt(times[0]);
        minutes = Integer.parseInt(times[1]);
        seconds = Integer.parseInt(times[2]);
        /*String cron = seconds + " " + minutes + " " + hours + " * * ?";*/
        cron ="*/" + seconds + " * * * * ?";
        redisTemplate.opsForValue().set("secondTime",cron);
        sendTwoTask.setCron(cron);
        return "ok";
    }

}