package com.rpzh.service.impl;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.*;
import com.rpzh.dao.*;
import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.dto.TaskDto;
import com.rpzh.entity.*;
import com.rpzh.enums.*;
import com.rpzh.feign.UserFeign;
import com.rpzh.pojo.EarlyWarning;
import com.rpzh.pojo.NodeData;
import com.rpzh.service.EarlyWarningService;
import com.rpzh.util.*;
import com.rpzh.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.StringUtil;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
/**
 * @Author: zyq
 * @Date: 2022/05/20/14:46
 * @Description: 功能
 */
@Slf4j
@Service
public class EarlyWarningServiceImpl implements EarlyWarningService {
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    //mq主题
    @Value("${rocketmq.topic}")
    private String topic;
    @Resource
    private EarlyWarningDao earlyWarningDao;
    @Resource
    private MaintainPlanDao maintainPlanDao;
    @Resource
    private AlarmValueSettingDao alarmValueSettingDao;
    @Resource
    private WarehouseDao warehouseDao;
    @Resource
    private WareAreaAlarmSettingDao wareAreaAlarmSettingDao;
    @Resource
    private PestMonitoringDao pestMonitoringDao;
    //redis中用户key前缀
    private static final String USER = "USER_ID_";
    private static final String USER_ID = "MAINTIAN_USER_ID_USERNAME";
    @Autowired
    private RedisUtil redisUtil;

    @Resource
    private UserFeign userFeign;

    @Resource
    private TaskManagementDao taskManagementDao;
    /*
     * 查询历史预警列表
     */
    @Override
    public PageInfo<EarlyWarningVo> findHistoryWarning(EarlyWarningDto earlyWarningDto) {
        String pidRoute = earlyWarningDto.getPidRoute();
        Integer stackId = earlyWarningDto.getStackId();
        List<EarlyWarningVo> earlyWarningInfos = new ArrayList<>();
        //设置结束时间
        if(ToolUtil.isNotEmpty(earlyWarningDto.getEndTime())){
            earlyWarningDto.setEndTime(new Date(earlyWarningDto.getEndTime().getTime() + 24 * 60 * 60 * 1000-1));
        }
        //路径拼接
        if(StringUtils.isNotEmpty(pidRoute) && ToolUtil.isNotEmpty(stackId)){
            earlyWarningDto.setPidRoute(SiteRouteUtil.route(pidRoute,stackId+""));
        }
        PageHelper.startPage(earlyWarningDto.getPage(), earlyWarningDto.getSize());
        //分页查询历史预警
        earlyWarningInfos = earlyWarningDao.findHistoryWarningData(earlyWarningDto);
        for (EarlyWarningVo earlyWarningInfo : earlyWarningInfos) {
            //设置位置名称
            if(ToolUtil.isNotEmpty(earlyWarningInfo.getStackId())){
                //查询垛位名称
                earlyWarningInfo.setSiteName(warehouseDao.findStackById(earlyWarningInfo.getStackId()).getSiteName());
            }else{
                //查询仓间名称
                earlyWarningInfo.setSiteName(warehouseDao.findWareArea(earlyWarningInfo.getAreaId()).getSiteName());
            }
            earlyWarningInfo.setPrincipalName((String)redisUtil.hget("USER_"+earlyWarningInfo.getPrincipalName(),"nickname") + "(" + earlyWarningInfo.getPrincipalName() + ")");
        }
        return new PageInfo<>(earlyWarningInfos);
    }

