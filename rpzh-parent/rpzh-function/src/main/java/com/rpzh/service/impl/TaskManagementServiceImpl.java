package com.rpzh.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.EarlyWarningDao;
import com.rpzh.dao.TaskManagementDao;
import com.rpzh.dao.WarehouseDao;
import com.rpzh.dto.TaskDto;
import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.WarehouseNodeType;
import com.rpzh.service.PestMonitoringService;
import com.rpzh.service.TaskManagementService;
import com.rpzh.util.SiteRouteUtil;
import com.rpzh.util.TimeUtils;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.StackVo;
import com.rpzh.vo.TaskVo;
import com.rpzh.vo.WareAreaVo;
import com.sun.xml.bind.v2.model.core.ID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.SQLOutput;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务管理中心
 *
 * @Author liubc
 * @create 2022-05-30 11:33
 * @Description: good good study,day day up
 */
@Service
@Slf4j
public class TaskManagementServiceImpl implements TaskManagementService {

    @Autowired
    private TaskManagementDao taskManagementDao;

    @Resource
    private WarehouseDao warehouseDao;

    @Resource
    private EarlyWarningDao earlyWarningDao;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PestMonitoringService pestMonitoringService;

    /**
     * 新建任务
     *
     * @param taskDto
     * @return
     */
    @Override
    public int addTask(TaskDto taskDto) {
        //获取推送方式
        String pushWay = taskDto.getPushWay();
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        //设置创建人
        taskDto.setCreatedUser(username);
        //随机生成任务编号
        String uuid = UUID.randomUUID().toString().replace("-", "");
        //设置任务编号
        taskDto.setTaskNum(uuid);
        //获取位置路径,这是父路径
        String siteRoute = taskDto.getSiteRoute();
        //获取当前位置id
        String siteId = taskDto.getSiteId();
        if (siteRoute.contains("04")) {
            //如果包含04,拼接05加位置id [00:0],[01:1],[02:1],[03:1],[04:1],[05:2]
            String siteRouteC = siteRoute + ",[05:" + siteId + "]";
            //设置位置路径
            taskDto.setTaskSiteRoute(siteRouteC);
        } else if (siteRoute.contains("03")) {
            String siteRouteC = siteRoute + ",[04:" + siteId + "]";
            //设置位置路径
            taskDto.setTaskSiteRoute(siteRouteC);
        } else if (siteRoute.contains("02")) {
            String siteRouteC = siteRoute + ",[03:" + siteId + "]";
            //设置位置路径
            taskDto.setTaskSiteRoute(siteRouteC);
        } else if (siteRoute.contains("01")) {
            String siteRouteC = siteRoute + ",[02:" + siteId + "]";
            //设置位置路径
            taskDto.setTaskSiteRoute(siteRouteC);
        } else {
            String siteRouteC = siteRoute + ",[01:" + siteId + "]";
            //设置位置路径
            taskDto.setTaskSiteRoute(siteRouteC);
        }
        //获取当前时间
        String nowDayTime = DateUtil.dateNew(new Date()).toString();
        taskDto.setCreatedTime(nowDayTime);
        //获取结束时间,用于计算时间差
        String endTime = taskDto.getEndTime();
        Date date1 = DateUtil.parse(endTime);
        //设置当前时间,用于计算时间差
        Date date2 = DateUtil.parse(nowDayTime);
        //当前时间与结束时间相差的毫秒数
        long betweenMs = DateUtil.between(date1, date2, DateUnit.MS);
        //当前时间与结束时间相差的毫秒字符串
        String betweenMsStr = String.valueOf(betweenMs);
        //  todo:任务过期
        //向mq中丢一条消息,当任务到期时候未处理,修改任务的状态
        /*rabbitTemplate.convertAndSend("queue1", (Object) uuid, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //消息过期设置
                message.getMessageProperties().setExpiration(betweenMsStr);
                return message;
            }
        });*/
        log.info("新建任务发送消息成功:" + uuid + ":结束时间:" + endTime + ":相差时间为:" + betweenMsStr);
        int addFlag = taskManagementDao.addTask(taskDto);
        return addFlag;
    }

    /**
     * 查询任务列表
     *
     * @param taskDto
     * @return
     */
    @Override
    public PageInfo<TaskVo> findTask(TaskDto taskDto) {
        int page = taskDto.getPage();
        int size = taskDto.getSize();
        //静态分页
        PageHelper.startPage(page, size);
        List<TaskVo> taskVoList = taskManagementDao.findTask(taskDto);
        return new PageInfo<TaskVo>(taskVoList);
    }

    /**
     * 修改任务状态
     *
     * @param msg
     * @return
     */
    @Override
    public int mqUpdateStatus(String msg) {
        //只有未完成的任务才能逾期
        String taskStatus = "0";
        int mqUpdateFlag = taskManagementDao.mqUpdateStatus(msg, taskStatus);
        return mqUpdateFlag;
    }

    /**
     * 批量删除任务
     *
     * @param ids
     * @return
     */
    @Override
    public int delTask(Long[] ids) {

        int delFlag = taskManagementDao.delTask(ids);
        return delFlag;
    }

