package com.rpzh.controller;

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
            @ApiImplicitParam(name = "type", value = "任务类型,01:虫情巡查任务,02:翻垛任务,03:移垛任务,04:垛内甲虫预警任务,05:仓间甲虫预警任务,06:垛内氧气预警任务," +
                    "07:仓间氧气预警任务,08:垛内温度预警任务,09:仓间温度预警任务,10:垛内湿度预警任务,11:仓间湿度预警任务,12:包芯温度预警任务," +
                    "13:平均包芯高于环境预警任务,14:仓间粉螟预警任务,15:仓库检查任务,16:其他任务", required = true,dataType = "String"),
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
            @ApiImplicitParam(name = "type", value = "任务类型,01:虫情巡查任务,02:翻垛任务,03:移垛任务,04:垛内甲虫预警任务,05:仓间甲虫预警任务,06:垛内氧气预警任务,\" +\n" +
                    "                    \"07:仓间氧气预警任务,08:垛内温度预警任务,09:仓间温度预警任务,10:垛内湿度预警任务,11:仓间湿度预警任务,12:包芯温度预警任务,\" +\n" +
                    "                    \"13:平均包芯高于环境预警任务,14:仓间粉螟预警任务,15:仓库检查任务,16:其他任务", required = false,dataType = "String"),
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
     * 根据位置id和任务类型查询任务详情,搜索垛位的任务类型
     * @param
     * @return
     */
    @ApiOperation(value = "根据位置id和任务类型查询任务详情", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "stackId", value = "垛位id",required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "areaId", value = "仓间id",required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "type", value = "任务类型",required = true,dataType = "String")
    })
    @PostMapping("/findTaskDetailBySiteId")
    public Result<TaskVo> findTaskDetailBySiteId(Integer stackId,Integer areaId,String type){
        if (ToolUtil.isEmpty(stackId) && ToolUtil.isEmpty(areaId)){
            return new Result<>(false, StatusCode.ERROR, "请选择一个任务");
        }
        if (ToolUtil.isEmpty(type)){
            return new Result<>(false, StatusCode.ERROR, "任务类型不能为空");
        }
        TaskVo taskVo = taskManagementService.findTaskDetailBySiteId(stackId,areaId,type);
        return new Result<TaskVo>(true, StatusCode.OK, "查询成功", taskVo);
    }

    /**
     * 工作任务查询列表
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "工作任务菜单查询任务任务列表", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "任务类型,01:虫情巡查任务,02:翻垛任务,03:移垛任务,04:垛内甲虫预警任务,05:仓间甲虫预警任务,06:垛内氧气预警任务,\" +\n" +
                    "                    \"07:仓间氧气预警任务,08:垛内温度预警任务,09:仓间温度预警任务,10:垛内湿度预警任务,11:仓间湿度预警任务,12:包芯温度预警任务,\" +\n" +
                    "                    \"13:平均包芯高于环境预警任务,14:仓间粉螟预警任务,15:仓库检查任务,16:其他任务", required = false,dataType = "String"),
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
        return new Result<>(true,StatusCode.OK,"查询成功",pageInfo);
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
     * 虫情巡查任务处理
     * @param pestData 虫情数据
     * @param dealResult 处理结果
     * @return
     */
    @ApiOperation(value = "工作任务菜单虫情巡查任务处理", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "任务id", required = true,dataType = "String"),
            @ApiImplicitParam(name = "pestData", value = "虫情数据案例:pestBoardId:虫情板,gatherSite采集位置,beetle:甲虫,mealworm:粉虫:[{\"pestBoardId\":\"123FAA1111111111\",\"gatherSite\":\"宁波仓库1楼二仓间\",\"beetle\":12,\"mealworm\":18},{ \"pestBoardId\":\"1231AA\", \"gatherSite\":\"宁波仓库1楼二仓间\",\"beetle\":11, \"mealworm\":38 }]", required = true,dataType = "String"),
            @ApiImplicitParam(name = "dealResult", value = "处理结果", required = true,dataType = "String")
    })
    @PostMapping("/pestTaskDeal")
    public Result pestTaskDeal(@ApiIgnore String pestData,@ApiIgnore String dealResult,@ApiIgnore String id){
        if (ToolUtil.isEmpty(pestData)){
            return new Result(false, StatusCode.ERROR, "请输入虫情数据");
        }
        if (ToolUtil.isEmpty(id)){
            return new Result(false, StatusCode.ERROR, "请选择一个任务");
        }
        int insertFlag = taskManagementService.pestTaskDeal(pestData,dealResult,id);
        return new Result(true, StatusCode.OK, "处理成功");
    }

    /**
     * 一键虫情上报
     * @param siteRoute
     * @return
     */
    @ApiOperation(value = "工作任务菜单虫情巡查任务中一键虫情上报", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "siteRoute", value = "任务地址路径", required = true,dataType = "String"),
            @ApiImplicitParam(name = "id", value = "任务地址id", required = true,dataType = "String"),
            @ApiImplicitParam(name = "taskAddress", value = "任务地址名称", required = true,dataType = "String")
    })
    @PostMapping("/oneButtonPestReport")
    public Result oneButtonPestReport(@ApiIgnore String siteRoute,@ApiIgnore String taskAddress,@ApiIgnore String id){
        if (ToolUtil.isEmpty(siteRoute)){
            return new Result(false, StatusCode.ERROR, "位置路径不能为空");
        }
        if (ToolUtil.isEmpty(taskAddress)){
            return new Result(false, StatusCode.ERROR, "任务地址名称不能为空");
        }
        if (ToolUtil.isEmpty(id)){
            return new Result(false, StatusCode.ERROR, "当前位置id不能为空");
        }
        //拼接id
        siteRoute = SiteRouteUtil.route(siteRoute, id);
        int insertFlag = taskManagementService.oneButtonPestReport(siteRoute,taskAddress);
        return new Result(true,StatusCode.OK,"一键上报成功");
    }


    /**
     * 移垛任务处理
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "工作任务菜单移垛任务处理", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "siteRoute", value = "任务目标地址父路径,移垛之后的位置路径", required = true,dataType = "String"),
            @ApiImplicitParam(name = "siteName", value = "任务目标地址名称", required = true,dataType = "String"),
            @ApiImplicitParam(name = "dealResult", value = "处理结果", required = true,dataType = "String"),
            @ApiImplicitParam(name = "id", value = "任务id", required = true,dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "任务目标地址Id", required = true,dataType = "integer"),
            @ApiImplicitParam(name = "siteDate", value = "移垛数据:[{\"nodeId\":\"88FFDFD29\",\"x\":15,\"y\":112,\"z\":132},{\"nodeId\":\"88FFDFD30\", \"x\":11, \"y\":122, \"z\":132}]", required = true,dataType = "String")
    })
    @PostMapping("/moveStackDeal")
    public Result moveStackDeal(@ApiIgnore TaskDto taskDto){
        if (ToolUtil.isEmpty(taskDto.getSiteRoute())){
            return new Result(false, StatusCode.ERROR, "目标地址路径不能为空");
        }
        if (ToolUtil.isEmpty(taskDto.getSiteName())){
            return new Result(false, StatusCode.ERROR, "目标地址名称不能为空");
        }
        if (ToolUtil.isEmpty(taskDto.getSiteId())){
            return new Result(false, StatusCode.ERROR, "目标地址id不能为空");
        }
        if (ToolUtil.isEmpty(taskDto.getId())){
            return new Result(false, StatusCode.ERROR, "任务id不能为空");
        }
        int updateFlag = taskManagementService.moveStackDeal(taskDto);
        return new Result(true,StatusCode.OK,"移垛成功");
    }


    /**
     * 翻垛任务处理
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "工作任务菜单翻垛任务处理", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dealResult", value = "处理结果", required = true,dataType = "String"),
            @ApiImplicitParam(name = "id", value = "任务id", required = true,dataType = "String"),
            @ApiImplicitParam(name = "siteDate", value = "翻垛数据", required = true,dataType = "String")
    })
    @PostMapping("/flipStackDeal")
    public Result flipStackDeal(@ApiIgnore TaskDto taskDto){
        if (ToolUtil.isEmpty(taskDto.getId())){
            return new Result(false, StatusCode.ERROR, "任务id不能为空");
        }
        if (ToolUtil.isEmpty(taskDto.getSiteDate())){
            return new Result(false, StatusCode.ERROR, "翻垛数据不能为空");
        }
        int updateFlag = taskManagementService.flipStackDeal(taskDto);
        return new Result(true, StatusCode.OK, "翻垛成功");
    }



    /**
     * 工作任务菜单移垛和翻垛任务处理查询当前垛位下所有的设备
     * @param siteRoute
     * @return
     */
    @ApiOperation(value = "工作任务菜单移垛和翻垛任务处理查询当前垛位下所有的设备", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "siteRoute", value = "任务地址路径", required = true,dataType = "String")
    })
    @PostMapping("/findAllNodeByStack")
    public Result<TaskVo> findAllNodeByStack(@ApiIgnore String siteRoute){
        if (ToolUtil.isEmpty(siteRoute)){
            return new Result(false, StatusCode.ERROR, "任务地址路径不能为空");
        }
        List<TaskVo> taskVoList = taskManagementService.findAllNodeByStack(siteRoute);
        return new Result<TaskVo>(true,StatusCode.OK,"查询成功",taskVoList);
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
     * 根据任务主键Id查询任务图片路径信息
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "根据任务主键Id查询任务图片路径信息", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "任务id", required = true,dataType = "String")
    })
    @PostMapping("/findPicUrlByTaskId")
    public Result<TaskVo> findPicUrlByTaskId(@ApiIgnore TaskDto taskDto){
        if (ToolUtil.isEmpty(taskDto.getId())){
            return new Result<>(false, StatusCode.ERROR, "请选择一个任务");
        }
        List<TaskVo> list = taskManagementService.findPicMes(taskDto.getId());
        return new Result<TaskVo>(true, StatusCode.OK, "查询成功",list);
    }

    /**
     * 任务进度统计
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "发布任务菜单任务进度统计", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deteType", value = "统计类型:Y:年统计,M:月统计,D:日统计", required = true,dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteRoute", value = "任务目标地址父路径", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "任务目标地址Id", required = false,dataType = "integer"),
            @ApiImplicitParam(name = "principal", value = "负责人账号", required = false,dataType = "String"),
            @ApiImplicitParam(name = "type", value = "任务类型,01:虫情巡查任务,02:翻垛任务,03:移垛任务,04:垛内甲虫预警任务,05:仓间甲虫预警任务,06:垛内氧气预警任务,\" +\n" +
                    "                    \"07:仓间氧气预警任务,08:垛内温度预警任务,09:仓间温度预警任务,10:垛内湿度预警任务,11:仓间湿度预警任务,12:包芯温度预警任务,\" +\n" +
                    "                    \"13:平均包芯高于环境预警任务,14:仓间粉螟预警任务,15:仓库检查任务,16:其他任务", required = false,dataType = "String")
    })
    @PostMapping("/taskScheduleCount")
    public Result<TaskVo> taskScheduleCount(@ApiIgnore TaskDto taskDto){
        String deteType = taskDto.getDeteType();
        if (ToolUtil.isEmpty(deteType)){
            return new Result<TaskVo>(false, StatusCode.ERROR, "统计类型不能为空");
        }
        if (!("D".equals(deteType)) && !("M".equals(deteType)) && !("Y".equals(deteType))){
            return new Result<TaskVo>(false, StatusCode.ERROR, "日期类型错误");
        }
        List<Map> taskVoList = taskManagementService.taskScheduleCount(taskDto);
        return new Result<TaskVo>(true,StatusCode.OK,"查询任务统计成功",taskVoList);
    }

    /**
     * 任务进度统计
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "工作任务菜单任务进度统计", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deteType", value = "统计类型:Y:年统计,M:月统计,D:日统计", required = true,dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteRoute", value = "任务目标地址父路径", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "任务目标地址Id", required = false,dataType = "integer"),
            @ApiImplicitParam(name = "type", value = "任务类型,01:虫情巡查任务,02:翻垛任务,03:移垛任务,04:垛内甲虫预警任务,05:仓间甲虫预警任务,06:垛内氧气预警任务,\" +\n" +
                    "                    \"07:仓间氧气预警任务,08:垛内温度预警任务,09:仓间温度预警任务,10:垛内湿度预警任务,11:仓间湿度预警任务,12:包芯温度预警任务,\" +\n" +
                    "                    \"13:平均包芯高于环境预警任务,14:仓间粉螟预警任务,15:仓库检查任务,16:其他任务", required = false,dataType = "String")
    })
    @PostMapping("/taskScheduleCountByOne")
    public Result<TaskVo> taskScheduleCountByOne(@ApiIgnore TaskDto taskDto){
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        //工作任务自己的任务
        taskDto.setPrincipal(username);
        String deteType = taskDto.getDeteType();
        if (ToolUtil.isEmpty(deteType)){
            return new Result<TaskVo>(false, StatusCode.ERROR, "统计类型不能为空");
        }
        if (!("D".equals(deteType)) && !("M".equals(deteType)) && !("Y".equals(deteType))){
            return new Result<TaskVo>(false, StatusCode.ERROR, "日期类型错误");
        }
        List<Map> taskVoList = taskManagementService.taskScheduleCount(taskDto);
        return new Result<TaskVo>(true,StatusCode.OK,"查询任务统计成功",taskVoList);
    }



    /**
     * 发布任务菜单任务统计任务量趋势统计
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "发布任务菜单任务统计任务量趋势统计", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteRoute", value = "任务目标地址父路径", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "任务目标地址Id", required = false,dataType = "integer"),
            @ApiImplicitParam(name = "principal", value = "负责人账号", required = false,dataType = "String")
    })
    @PostMapping("/taskCountTrend")
    public Result<TaskVo> taskCountTrend(@ApiIgnore TaskDto taskDto){
        List<Map> taskList = taskManagementService.taskCountTrend(taskDto);
        return new Result<>(true,StatusCode.OK,"查询成功",taskList);
    }

    /**
     * 发布任务菜单任务统计任务量趋势统计
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "发布任务菜单任务统计任务量趋势统计", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteRoute", value = "任务目标地址父路径", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "任务目标地址Id", required = false,dataType = "integer")
    })
    @PostMapping("/taskCountTrendByOne")
    public Result<TaskVo> taskCountTrendByOne(@ApiIgnore TaskDto taskDto){
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
       //获取用户账号
        String username = userInfo.get("username");
        taskDto.setPrincipal(username);
        List<Map> taskList = taskManagementService.taskCountTrend(taskDto);
        return new Result<>(true,StatusCode.OK,"查询成功",taskList);
    }


    /**
     * 发布任务菜单按照类型统计数量
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "发布任务菜单任务统计任务类型数量", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteRoute", value = "任务目标地址父路径", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "任务目标地址Id", required = false,dataType = "integer"),
            @ApiImplicitParam(name = "principal", value = "负责人账号", required = false,dataType = "String")
    })
    @PostMapping("/taskCountByType")
    public Result taskCountByType(@ApiIgnore TaskDto taskDto){
        Map typeCount = taskManagementService.taskCountByType(taskDto);
        return new Result(true,StatusCode.OK,"查询成功",typeCount);
    }

    /**
     * 发布任务菜单按照类型统计数量
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "发布任务菜单任务统计任务类型数量", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteRoute", value = "任务目标地址父路径", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "任务目标地址Id", required = false,dataType = "integer")
    })
    @PostMapping("/taskCountByTypeByOne")
    public Result taskCountByTypeByOne(@ApiIgnore TaskDto taskDto){
         //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        taskDto.setPrincipal(username);
        Map typeCount = taskManagementService.taskCountByType(taskDto);
        return new Result(true,StatusCode.OK,"查询成功",typeCount);
    }


    /**
     * 任务类型占比
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "发布任务菜单任务统计任务类型占比", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteRoute", value = "任务目标地址父路径", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "任务目标地址Id", required = false,dataType = "integer"),
            @ApiImplicitParam(name = "principal", value = "负责人账号", required = false,dataType = "String")
    })
    @PostMapping("/taskTypePercent")
    public Result taskTypePercent(@ApiIgnore TaskDto taskDto){
        Map typeCount = taskManagementService.taskCountByType(taskDto);
        //获取所有的和,所有的任务和
        int sum = typeCount.values().stream().mapToInt(s -> Integer.parseInt(s.toString())).sum();
        //保留两位小数
        DecimalFormat df = new DecimalFormat("######0.00");
        Map<String,String> map = new HashMap<>();
        //获取百分比
        double percent01 = Double.parseDouble(typeCount.get("01").toString())/sum;
        double percent02 = Double.parseDouble(typeCount.get("02").toString())/sum;
        double percent03 = Double.parseDouble(typeCount.get("03").toString())/sum;
        double percent04 = Double.parseDouble(typeCount.get("04").toString())/sum;
        double percent05 = Double.parseDouble(typeCount.get("05").toString())/sum;
        double percent06 = Double.parseDouble(typeCount.get("06").toString())/sum;
        double percent07 = Double.parseDouble(typeCount.get("07").toString())/sum;
        double percent08 = Double.parseDouble(typeCount.get("08").toString())/sum;
        double percent09 = Double.parseDouble(typeCount.get("09").toString())/sum;
        double percent10 = Double.parseDouble(typeCount.get("10").toString())/sum;
        double percent11 = Double.parseDouble(typeCount.get("11").toString())/sum;
        double percent12 = Double.parseDouble(typeCount.get("12").toString())/sum;
        double percent13 = Double.parseDouble(typeCount.get("13").toString())/sum;
        double percent14 = Double.parseDouble(typeCount.get("14").toString())/sum;
        double percent15 = Double.parseDouble(typeCount.get("15").toString())/sum;
        double percent16 = Double.parseDouble(typeCount.get("16").toString())/sum;
        //虫情巡查
        map.put("01",df.format(percent01));
        map.put("02",df.format(percent02));
        map.put("03",df.format(percent03));
        map.put("04",df.format(percent04));
        map.put("05",df.format(percent05));
        map.put("06",df.format(percent06));
        map.put("07",df.format(percent07));
        map.put("08",df.format(percent08));
        map.put("09",df.format(percent09));
        map.put("10",df.format(percent10));
        map.put("11",df.format(percent11));
        map.put("12",df.format(percent12));
        map.put("13",df.format(percent13));
        map.put("14",df.format(percent14));
        map.put("15",df.format(percent15));
        map.put("16",df.format(percent16));
        return new Result(true,StatusCode.OK,"查询成功",map);
    }


    /**
     * 任务类型占比
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "工作任务菜单任务统计任务类型占比", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteRoute", value = "任务目标地址父路径", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "任务目标地址Id", required = false,dataType = "integer")
    })
    @PostMapping("/taskTypePercentByOne")
    public Result taskTypePercentByOne(@ApiIgnore TaskDto taskDto){
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
       //获取用户账号
        String username = userInfo.get("username");
        taskDto.setPrincipal(username);
        Map typeCount = taskManagementService.taskCountByType(taskDto);
        //获取所有的和,所有的任务和
        int sum = typeCount.values().stream().mapToInt(s -> Integer.parseInt(s.toString())).sum();
        //保留两位小数
        DecimalFormat df = new DecimalFormat("######0.00");
        Map<String,String> map = new HashMap<>();
        //获取百分比
        double percent01 = Double.parseDouble(typeCount.get("01").toString())/sum;
        double percent02 = Double.parseDouble(typeCount.get("02").toString())/sum;
        double percent03 = Double.parseDouble(typeCount.get("03").toString())/sum;
        double percent04 = Double.parseDouble(typeCount.get("04").toString())/sum;
        double percent05 = Double.parseDouble(typeCount.get("05").toString())/sum;
        double percent06 = Double.parseDouble(typeCount.get("06").toString())/sum;
        double percent07 = Double.parseDouble(typeCount.get("07").toString())/sum;
        double percent08 = Double.parseDouble(typeCount.get("08").toString())/sum;
        double percent09 = Double.parseDouble(typeCount.get("09").toString())/sum;
        double percent10 = Double.parseDouble(typeCount.get("10").toString())/sum;
        double percent11 = Double.parseDouble(typeCount.get("11").toString())/sum;
        double percent12 = Double.parseDouble(typeCount.get("12").toString())/sum;
        double percent13 = Double.parseDouble(typeCount.get("13").toString())/sum;
        double percent14 = Double.parseDouble(typeCount.get("14").toString())/sum;
        double percent15 = Double.parseDouble(typeCount.get("15").toString())/sum;
        double percent16 = Double.parseDouble(typeCount.get("16").toString())/sum;
        //虫情巡查
        map.put("01",df.format(percent01));
        map.put("02",df.format(percent02));
        map.put("03",df.format(percent03));
        map.put("04",df.format(percent04));
        map.put("05",df.format(percent05));
        map.put("06",df.format(percent06));
        map.put("07",df.format(percent07));
        map.put("08",df.format(percent08));
        map.put("09",df.format(percent09));
        map.put("10",df.format(percent10));
        map.put("11",df.format(percent11));
        map.put("12",df.format(percent12));
        map.put("13",df.format(percent13));
        map.put("14",df.format(percent14));
        map.put("15",df.format(percent15));
        map.put("16",df.format(percent16));
        return new Result(true,StatusCode.OK,"查询成功",map);
    }


    /**
     * 任务平均完成时间
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "发布任务菜单任务统计任务平均完成时间", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteRoute", value = "任务目标地址父路径", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "任务目标地址Id", required = false,dataType = "integer"),
            @ApiImplicitParam(name = "principal", value = "负责人账号", required = false,dataType = "String")
    })
    @PostMapping("/taskAvlFinishTime")
    public Result taskAvlFinishTime(@ApiIgnore TaskDto taskDto){
        List<Map> mapList = taskManagementService.taskAvlFinishTime(taskDto);
        return new Result(true,StatusCode.OK,"查询成功",mapList);
    }

    /**
     * 任务平均完成时间
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "工作任务菜单任务统计任务平均完成时间", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteRoute", value = "任务目标地址父路径", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "任务目标地址Id", required = false,dataType = "integer")
    })
    @PostMapping("/taskAvlFinishTimeByOne")
    public Result taskAvlFinishTimeByOne(@ApiIgnore TaskDto taskDto){
         //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        taskDto.setPrincipal(username);
        List<Map> mapList = taskManagementService.taskAvlFinishTime(taskDto);
        return new Result(true,StatusCode.OK,"查询成功",mapList);
    }



}