    //预警统计
    @Override
    public Result findWarningCount(EarlyWarningDto earlyWarningDto) {
        String pidRoute = earlyWarningDto.getPidRoute();
        Integer siteId = earlyWarningDto.getSiteId();
        //设置时间
        Map<String, String> time = GetBetweenDayUtils.get(earlyWarningDto.getBeginTime(), earlyWarningDto.getFinishTime());
        //设置开始时间
        earlyWarningDto.setBeginTime(time.get("startTime"));
        //设置结束时间
        earlyWarningDto.setFinishTime(time.get("endTime"));
        //获取时间类型
        Integer dayType = earlyWarningDto.getDayType();
        // 时间类型,1:日,2:周,3:月,4:年
        if(dayType!=1 && dayType != 2 && dayType !=3 && dayType != 4){
            return new Result(false, StatusCode.ERROR, "时间类型错误!");
        }
        //根据位置路由和位置id拼接完整的位置路由
        if(StringUtils.isNotEmpty(pidRoute) && ToolUtil.isNotEmpty(siteId)) {
            earlyWarningDto.setPidRoute(SiteRouteUtil.route(pidRoute,siteId+""));
        }
        //查询满足条件的所有id
        List<Integer> ids = earlyWarningDao.findIds(earlyWarningDto);
        Map<String,Object> map1 = new HashMap<>();
        //预警总数量统计
        List<Map> warningCount = new ArrayList<>();
        //预警响应率统计
        List<Map> isDealCount = new ArrayList<>();
        //预警类型统计   百分比
        List<Map> warningTypeCount = new ArrayList<>();
        //预警类型统计   个数
        List<Map> warningTypeNumber = new ArrayList<>();
        //返回给前端数据中的value
        List<Object> values;
        //报警类型统计 各种类型所占总数
        Map isDeal = new HashMap();
        //所有的预警类型
        List<String> types = Arrays.asList("设备故障", "垛内虫数预警", "仓间粉螟预警", "平均包芯高于环境预警","平均包芯低于环境预警", "包芯温度预警", "仓间湿度预警", "垛内湿度预警", "仓间温度预警", "垛内温度预警", "仓间氧气预警", "垛内氧气预警", "仓间虫数预警");
        //查询出来无数据时处理
        if(ToolUtil.isEmpty(ids)){
            //初始化预警类型统计   百分比
            warningTypeCount.add(getMap(Arrays.asList("warningType,sum".split(",")),Arrays.asList("0,0".split(","))));
            //初始化预警类型统计   个数(预警类型全部显示)
            for (String type : types) {
                warningTypeNumber.add(getMap(Arrays.asList("sum", "warningType"), Arrays.asList(0,type)));
            }
            //初始化预警总数量统计
            warningCount.add(getMap(Arrays.asList("day,sum".split(",")),Arrays.asList("0,0".split(","))));
            //初始化预警响应率统计
            isDealCount.add(getMap(Arrays.asList("day,sum,deal,treatmentRate,noDeal".split(",")),Arrays.asList("0,0,0,0,0".split(","))));
            //报警类型统计 各种类型所占总数
            isDeal = getMap(Arrays.asList("ratioDeal,sumDeal,sumNotDeal,ratioNotDeal".split(",")),Arrays.asList("0,0,0,0".split(",")));
            values = Arrays.asList(0,warningTypeCount,warningTypeNumber,warningCount,isDealCount,isDeal);
        }else {
            //查询预警总数量
            Integer sum = ids.size();
            //报警类型统计 百分比
            warningTypeCount = earlyWarningDao.findWarningTypeCount(ids);
            Integer countByOne = 0;
            for (int i = 0; i < warningTypeCount.size(); i++) {
                String warningType = getWarningType(warningTypeCount.get(i).get("warningType").toString()).get("warningType");
                warningTypeCount.get(i).put("warningType",warningType);
                warningTypeCount.get(i).put("sum", Math.round(Double.parseDouble(warningTypeCount.get(i).get("sum").toString())));
                if("设备故障".equals(warningType)){
                    countByOne = countByOne + Integer.parseInt(warningTypeCount.get(i).get("sum").toString());
                }
            }
            //将设备故障类型(01,02,03)进行处理
            warningTypeCount = warningTypeCount.stream().filter((Map map) -> !"设备故障".equals(map.get("warningType").toString())).collect(Collectors.toList());
            warningTypeCount.add(getMap(Arrays.asList("sum", "warningType"), Arrays.asList(countByOne, "设备故障")));
            warningTypeNumber =  JSON.parseArray(JSON.toJSONString(warningTypeCount),Map.class).stream().collect(Collectors.toList());
            List<String> type = warningTypeCount.stream().map((Map map) -> map.get("warningType").toString()).collect(Collectors.toList());
            //将没有的预警类型进行补充
            for (String s : types) {
                if(!type.contains(s)){
                    warningTypeCount.add(getMap(Arrays.asList("sum", "warningType"), Arrays.asList(0, s)));
                }
            }
            double sumType = 0;
            for (int i = 0; i < warningTypeNumber.size(); i++) {
                if (i < warningTypeNumber.size() - 1) {
                    double sum1 = Double.parseDouble(String.format("%.2f", Double.parseDouble(warningTypeNumber.get(i).get("sum").toString()) * 100 / sum));
                    warningTypeNumber.get(i).put("sum", sum1);
                    sumType = sumType + sum1;
                } else {
                    warningTypeNumber.get(i).put("sum", Double.parseDouble(String.format("%.2f",100d - sumType)));
                }
            }
            //报警类型统计 各种类型所占总数
            isDeal = earlyWarningDao.findIsDeal(ids, sum);
            String sumDeal = isDeal.get("sumDeal").toString();
            if (sum == 0) {
                isDeal = getMap(Arrays.asList("sumDeal","sumNotDeal", "ratioDeal", "ratioNotDeal"), Arrays.asList(0,0,0,0));
            } else {
                double ratioDeal = Double.parseDouble(isDeal.get("ratioDeal").toString());
                isDeal = getMap(Arrays.asList("sumDeal","sumNotDeal", "ratioDeal", "ratioNotDeal"), Arrays.asList(sumDeal,sum - Integer.parseInt(sumDeal),ratioDeal,100 - ratioDeal));
            }
            List<String> betweenDays = new ArrayList<>();
            String dateType = "";
            //获取统计方式,1:日统计,2:周统计,3:月统计,4:年统计
            if (dayType == 1) {
                //日统计
                betweenDays = GetBetweenDayUtils.getBetweenDays(earlyWarningDto.getBeginTime(), earlyWarningDto.getFinishTime());//对获取日期排序
                dateType = "to_char(warning_time, 'YYYY-MM-DD')";
            } else if (dayType == 2) {
                //周统计
                betweenDays = GetBetweenDayUtils.getBetweenWeek(earlyWarningDto.getBeginTime(), earlyWarningDto.getFinishTime());
                dateType = "to_char(NEXT_DAY(trunc(warning_time),'星期一')，'YYYY-MM-DD')";
            } else if (dayType == 3) {
                //月统计
                betweenDays = GetBetweenDayUtils.getMonthBetweenDate(earlyWarningDto.getBeginTime(), earlyWarningDto.getFinishTime());
                dateType = "to_char(warning_time , 'YYYY-MM')";
            } else if (dayType == 4) {
                //年统计
                betweenDays = GetBetweenDayUtils.getYearBetweenDate(earlyWarningDto.getBeginTime(), earlyWarningDto.getFinishTime());
                dateType = "to_char(warning_time , 'YYYY')";
            }
            //报警数量统计
            warningCount = earlyWarningDao.findWarningCountByDay(ids, dateType);
            List<String> day = warningCount.stream().map((Map timeMap) -> timeMap.get("day").toString()).collect(Collectors.toList());
            for (String betweenDay : betweenDays) {
                if (!day.contains(betweenDay)) {
                    warningCount.add(getMap(Arrays.asList("day", "sum", "deal", "noDeal", "treatmentRate"), Arrays.asList(betweenDay, 0, 0, 0, 0)));
                }
            }
            warningCount.sort((v1, v2) -> v1.get("day").toString().compareTo(v2.get("day").toString()));
            //报警类型统计 各种类型所占总数
            isDealCount = JSON.parseArray(JSON.toJSONString(warningCount), Map.class);
            for (Map map : warningCount) {
                map.remove("deal");
                map.remove("noDeal");
                map.remove("treatmentRate");
            }
            values = Arrays.asList(sum, warningTypeNumber, warningTypeCount, warningCount, isDealCount, isDeal);
        }
        List<String> keys = Arrays.asList("sum", "warningTypeCount", "warningTypeNumber", "warningCount", "isDealCount", "dayCount");
        map1 = getMap(keys, values);
        return new Result(true, StatusCode.OK, "查询报警统计成功",map1);
    }