    /**
     * 查询任务详情
     *
     * @param id
     * @return
     */
    @Override
    public TaskVo findTaskDetail(Long id) {
        TaskVo taskVo = taskManagementDao.findTaskDetail(id);
        return taskVo;
    }

    /**
     * 根据位置id和任务类型查询任务详情
     *
     * @param stackId
     * @return
     */
    @Override
    public TaskVo findTaskDetailBySiteId(Integer stackId, Integer areaId, String type) {
        String siteRoute = "";
        if (ToolUtil.isNotEmpty(stackId)) {
            siteRoute = warehouseDao.findStackById(stackId).getPidRoute() + ",[" + WarehouseNodeType.STACK_NODE.getType() + ":" + stackId + "]";
        }
        if (ToolUtil.isNotEmpty(areaId)) {
            siteRoute = warehouseDao.findWareArea(areaId).getPidRoute() + ",[" + WarehouseNodeType.WAREAREA_NODE.getType() + ":" + areaId + "]";
        }
        TaskVo taskVo = taskManagementDao.findTaskDetailBySiteId(siteRoute, type);
        return taskVo;
    }

    /**
     * 工作任务查询任务列表
     *
     * @param taskDto
     * @return
     */
    @Override
    public PageInfo<TaskVo> findTaskByUser(TaskDto taskDto) {
        //获取用户信息,只查询自己的任务
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        //设置负责人,只能看到自己的任务
        taskDto.setPrincipal(username);
        int page = taskDto.getPage();
        int size = taskDto.getSize();
        //静态分页
        PageHelper.startPage(page, size);
        List<TaskVo> taskVoList = taskManagementDao.findTaskByUser(taskDto);
        return new PageInfo<TaskVo>(taskVoList);
    }

    /**
     * 任务处理
     *
     * @param taskDto
     * @return
     */
    @Override
    public int taskDispose(TaskDto taskDto) {
        //获取当前时间,任务完成的时间
        String nowTime = DateUtil.dateNew(new Date()).toString();
        //设置完成时间
        taskDto.setFinishTime(nowTime);
        //获取处理结果
        String dealResult = taskDto.getDealResult();
        String affirm = "";
        //如果处理结果为空,状态为处理中
        if (ToolUtil.isEmpty(dealResult)) {
            taskDto.setStatus("3");
            //设置预警表中的处理状态为正在处理
            affirm = "1";
        } else {
            //设置任务状态为完成
            taskDto.setStatus("1");
            //设置预警表中的处理状态为已处理
            affirm = "0";
        }
        TaskVo taskDetail = taskManagementDao.findTaskDetail(Long.parseLong(taskDto.getId()));
        String type = taskDetail.getType();
        String siteRoute = taskDetail.getSiteRoute();
        String principal = taskDetail.getPrincipal();
        if(!"01,02,03".contains(type)){
            earlyWarningDao.updateTackId(affirm,type,siteRoute,Integer.parseInt(taskDto.getId()),principal,nowTime);
        }
        int updateFlag = taskManagementDao.taskDispose(taskDto);
        return updateFlag;
    }

    /**
     * 任务评价
     *
     * @param taskDto
     * @return
     */
    @Override
    public int taskEvaluate(TaskDto taskDto) {
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        //设置评价人
        taskDto.setEvaluateUser(username);
        //获取当前时间
        String nowTime = DateUtil.dateNew(new Date()).toString();
        //设置任务评价时间
        taskDto.setEvaluateTime(nowTime);
        int updateFlag = taskManagementDao.taskEvaluate(taskDto);
        return updateFlag;
    }

    /**
     * 根据任务id查询任务图片
     *
     * @param id
     * @return
     */
    @Override
    public List<TaskVo> findPicMes(String id) {
        List<TaskVo> list = taskManagementDao.findPicMes(id);
        return list;
    }

