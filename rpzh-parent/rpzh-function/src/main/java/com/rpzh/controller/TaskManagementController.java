package com.rpzh.controller;

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
            @ApiImplicitParam(name = "targetSiteName", value = "移垛任务目标位置名称", required = true,dataType = "String"),
            @ApiImplicitParam(name = "targetSiteRoute", value = "移垛任务目标位置路径", required = true,dataType = "String"),
            @ApiImplicitParam(name = "targetSiteId", value = "移垛任务目标位置路径id", required = true,dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "当前位置id", required = true,dataType = "String"),
            @ApiImplicitParam(name = "jwt", value = "令牌",required = true,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间",required = true,dataType = "String"),
            @ApiImplicitParam(name = "detail", value = "任务详情",required = true,dataType = "String"),
            @ApiImplicitParam(name = "pushWay", value = "推送方式,短信:M,App:A,全部:Q",required = true,dataType = "String")
    })
    @PreAuthorize("hasAnyAuthority('f:t:add')")
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
        //获取结束时间
        String endTime = taskDto.getEndTime();
        //昨天的时间,任务日期只能选择今天之后的日期
        String yesterday = DateUtil.yesterday().toString();
        int i = endTime.compareTo(yesterday);
        if (i < 0){
            return new Result(false, StatusCode.ERROR, "结束时间不能小于当前时间");
        }
        if (ToolUtil.isEmpty(taskDto.getPushWay())){
            return new Result(false, StatusCode.ERROR, "推送方式不能为空");
        }
        if (!("M".equals(taskDto.getPushWay()) || "A".equals(taskDto.getPushWay()) || "Q".equals(taskDto.getPushWay()))){
            return new Result(false, StatusCode.ERROR, "推送方式参数错误");
        }
        if (ToolUtil.isEmpty(taskDto.getDetail())){
            return new Result(false, StatusCode.ERROR, "任务详情不能为空");
        }
        if ("03".equals(taskDto.getType())){
            //移垛任务下目标任务路径和名称不能为空
            if (ToolUtil.isEmpty(taskDto.getTargetSiteName()) || ToolUtil.isEmpty(taskDto.getTargetSiteRoute()) || ToolUtil.isEmpty(taskDto.getTargetSiteId())){
                return new Result(false,StatusCode.ERROR,"移垛任务中目标位置,目标路径,目标位置id不能为空");
            }
            String route = SiteRouteUtil.route(taskDto.getTargetSiteRoute(), taskDto.getTargetSiteId());
            if (!(route.contains("05"))){
                return new Result(false, StatusCode.ERROR, "请选择到垛位");
            }
            taskDto.setTargetSiteRoute(route);
            int warmimgCount = taskManagementService.findWarmimgCountBySiteId(Integer.parseInt(taskDto.getSiteId()));
            if(warmimgCount>0){
                return new Result(false, StatusCode.ERROR, "该垛位有预警任务,请处理后再创建移垛任务");
            }
            int maintainCount = taskManagementService.findMaintainCountBySiteId(Integer.parseInt(taskDto.getTargetSiteId()));
            if(maintainCount>0){
                return new Result(false, StatusCode.ERROR, "目标垛位有养护计划,请选择一个没有养护计划的垛位,再创建移垛任务");
            }
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
            @ApiImplicitParam(name = "siteRoute", value = "位置路径父路径route", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "位置路径当前id", required = false,dataType = "String"),
            @ApiImplicitParam(name = "jwt", value = "令牌",required = true,dataType = "String"),
            @ApiImplicitParam(name = "endStartTime", value = "结束开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间",required = false,dataType = "String"),
            @ApiImplicitParam(name = "status", value = "任务状态,0:未完成,1:完成,2:逾期,3:处理中",required = false,dataType = "String"),
            @ApiImplicitParam(name = "page", value = "分页开始页",required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "分页每页显示",required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "principal", value = "负责人",required = false,dataType = "String")
    })
    @PostMapping("/findTask")
    public Result<PageInfo<TaskVo>> findTask(@ApiIgnore TaskDto taskDto){
        //如果查询位置地址不为空,查询精确位置
        if (ToolUtil.isNotEmpty(taskDto.getSiteRoute()) && ToolUtil.isNotEmpty(taskDto.getSiteId())){
            //拼接获取真实的路径
            String route = SiteRouteUtil.route(taskDto.getSiteRoute(), taskDto.getSiteId());
            taskDto.setSiteRoute(route);
        }
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
    @PreAuthorize("hasAnyAuthority('f:t:del')")
    public Result delTask(@ApiIgnore Long[] ids){
        if (ToolUtil.isEmpty(ids)){
            return new Result(false, StatusCode.ERROR, "请选择一个任务");
        }
        //ljj 判断是否是系统生成的任务
        List<String> list = taskManagementService.findSystemType(ids);

        if (list.size()>0){
            String msg ="任务编号";
            for (String s : list) {
                msg+=(s+" ");
            }
            msg+="是系统生成的任务，无法删除";
            return new Result(false, StatusCode.ERROR, msg);
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
    //@ApiOperation(value = "工作任务菜单查询任务任务列表", tags = "任务管理中心")
    @ApiOperation(value = "APP任务列表首页,未处理,处理中,已处理", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "任务类型,01:虫情巡查任务,02:翻垛任务,03:移垛任务,04:垛内甲虫预警任务,05:仓间甲虫预警任务,06:垛内氧气预警任务,\" +\n" +
                    "                    \"07:仓间氧气预警任务,08:垛内温度预警任务,09:仓间温度预警任务,10:垛内湿度预警任务,11:仓间湿度预警任务,12:包芯温度预警任务,\" +\n" +
                    "                    \"13:平均包芯高于环境预警任务,14:仓间粉螟预警任务,15:仓库检查任务,16:其他任务", required = false,dataType = "String"),
            @ApiImplicitParam(name = "taskNum", value = "任务编号", required = false,dataType = "String"),
            @ApiImplicitParam(name = "jwt", value = "令牌",required = true,dataType = "String"),
            @ApiImplicitParam(name = "endStartTime", value = "结束开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间",required = false,dataType = "String"),
            @ApiImplicitParam(name = "status", value = "任务状态,0:未完成,1:完成,2:逾期",required = false,dataType = "String"),
            @ApiImplicitParam(name = "createdUser", value = "创建人账号",required = false,dataType = "String"),
            @ApiImplicitParam(name = "page", value = "分页开始页",required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "分页每页显示",required = true,dataType = "Integer")
    })
    @PostMapping("/findTaskByUser")
    public Result<PageInfo<TaskVo>> findTaskByUser(@ApiIgnore TaskDto taskDto){
        //如果查询位置地址不为空,查询精确位置
        if (ToolUtil.isNotEmpty(taskDto.getSiteRoute()) && ToolUtil.isNotEmpty(taskDto.getSiteId())){
            //拼接获取真实的路径
            String route = SiteRouteUtil.route(taskDto.getSiteRoute(), taskDto.getSiteId());
            taskDto.setSiteRoute(route);
        }
        PageInfo<TaskVo> pageInfo = taskManagementService.findTaskByUser(taskDto);
        return new Result<>(true,StatusCode.OK,"查询成功",pageInfo);
    }


    /**
     * App端我发起的任务
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "APP任务列表首页,我发起的任务", tags = "任务管理中心")
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
    @PostMapping("/findTeskByMyCreate")
    public Result<PageInfo<TaskVo>> findTeskByMyCreate(@ApiIgnore TaskDto taskDto){
        PageInfo<TaskVo> pageInfo = taskManagementService.findTeskByMyCreate(taskDto);
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
    @PreAuthorize("hasAnyAuthority('f:t:td')")
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
    @PreAuthorize("hasAnyAuthority('f:t:td')")
    public Result pestTaskDeal(@ApiIgnore String pestData,@ApiIgnore String dealResult,@ApiIgnore String id,String siteRoute){
        if (ToolUtil.isEmpty(pestData)){
            return new Result(false, StatusCode.ERROR, "请输入虫情数据");
        }
        if (ToolUtil.isEmpty(id)){
            return new Result(false, StatusCode.ERROR, "请选择一个任务");
        }
        int insertFlag = taskManagementService.pestTaskDeal(pestData,dealResult,id,siteRoute);
        return new Result(true, StatusCode.OK, "处理成功");
    }


    /**
     * 虫情任务处理提交的虫数量返回
     * @param
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "任务id", required = true,dataType = "String")
    })
    @PostMapping("/pestCountDetail")
    public Result pestCountDetail(@ApiIgnore String id){
        if (ToolUtil.isEmpty(id)){
            return new Result(false, StatusCode.ERROR, "位置路径不能为空");
        }
        List<Map> mapList = taskManagementService.pestCountDetail(id);
        return new Result(true, StatusCode.OK, "查询成功", mapList);
    }


    /**
     * 一键虫情上报
     * @param siteRoute
     * @return
     */
    @ApiOperation(value = "工作任务菜单虫情巡查任务中一键虫情上报", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "siteRoute", value = "任务地址路径", required = true,dataType = "String"),
            @ApiImplicitParam(name = "taskAddress", value = "任务地址名称", required = true,dataType = "String")
    })
    @PostMapping("/oneButtonPestReport")
    public Result oneButtonPestReport(@ApiIgnore String siteRoute,@ApiIgnore String taskAddress){
        if (ToolUtil.isEmpty(siteRoute)){
            return new Result(false, StatusCode.ERROR, "位置路径不能为空");
        }
        if (ToolUtil.isEmpty(taskAddress)){
            return new Result(false, StatusCode.ERROR, "任务地址名称不能为空");
        }
        int insertFlag = taskManagementService.oneButtonPestReport(siteRoute,taskAddress);
        return new Result(true,StatusCode.OK,"一键上报成功");
    }



    @ApiOperation(value = "获取垛位下所有的虫情板", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "siteRoute", value = "位置路径", required = true,dataType = "String")
    })
    @PostMapping("/findPestBoardByRoute")
    public Result findPestBoardByRoute(@ApiIgnore String siteRoute){
        List<String> pestBoardList = taskManagementService.findPestBoardByRoute(siteRoute);
        return new Result(true,StatusCode.OK,"查询成功",pestBoardList);
    }


    /**
     * 移垛任务处理
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "工作任务菜单移垛任务处理", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "siteRoute", value = "任务目标地址父路径,移垛之后的位置路径", required = true,dataType = "String"),
            @ApiImplicitParam(name = "siteRouteBefore", value = "移垛之前的位置路径", required = true,dataType = "String"),
            @ApiImplicitParam(name = "siteName", value = "任务目标地址名称", required = true,dataType = "String"),
            @ApiImplicitParam(name = "dealResult", value = "处理结果", required = true,dataType = "String"),
            @ApiImplicitParam(name = "id", value = "任务id", required = true,dataType = "String"),
            @ApiImplicitParam(name = "caseNo", value = "箱号", required = true,dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "任务目标地址Id", required = true,dataType = "integer"),
            @ApiImplicitParam(name = "siteDate", value = "移垛数据:[{\"nodeId\":\"88FFDFD29\",\"x\":15,\"y\":112,\"z\":132},{\"nodeId\":\"88FFDFD30\", \"x\":11, \"y\":122, \"z\":132}]", required = true,dataType = "String")
    })
    @PostMapping("/moveStackDeal")
    @PreAuthorize("hasAnyAuthority('f:t:td')")
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
        if (ToolUtil.isEmpty(taskDto.getSiteRouteBefore())){
            return new Result(false, StatusCode.ERROR, "移垛之前的路径不能为空");
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
    @PreAuthorize("hasAnyAuthority('f:t:td')")
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
    @PreAuthorize("hasAnyAuthority('f:t:tc')")
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
    @PreAuthorize("hasAnyAuthority('f:t:tco')")
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
    @PreAuthorize("hasAnyAuthority('f:t:tc')")
    public Result<TaskVo> taskCountTrend(@ApiIgnore TaskDto taskDto){
        List<Map> taskList = taskManagementService.taskCountTrend(taskDto);
        return new Result<>(true,StatusCode.OK,"查询成功",taskList);
    }

    /**
     * 发布任务菜单任务统计任务量趋势统计
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "工作任务菜单任务统计任务量趋势统计", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteRoute", value = "任务目标地址父路径", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "任务目标地址Id", required = false,dataType = "integer")
    })
    @PostMapping("/taskCountTrendByOne")
    @PreAuthorize("hasAnyAuthority('f:t:tco')")
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
    @PreAuthorize("hasAnyAuthority('f:t:tc')")
    public Result taskCountByType(@ApiIgnore TaskDto taskDto){
        List<Map> typeCount = taskManagementService.taskCountByType(taskDto);
        return new Result(true,StatusCode.OK,"查询成功",typeCount);
    }

    /**
     * 发布任务菜单按照类型统计数量
     * @param taskDto
     * @return
     */
    @ApiOperation(value = "工作任务菜单任务统计任务类型数量", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteRoute", value = "任务目标地址父路径", required = false,dataType = "String"),
            @ApiImplicitParam(name = "siteId", value = "任务目标地址Id", required = false,dataType = "integer")
    })
    @PostMapping("/taskCountByTypeByOne")
    @PreAuthorize("hasAnyAuthority('f:t:tco')")
    public Result taskCountByTypeByOne(@ApiIgnore TaskDto taskDto){
         //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        taskDto.setPrincipal(username);
        List<Map> typeCount = taskManagementService.taskCountByType(taskDto);
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
    @PreAuthorize("hasAnyAuthority('f:t:tc')")
    public Result taskTypePercent(@ApiIgnore TaskDto taskDto){
        Map typeCount = taskManagementService.taskCountByTypeMap(taskDto);
        //获取所有的和,所有的任务和
        int sum = typeCount.values().stream().mapToInt(s -> Integer.parseInt(s.toString())).sum();
        //防止出现0/0情况
        if (sum == 0){
            sum = 1;
        }
        //保留两位小数
        DecimalFormat df = new DecimalFormat("######0.00");
        //Map<String,String> map = new HashMap<>();
        List<Map> mapList = new ArrayList<>();
        //获取百分比
        double percent01 = Double.parseDouble(typeCount.get("01").toString())/sum;
        Map map01 = new HashMap();
        map01.put("type", "虫情巡查任务");
        map01.put("percent",percent01*100);
        mapList.add(map01);
        double percent02 = Double.parseDouble(typeCount.get("02").toString())/sum;
        Map map02 = new HashMap();
        map02.put("type", "翻垛任务");
        map02.put("percent",percent02*100);
        mapList.add(map02);
        double percent03 = Double.parseDouble(typeCount.get("03").toString())/sum;
        Map map03 = new HashMap();
        map03.put("type", "移垛任务");
        map03.put("percent",percent03*100);
        mapList.add(map03);
        double percent04 = Double.parseDouble(typeCount.get("04").toString())/sum;
        Map map04 = new HashMap();
        map04.put("type", "垛内甲虫预警任务");
        map04.put("percent",percent04*100);
        mapList.add(map04);
        double percent05 = Double.parseDouble(typeCount.get("05").toString())/sum;
        Map map05 = new HashMap();
        map05.put("type", "仓间甲虫预警任务");
        map05.put("percent",percent05*100);
        mapList.add(map05);
        double percent06 = Double.parseDouble(typeCount.get("06").toString())/sum;
        Map map06 = new HashMap();
        map06.put("type", "垛内氧气预警任务");
        map06.put("percent",percent06*100);
        mapList.add(map06);
        double percent07 = Double.parseDouble(typeCount.get("07").toString())/sum;
        Map map07 = new HashMap();
        map07.put("type", "仓间氧气预警任务");
        map07.put("percent",percent07*100);
        mapList.add(map07);
        double percent08 = Double.parseDouble(typeCount.get("08").toString())/sum;
        Map map08 = new HashMap();
        map08.put("type", "垛内温度预警任务");
        map08.put("percent",percent08*100);
        mapList.add(map08);
        double percent09 = Double.parseDouble(typeCount.get("09").toString())/sum;
        Map map09 = new HashMap();
        map09.put("type", "仓间温度预警任务");
        map09.put("percent",percent09*100);
        mapList.add(map09);
        double percent10 = Double.parseDouble(typeCount.get("10").toString())/sum;
        Map map10 = new HashMap();
        map10.put("type", "垛内湿度预警任务");
        map10.put("percent",percent10*100);
        mapList.add(map10);
        double percent11 = Double.parseDouble(typeCount.get("11").toString())/sum;
        Map map11 = new HashMap();
        map11.put("type", "仓间湿度预警任务");
        map11.put("percent",percent11*100);
        mapList.add(map11);
        double percent12 = Double.parseDouble(typeCount.get("12").toString())/sum;
        Map map12 = new HashMap();
        map12.put("type", "包芯温度预警任务");
        map12.put("percent",percent12*100);
        mapList.add(map12);
        double percent13 = Double.parseDouble(typeCount.get("13").toString())/sum;
        Map map13 = new HashMap();
        map13.put("type", "平均包芯高于环境预警任务");
        map13.put("percent",percent13*100);
        mapList.add(map13);
        double percent19 = Double.parseDouble(typeCount.get("19").toString())/sum;
        Map map19 = new HashMap();
        map19.put("type", "平均包芯低于环境预警任务");
        map19.put("percent",percent19*100);
        mapList.add(map19);
        double percent14 = Double.parseDouble(typeCount.get("14").toString())/sum;
        Map map14 = new HashMap();
        map14.put("type", "仓间粉螟预警任务");
        map14.put("percent",percent14*100);
        mapList.add(map14);
        double percent15 = Double.parseDouble(typeCount.get("15").toString())/sum;
        Map map15 = new HashMap();
        map15.put("type", "仓库检查任务");
        map15.put("percent",percent15*100);
        mapList.add(map15);
        double percent16 = Double.parseDouble(typeCount.get("16").toString())/sum;
        Map map16 = new HashMap();
        map16.put("type", "其他任务");
        map16.put("percent",percent16*100);
        mapList.add(map16);
        Map mapCount = new HashMap();
        //重新计算总任务
        int sum1 = typeCount.values().stream().mapToInt(s -> Integer.parseInt(s.toString())).sum();
        mapCount.put("type", "总任务");
        mapCount.put("count", sum1);
        List allData = new ArrayList();
        allData.add(mapList);
        allData.add(mapCount);
        return new Result(true,StatusCode.OK,"查询成功",allData);
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
    @PreAuthorize("hasAnyAuthority('f:t:tco')")
    public Result taskTypePercentByOne(@ApiIgnore TaskDto taskDto){
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
       //获取用户账号
        String username = userInfo.get("username");
        taskDto.setPrincipal(username);
        Map typeCount = taskManagementService.taskCountByTypeMap(taskDto);
        //获取所有的和,所有的任务和
        int sum = typeCount.values().stream().mapToInt(s -> Integer.parseInt(s.toString())).sum();
        //防止出现0/0情况
        if (sum == 0){
            sum = 1;
        }
        //保留两位小数
        DecimalFormat df = new DecimalFormat("######0.00");
        Map<String,String> map = new HashMap<>();
        List<Map> mapList = new ArrayList<>();
        //获取百分比
        double percent01 = Double.parseDouble(typeCount.get("01").toString())/sum;
        Map map01 = new HashMap();
        map01.put("type", "虫情巡查任务");
        map01.put("percent",percent01*100);
        mapList.add(map01);
        double percent02 = Double.parseDouble(typeCount.get("02").toString())/sum;
        Map map02 = new HashMap();
        map02.put("type", "翻垛任务");
        map02.put("percent",percent02*100);
        mapList.add(map02);
        double percent03 = Double.parseDouble(typeCount.get("03").toString())/sum;
        Map map03 = new HashMap();
        map03.put("type", "移垛任务");
        map03.put("percent",percent03*100);
        mapList.add(map03);
        double percent04 = Double.parseDouble(typeCount.get("04").toString())/sum;
        Map map04 = new HashMap();
        map04.put("type", "垛内甲虫预警任务");
        map04.put("percent",percent04*100);
        mapList.add(map04);
        double percent05 = Double.parseDouble(typeCount.get("05").toString())/sum;
        Map map05 = new HashMap();
        map05.put("type", "仓间甲虫预警任务");
        map05.put("percent",percent05*100);
        mapList.add(map05);
        double percent06 = Double.parseDouble(typeCount.get("06").toString())/sum;
        Map map06 = new HashMap();
        map06.put("type", "垛内氧气预警任务");
        map06.put("percent",percent06*100);
        mapList.add(map06);
        double percent07 = Double.parseDouble(typeCount.get("07").toString())/sum;
        Map map07 = new HashMap();
        map07.put("type", "仓间氧气预警任务");
        map07.put("percent",percent07*100);
        mapList.add(map07);
        double percent08 = Double.parseDouble(typeCount.get("08").toString())/sum;
        Map map08 = new HashMap();
        map08.put("type", "垛内温度预警任务");
        map08.put("percent",percent08*100);
        mapList.add(map08);
        double percent09 = Double.parseDouble(typeCount.get("09").toString())/sum;
        Map map09 = new HashMap();
        map09.put("type", "仓间温度预警任务");
        map09.put("percent",percent09*100);
        mapList.add(map09);
        double percent10 = Double.parseDouble(typeCount.get("10").toString())/sum;
        Map map10 = new HashMap();
        map10.put("type", "垛内湿度预警任务");
        map10.put("percent",percent10*100);
        mapList.add(map10);
        double percent11 = Double.parseDouble(typeCount.get("11").toString())/sum;
        Map map11 = new HashMap();
        map11.put("type", "仓间湿度预警任务");
        map11.put("percent",percent11*100);
        mapList.add(map11);
        double percent12 = Double.parseDouble(typeCount.get("12").toString())/sum;
        Map map12 = new HashMap();
        map12.put("type", "包芯温度预警任务");
        map12.put("percent",percent12*100);
        mapList.add(map12);
        double percent13 = Double.parseDouble(typeCount.get("13").toString())/sum;
        Map map13 = new HashMap();
        map13.put("type", "平均包芯高于环境预警任务");
        map13.put("percent",percent13*100);
        mapList.add(map13);
        double percent19 = Double.parseDouble(typeCount.get("19").toString())/sum;
        Map map19 = new HashMap();
        map19.put("type", "平均包芯低于环境预警任务");
        map19.put("percent",percent19*100);
        mapList.add(map19);
        double percent14 = Double.parseDouble(typeCount.get("14").toString())/sum;
        Map map14 = new HashMap();
        map14.put("type", "仓间粉螟预警任务");
        map14.put("percent",percent14*100);
        mapList.add(map14);
        double percent15 = Double.parseDouble(typeCount.get("15").toString())/sum;
        Map map15 = new HashMap();
        map15.put("type", "仓库检查任务");
        map15.put("percent",percent15*100);
        mapList.add(map15);
        double percent16 = Double.parseDouble(typeCount.get("16").toString())/sum;
        Map map16 = new HashMap();
        map16.put("type", "其他任务");
        map16.put("percent",percent16*100);
        mapList.add(map16);
        Map mapCount = new HashMap();
        //重新计算任务
        int sum1 = typeCount.values().stream().mapToInt(s -> Integer.parseInt(s.toString())).sum();
        mapCount.put("type", "总任务");
        mapCount.put("count", sum1);
        List allData = new ArrayList();
        allData.add(mapList);
        allData.add(mapCount);
        return new Result(true,StatusCode.OK,"查询成功",allData);
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
    @PreAuthorize("hasAnyAuthority('f:t:tc')")
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
    @PreAuthorize("hasAnyAuthority('f:t:tco')")
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