    //误报
    @Override
    public Integer misreport(Integer id) {
        Integer taskId = earlyWarningDao.findTaskId(id);
        taskManagementDao.delete(taskId);//点击误报时删除相关任务
        return earlyWarningDao.misreport(id);
    }

    @Resource
    private NodeDao nodeDao;

    public EarlyWarningVo getEarlyWarningVo(String warningType,String pidRoute,Integer stackId){
        EarlyWarningVo earlyWarningInfo = new EarlyWarningVo();
        String warningName = "";
        String column = "";
        if(warningType.equals("06") || warningType.equals("08") || warningType.equals("10") || warningType.equals("12") || warningType.equals("13")){
            if(warningType.equals("06")){
                column = "stack_oxygen_upper_one upperOne,stack_oxygen_upper_two upperTwo,stack_oxygen_upper_three upperThree,stack_oxygen_lower lower";
                warningName = "垛内氧气";
            }else if(warningType.equals("08")){
                column = "stack_temp_upper_one as upperOne,stack_temp_upper_two as upperTwo,stack_temp_upper_three as upperThree,stack_temp_lower as lower";
                warningName = "垛内温度";
            }else if(warningType.equals("10")){
                column = "stack_moisture_upper_one as upperOne,stack_moisture_upper_two as upperTwo,stack_moisture_upper_one as upperThree,stack_moisture_lower as lower";
                warningName = "垛内湿度";
            }else if(warningType.equals("12")){
                column = "temp_core_chip_upper_one as upperOne,temp_core_chip_upper_two as upperTwo,temp_core_chip_upper_three as upperThree,temp_core_chip_lower as lower";
                warningName = "包芯温度";
            }else if(warningType.equals("13")){
                column = "core_temp_high_env_temp_one as upperOne,core_temp_high_env_temp_two as upperTwo,core_temp_high_env_temp_three as upperThree,core_temp_low_env_temp as lower";
                warningName = "平均包芯高于环境";
            }
            earlyWarningInfo = alarmValueSettingDao.findAlarmValueSettingByStackId(column,stackId);
        }else if(warningType.equals("07") || warningType.equals("09") || warningType.equals("11")){
            if(warningType.equals("07")){
                column = "warehouse_oxygen_upper_one as upperOne,warehouse_oxygen_upper_two as upperTwo,warehouse_oxygen_upper_three as upperThree,warehouse_oxygen_lower as lower";
                warningName = "仓间氧气";
            }else if(warningType.equals("09")){
                column = "warehouse_temp_upper_one as upperOne,warehouse_temp_upper_two as upperThree,warehouse_temp_upper_three as upperThree,warehouse_temp_lower as lower";
                warningName = "仓间温度";
            }else if(warningType.equals("11")){
                column = "warehouse_moisture_upper_one as upperOne,warehouse_moisture_upper_two as upperTwo,warehouse_moisture_upper_three as upperThree,warehouse_moisture_lower as lower";
                warningName = "仓间湿度";
            }
            List<String> pidRoutes = Arrays.asList(pidRoute.replaceAll("\\[","").replaceAll("\\]","").split(","));
            pidRoutes.sort((v1, v2) -> v2.compareTo(v1));
            //从下往上找
            for (String routes : pidRoutes) {
                String[] route = routes.split(":");
                earlyWarningInfo = wareAreaAlarmSettingDao.findAlarmValueSettingByAreaId(column,route[0]+route[1]);
                if(ToolUtil.isNotEmpty(earlyWarningInfo)) {
                    break;
                }
            }
        }else {
            warningName = "虫数";
            String[] pidRoutes = pidRoute.split(",");
            List<String> siteRoutes = Arrays.asList(pidRoute,pidRoutes[0] + "," + pidRoutes[1] + "," + pidRoutes[2] + "," + pidRoutes[3] + "," + pidRoutes[4],pidRoutes[0] + "," + pidRoutes[1] + "," + pidRoutes[2] + "," + pidRoutes[3],pidRoutes[0] + "," + pidRoutes[1] + "," + pidRoutes[2],pidRoutes[0] + "," + pidRoutes[1]);
            for (String siteRoute : siteRoutes) {
                if (ToolUtil.isEmpty(earlyWarningInfo)) {
                    earlyWarningInfo = pestMonitoringDao.findPestStackMonitor(siteRoute);
                    break;
                }
            }
            if (ToolUtil.isEmpty(earlyWarningInfo)) {
                earlyWarningInfo = pestMonitoringDao.findPestMonitor();
            }
        }
        if(ToolUtil.isNotEmpty(earlyWarningInfo)) {
            earlyWarningInfo.setWarningName(warningName);
        }
        return earlyWarningInfo;
    }