    /**
     * 虫情监控任务处理
     *
     * @param pestData
     * @param dealResult
     * @return
     */
    @Override
    @Transactional
    public int pestTaskDeal(String pestData, String dealResult, String id) {
        String status = null;
        if (ToolUtil.isEmpty(dealResult)) {
            //处理结果为空,状态是处理中
            status = "3";
        } else {
            //不为空,状态为完成
            status = "1";
        }
        //修改任务状态
        int updateFlag = taskManagementDao.updateTaskStatus(id, status, dealResult);
        //当前时间
        String nowTime = DateUtil.dateNew(new Date()).toString();
        //解析虫情数据
        try {
            List<Map> list = JSONObject.parseObject(pestData, List.class);
            for (Map map : list) {
                //虫情板id
                String pestBoardId = map.get("pestBoardId") + "";
                //获取采集位置
                String gatherSite = map.get("gatherSite") + "";
                //获取甲虫数量
                int beetle = Integer.parseInt(map.get("beetle") + "");
                //获取粉虫数量
                int mealworm = Integer.parseInt(map.get("mealworm") + "");
                //虫情预警等级
                //pestMonitoringService.decideLevel(pestBoardId,beetle,mealworm);
                int insertPest = taskManagementDao.insertPest(pestBoardId, gatherSite, beetle, mealworm, nowTime,id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("虫情数据格式错误");
        }
        return updateFlag;
    }

    /**
     * 一键虫情上报
     *
     * @param siteRoute
     * @return
     */
    @Override
    public int oneButtonPestReport(String siteRoute, String taskAddress) {
        //获取路径下所有的虫情板
        List<String> pestBoardList = taskManagementDao.findPestBoard(siteRoute);
        //当前时间
        String nowTime = DateUtil.dateNew(new Date()).toString();
        int insertPest = 0;
        for (String pestBoard : pestBoardList) {
            insertPest = taskManagementDao.oneButtonPestReport(pestBoard, taskAddress, nowTime);
        }
        return insertPest;
    }

    /**
     * 根据位置路径,查询所有的设备,进行移垛处理
     *
     * @param siteRoute
     * @return
     */
    @Override
    public List<TaskVo> findAllNodeByStack(String siteRoute) {
        List<TaskVo> taskVoList = taskManagementDao.findAllNodeByStack(siteRoute);
        return taskVoList;
    }

    /**
     * 移垛处理
     *
     * @param taskDto
     * @return
     */
    @Override
    @Transactional
    public int moveStackDeal(TaskDto taskDto) {
        //获取处理结果
        String dealResult = taskDto.getDealResult();
        if (ToolUtil.isEmpty(dealResult)) {
            //没有处理结果,任务状态为处理中
            taskDto.setStatus("3");
        } else {
            //有处理效果,任务状态为完成
            taskDto.setStatus("1");
        }
        int updateStatus = taskManagementDao.updateTaskStatus(taskDto.getId(), taskDto.getStatus(), taskDto.getDealResult());
        //获取移垛路径,传入的是移垛后路径
        String siteRoute = taskDto.getSiteRoute();
        //获取移垛垛位名称
        String siteName = taskDto.getSiteName();
        //获取目标地址id
        String siteId = taskDto.getSiteId();
        //拼接获取当前地址
        //siteRoute = SiteRouteUtil.route(siteRoute, siteId);
        //含有05,说明是垛位
        String stackId = "";
        if (siteRoute.contains("05")) {
            stackId = siteId;
        }
        //获取移垛数据
        String siteDate = taskDto.getSiteDate();
        try {
            List<Map> list = JSONObject.parseObject(siteDate, List.class);
            for (Map map : list) {
                //设备id
                String nodeId = map.get("nodeId").toString();
                //设备坐标
                int x = Integer.parseInt(map.get("x").toString());
                int y = Integer.parseInt(map.get("y").toString());
                int z = Integer.parseInt(map.get("z").toString());
                int updateFlag = taskManagementDao.updateNodeAddress(nodeId, x, y, z, siteRoute, siteId, siteName, stackId);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new RuntimeException("数据输入错误");
        }
        return updateStatus;
    }

    /**
     * 翻垛任务处理
     *
     * @param taskDto
     * @return
     */
    @Override
    @Transactional
    public int flipStackDeal(TaskDto taskDto) {
        //获取处理结果
        String dealResult = taskDto.getDealResult();
        if (ToolUtil.isEmpty(dealResult)) {
            //没有处理结果,任务状态为处理中
            taskDto.setStatus("3");
        } else {
            //有处理效果,任务状态为完成
            taskDto.setStatus("1");
        }
        int updateStatus = taskManagementDao.updateTaskStatus(taskDto.getId(), taskDto.getStatus(), taskDto.getDealResult());
        //获取翻垛数据
        String siteDate = taskDto.getSiteDate();
        try {
            List<Map> list = JSONObject.parseObject(siteDate, List.class);
            for (Map map : list) {
                String nodeId = map.get("nodeId").toString();
                String x = map.get("x").toString();
                String y = map.get("y").toString();
                String z = map.get("z").toString();
                int updateFlag = taskManagementDao.updateNodeXYZ(nodeId, x, y, z);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("数据输入错误");
        }
        return updateStatus;
    }

    /**
     * 任务进度统计
     *
     * @param taskDto
     * @return
     */
    @Override
    public List<Map> taskScheduleCount(TaskDto taskDto) {
        //获取任务路径
        String siteRoute = taskDto.getSiteRoute();
        //获取当前任务id
        String siteId = taskDto.getSiteId();
        if (ToolUtil.isNotEmpty(siteRoute) && ToolUtil.isNotEmpty(siteId)){
            //当前位置路径
            String route = SiteRouteUtil.route(siteRoute, siteId);
            taskDto.setTaskSiteRoute(route);
        }
        //获取开始时间
        String startTime = taskDto.getStartTime();
        //获取结束时间
        String endTime = taskDto.getEndTime();
        //当前时间
        String nowTime = DateUtil.dateNew(new Date()).toString();
        //如果开始时间和结束时间都为空,日期向前推一周
        if (ToolUtil.isEmpty(startTime) && ToolUtil.isEmpty(endTime)) {
            startTime = DateUtil.lastWeek().toString();
            endTime = nowTime;
        }
        //如果开始时间不为空,结束时间为空,结束时间向后推一周
        if (ToolUtil.isNotEmpty(startTime) && ToolUtil.isEmpty(endTime)) {
            Date date = DateUtil.parse(startTime);
            endTime = DateUtil.offsetDay(date, 7).toString();
        }
        //如果开始时间为空,结束时间不为空,开始时间为向前推一周
        if (ToolUtil.isEmpty(startTime) && ToolUtil.isNotEmpty(endTime)) {
            Date date = DateUtil.parse(endTime);
            startTime = DateUtil.offsetDay(date, -7).toString();
        }
        //获取统计方式,Y:年统计,M:月统计,D:日统计
        String deteType = taskDto.getDeteType();
        if ("D".equals(deteType)) {
            //按照日统计,获取开始时间结束时间所有的日期
            List<String> betweenDays = getBetweenDays(startTime, endTime);
            //对获取日期排序
            List<String> dayList = betweenDays.stream().sorted(String::compareTo).collect(Collectors.toList());
            taskDto.setDays(betweenDays);
            //按照日统计数据,数据总量,包含完成和未完成的任务,所有的任务
            List<Map> taskVoList = taskManagementDao.taskScheduleDayCount(taskDto);
            for (Map map : taskVoList) {
                //获取日期
                String day = map.get("day").toString();
                //获取任务总数
                int sum = Integer.parseInt(map.get("sum").toString());
                if (sum == 0) {
                    map.put("finish", 0);
                    map.put("noFinish", 0);
                } else {
                    //查询完成的任务数量
                    int count = taskManagementDao.findTaskFinish(taskDto.getType(), day,taskDto.getTaskSiteRoute(),taskDto.getPrincipal());
                    map.put("finish", count);
                    map.put("noFinish", sum - count);
                }
            }
            //获取每一个日期,判断是否存在,不存在的日期数量为0
            for (String day : dayList) {
                boolean flag = false;
                for (Map map : taskVoList) {
                    //判断日期是否存在
                    String day1 = map.get("day").toString();
                    if (day.equals(day1)) {
                        flag = true;
                        break;
                    }
                }
                //如果日期不存在,补充日期数据,任务为0
                if (!flag) {
                    Map map = new HashMap();
                    map.put("day", day);
                    map.put("sum", 0);
                    map.put("finish", 0);
                    map.put("noFinish", 0);
                    taskVoList.add(map);
                }
            }
            List<Map> taskList = taskVoList.stream().sorted((m1, m2) -> m1.get("day").toString().compareTo(m2.get("day").toString())).collect(Collectors.toList());
            return taskList;
        } else if ("M".equals(deteType)) {
            //按照月统计,获取开始时间结束时间所有的月份
            List<String> monthBetweenDate = getMonthBetweenDate(startTime, endTime);
            //对获取日期排序
            List<String> dayList = monthBetweenDate.stream().sorted(String::compareTo).collect(Collectors.toList());
            taskDto.setDays(dayList);
            //按照月统计数据,数据总量,包含完成和未完成的任务,所有的任务
            List<Map> taskVoList = taskManagementDao.taskScheduleMonthCount(taskDto);
            for (Map map : taskVoList) {
                //获取日期
                String day = map.get("day").toString();
                //获取任务总数
                int sum = Integer.parseInt(map.get("sum").toString());
                if (sum == 0) {
                    map.put("finish", 0);
                    map.put("noFinish", 0);
                } else {
                    //查询完成的任务数量
                    int count = taskManagementDao.findTaskMonthFinish(taskDto.getType(), day,taskDto.getTaskSiteRoute(),taskDto.getPrincipal());
                    map.put("finish", count);
                    map.put("noFinish", sum - count);
                }
            }
            //获取每一个日期,判断是否存在,不存在的日期数量为0
            for (String day : dayList) {
                boolean flag = false;
                for (Map map : taskVoList) {
                    //判断日期是否存在
                    String day1 = map.get("day").toString();
                    if (day.equals(day1)) {
                        flag = true;
                        break;
                    }
                }
                //如果日期不存在,补充日期数据,任务为0
                if (!flag) {
                    Map map = new HashMap();
                    map.put("day", day);
                    map.put("sum", 0);
                    map.put("finish", 0);
                    map.put("noFinish", 0);
                    taskVoList.add(map);
                }
            }
            List<Map> taskList = taskVoList.stream().sorted((m1, m2) -> m1.get("day").toString().compareTo(m2.get("day").toString())).collect(Collectors.toList());
            return taskList;
        } else if ("Y".equals(deteType)) {
            //按照年统计
            List<String> yearBetweenDate = getYearBetweenDate(startTime, endTime);
            //对获取日期排序
            List<String> dayList = yearBetweenDate.stream().sorted(String::compareTo).collect(Collectors.toList());
            taskDto.setDays(dayList);
            //按照年统计数据,数据总量,包含完成和未完成的任务,所有的任务
            List<Map> taskVoList = taskManagementDao.taskScheduleYearCount(taskDto);
            for (Map map : taskVoList) {
                //获取日期
                String day = map.get("day").toString();
                //获取任务总数
                int sum = Integer.parseInt(map.get("sum").toString());
                if (sum == 0) {
                    map.put("finish", 0);
                    map.put("noFinish", 0);
                } else {
                    //查询完成的任务数量
                    int count = taskManagementDao.findTaskYearFinish(taskDto.getType(), day,taskDto.getTaskSiteRoute(),taskDto.getPrincipal());
                    map.put("finish", count);
                    map.put("noFinish", sum - count);
                }
            }
            //获取每一个日期,判断是否存在,不存在的日期数量为0
            for (String day : dayList) {
                boolean flag = false;
                for (Map map : taskVoList) {
                    //判断日期是否存在
                    String day1 = map.get("day").toString();
                    if (day.equals(day1)) {
                        flag = true;
                        break;
                    }
                }
                //如果日期不存在,补充日期数据,任务为0
                if (!flag) {
                    Map map = new HashMap();
                    map.put("day", day);
                    map.put("sum", 0);
                    map.put("finish", 0);
                    map.put("noFinish", 0);
                    taskVoList.add(map);
                }
            }
            List<Map> taskList = taskVoList.stream().sorted((m1, m2) -> m1.get("day").toString().compareTo(m2.get("day").toString())).collect(Collectors.toList());
            return taskList;
        }
        return null;
    }

    /**
     * 任务量趋势统计
     *
     * @param taskDto
     * @return
     */
    @Override
    public List<Map> taskCountTrend(TaskDto taskDto) {
        //获取任务路径
        String siteRoute = taskDto.getSiteRoute();
        //获取当前任务id
        String siteId = taskDto.getSiteId();
        if (ToolUtil.isNotEmpty(siteRoute) && ToolUtil.isNotEmpty(siteId)){
            //当前位置路径
            String route = SiteRouteUtil.route(siteRoute, siteId);
            taskDto.setTaskSiteRoute(route);
        }
        //获取开始时间
        String startTime = taskDto.getStartTime();
        //获取结束时间
        String endTime = taskDto.getEndTime();
        //当前时间
        String nowTime = DateUtil.dateNew(new Date()).toString();
        //如果开始时间和结束时间都为空,日期向前推一周
        if (ToolUtil.isEmpty(startTime) && ToolUtil.isEmpty(endTime)) {
            startTime = DateUtil.lastWeek().toString();
            endTime = nowTime;
        }
        //如果开始时间不为空,结束时间为空,结束时间向后推一周
        if (ToolUtil.isNotEmpty(startTime) && ToolUtil.isEmpty(endTime)) {
            Date date = DateUtil.parse(startTime);
            endTime = DateUtil.offsetDay(date, 7).toString();
        }
        //如果开始时间为空,结束时间不为空,开始时间为向前推一周
        if (ToolUtil.isEmpty(startTime) && ToolUtil.isNotEmpty(endTime)) {
            Date date = DateUtil.parse(endTime);
            startTime = DateUtil.offsetDay(date, -7).toString();
        }
        //按照日统计,获取开始时间结束时间所有的日期
        List<String> betweenDays = getBetweenDays(startTime, endTime);
        //对获取日期排序
        List<String> dayList = betweenDays.stream().sorted(String::compareTo).collect(Collectors.toList());
        taskDto.setDays(betweenDays);
        //按照日统计数据,数据总量,包含完成和未完成的任务,所有的任务
        List<Map> taskVoList = taskManagementDao.taskScheduleDayCount(taskDto);
        //获取每一个日期,判断是否存在,不存在的日期数量为0
        for (String day : dayList) {
            boolean flag = false;
            for (Map map : taskVoList) {
                //判断日期是否存在
                String day1 = map.get("day").toString();
                if (day.equals(day1)) {
                    flag = true;
                    break;
                }
            }
            //如果日期不存在,补充日期数据,任务为0
            if (!flag) {
                Map map = new HashMap();
                map.put("day", day);
                map.put("sum", 0);
                taskVoList.add(map);
            }
        }
        List<Map> taskList = taskVoList.stream().sorted((m1, m2) -> m1.get("day").toString().compareTo(m2.get("day").toString())).collect(Collectors.toList());
        return taskList;
    }

    /**
     * 查询各种类型的任务数量
     *
     * @param taskDto
     * @return
     */
    @Override
    public List<Map> taskCountByType(TaskDto taskDto) {
        //获取任务路径
        String siteRoute = taskDto.getSiteRoute();
        //获取当前任务id
        String siteId = taskDto.getSiteId();
        if (ToolUtil.isNotEmpty(siteRoute) && ToolUtil.isNotEmpty(siteId)){
            //当前位置路径
            String route = SiteRouteUtil.route(siteRoute, siteId);
            taskDto.setTaskSiteRoute(route);
        }
        //获取开始时间
        String startTime = taskDto.getStartTime();
        //获取结束时间
        String endTime = taskDto.getEndTime();
        //当前时间
        String nowTime = DateUtil.dateNew(new Date()).toString();
        //如果开始时间和结束时间都为空,日期向前推一周
        if (ToolUtil.isEmpty(startTime) && ToolUtil.isEmpty(endTime)) {
            startTime = DateUtil.lastWeek().toString();
            endTime = nowTime;
        }
        //如果开始时间不为空,结束时间为空,结束时间向后推一周
        if (ToolUtil.isNotEmpty(startTime) && ToolUtil.isEmpty(endTime)) {
            Date date = DateUtil.parse(startTime);
            endTime = DateUtil.offsetDay(date, 7).toString();
        }
        //如果开始时间为空,结束时间不为空,开始时间为向前推一周
        if (ToolUtil.isEmpty(startTime) && ToolUtil.isNotEmpty(endTime)) {
            Date date = DateUtil.parse(endTime);
            startTime = DateUtil.offsetDay(date, -7).toString();
        }
        List<Map> mapList = new ArrayList<>();
        //查询虫情监控任务01:虫情巡查任务
        int type01 = taskManagementDao.findPestCount(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        Map map01 = new HashMap();
        map01.put("type","虫情巡查任务");
        map01.put("count",type01);
        mapList.add(map01);
        //查询02:翻垛任务数量
        int type02 = taskManagementDao.findRollStack(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        Map map02 = new HashMap();
        map02.put("type","翻垛任务");
        map02.put("count",type02);
        mapList.add(map02);
        //查询03:移垛任务
        int type03 = taskManagementDao.findMoveStack(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        Map map03 = new HashMap();
        map03.put("type","移垛任务");
        map03.put("count",type03);
        mapList.add(map03);
        //查询04:垛内甲虫预警任务
        int type04 = taskManagementDao.findBeetle(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        Map map04 = new HashMap();
        map04.put("type","垛内甲虫预警任务");
        map04.put("count",type04);
        mapList.add(map04);
        //查询05:仓间甲虫预警任务
        int type05 = taskManagementDao.findHouseBeetle(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        Map map05 = new HashMap();
        map05.put("type","仓间甲虫预警任务");
        map05.put("count",type05);
        mapList.add(map05);
        //查询06:垛内氧气预警任务
        int type06 = taskManagementDao.findO2(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        Map map06 = new HashMap();
        map06.put("type","垛内氧气预警任务");
        map06.put("count",type06);
        mapList.add(map06);
        //查询07:仓间氧气预警任务
        int type07 = taskManagementDao.findHouseO2(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        Map map07 = new HashMap();
        map07.put("type","仓间氧气预警任务");
        map07.put("count",type07);
        mapList.add(map07);
        //查询08:垛内温度预警任务
        int type08 = taskManagementDao.findStackTemp(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        Map map08 = new HashMap();
        map08.put("type","垛内温度预警任务");
        map08.put("count",type08);
        mapList.add(map08);
        //查询09:仓间温度预警任务
        int type09 = taskManagementDao.findHouseTemp(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        Map map09 = new HashMap();
        map09.put("type","仓间温度预警任务");
        map09.put("count",type09);
        mapList.add(map09);
        //查询10:垛内湿度预警任务
        int type10 = taskManagementDao.findStackMoist(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        Map map10 = new HashMap();
        map10.put("type","垛内湿度预警任务");
        map10.put("count",type10);
        mapList.add(map10);
        //查询11:仓间湿度预警任务
        int type11 = taskManagementDao.findHouseMoist(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        Map map11 = new HashMap();
        map11.put("type","仓间湿度预警任务");
        map11.put("count",type11);
        mapList.add(map11);
        //查询12:包芯温度预警任务
        int type12 = taskManagementDao.findCoreTemp(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        Map map12 = new HashMap();
        map12.put("type","包芯温度预警任务");
        map12.put("count",type12);
        mapList.add(map12);
        //查询13:平均包芯高于环境预警任务
        int type13 = taskManagementDao.findMoreAveTemp(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        Map map13 = new HashMap();
        map13.put("type","平均包芯高于环境预警任务");
        map13.put("count",type13);
        mapList.add(map13);
        //查询14:仓间粉螟预警任务
        int type14 = taskManagementDao.findHouseMealworm(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        Map map14 = new HashMap();
        map14.put("type","仓间粉螟预警任务");
        map14.put("count",type14);
        mapList.add(map14);
        //查询15:仓库检查任务
        int type15 = taskManagementDao.findCheck(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        Map map15 = new HashMap();
        map15.put("type","仓库检查任务");
        map15.put("count",type15);
        mapList.add(map15);
        //查询16其他任务
        int type16 = taskManagementDao.findOther(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        Map map16 = new HashMap();
        map16.put("type","其他任务");
        map16.put("count",type16);
        mapList.add(map16);
        return mapList;
    }

    /**
     * 任务平均完成时间
     *
     * @param taskDto
     * @return
     */
    @Override
    public List<Map> taskAvlFinishTime(TaskDto taskDto) {
        //获取开始时间
        String startTime = taskDto.getStartTime();
        //获取结束时间
        String endTime = taskDto.getEndTime();
        //当前时间
        String nowTime = DateUtil.dateNew(new Date()).toString();
        //如果开始时间和结束时间都为空,日期向前推一周
        if (ToolUtil.isEmpty(startTime) && ToolUtil.isEmpty(endTime)) {
            startTime = DateUtil.lastWeek().toString();
            endTime = nowTime;
        }
        //如果开始时间不为空,结束时间为空,结束时间向后推一周
        if (ToolUtil.isNotEmpty(startTime) && ToolUtil.isEmpty(endTime)) {
            Date date = DateUtil.parse(startTime);
            endTime = DateUtil.offsetDay(date, 7).toString();
        }
        //如果开始时间为空,结束时间不为空,开始时间为向前推一周
        if (ToolUtil.isEmpty(startTime) && ToolUtil.isNotEmpty(endTime)) {
            Date date = DateUtil.parse(endTime);
            startTime = DateUtil.offsetDay(date, -7).toString();
        }
        //开始时间结束时间所有的时间,yyyy-MM-dd
        List<String> days = getBetweenDays(startTime, endTime);
        //获取仓库负责人
        String principal = taskDto.getPrincipal();
        //获取位置路径,传入的父路径,需要拼接当前位置id
        String siteRoute = taskDto.getSiteRoute();
        //获取当前地址id
        String siteId = taskDto.getSiteId();
        String route = "";
        if (ToolUtil.isNotEmpty(siteRoute) && ToolUtil.isNotEmpty(siteId)) {
            //拼接获取当前路径
            route = SiteRouteUtil.route(siteRoute, siteId);
        }
        //返回数据
        List<Map> mapList = new ArrayList<>();
        for (String day : days) {
            //查询当天所有的任务时间,任务创建时间,完成时间
            List<Map> timeList = taskManagementDao.taskAvlFinishTime(day, principal, route);
            //如果今天没有完成的任务,时间是0
            if (ToolUtil.isEmpty(timeList)) {
                Map map = new HashMap();
                map.put("day", day);
                map.put("time", 0);
                mapList.add(map);
            } else {
                //完成任务的总时间
                long sum = 0;
                //完成任务的数量
                long size = timeList.size();
                for (Map map : timeList) {
                    //任务开始时间
                    String createTime = map.get("createTime").toString();
                    //任务完成时间
                    String finishTime = map.get("finishTime").toString();
                    //完成所用的时间,分钟
                    long minute = TimeUtils.betweenMinute(createTime, finishTime);
                    sum = sum + minute;
                }
                //获取平均完成时间,小时
                double avlTime = sum/(double)size/60;
                //保留两位小数
                DecimalFormat df = new DecimalFormat("######0.00");
                Map map = new HashMap();
                map.put("day", day);
                map.put("time", df.format(avlTime));
                mapList.add(map);
            }
        }
        return mapList;
    }

    /**
     * 查询垛位下所有的虫情板
     *
     * @param siteRoute
     * @return
     */
    @Override
    public List<String> findPestBoardByRoute(String siteRoute) {
        //获取路径下所有的虫情板
        List<String> pestBoardList = taskManagementDao.findPestBoard(siteRoute);
        return pestBoardList;
    }

    /**
     * 查询所有的过期任务
     *
     * @return
     */
    @Override
    public List<Map> findAllOverdueTask() {
        List<Map> taskList = taskManagementDao.findAllOverdueTask();
        return taskList;
    }

    /**
     * 虫情任务处理提交后返回虫数量
     *
     * @param id
     * @return
     */
    @Override
    public List<Map> pestCountDetail(String id) {
        List<Map> mapList = taskManagementDao.pestCountDetail(id);
        return mapList;
    }

    /**
     * 统计类型占比查询数量
     *
     * @param taskDto
     * @return
     */
    @Override
    public Map taskCountByTypeMap(TaskDto taskDto) {
        //获取任务路径
        String siteRoute = taskDto.getSiteRoute();
        //获取当前任务id
        String siteId = taskDto.getSiteId();
        if (ToolUtil.isNotEmpty(siteRoute) && ToolUtil.isNotEmpty(siteId)){
            //当前位置路径
            String route = SiteRouteUtil.route(siteRoute, siteId);
            taskDto.setTaskSiteRoute(route);
        }
        //获取开始时间
        String startTime = taskDto.getStartTime();
        //获取结束时间
        String endTime = taskDto.getEndTime();
        //当前时间
        String nowTime = DateUtil.dateNew(new Date()).toString();
        //如果开始时间和结束时间都为空,日期向前推一周
        if (ToolUtil.isEmpty(startTime) && ToolUtil.isEmpty(endTime)) {
            startTime = DateUtil.lastWeek().toString();
            endTime = nowTime;
        }
        //如果开始时间不为空,结束时间为空,结束时间向后推一周
        if (ToolUtil.isNotEmpty(startTime) && ToolUtil.isEmpty(endTime)) {
            Date date = DateUtil.parse(startTime);
            endTime = DateUtil.offsetDay(date, 7).toString();
        }
        //如果开始时间为空,结束时间不为空,开始时间为向前推一周
        if (ToolUtil.isEmpty(startTime) && ToolUtil.isNotEmpty(endTime)) {
            Date date = DateUtil.parse(endTime);
            startTime = DateUtil.offsetDay(date, -7).toString();
        }
        Map mapAll = new HashMap();
        //查询虫情监控任务01:虫情巡查任务
        int type01 = taskManagementDao.findPestCount(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        mapAll.put("01", type01);
        //查询02:翻垛任务数量
        int type02 = taskManagementDao.findRollStack(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        mapAll.put("02", type02);
        //查询03:移垛任务
        int type03 = taskManagementDao.findMoveStack(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        mapAll.put("03", type03);
        //查询04:垛内甲虫预警任务
        int type04 = taskManagementDao.findBeetle(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        mapAll.put("04", type04);
        //查询05:仓间甲虫预警任务
        int type05 = taskManagementDao.findHouseBeetle(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        mapAll.put("05", type05);
        //查询06:垛内氧气预警任务
        int type06 = taskManagementDao.findO2(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        mapAll.put("06", type06);
        //查询07:仓间氧气预警任务
        int type07 = taskManagementDao.findHouseO2(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        mapAll.put("07", type07);
        //查询08:垛内温度预警任务
        int type08 = taskManagementDao.findStackTemp(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        mapAll.put("08", type08);
        //查询09:仓间温度预警任务
        int type09 = taskManagementDao.findHouseTemp(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        mapAll.put("09", type09);
        //查询10:垛内湿度预警任务
        int type10 = taskManagementDao.findStackMoist(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        mapAll.put("10", type10);
        //查询11:仓间湿度预警任务
        int type11 = taskManagementDao.findHouseMoist(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        mapAll.put("11", type11);
        //查询12:包芯温度预警任务
        int type12 = taskManagementDao.findCoreTemp(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        mapAll.put("12", type12);
        //查询13:平均包芯高于环境预警任务
        int type13 = taskManagementDao.findMoreAveTemp(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        mapAll.put("13", type13);
        //查询14:仓间粉螟预警任务
        int type14 = taskManagementDao.findHouseMealworm(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        mapAll.put("14", type14);
        //查询15:仓库检查任务
        int type15 = taskManagementDao.findCheck(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        mapAll.put("15", type15);
        //查询16其他任务
        int type16 = taskManagementDao.findOther(startTime, endTime,taskDto.getPrincipal(),taskDto.getTaskSiteRoute());
        mapAll.put("16", type16);
        return mapAll;
    }

    @Override
    public Integer findTaskBySiteRoute(String siteRoute, String type) {
        return taskManagementDao.findTaskBySiteRoute(siteRoute,type);
    }


    /**
     * 获取两个日期之间的所有日期 (年月日)
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    public List<String> getBetweenDays(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 声明保存日期集合
        List<String> list = new ArrayList<String>();
        try {
            // 转化成日期类型
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);
            //用Calendar 进行日期比较判断
            Calendar calendar = Calendar.getInstance();
            while (startDate.getTime() <= endDate.getTime()) {
                // 把日期添加到集合
                list.add(sdf.format(startDate));
                // 设置日期
                calendar.setTime(startDate);
                //把日期增加一天
                calendar.add(Calendar.DATE, 1);
                // 获取增加后的日期
                startDate = calendar.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取两个日期之间的所有月份 (年月)
     *
     * @param startTime
     * @param endTime
     * @return：YYYY-MM
     */
    public List<String> getMonthBetweenDate(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        // 声明保存日期集合
        List<String> list = new ArrayList<String>();
        try {
            // 转化成日期类型
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);

            //用Calendar 进行日期比较判断
            Calendar calendar = Calendar.getInstance();
            while (startDate.getTime() <= endDate.getTime()) {
                // 把日期添加到集合
                list.add(sdf.format(startDate));
                // 设置日期
                calendar.setTime(startDate);
                //把日期增加一天
                calendar.add(Calendar.MONTH, 1);
                // 获取增加后的日期
                startDate = calendar.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取两个日期之间的所有年
     *
     * @param startTime
     * @param endTime
     * @return：YYYY
     */
    public List<String> getYearBetweenDate(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        // 声明保存日期集合
        List<String> list = new ArrayList<String>();
        try {
            // 转化成日期类型
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);

            //用Calendar 进行日期比较判断
            Calendar calendar = Calendar.getInstance();
            while (startDate.getTime() <= endDate.getTime()) {
                // 把日期添加到集合
                list.add(sdf.format(startDate));
                // 设置日期
                calendar.setTime(startDate);
                //把日期增加一天
                calendar.add(Calendar.YEAR, 1);
                // 获取增加后的日期
                startDate = calendar.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 新建数据处理发送过来的任务
     *
     * @param taskDto
     * @return
     */
    @Override
    public int addDataHandTask(TaskDto taskDto) {
        //获取当前时间
        String nowDayTime = DateUtil.dateNew(new Date()).toString();
        taskDto.setPushWay("A");
        taskDto.setCreatedTime(nowDayTime);
        //获取结束时间,用于计算时间差
        String endTime = taskDto.getEndTime();
        Date date1 = DateUtil.parse(endTime);
        //设置当前时间,用于计算时间差
        Date date2 = DateUtil.parse(nowDayTime);
        //当前时间与结束时间相差的毫秒数
        long betweenMs = DateUtil.between(date1, date2, DateUnit.MS);
        //当前时间与结束时间相差的毫秒字符串
        String betweenMsStr = String.valueOf(betweenMs);
        //  todo:任务过期
        //向mq中丢一条消息,当任务到期时候未处理,修改任务的状态
        /*rabbitTemplate.convertAndSend("queue1", (Object) uuid, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //消息过期设置
                message.getMessageProperties().setExpiration(betweenMsStr);
                return message;
            }
        });*/
        log.info("新建任务发送消息成功:" + taskDto.getTaskNum() + ":结束时间:" + endTime + ":相差时间为:" + betweenMsStr);
        int addFlag = taskManagementDao.addTask(taskDto);
        return addFlag;
    }
}