package com.rpzh.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.*;
import com.rpzh.dao.*;
import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.entity.*;
import com.rpzh.enums.*;
import com.rpzh.pojo.NodeData;
import com.rpzh.service.EarlyWarningService;
import com.rpzh.util.*;
import com.rpzh.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
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
    private static final String USER = "USER_";
    @Autowired
    private RedisUtil redisUtil;
    @Override
    public PageInfo<EarlyWarningVo> findHistoryWarning(EarlyWarningDto earlyWarningDto) {
        String pidRoute = earlyWarningDto.getPidRoute();
        Integer stackId = earlyWarningDto.getStackId();
        List<EarlyWarningVo> earlyWarningInfos = new ArrayList<>();
        if(ToolUtil.isNotEmpty(earlyWarningDto.getEndTime())){
            earlyWarningDto.setEndTime(new Date(earlyWarningDto.getEndTime().getTime() + 24 * 60 * 60 * 1000-1));
        }
        if(StringUtils.isNotEmpty(pidRoute) && ToolUtil.isNotEmpty(stackId)){
            earlyWarningDto.setPidRoute(SiteRouteUtil.route(pidRoute,stackId+""));
        }
        PageHelper.startPage(earlyWarningDto.getPage(), earlyWarningDto.getSize());
        earlyWarningInfos = earlyWarningDao.findHistoryWarningData(earlyWarningDto);
        return new PageInfo<>(earlyWarningInfos);
    }

    //预警统计
    @Override
    public Result findWarningCount(EarlyWarningDto earlyWarningDto) {
        String pidRoute = earlyWarningDto.getPidRoute();
        Integer siteId = earlyWarningDto.getSiteId();
        Map<String, String> time = GetBetweenDayUtils.get(earlyWarningDto.getBeginTime(), earlyWarningDto.getFinishTime());
        //设置开始时间
        earlyWarningDto.setBeginTime(time.get("startTime"));
        //设置结束时间
        earlyWarningDto.setFinishTime(time.get("endTime"));
        Integer dayType = earlyWarningDto.getDayType();
        if(dayType==0){
            return new Result(false, StatusCode.ERROR, "日期类型不能为空");
        }
        //根据位置路由和位置id拼接完整的位置路由
        if(StringUtils.isNotEmpty(pidRoute) && ToolUtil.isNotEmpty(siteId)) {
            earlyWarningDto.setPidRoute(SiteRouteUtil.route(pidRoute,siteId+""));
        }
        //查询满足条件的所有id
        List<Integer> ids = earlyWarningDao.findIds(earlyWarningDto);
        Map<String,Object> map1 = new HashMap<>();
        List<Map> warningCount = new ArrayList<>();
        List<Map> isDealCount = new ArrayList<>();
        List<Map> warningTypeCount = new ArrayList<>();
        List<Map> warningTypeNumber = new ArrayList<>();
        List<Object> values;
        List<String> types = Arrays.asList("设备故障", "垛内虫数预警", "仓间粉螟预警", "平均包芯高于环境预警", "包芯温度预警", "仓间湿度预警", "垛内湿度预警", "仓间温度预警", "垛内温度预警", "仓间氧气预警", "垛内氧气预警", "仓间虫数预警");
        if(ToolUtil.isEmpty(ids)){
            warningTypeCount.add(getMap(Arrays.asList("warningType,sum".split(",")),Arrays.asList("0,0".split(","))));
            for (String type : types) {
                Map map = new HashMap();
                map.put(type,0);
                warningTypeNumber.add(map);
            }
            warningCount.add(getMap(Arrays.asList("day,sum".split(",")),Arrays.asList("0,0".split(","))));
            isDealCount.add(getMap(Arrays.asList("day,sum,deal,treatmentRate,noDeal".split(",")),Arrays.asList("0,0,0,0,0".split(","))));
            Map countMap = getMap(Arrays.asList("ratioDeal,sumDeal,sumNotDeal,ratioNotDeal".split(",")),Arrays.asList("0,0,0,0".split(",")));
            values = Arrays.asList(0,warningTypeCount,warningTypeNumber,warningCount,isDealCount,countMap);
        }else {
            //查询预警总数量
            Integer sum = ids.size();
            //报警类型统计 各种类型所占比例
            warningTypeCount = earlyWarningDao.findWarningTypeCount(ids);
            Integer countByOne = 0;
            for (int i = 0; i < warningTypeCount.size(); i++) {
                String warningType = getWarningType(warningTypeCount.get(i).get("warningType").toString()).get("warningType").toString();
                warningTypeCount.get(i).put("warningType",warningType);
                warningTypeCount.get(i).put("sum", Math.round(Double.parseDouble(warningTypeCount.get(i).get("sum").toString())));
                if("设备故障".equals(warningType)){
                    countByOne = countByOne + Integer.parseInt(warningTypeCount.get(i).get("sum").toString());
                }
            }
            warningTypeCount = warningTypeCount.stream().filter((Map map) -> !"设备故障".equals(map.get("warningType").toString())).collect(Collectors.toList());
            warningTypeCount.add(getMap(Arrays.asList("sum", "warningType"), Arrays.asList(countByOne, "设备故障")));
            List<String> type = warningTypeCount.stream().map((Map map) -> map.get("warningType").toString()).collect(Collectors.toList());
            for (String s : types) {
                if(!type.contains(s)){
                    warningTypeCount.add(getMap(Arrays.asList("sum", "warningType"), Arrays.asList(0, s)));
                }
            }
            warningTypeNumber = JSON.parseArray(JSON.toJSONString(warningTypeCount), Map.class).stream().filter((Map map) -> !"0".equals(map.get("sum").toString())).collect(Collectors.toList());
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
            Map isDeal = earlyWarningDao.findIsDeal(ids, sum);
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
                dateType = "DATE_FORMAT(warning_time,'%Y-%m-%d' )";
            } else if (dayType == 2) {
                //周统计
                betweenDays = GetBetweenDayUtils.getBetweenWeek(earlyWarningDto.getBeginTime(), earlyWarningDto.getFinishTime());
                dateType = "DATE_FORMAT(DATE_SUB(warning_time,INTERVAL DATE_FORMAT(warning_time,'%w')-1 DAY),'%Y-%m-%d' )";
            } else if (dayType == 3) {
                //月统计
                betweenDays = GetBetweenDayUtils.getMonthBetweenDate(earlyWarningDto.getBeginTime(), earlyWarningDto.getFinishTime());
                dateType = "DATE_FORMAT(warning_time, '%Y-%m')";
            } else if (dayType == 4) {
                //年统计
                betweenDays = GetBetweenDayUtils.getYearBetweenDate(earlyWarningDto.getBeginTime(), earlyWarningDto.getFinishTime());
                dateType = "DATE_FORMAT(warning_time, '%Y')";
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

    @Override
    public Integer misreport(Integer id) {
        return earlyWarningDao.misreport(id);
    }

    @Resource
    private NodeDao nodeDao;

    public EarlyWarningVo getEarlyWarningVo(String warningType,String pidRoute,Integer stackId){
        EarlyWarningVo earlyWarningInfo = new EarlyWarningVo();
        String warningName = "";
        String column = "";
        if(warningType.equals("06") || warningType.equals("08") || warningType.equals("10") || warningType.equals("12") || warningType.equals("13")){
            //04:垛内甲虫预警,06:垛内氧气预警,08:垛内温度预警,10:垛内湿度预警,12:包芯温度预警,13:平均包芯高于环境预警
            if(warningType.equals("06")){
                column = "stack_oxygen_upper_one as upperOne,stack_oxygen_upper_two as upperTwo,stack_oxygen_upper_three as upperThree,stack_oxygen_lower as lower";
                warningName = "垛内氧气";
            }else if(warningType.equals("08")){
                column = "stack_temp_upper_one as upperOne,stack_temp_upper_two as upperTwo,stack_temp_upper_three as upperThree,stack_temp_upper_lower as lower";
                warningName = "垛内温度";
            }else if(warningType.equals("10")){
                column = "stack_moisture_upper_one as upperOne,stack_moisture_upper_two as upperTwo,stack_moisture_upper_three as upperThree,stack_moisture_upper_lower as lower";
                warningName = "垛内湿度";
            }else if(warningType.equals("12")){
                column = "temp_core_chip_upper_one as upperOne,temp_core_chip_upper_two as upperTwo,temp_core_chip_upper_three as upperThree,temp_core_chip_upper_lower as lower";
                warningName = "包芯温度";
            }else{
                column = "core_temp_high_env_temp_one as upperOne,core_temp_high_env_temp_two as upperTwo,core_temp_high_env_temp_three as upperThree,core_temp_low_env_temp as lower";
                warningName = "平均包芯高于环境";
            }
            earlyWarningInfo = alarmValueSettingDao.findAlarmValueSettingByStackId(column,stackId);
        }else if(warningType.equals("07") || warningType.equals("09") || warningType.equals("11")){
            if(warningType.equals("07")){
                column = "warehouse_oxygen_upper_one as upperOne,warehouse_oxygen_upper_two as upperTwo,warehouse_oxygen_upper_three as upperThree,warehouse_oxygen_lower as lower";
                warningName = "仓间氧气";
            }else if(warningType.equals("09")){
                column = "warehouse_temp_upper_one as upperOne,warehouse_temp_upper_two as upperTwo,warehouse_temp_upper_three as upperThree,warehouse_temp_upper_lower as lower";
                warningName = "仓间温度";
            }else{
                column = "warehouse_moisture_upper_one as upperOne,warehouse_moisture_upper_two as upperTwo,warehouse_moisture_upper_three as upperThree,warehouse_moisture_upper_lower as lower";
                warningName = "仓间湿度";
            }
            earlyWarningInfo = wareAreaAlarmSettingDao.findAlarmValueSettingByStackId(column,pidRoute);
        }else {
            warningName = "虫数";
            earlyWarningInfo = pestMonitoringDao.findPestStackMonitor(pidRoute);
            String[] pidRoutes = pidRoute.split(",");
            if (ToolUtil.isEmpty(earlyWarningInfo)) {
                if (ToolUtil.isNotEmpty(stackId)) {
                    pidRoute = pidRoutes[0] + "," + pidRoutes[1] + "," + pidRoutes[2] + "," + pidRoutes[3] + "," + pidRoutes[4];
                    earlyWarningInfo = pestMonitoringDao.findPestStackMonitor(pidRoute);
                }
            }
            if (ToolUtil.isEmpty(earlyWarningInfo)) {
                pidRoute = pidRoutes[0] + "," + pidRoutes[1] + "," + pidRoutes[2] + "," + pidRoutes[3];
                earlyWarningInfo = pestMonitoringDao.findPestStackMonitor(pidRoute);
            }
            if (ToolUtil.isEmpty(earlyWarningInfo)) {
                pidRoute = pidRoutes[0] + "," + pidRoutes[1] + "," + pidRoutes[2];
                earlyWarningInfo = pestMonitoringDao.findPestStackMonitor(pidRoute);
            }
            if (ToolUtil.isEmpty(earlyWarningInfo)) {
                pidRoute = pidRoutes[0] + "," + pidRoutes[1];
                earlyWarningInfo = pestMonitoringDao.findPestStackMonitor(pidRoute);
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
        Integer stackId = earlyWarningDto.getStackId();
        Map<String, Object> map = new HashMap<>();
        String nodeId = earlyWarningDto.getNodeId();
        String time = earlyWarningDao.findMaxTime(nodeId);
        EarlyWarningVo earlyWarningVo = getEarlyWarningVo(warningType,pidRoute,stackId);
        Map<String,Object> earlyWarning = new HashMap<>();
        earlyWarning.put("earlyWarningOne",earlyWarningVo.getUpperOne());
        earlyWarning.put("earlyWarningTwo",earlyWarningVo.getUpperTwo());
        earlyWarning.put("earlyWarningThree",earlyWarningVo.getUpperThree());
        earlyWarning.put("earlyWarningLow",earlyWarningVo.getLower());
        map.put("earlyWarningVo", earlyWarning);
        NodeTypeEnum nodeType = NodeTypeEnum.getNodeType(earlyWarningDto.getNodeType());
        List<NodeData> nodeData = nodeDao.findNodeData(nodeId, nodeType.getTableName(), nodeType.getTableColumn());
        map.put("nodeData", nodeData);
        map.put("time", time);
        return new Result<>(true, StatusCode.OK, "查询成功,无预警信息", map);
    }

    //预警信息
    @Override
    public Result findEarlyWarningInfo(EarlyWarningDto earlyWarningDto) {
        Map<String, Date> month = GetBetweenDayUtils.getMonth(earlyWarningDto.getStartTime(), earlyWarningDto.getEndTime());
        earlyWarningDto.setStartTime(month.get("startTime"));
        earlyWarningDto.setEndTime(month.get("endTime"));
        String warningType = earlyWarningDto.getWarningType();
        List<WarningCountVo> warningCount = new ArrayList<>();
        if(ToolUtil.isNotEmpty(warningType)){
            switch (warningType){
                case "01":
                    warningCount = earlyWarningDao.findEarlyWarningCountByOne(earlyWarningDto);
                    break;
                default:
                    warningCount = earlyWarningDao.findEarlyWarningCountByNotOne(earlyWarningDto);
                    break;

            }
        }else{
            //查询设备故障的个数
            WarningCountVo warningCountVo = earlyWarningDao.findEarlyWarningCountByOne(earlyWarningDto).get(0);
            //查询除设备故障外预警的个数
            warningCount = earlyWarningDao.findEarlyWarningCount(earlyWarningDto);
            if(warningCountVo.getOneWarningCount()!=0 || warningCountVo.getTwoWarningCount()!=0 || warningCountVo.getThreeWarningCount()!=0){
                warningCount.add(0,warningCountVo);
            }
        }
        Map<String, String> color = earlyWarningDao.findColor();
        warningCount.forEach(count ->{
            count.setWarningName(getWarningType(count.getWarningType()).get("warningType"));
            count.setColorOne(color.get("oneColor"));
            count.setColorTwo(color.get("twoColor"));
            count.setColorThree(color.get("threeColor"));
        });
        return new Result<>(true, StatusCode.OK, "查询成功",warningCount);
    }

    @Override
    public Result findEarlyWarningByWarningType(EarlyWarningDto earlyWarningDto) {
        String warningType = earlyWarningDto.getWarningType();
        List<EarlyWarningVo> earlyWarningVos = new ArrayList<>();
        if(ToolUtil.isEmpty(warningType)){
            return new Result<>(false, StatusCode.ERROR, "预警类型不能为空",earlyWarningVos);
        }
        //设置开始时间和结束时间
        Map<String, Date> month = GetBetweenDayUtils.getMonth(earlyWarningDto.getStartTime(), earlyWarningDto.getEndTime());
        earlyWarningDto.setStartTime(month.get("startTime"));
        earlyWarningDto.setEndTime(month.get("endTime"));
        Map<String, String> color = earlyWarningDao.findColor();
        if(warningType.equals("01")){
            earlyWarningVos = earlyWarningDao.findEarlyWarningByOne(earlyWarningDto);
            earlyWarningVos.forEach(earlyWarningVo -> {
                earlyWarningVo.setWarningNews("设备故障");
                earlyWarningVo.setWarningType("01");
            });
        }else{
            earlyWarningVos = earlyWarningDao.findEarlyWarningByNotOne(earlyWarningDto);
        }
        earlyWarningVos.forEach(earlyWarningVo -> {
            switch (earlyWarningVo.getAffirm()){
                case "0":
                    earlyWarningVo.setAffirmName("已处理");
                    break;
                case "1":
                    earlyWarningVo.setAffirmName("正在处理");
                    break;
                case "2":
                    earlyWarningVo.setAffirmName("未确认");
                    break;
            }
            earlyWarningVo.setNodeName(getNodeType(earlyWarningVo.getNodeType()));
            earlyWarningVo.setColor(getColor(earlyWarningVo.getWarningGrade(), color).get("color"));
        });
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
        List<EarlyWarningVo> earlyWarningVos = new ArrayList<>();
        if(ToolUtil.isEmpty(warningType) || ToolUtil.isEmpty(isAffirm)){
            return new Result<>(false, StatusCode.ERROR, "预警类型或处理状态不能为空",earlyWarningVos);
        }
        //设置开始时间和结束时间
        Map<String, String> time = GetBetweenDayUtils.get(earlyWarningDto.getBeginTime(), earlyWarningDto.getFinishTime());
        earlyWarningDto.setBeginTime(time.get("startTime"));
        earlyWarningDto.setFinishTime(time.get("endTime"));
        //最新报警时间
        String newTime = earlyWarningDao.findNewTime(isAffirm, warningGrade, warningType, pidRoute);
        List<Map<String,String>> user = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        List<Map> warningDatas = new ArrayList<>();
        List<Map> warningInfos = new ArrayList<>();
        if (warningType.equals("01")){
            warningDatas = earlyWarningDao.findWarningDataWarningTypeIsOne(isAffirm,pidRoute);
        }else{
            warningDatas = earlyWarningDao.findWarningDataWarningTypeIsNotOne(isAffirm,warningType, pidRoute);
        }
        if(ToolUtil.isNotEmpty(warningDatas)) {
            String siteName = "";
            String materialCode = "";
            if(ToolUtil.isNotEmpty(stackId)){
                materialCode = maintainPlanDao.findMaintainMaxIdByStackId(stackId);
                siteName = warehouseDao.findStackById(stackId).getSiteName();
            }
            if(ToolUtil.isNotEmpty(areaId)){
                siteName = warehouseDao.findWareArea(areaId).getSiteName();
            }
            Map<String, String> typeMap = getWarningType(warningType);
            map = getMap(Arrays.asList("warningTime","materialCode","siteName","warningName"),Arrays.asList(newTime,materialCode,siteName, typeMap.get("warningType")));
            String name = typeMap.get("name");
            List<Integer> ids = new ArrayList<>();
            warningDatas.forEach(warningData -> {
                ids.add(Integer.parseInt(warningData.get("id").toString()));
                switch (warningData.get("warningType").toString()) {

                }
                warningData.put("nodeType", getNodeType(warningData.get("nodeType").toString()));
                if (!"01".equals(warningType)) {
                    EarlyWarningVo earlyWarningInfo = getEarlyWarningVo(warningType, pidRoute, stackId);
                    String warningName = earlyWarningInfo.getWarningName();
                    warningData.put("warningVal", warningName + "当前值:" + warningData.get("warningVal").toString());
                    switch (warningData.get("warningGrade").toString()){
                        case "1":
                            warningData.put("val", warningName + "上限:" + earlyWarningInfo.getUpperOne());
                            break;
                        case "2":
                            warningData.put("val", warningName + "上限:" + earlyWarningInfo.getUpperTwo());
                            break;
                        case "3":
                            warningData.put("val", warningName + "上限:" + earlyWarningInfo.getUpperThree());
                            break;
                    }
                    warningData.put("low", warningName + "下限:" + earlyWarningInfo.getLower());
                }
            });
            List<EarlyWarningVo> warningVos = earlyWarningDao.findByIds(ids);
            Map<String, String> color = earlyWarningDao.findColor();
            if (!warningType.equals("01")) {
                String finalName = name;
                warningVos.forEach(warningVo -> {
                    Map warningInfo = new HashMap();
                    Map<String, String> colorMap = getColor(warningVo.getWarningGrade(), color);
                    warningInfo.put("name", colorMap.get("name"));
                    warningInfo.put("color",colorMap.get("color"));
                    warningInfo.put("warningTime", warningVo.getWarningTime());
                    warningInfo.put("warningVal",warningVo.getWarningVal()+ finalName);
                    warningInfos.add(warningInfo);
                });
            } else {
                warningVos.forEach(warningVo -> {
                    Map warningInfo = new HashMap();
                    warningInfo.put("warningTime", warningVo.getWarningTime());
                    warningInfo.put("warningType", "故障报警");
                    warningInfo.put("warningNews", "01");
                    warningInfos.add(warningInfo);
                });
            }
            List<String> usernames = Arrays.asList(earlyWarningDao.findPrincipal(earlyWarningDto).split(","));
            usernames.forEach(username -> {
                Map<String,String> userName = new HashMap<>();
                String phonenumber = (String)redisUtil.hget(USER + username, "phonenumber");
                userName.put("username",username);
                userName.put("number",phonenumber);
                user.add(userName);
            });
        }
        Map<String,Object> map1 = new HashMap<>();
        map1.put("warningInfo",warningInfos);
        map1.put("earlyWarningVo",map);
        map1.put("warningDatas",warningDatas);
        map1.put("user",user);
        return new Result<>(true, StatusCode.OK, "查询成功",map1);
    }

    public Map<String,String> getColor(Integer warningGrade,Map<String, String> color){
        Map<String,String> map = new HashMap<>();
        switch (warningGrade){
            case 1:
                map.put("color",color.get("oneColor"));
                map.put("name", "一级预警");
                break;
            case 2:
                map.put("color",color.get("twoColor"));
                map.put("name", "二级预警");
                break;
            case 3:
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
        }
        return nodeType;
    }
    public Map<String,String> getWarningType(String warningType){
        Map<String,String> map = new HashMap<>();
        map.put("name","");
        switch (warningType) {
            /*case "01":
                warningData.put("warningType", "设备电量电量低");
                break;
            case "02":
                warningData.put("warningType", "设备信号强度差");
                break;
            case "03":
                warningData.put("warningType", "设备丢包");
                break;*/
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
}