    @Override
    public Result findNodeData(EarlyWarningDto earlyWarningDto) {
        String warningType = earlyWarningDto.getWarningType();
        String pidRoute = earlyWarningDto.getPidRoute();
        Integer stackId = 0;
        Map<String, Object> map = new HashMap<>();
        if(pidRoute.contains("05")){
            stackId = Integer.parseInt(pidRoute.split("05:")[1].split("]")[0]);
            MaintainPlanVo maintainPlanVo = maintainPlanDao.findMaintainPlanNow(stackId);
            if(ToolUtil.isNotEmpty(maintainPlanVo)) {
                List<String> usernames = Arrays.asList(maintainPlanVo.getPrincipalName().split(","));
                String userNames = "";
                for (int i = 0; i < usernames.size(); i++) {
                    if (i == 0) {
                        userNames = (String) redisUtil.hget("USER_" + usernames.get(i), "nickname") + "(" + usernames.get(i) + ")";
                    } else {
                        userNames = userNames + "," + (String) redisUtil.hget("USER_" + usernames.get(i), "nickname") + "(" + usernames.get(i) + ")";
                    }
                }
                maintainPlanVo.setPrincipalName(userNames);
            }else{
                maintainPlanVo = new MaintainPlanVo();
                maintainPlanVo.setMaintenceStyle("---");
                maintainPlanVo.setMaterialCode("---");
                maintainPlanVo.setPhases("---");
                maintainPlanVo.setPrincipalName("---");
                maintainPlanVo.setStackName("---");
            }
            map.put("maintainPlanVo", maintainPlanVo);
        }
        String nodeId = earlyWarningDto.getNodeId();
        EarlyWarningVo earlyWarningVo = getEarlyWarningVo(warningType,pidRoute,stackId);
        Map<String,Object> earlyWarning = new HashMap<>();
        Map<String, String> color = earlyWarningDao.findColor();
        earlyWarning.put("earlyWarningOne",earlyWarningVo.getUpperOne());
        earlyWarning.put("oneColor",color.get("oneColor"));
        earlyWarning.put("earlyWarningTwo",earlyWarningVo.getUpperTwo());
        earlyWarning.put("twoColor",color.get("twoColor"));
        earlyWarning.put("earlyWarningThree",earlyWarningVo.getUpperThree());
        earlyWarning.put("threeColor",color.get("threeColor"));
        earlyWarning.put("earlyWarningLow",earlyWarningVo.getLower());
        map.put("earlyWarningVo", earlyWarning);
        String nodeType = earlyWarningDto.getNodeType();
        List<NodeData> nodeData = new ArrayList<>();
        if("01".equals(nodeType)){
            nodeData = nodeDao.findNodeData(nodeId, "tb_node_o2_history", "oxygen");
        }else if("03".equals(nodeType)){
            switch (warningType){
                case "08":
                    nodeData = nodeDao.findNodeData(nodeId, "tb_node_env_history", "temp");
                    break;
                case "10":
                    nodeData = nodeDao.findNodeData(nodeId, "tb_node_env_history", "humiture");
                    break;
                case "09":
                    nodeData = nodeDao.findNodeData(nodeId, "tb_air_conditioner_history", "temp");
                    break;
                case "11":
                    nodeData = nodeDao.findNodeData(nodeId, "tb_air_conditioner_history", "humiture");
                    break;
            }
        }else{
            if("13".equals(warningType) || "19".equals(warningType)){
                List<String> nodeIds = nodeDao.findNodeIds(stackId);
                List<List<NodeData>> nodeList  = new ArrayList<>();
                int size = 0;
                int a = 0;
                for (int i = 0; i < nodeIds.size(); i++) {
                    List<NodeData> node = nodeDao.findNodeData(nodeIds.get(i), "tb_node_temp_core_chip_history", "temp");
                    if(ToolUtil.isNotEmpty(node)) {
                        nodeList.add(node);
                    }
                    if(size<node.size()){
                        size = node.size();
                        a = i;
                    }
                }
                for (int j = 0; j < size; j++) {
                    Double data = 0d;
                    for (int i = 0; i < nodeList.size(); i++) {
                        if(j>nodeList.get(i).size()-1){
                            data = data + 0;
                        }else {
                            data = data + nodeList.get(i).get(j).getNode();
                        }
                    }
                    NodeData nodeVo = new NodeData();
                    nodeVo.setTransferTime(nodeList.get(a).get(j).getTransferTime());
                    nodeVo.setNode(data / nodeList.size());
                    nodeData.add(nodeVo);
                }
            }else {
                nodeData = nodeDao.findNodeData(nodeId, "tb_node_temp_core_chip_history", "temp");
            }
        }
        switch (nodeType){
            case "01":
                map.put("type", "无线氧气传感器");
                break;
            case "03":
                map.put("type", "无线温湿度传感器");
                break;
            case "04":
                map.put("type", "无线包芯温度传感器");
                break;
            default:
                map.put("type", "");
                break;
        }
        if(ToolUtil.isEmpty(nodeData)){
            map.put("nodeData", new ArrayList<>());
            map.put("maintainPlanVo", new MaintainPlanVo());
            return new Result<>(true, StatusCode.OK, "查询成功", map);
        }
        List<Map> node = new ArrayList<>();
        for (NodeData nodeDatum : nodeData) {
            Map map1 = getMap(Arrays.asList("node","day"),Arrays.asList(nodeDatum.getNode(),nodeDatum.getTransferTime()));
            node.add(map1);
        }
        map.put("nodeData", node);
        return new Result<>(true, StatusCode.OK, "查询成功", map);
    }

    @Override
    public int updateRepairId(List<Map<String,String>> repairInfos) {
        if(ToolUtil.isNotEmpty(repairInfos)){
            String taskId = earlyWarningDao.findMaxId(repairInfos.get(0).get("deviceId"), repairInfos.get(0).get("warningType"));
            TaskDto taskDto = new TaskDto();
            taskDto.setDealMethod("需要提交给运维人员进行维修");
            taskDto.setDealResult("进行运维");
            taskDto.setStatus("1");
            taskDto.setId(taskId);
            taskDto.setPrincipal(repairInfos.get(0).get("repairPrincipal"));
            taskManagementDao.taskDispose(taskDto);
            List<EarlyWarning> earlyWarnings = new ArrayList<>();
            for (Map<String, String> repairInfo : repairInfos) {
                EarlyWarning earlyWarning = new EarlyWarning();
                earlyWarning.setWarningType(repairInfo.get("warningType"));
                earlyWarning.setNodeId(repairInfo.get("deviceId"));
                earlyWarning.setPrincipalName(repairInfo.get("repairPrincipal"));
                earlyWarnings.add(earlyWarning);
            }
            return earlyWarningDao.updateRepairId(earlyWarnings);
        }
        return -1;
    }

