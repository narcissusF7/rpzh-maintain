package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.TaskDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.TaskManagementService;
import com.rpzh.vo.TaskVo;
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
    @ApiOperation(value = "发布任务菜单新建任务", tags = "任务管理中心")
    @PostMapping("/addTask")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "任务类型,01:虫情巡查,02:虫情杀虫,03:仓库检查," +
                    "04:仓间温度预警,05:垛内温度预警,06:垛内湿度预警,07:翻垛任务,08:移垛任务", required = true,dataType = "String"),
            @ApiImplicitParam(name = "principal", value = "负责人", required = true,dataType = "String"),
            @ApiImplicitParam(name = "taskAddress", value = "任务地点名字", required = true,dataType = "String"),
            @ApiImplicitParam(name = "siteRoute", value = "任务地点位置路径", required = true,dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "当前位置id", required = true,dataType = "String"),
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
        if (ToolUtil.isEmpty(taskDto.getSiteRoute())){
            return new Result(false, StatusCode.ERROR, "任务位置路径不能为空");
        }
        if (ToolUtil.isEmpty(taskDto.getSiteId())){
            return new Result(false, StatusCode.ERROR, "当前位置id不能为空");
        }
        if (ToolUtil.isEmpty(taskDto.getEndTime())){
            return new Result(false, StatusCode.ERROR, "结束时间不能为空");
        }
        if (ToolUtil.isEmpty(taskDto.getPushWay())){
            return new Result(false, StatusCode.ERROR, "推送方式不能为空");
        }
        if (!("M".equals(taskDto.getPushWay()) || "A".equals(taskDto.getPushWay()))){
            return new Result(false, StatusCode.ERROR, "推送方式参数错误");
        }
        if (ToolUtil.isEmpty(taskDto.getDetail())){
            return new Result(false, StatusCode.ERROR, "任务详情不能为空");
        }
        int addFlag = taskManagementService.addTask(taskDto);
        return new Result(true,StatusCode.OK,"添加任务成功");
    }


    /**
     * 查询任务列表,管理员查询任务列表,可以查询所有的任务
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "发布任务菜单查询任务列表", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "任务类型,01:虫情巡查,02:虫情杀虫,03:仓库检查," +
                    "04:仓间温度预警,05:垛内温度预警,06:垛内湿度预警,07:翻垛任务,08:移垛任务", required = false,dataType = "String"),
            @ApiImplicitParam(name = "taskNum", value = "任务编号", required = false,dataType = "String"),
            @ApiImplicitParam(name = "jwt", value = "令牌",required = true,dataType = "String"),
            @ApiImplicitParam(name = "endStartTime", value = "结束开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间",required = false,dataType = "String"),
            @ApiImplicitParam(name = "status", value = "任务状态,0:未完成,1:完成,2:逾期",required = false,dataType = "String"),
            @ApiImplicitParam(name = "page", value = "分页开始页",required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "分页每页显示",required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "principal", value = "负责人",required = false,dataType = "String")
    })
    @PostMapping("/findTask")
    public Result<PageInfo<TaskVo>> findTask(@ApiIgnore TaskDto taskDto){
        PageInfo<TaskVo> pageInfo = taskManagementService.findTask(taskDto);
        return new Result<>(true, StatusCode.OK, "查询成功", pageInfo);
    }


    /**
     * 批量删除任务
     * @param ids
     * @return
     */
    @ApiOperation(value = "发布任务菜单删除任务", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "任务主键id,id按照逗号隔开,例如1,2,3",required = true,dataType = "Long[]")
    })
    @PostMapping("/delTask")
    public Result delTask(@ApiIgnore Long[] ids){
        if (ToolUtil.isEmpty(ids)){
            return new Result(false, StatusCode.ERROR, "请选择一个设备");
        }
        int delFlag = taskManagementService.delTask(ids);
        return new Result(true, StatusCode.OK, "删除成功");
    }


    /**
     * 查询任务的详情
     * @param id
     * @return
     */
    @ApiOperation(value = "发布任务菜单查询任务详情", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "任务主键id",required = true,dataType = "Long")
    })
    @PostMapping("/findTaskDetail")
    public Result<TaskVo> findTaskDetail(Long id){
        if (ToolUtil.isEmpty(id)){
            return new Result<>(false, StatusCode.ERROR, "请选择一个任务");
        }
        TaskVo taskVo = taskManagementService.findTaskDetail(id);
        return new Result<TaskVo>(true, StatusCode.OK, "查询成功", taskVo);
    }


    /**
     * 工作任务查询列表
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "工作任务菜单查询任务任务列表", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "任务类型,01:虫情巡查,02:虫情杀虫,03:仓库检查," +
                    "04:仓间温度预警,05:垛内温度预警,06:垛内湿度预警,07:翻垛任务,08:移垛任务", required = false,dataType = "String"),
            @ApiImplicitParam(name = "taskNum", value = "任务编号", required = false,dataType = "String"),
            @ApiImplicitParam(name = "jwt", value = "令牌",required = true,dataType = "String"),
            @ApiImplicitParam(name = "endStartTime", value = "结束开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间",required = false,dataType = "String"),
            @ApiImplicitParam(name = "status", value = "任务状态,0:未完成,1:完成,2:逾期",required = false,dataType = "String"),
            @ApiImplicitParam(name = "page", value = "分页开始页",required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "分页每页显示",required = true,dataType = "Integer")
    })
    @PostMapping("/findTaskByUser")
    public Result<PageInfo<TaskVo>> findTaskByUser(@ApiIgnore TaskDto taskDto){
        PageInfo<TaskVo> pageInfo = taskManagementService.findTaskByUser(taskDto);
        return new Result<>(true,StatusCode.ERROR,"查询成功",pageInfo);
    }


    /**
     * 任务处理
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "工作任务菜单任务处理", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "任务id", required = true,dataType = "String"),
            @ApiImplicitParam(name = "dealMethod", value = "处理措施", required = true,dataType = "String"),
            @ApiImplicitParam(name = "dealResult", value = "处理结果", required = true,dataType = "String")
    })
    @PostMapping("/taskDispose")
    public Result taskDispose(@ApiIgnore TaskDto taskDto){
        String id = taskDto.getId();
        if (ToolUtil.isEmpty(id)){
            return new Result(false, StatusCode.ERROR, "请选择要处理的任务");
        }
        int updateFlag = taskManagementService.taskDispose(taskDto);
        return new Result(true,StatusCode.OK,"任务处理成功");
    }

    /**
     * 任务评价
     * @param taskDto
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "任务id", required = true,dataType = "String"),
            @ApiImplicitParam(name = "evaluate", value = "评价内容", required = true,dataType = "String")
    })
    @ApiOperation(value = "发布任务菜单查看详情任务评价", tags = "任务管理中心")
    @PostMapping("/taskEvaluate")
    public Result taskEvaluate(@ApiIgnore TaskDto taskDto){
        if (ToolUtil.isEmpty(taskDto.getId())){
            return new Result(false, StatusCode.ERROR, "请选择一个任务");
        }
        int updateFlag = taskManagementService.taskEvaluate(taskDto);
        return new Result(true, StatusCode.OK, "评价成功");
    }


    /**
     * 任务进度统计
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "工作任务菜单查看详情任务评价", tags = "任务管理中心")
    @PostMapping("/taskScheduleCount")
    public Result<TaskVo> taskScheduleCount(@ApiIgnore TaskDto taskDto){

        return null;
    }

}