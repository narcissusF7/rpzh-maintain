package com.rpzh.controller;

import com.rpzh.dto.TaskDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.TaskManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 养护任务中心
 * @Author liubc
 * @create 2022-05-30 11:31
 * @Description: good good study,day day up
 */
@RestController
@RequestMapping("/maintain/taskmanager")
@CrossOrigin
@Api(value = "任务管理中心", tags = "任务管理中心")
public class TaskManagementController {

    @Autowired
    private TaskManagementService taskManagementService;


    /**
     * 新建任务
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "新建任务", tags = "新建任务")
    @PostMapping("/addTask")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "任务类型,01:虫情巡查,02:虫情杀虫,03:仓库检查," +
                    "04:仓间温度预警,05:垛内温度预警,06:垛内湿度预警,07:翻垛任务,08:移垛任务", required = true,dataType = "String"),
            @ApiImplicitParam(name = "principal", value = "负责人", required = true,dataType = "String"),
            @ApiImplicitParam(name = "taskAddress", value = "任务地点", required = true,dataType = "String"),
            @ApiImplicitParam(name = "jwt", value = "令牌",required = true,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间",required = true,dataType = "String"),
            @ApiImplicitParam(name = "detail", value = "任务详情",required = true,dataType = "String"),
            @ApiImplicitParam(name = "pushWay", value = "推送方式,短信:M,App:A",required = true,dataType = "String")
    })
    public Result addTask(@ApiIgnore TaskDto taskDto){
        if (ToolUtil.isEmpty(taskDto.getType())){
            return new Result(false, StatusCode.ERROR, "任务类型不能为空");
        }
        if (ToolUtil.isEmpty(taskDto.getPrincipal())){
            return new Result(false, StatusCode.ERROR, "任务负责人不能为空");
        }
        if (ToolUtil.isEmpty(taskDto.getTaskAddress())){
            return new Result(false, StatusCode.ERROR, "任务地址不能为空");
        }
        if (ToolUtil.isEmpty(taskDto.getEndTime())){
            return new Result(false, StatusCode.ERROR, "结束时间不能为空");
        }
        if (ToolUtil.isEmpty(taskDto.getPushWay())){
            return new Result(false, StatusCode.ERROR, "推送方式不能为空");
        }
        if (ToolUtil.isEmpty(taskDto.getDetail())){
            return new Result(false, StatusCode.ERROR, "任务详情不能为空");
        }
        int addFlag = taskManagementService.addTask(taskDto);
        return new Result(true,StatusCode.OK,"添加任务成功");
    }




}