    //预警信息
    @Override
    public Result findEarlyWarningInfo(EarlyWarningDto earlyWarningDto) {
        Map<String, Date> month = GetBetweenDayUtils.getMonth(earlyWarningDto.getStartTime(), earlyWarningDto.getEndTime());
        earlyWarningDto.setStartTime(month.get("startTime"));
        earlyWarningDto.setEndTime(month.get("endTime"));
        String warningType = earlyWarningDto.getWarningType();
        List<WarningCountVo> warningCountVos = new ArrayList<>();
        List<Map> mapId = earlyWarningDao.findIdsByWarning(earlyWarningDto);
        List<WarningCountVo> warningCountInfos = new ArrayList<>();
        Map<String, String> color = earlyWarningDao.findColor();
        if(ToolUtil.isNotEmpty(mapId)){
            List<Integer> ids = mapId.stream().map((Map map) -> Integer.parseInt(map.get("id").toString())).collect(Collectors.toList());
            if(ToolUtil.isNotEmpty(warningType)) {
                WarningCountVo warningCountInfo = earlyWarningDao.findEarlyWarningCountByWarningType(ids,ids.size());
                warningCountInfo.setWarningName(getWarningType(warningType).get("warningType"));
                warningCountInfo.setWarningType(warningType);
                warningCountInfo.setColorOne(color.get("oneColor"));
                warningCountInfo.setColorTwo(color.get("twoColor"));
                warningCountInfo.setColorThree(color.get("threeColor"));
                warningCountInfos.add(warningCountInfo);
            }else{
                List<String> types = new ArrayList<>(earlyWarningDao.findWarningTypes(ids));
                List<String> warningTypes = Arrays.asList(String.join(",", types).replace("01,02,03","01").replace("01,02","01").replace("01,03","01").replace("02,03","01").replace("02","01").replace("03","01").split(","));
                for (String type : warningTypes) {
                    if("01".equals(type)){
                        ids = mapId.stream().filter((Map map) -> "01".equals(map.get("warningType").toString()) || "02".equals(map.get("warningType").toString()) || "03".equals(map.get("warningType").toString())).map((Map map) -> Integer.parseInt(map.get("id").toString())).collect(Collectors.toList());
                    }else {
                        ids = mapId.stream().filter((Map map) -> type.equals(map.get("warningType").toString())).map((Map map) -> Integer.parseInt(map.get("id").toString())).collect(Collectors.toList());
                    }
                    WarningCountVo warningCountInfo = earlyWarningDao.findEarlyWarningCountByWarningType(ids,ids.size());
                    warningCountInfo.setWarningName(getWarningType(type).get("warningType"));
                    warningCountInfo.setWarningType(type);
                    warningCountInfo.setColorOne(color.get("oneColor"));
                    warningCountInfo.setColorTwo(color.get("twoColor"));
                    warningCountInfo.setColorThree(color.get("threeColor"));
                    warningCountInfos.add(warningCountInfo);
                }
            }
        }
        return new Result(true, StatusCode.OK, "查询成功",warningCountInfos);
    }

    @Override
    public Result findEarlyWarningByWarningType(EarlyWarningDto earlyWarningDto) {
        String warningType = earlyWarningDto.getWarningType();
        List<EarlyWarningVo> earlyWarningVos = new ArrayList<>();
        if(ToolUtil.isEmpty(warningType)){
            return new Result<>(true, StatusCode.OK, "",earlyWarningVos);
        }
        //设置开始时间和结束时间
        Map<String, Date> month = GetBetweenDayUtils.getMonth(earlyWarningDto.getStartTime(), earlyWarningDto.getEndTime());
        earlyWarningDto.setStartTime(month.get("startTime"));
        earlyWarningDto.setEndTime(month.get("endTime"));
        if("01".equals(warningType)){
            earlyWarningVos = earlyWarningDao.findEarlyWarningByWarningFault(earlyWarningDto);
        }else{
            earlyWarningVos = earlyWarningDao.findEarlyWarningByWarningType(earlyWarningDto);
        }
        if(ToolUtil.isNotEmpty(earlyWarningVos)) {
            Map<String, String> color = earlyWarningDao.findColor();
            earlyWarningVos.forEach(earlyWarningVo -> {
                earlyWarningVo.setNodeName(getNodeType(earlyWarningVo.getNodeType()));
                earlyWarningVo.setColor(getColor(earlyWarningVo.getWarningGrade(), color).get("color"));
            });
        }
        return new Result<>(true, StatusCode.OK, "查询成功",earlyWarningVos);
    }

