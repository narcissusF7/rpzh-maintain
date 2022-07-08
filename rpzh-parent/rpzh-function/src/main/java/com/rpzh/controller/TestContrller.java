package com.rpzh.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.rpzh.util.SendUtils;
import com.rpzh.dao.EarlyWarningDao;
import com.rpzh.dto.NodeDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.handler.DataHandler;
import com.rpzh.parse.ParseData;
import com.rpzh.service.NodeService;
import com.rpzh.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.ArrayList;
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
public class TestContrller{


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

    @Resource
    private EarlyWarningDao earlyWarningDao;
    @Resource
    private RedisTemplate redisTemplate;



    @GetMapping("/updateCron")
    public String updateCron(String firstTime,String secondTime) throws SchedulerException {
        /*if(ToolUtil.isEmpty(firstTime) || ToolUtil.isEmpty(secondTime)) {
            return null;
        }
        earlyWarningDao.insertTime(firstTime,secondTime);
        //1、调度器(Schedular)，从工厂中获取调度实例（默认：实例化new StdSchedulerFactory();)
        Scheduler scheduler= null;
        String[] times = firstTime.split(":");
        String cron =Integer.parseInt(times[2]) + " " + Integer.parseInt(times[1]) + " " + Integer.parseInt(times[0]) + " * * ?";
        redisTemplate.opsForValue().set("firstTime",cron);
        log.debug(cron);
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        List<JobKey> keys = new ArrayList<>();
        keys.add(new JobKey("job1","group1"));
        keys.add(new JobKey("job2","group2"));
        scheduler.deleteJobs(keys);
        //2、任务实例（JobDetail）
        JobDetail jobDetail= JobBuilder.newJob(SendUtils.class) //加载任务类，与HelloJob完成绑定，要求HelloJob实现Job接口
                .withIdentity("job1","group1") //参数1：任务的名称（唯一实例）；参数2：任务组的名称
                .build();
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group1").withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();
        scheduler.scheduleJob(jobDetail,trigger);
        String[] time1 = secondTime.split(":");
        cron =Integer.parseInt(time1[2]) + " " + Integer.parseInt(time1[1]) + " " + Integer.parseInt(time1[0]) + " * * ?";
        redisTemplate.opsForValue().set("secondTime",cron);
        jobDetail= JobBuilder.newJob(SendUtils.class) //加载任务类，与HelloJob完成绑定，要求HelloJob实现Job接口
                .withIdentity("job2","group2") //参数1：任务的名称（唯一实例）；参数2：任务组的名称
                .build();
        trigger = TriggerBuilder.newTrigger().withIdentity("trigger2", "group2").withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();
        scheduler.scheduleJob(jobDetail,trigger);
        scheduler.start();
        log.debug(cron);*/
        return "ok";
    }

}