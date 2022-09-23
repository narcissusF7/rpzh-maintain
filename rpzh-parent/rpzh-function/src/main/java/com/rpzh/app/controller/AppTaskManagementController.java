package com.rpzh.app.controller;

import cn.hutool.core.date.DateUtil;
import com.github.pagehelper.PageInfo;
import com.rpzh.dto.TaskDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.TaskManagementService;
import com.rpzh.util.SiteRouteUtil;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.TaskVo;
//import com.sun.org.apache.regexp.internal.RE;
import com.sun.xml.bind.v2.model.core.ID;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import sun.net.www.protocol.http.HttpURLConnection;

import java.text.DecimalFormat;
import java.util.*;

/**
 * 养护任务中心
 * @Author liubc
 * @create 2022-05-30 11:31
 * @Description: good good study,day day up
 * http://192.168.3.5:9001/api/maintain/warehouse/locating
 */
@RestController
@RequestMapping("/maintain/appTaskmanager")
@CrossOrigin
@Api(value = "app任务管理中心", tags = "app任务管理中心")
public class AppTaskManagementController {

    @Autowired
    private TaskManagementService taskManagementService;


    /**
     * 去处理中的任务详情/任务管理的查询任务的详情
     * @param taskId
     * @return
     */
    @ApiOperation(value = "发布任务菜单查询任务详情", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "任务主键id",required = true,dataType = "Long")
    })
    @PostMapping("/findAppTaskDetail")
    public Result<TaskVo> findTaskDetail(Long taskId){
        if (ToolUtil.isEmpty(taskId)){
            return new Result<>(false, StatusCode.ERROR, "请选择一个任务");
        }
        TaskVo taskVo = taskManagementService.findTaskDetail(taskId);
        return new Result<TaskVo>(true, StatusCode.OK, "查询成功", taskVo);
    }


    /**
     * 任务处理
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "工作任务菜单任务处理", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "任务id", required = true,dataType = "String"),
            @ApiImplicitParam(name = "dealMethod", value = "处理措施", required = true,dataType = "String"),
            @ApiImplicitParam(name = "dealResult", value = "处理结果", required = true,dataType = "String")
    })
    @PostMapping("/appTaskDispose")
    public Result taskDispose(@ApiIgnore TaskDto taskDto){

        String id = taskDto.getTaskId();
        if (ToolUtil.isEmpty(id)){
            return new Result(false, StatusCode.ERROR, "请选择要处理的任务");
        }
        taskDto.setId(id);
        int updateFlag = taskManagementService.taskDispose(taskDto);
        return new Result(true,StatusCode.OK,"任务处理成功");
    }

    /**
     * 根据任务主键Id查询任务图片路径信息
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "上传图片后回显,根据任务主键Id查询任务图片路径信息", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "任务id", required = true,dataType = "String")
    })
    @PostMapping("/findAppPicUrlByTaskId")
    public Result<TaskVo> findPicUrlByTaskId(String taskId){
        if (ToolUtil.isEmpty(taskId)){
            return new Result<>(false, StatusCode.ERROR, "请选择一个任务");
        }
        List<TaskVo> list = taskManagementService.findPicMes(taskId);
        return new Result<TaskVo>(true, StatusCode.OK, "查询成功",list);
    }

}