    @Override
    public Result findEarlyWarningBySiteId(EarlyWarningDto earlyWarningDto) {
        String warningType = earlyWarningDto.getWarningType();
        String isAffirm = earlyWarningDto.getIsAffirm();
        String warningGrade = earlyWarningDto.getWarningGrade();
        String pidRoute = earlyWarningDto.getPidRoute();
        Integer stackId = earlyWarningDto.getStackId();
        Integer areaId = earlyWarningDto.getAreaId();
        String nodeType = earlyWarningDto.getNodeType();
        List<EarlyWarningVo> earlyWarningVos = new ArrayList<>();
        Map<String,Object> map1 = new HashMap<>();
        if(ToolUtil.isEmpty(warningType) || ToolUtil.isEmpty(isAffirm)){
            return new Result<>(false, StatusCode.ERROR, "预警类型或处理状态不能为空",earlyWarningVos);
        }
        //设置开始时间和结束时间
        Map<String, String> time = GetBetweenDayUtils.get(earlyWarningDto.getBeginTime(), earlyWarningDto.getFinishTime());
        earlyWarningDto.setBeginTime(time.get("startTime"));
        earlyWarningDto.setFinishTime(time.get("endTime"));
        List<Map> warningNews  = earlyWarningDao.findWarningDataWarningType(isAffirm,pidRoute,warningType,nodeType);
        List<Map> warningDatas = new ArrayList<>();
        List<Map> warningInfos = new ArrayList<>();
        List<EarlyWarningVo> warningVos = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        List<Map> user = new ArrayList<>();
        if(ToolUtil.isNotEmpty(warningNews)) {
            Map<String, String> typeMap = getWarningType(warningType);
            //最新报警时间
            Map<String, String> newInfo = earlyWarningDao.findNewTime(isAffirm, warningType, pidRoute);
            String principal = newInfo.get("principal");
            map = getMap(Arrays.asList("warningTime","warningName","warningType","taskId","id"),Arrays.asList(newInfo.get("time"),typeMap.get("warningType"),warningType,taskManagementDao.findTaskId(pidRoute, warningType),warningNews.get(0).get("id")));
            Map<String, String> color = earlyWarningDao.findColor();
            if (ToolUtil.isNotEmpty(stackId)) {
                map.put("materialCode", maintainPlanDao.findMaintainMaxIdByStackId(stackId));
                map.put("siteName", warehouseDao.findStackById(stackId).getSiteName());
            } else {
                map.put("siteName", warehouseDao.findWareArea(areaId).getSiteName());
            }
            EarlyWarningVo earlyWarningInfo = getEarlyWarningVo(warningType, pidRoute, stackId);
            for (Map warningData : warningNews) {
                List<String> warningVals = new ArrayList<>();
                int size = 0;
                if ("01".equals(warningData.get("warningType")) || "02".equals(warningData.get("warningType")) || "03".equals(warningData.get("warningType"))) {
                    warningData.put("warningName", getWarningType("0" + warningData.get("warningType").toString()).get("warningType"));
                    warningData.put("deal", getWarningType("0" + warningData.get("warningType").toString()).get("deal"));
                    warningData.remove("warningVal");
                    if(warningInfos.size()==0) {
                        warningInfos.add(getMap(Arrays.asList("name", "warningTime", "warningVal"), Arrays.asList("故障报警", newInfo.get("time"), "故障")));
                    }
                } else {
                    String warningName = earlyWarningInfo.getWarningName();
                    warningVals = Arrays.asList((warningName + "当前值:"+ warningData.get("warningVal").toString()).replaceAll(",",","+warningName + "当前值:").split(","));
                    size = warningVals.size();
                    String name = typeMap.get("name");
                    warningData.put("val", warningName + "上限:" + earlyWarningInfo.getUpperOne());
                    warningData.put("low", warningName + "下限:" + earlyWarningInfo.getLower());
                    Map warningVo = earlyWarningDao.findById(warningData.get("id").toString());
                    if (ToolUtil.isNotEmpty(warningVo.get("warningValOne"))) {
                        Map<String, String> colorMap = getColor("1", color);
                        warningInfos.add(getMap(Arrays.asList("name", "color", "warningTime", "warningVal"), Arrays.asList(colorMap.get("name"), colorMap.get("color"), warningVo.get("warningTimeOne"), warningVo.get("warningValOne") + name)));
                    }
                    if (ToolUtil.isNotEmpty(warningVo.get("warningValTwo"))) {
                        Map<String, String> colorMap = getColor("2", color);
                        warningInfos.add(getMap(Arrays.asList("name", "color", "warningTime", "warningVal"), Arrays.asList(colorMap.get("name"), colorMap.get("color"), warningVo.get("warningTimeTwo"), warningVo.get("warningValTwo") + name)));
                    }
                    if (ToolUtil.isNotEmpty(warningVo.get("warningValThree"))) {
                        Map<String, String> colorMap = getColor("3", color);
                        warningInfos.add(getMap(Arrays.asList("name", "color", "warningTime", "warningVal"), Arrays.asList(colorMap.get("name"), colorMap.get("color"), warningVo.get("warningTimeThree"), warningVo.get("warningValThree") + name)));
                    }
                }
                warningData.put("nodeName", getNodeType(warningData.get("nodeType").toString()));
                List<String> nodeIds = new ArrayList<>();
                List<Map> maps = new ArrayList<>();
                if (!"13".equals(warningType) && !"19".equals(warningType)) {
                    nodeIds = Arrays.asList(warningData.get("nodeId").toString().split(","));
                    maps = earlyWarningDao.findsiteXYZByNodeIds(nodeIds);
                    size = nodeIds.size();
                }
                for (int i = 0; i < size; i++) {
                    Map warningInfo = new HashMap();
                    warningInfo.putAll(warningData);
                    if (ToolUtil.isNotEmpty(warningVals)) {
                        warningInfo.put("warningVal", warningVals.get(i));
                    }
                    if (ToolUtil.isNotEmpty(nodeIds)) {
                        warningInfo.put("nodeId", nodeIds.get(i));
                        if (ToolUtil.isNotEmpty(maps)) {
                            warningInfo.putAll(maps.get(i));
                        }
                    }
                    warningDatas.add(warningInfo);
                }
            }
            List<String> principals = Arrays.asList(principal.split(","));
            for (String name : principals) {
                Map<String, String> userName = new HashMap<>();
                String phonenumber = (String) redisUtil.hget("USER_" + name, "phonenumber");
                userName.put("siteRoute", pidRoute);
                userName.put("username", name);
                userName.put("number", phonenumber);
                user.add(userName);
            }
        }
        map1.put("warningInfo",warningInfos);
        map1.put("earlyWarningVo",map);
        map1.put("warningDatas",warningDatas);
        map1.put("user",user);
        return new Result<>(true, StatusCode.OK, "查询成功",map1);
    }

