package com.rpzh.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.TaskManagementDao;
import com.rpzh.dao.WarehouseDao;
import com.rpzh.dto.TaskDto;
import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.WarehouseNodeType;
import com.rpzh.service.PestMonitoringService;
import com.rpzh.service.TaskManagementService;
import com.rpzh.util.SiteRouteUtil;
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
        //如果处理结果为空,状态为处理中
        if (ToolUtil.isEmpty(dealResult)) {
            taskDto.setStatus("3");
        } else {
            //设置任务状态为完成
            taskDto.setStatus("1");
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
                int insertPest = taskManagementDao.insertPest(pestBoardId, gatherSite, beetle, mealworm, nowTime);
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
        for (String pestBoard : pestBoardList) {
            int insertPest = taskManagementDao.oneButtonPestReport(pestBoard, taskAddress, nowTime);
        }
        return 0;
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
        //获取移垛路径,传入的是父路径,需要拼接当前路径
        String siteRoute = taskDto.getSiteRoute();
        //获取移垛垛位名称
        String siteName = taskDto.getSiteName();
        //获取目标地址id
        String siteId = taskDto.getSiteId();
        //拼接获取当前地址
        siteRoute = SiteRouteUtil.route(siteRoute, siteId);
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
                    int count = taskManagementDao.findTaskFinish(taskDto.getType(), day);
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
                    int count = taskManagementDao.findTaskMonthFinish(taskDto.getType(), day);
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
                    int count = taskManagementDao.findTaskYearFinish(taskDto.getType(), day);
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
     * 获取两个日期之间的所有日期 (年月日)
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
}