    public Map<String,String> getColor(String warningGrade,Map<String, String> color){
        Map<String,String> map = new HashMap<>();
        switch (warningGrade){
            case "1":
                map.put("color",color.get("oneColor"));
                map.put("name", "一级预警");
                break;
            case "2":
                map.put("color",color.get("twoColor"));
                map.put("name", "二级预警");
                break;
            case "3":
                map.put("color",color.get("threeColor"));
                map.put("name", "三级预警");
                break;
        }
        return map;
    }
    public String getNodeType(String nodeType){
        switch (nodeType) {
            case "01":
                nodeType = "无线氧气传感器";
                break;
            case "03":
                nodeType =  "无线温湿度传感器";
                break;
            case "04":
                nodeType =  "无线包芯温度传感器";
                break;
            default:
                nodeType =  "虫情板";
                break;
        }
        return nodeType;
    }
    public Map<String,String> getWarningType(String warningType){
        Map<String,String> map = new HashMap<>();
        map.put("name","");
        switch (warningType) {
            case "001":
                map.put("warningType", "设备电量低");
                map.put("deal","建议更换电池");
                return map;
            case "002":
                map.put("warningType", "设备信号强度差");
                map.put("deal","增强网关信号强度");
                return map;
            case "003":
                map.put("warningType", "设备丢包");
                map.put("deal","增强网关信号强度");
                return map;
            case "04":
                map.put("warningType","垛内虫数预警");
                return map;
            case "05":
                map.put("warningType","仓间虫数预警");
                return map;
            case "06":
                map.put("warningType","垛内氧气预警");
                map.put("name","%");
                return map;
            case "07":
                map.put("warningType","仓间氧气预警");
                map.put("name","%");
                return map;
            case "08":
                map.put("warningType","垛内温度预警");
                map.put("name","℃");
                return map;
            case "09":
                map.put("warningType","仓间温度预警");
                map.put("name","℃");
                return map;
            case "10":
                map.put("warningType","垛内湿度预警");
                map.put("name","%");
                return map;
            case "11":
                map.put("warningType","仓间湿度预警");
                map.put("name","%");
                return map;
            case "12":
                map.put("warningType","包芯温度预警");
                map.put("name","℃");
                return map;
            case "13":
                map.put("warningType","平均包芯高于环境预警");
                map.put("name","℃");
                return map;
            case "14":
                map.put("warningType","仓间粉螟预警");
                return map;
            case "19":
                map.put("warningType","平均包芯低于环境预警");
                return map;
            default:
                map.put("warningType","设备故障");
                return map;
        }
    }

    public Map<String,Object> getMap(List<String> keys,List<Object> values){
        Map<String,Object> map = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            map.put(keys.get(i),values.get(i));
        }
        return map;
    }

    @Override
    public List<RepairDataVo> getRepairData(Integer stackId,Integer areaId){
        List<RepairDataVo> repairDataInfos = earlyWarningDao.findRepairData(stackId, areaId);
        for (RepairDataVo repairDataInfo : repairDataInfos) {
            repairDataInfo.setRepairReason(getWarningType("0"+repairDataInfo.getRepairReason()).get("warningType"));
        }
        return repairDataInfos;
    }

    /*
     * 短信推送
     * */
    @Override
    public void warningSend(String number,String username,String siteRoute){
        //earlyWarningDao.findThreeBySiteRoute()
        String message = "123434";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map map = new HashMap();
        map.put("username",username);
        map.put("type", "16");
        map.put("data", "发送养护预警短信内容");
        map.put("phone", number);
        map.put("createTime",sdf.format(new Date()));
        if(message.length()<=460){
            get(message,map,number);
        }else if(message.length()>460 && message.length()<=920){
            String oneMessage = message.substring(0,460);
            get(oneMessage,map,number);
            String twoMessage = message.substring(460,message.length());
            get(twoMessage,map,number);
        }else if(message.length()>920 && message.length()<=1380){
            String oneMessage = message.substring(0,460);
            get(oneMessage,map,number);
            String twoMessage = message.substring(460,960);
            get(twoMessage,map,number);
            String threeMessage = message.substring(960,message.length());
            get(threeMessage,map,number);
        }
    }

    public void get(String message,Map map,String number) {
        try {
            map.put("content", message);
            //SendMessageUtil.sendSms(number, message, "h2");
            //系统标识,标识是哪个系统发送的消息,养护系统发送
            rocketMQTemplate.send(topic + ":" , MessageBuilder.withPayload(map).build()) ;
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map findAppWarningIndex(){
        Map<String, Object> map = new HashMap<>();
        map.put("warningCount",earlyWarningDao.findAppCount());
        List<Map> warningTypeCount = new ArrayList<>();
        warningTypeCount.add(earlyWarningDao.findAppDeviceFault());
        List<String> names = Arrays.asList("垛内虫数预警", "仓间甲虫预警", "垛内氧气预警","仓间氧气预警","垛内温度预警","仓间温度预警","垛内湿度预警",
                "仓间湿度预警", "包芯温度预警", "平均包芯高于环境预警", "仓间粉螟预警", "平均包芯低于环境预警");
        List<String> types = Arrays.asList("04","05","06","07","08","09","10","11","12","13","14","19");
        for (int i = 0; i < types.size(); i++) {
            Map appWarning = new HashMap();
            String typeCount = earlyWarningDao.findAppWarning(types.get(i));
            appWarning.put("warningType",types.get(i));
            appWarning.put("warningName",names.get(i));
            appWarning.put("warningTypeCount",typeCount);
            warningTypeCount.add(appWarning);
        }
        map.put("warningTypeCount",warningTypeCount);
        return map;
    }

    @Override
    public Map findAppWarningInfoByWarningType(String warningType, String warningGrade){
        Map<String, Object> map = new HashMap<>();
        if("04".equals(warningGrade)){
            warningGrade = null;
        }
        String appCount = earlyWarningDao.findAppCountByWarningType(warningType, warningGrade);
        if(ToolUtil.isEmpty(appCount)){
            map.put("count", "0");
            map.put("warningInfo",new ArrayList<>());
            return map;
        }
        List<Map> warningInfos = earlyWarningDao.findAppWarningInfoByWarningType(warningType, warningGrade);
        for (Map warningInfo : warningInfos) {
            List<String> principals = Arrays.asList(warningInfo.get("principal").toString().split(","));
            String username = "";
            for (int i = 0; i < principals.size(); i++) {
                if(i==0){
                    username = principals.get(i) +"/"+redisUtil.hget("USER_"+principals.get(i),"phonenumber").toString();
                }else{
                    username = username + ","+ principals.get(i) +"/"+redisUtil.hget("USER_"+principals.get(i),"phonenumber").toString();
                }
            }
            warningInfo.put("username",username);
            if("13".equals(warningType) || "19".equals(warningType)){
                List<String> nodeIds = nodeDao.findNodeBySiteRoute(warningInfo.get("siteRoute").toString(),"04");
                Double coreTemp = 0d;
                for (String nodeId : nodeIds) {
                    coreTemp = coreTemp + Double.parseDouble(redisUtil.hget("MAINTAIN_NODE_NEW",nodeId).toString());
                }
                List<String> nIds = nodeDao.findNodeBySiteRoute(warningInfo.get("siteRoute").toString(),"03");
                Double temp = 0d;
                for (String nodeId : nIds) {
                    temp = temp + Double.parseDouble(redisUtil.hget("MAINTAIN_NODE_NEW",nodeId+"01").toString());
                }
                warningInfo.put("nowData",coreTemp/nodeIds.size() - temp/nIds.size());
                warningInfo.put("nodeId",null);
            } else {
                List<String> nodeIds = Arrays.asList(warningInfo.get("nodeId").toString().split(","));
                int i = Arrays.asList(warningInfo.get("grade").toString().split(",")).indexOf(warningInfo.get("warningGrade"));
                String nodeId = nodeIds.get(i);
                if("01".equals(warningType) || "02".equals(warningType) || "03".equals(warningType)){
                    warningInfo.put("nodeName",getNodeType(warningInfo.get("nodeType").toString()));
                    warningInfo.put("nodeId",null);
                }else if("04".equals(warningType) || "05".equals(warningType) || "14".equals(warningType)){
                    Map pestCount = earlyWarningDao.findPestCount(nodeId);
                    if("04".equals(warningType) || "05".equals(warningType)){
                        warningInfo.put("nowData",pestCount.get("beetle"));
                    }else{
                        warningInfo.put("nowData",pestCount.get("mealworm"));
                    }
                    warningInfo.put("nodeId",null);
                }else{
                    warningInfo.put("nodeId",null);
                    if("06".equals(warningType) || "07".equals(warningType) || "12".equals(warningType)){
                        warningInfo.put("nowData",redisUtil.hget("MAINTAIN_NODE_NEW",nodeId));
                        if("12".equals(warningType) || "07".equals(warningType)){
                            warningInfo.put("nodeId",nodeId);
                        }
                    }else if("08".equals(warningType) || "09".equals(warningType)){
                        warningInfo.put("nowData",redisUtil.hget("MAINTAIN_NODE_NEW",nodeId+"01"));
                    }else{
                        warningInfo.put("nowData",redisUtil.hget("MAINTAIN_NODE_NEW",nodeId+"02"));
                    }
                }
            }
            warningInfo.remove("principal");
            warningInfo.remove("grade");
        }
        map.put("warningInfo",warningInfos);
        map.put("count", appCount);
        return map;
    }

    @Override
    public Map findAppWarningInfoBySiteRoute(String siteRoute,String warningType,String warningGrade,String nowData,String warningVal,String nodeType,String warningTime,String id){
        Map map = new HashMap();
        String principal = earlyWarningDao.findAppPrincipalById(id);
        List<String> principals = Arrays.asList(principal.split(","));
        List<Map> userMap = new ArrayList<>();
        for (String name : principals) {
            Map<String, String> userName = new HashMap<>();
            userName.put("username", (String)redisUtil.hget("USER_"+name,"nickname") + "(" + name + ")");
            userName.put("roleName", (String)redisUtil.hget("USER_"+name,"roleName"));
            userMap.add(userName);
        }
        map.put("userBase",userMap);
        //if()
        Map baseMap = new HashMap();
        baseMap.put("warningGrade",warningGrade);
        baseMap.put("warningVal",warningVal);
        baseMap.put("warningTime",warningTime);
        Integer stackId=null;
        if(siteRoute.contains("05")){
            stackId = Integer.parseInt(siteRoute.split("05:")[1].split("]")[0]);
            baseMap.put("siteName",warehouseDao.findStackById(stackId).getSiteName());
        }else{
            baseMap.put("siteName",warehouseDao.findWareArea(Integer.parseInt(siteRoute.split("04:")[1].split("]")[0])).getSiteName());
        }
        map.put("base",baseMap);
        Map warningMap = new HashMap();
        warningMap.put("nowData",nowData);
        Map<String, String> color = earlyWarningDao.findColor();
        EarlyWarningVo earlyWarningVo = getEarlyWarningVo(warningType, siteRoute, stackId);
        warningMap.put("earlyWarningOne",earlyWarningVo.getUpperOne());
        warningMap.put("oneColor",color.get("oneColor"));
        warningMap.put("earlyWarningTwo",earlyWarningVo.getUpperTwo());
        warningMap.put("twoColor",color.get("twoColor"));
        warningMap.put("earlyWarningThree",earlyWarningVo.getUpperThree());
        warningMap.put("threeColor",color.get("threeColor"));
        warningMap.put("earlyWarningLow",earlyWarningVo.getLower());
        map.put("warningMap", warningMap);
        return map;
    }
}
