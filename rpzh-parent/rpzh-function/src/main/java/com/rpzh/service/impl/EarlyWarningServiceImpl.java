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
import org.springframework.data.redis.core.RedisTemplate;
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

    @Override
    public PageInfo<EarlyWarningVo> findHistoryWarning(EarlyWarningDto earlyWarningDto) {
        String pidRoute = earlyWarningDto.getPidRoute();
        Integer stackId = earlyWarningDto.getStackId();
        List<EarlyWarningVo> earlyWarningInfos = new ArrayList<>();
        Date endTime = earlyWarningDto.getEndTime();
        if(ToolUtil.isNotEmpty(endTime)){
            earlyWarningDto.setEndTime(new Date(endTime.getTime() + 24 * 60 * 60 * 1000));
        }
        if(StringUtils.isNotEmpty(pidRoute)){
            int length = pidRoute.split(",").length;
            List<Integer> stackIds = new ArrayList<>();
            switch (length){
                case 4:
                    stackIds = warehouseDao.findStackByAreId(stackId);
                    if(ToolUtil.isEmpty(stackIds)){
                        stackIds.add(0);
                    }
                    earlyWarningDto.setStackIds(stackIds);
                    break;
                case 5:
                    stackIds.add(stackId);
                    earlyWarningDto.setStackIds(stackIds);
                    break;
                default:
                    break;
            }
            PageHelper.startPage(earlyWarningDto.getPage(), earlyWarningDto.getSize());
            earlyWarningInfos = earlyWarningDao.findHistoryWarningInfo(earlyWarningDto);
            return new PageInfo<>(earlyWarningInfos);
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
        //查询预警总数量
        Integer sum = earlyWarningDao.findSum(earlyWarningDto);
        //报警类型统计 各种类型所占比例
        List<Map> warningTypeCount = earlyWarningDao.findWarningTypeCount(earlyWarningDto);
        if("03".equals(warningTypeCount.get(0).get("warningType"))){
            warningTypeCount.get(0).put("warningType","01");
        }
        if("02".equals(warningTypeCount.get(0).get("warningType"))){
            if("03".equals(warningTypeCount.get(1).get("warningType"))){
                Integer sumOne = Integer.parseInt(warningTypeCount.get(0).get("sum").toString());
                Integer sumTwo = Integer.parseInt(warningTypeCount.get(1).get("sum").toString());
                warningTypeCount.get(0).put("sum", sumOne+sumTwo);
                warningTypeCount.remove(1);
            }
            warningTypeCount.get(0).put("warningType","01");
        }
        if("01".equals(warningTypeCount.get(0).get("warningType")) && ("02".equals(warningTypeCount.get(1).get("warningType")) || "03".equals(warningTypeCount.get(1).get("warningType")))){
            Integer sumOne = Integer.parseInt(warningTypeCount.get(0).get("sum").toString());
            Integer sumTwo = Integer.parseInt(warningTypeCount.get(1).get("sum").toString());
            if("03".equals(warningTypeCount.get(2).get("warningType"))){
                Integer sumThree = Integer.parseInt(warningTypeCount.get(2).get("sum").toString());
                warningTypeCount.get(0).put("sum", sumOne+sumTwo+sumThree);
                warningTypeCount.remove(2);
            }
            warningTypeCount.get(0).put("sum",sumOne+sumTwo);
            warningTypeCount.remove(1);
        }
        String countJson = JSON.toJSONString(warningTypeCount);
        //报警类型统计 各种类型所占总数
        List<Map> warningTypeNumber =  JSON.parseArray(countJson,Map.class);
        List<String> warningTypes = Arrays.asList("01,04,05,06,07,08,09,10,11,12,13,14".split(","));
        List<String> types = new ArrayList<>();
        for (int i = 0; i < warningTypeCount.size(); i++) {
            Map map = warningTypeCount.get(i);
            map.put("sum",Math.round(Double.parseDouble(map.get("sum").toString())/sum*100)+"%");
            types.add(map.get("warningType").toString());
        }
        warningTypes.forEach(warningType -> {
            if(!types.contains(warningType)){
                Map map = new HashMap();
                map.put("warningType",warningType);
                map.put("sum",0);
                warningTypeNumber.add(map);
            }
        });
        warningTypeNumber.sort((v1, v2) -> v1.get("warningType").toString().compareTo(v2.get("warningType").toString()));
        List<Map> isDeal = earlyWarningDao.findIsDeal(earlyWarningDto,sum);
        for (int i = 0; i < 3; i++) {
            if((i+"").equals(isDeal.get(0).get("deal").toString())){
                Map map = new HashMap();
                map.put("deal",i);
                map.put("sum",0);
                map.put("ratio",0+"");
            }
        }
        List<Map> warningCount = new ArrayList<>();
        List<Map> isDealCount = new ArrayList<>();
        List<String> betweenDays = new ArrayList<>();
        String dateType="";
        //获取统计方式,1:日统计,2:周统计,3:月统计,4:年统计
        if(dayType==1){
            //日统计
            betweenDays = GetBetweenDayUtils.getBetweenDays(earlyWarningDto.getBeginTime(), earlyWarningDto.getFinishTime());//对获取日期排序
            dateType = "DATE_FORMAT(warning_time,'%Y-%m-%d' )";
        }else if(dayType==2){
            //周统计
            betweenDays = GetBetweenDayUtils.getBetweenWeek(earlyWarningDto.getBeginTime(), earlyWarningDto.getFinishTime());
            dateType = "DATE_FORMAT(DATE_SUB(warning_time,INTERVAL DATE_FORMAT(warning_time,'%w')-1 DAY),'%Y-%m-%d' )";
        }else if(dayType==3){
            //月统计
            betweenDays = GetBetweenDayUtils.getMonthBetweenDate(earlyWarningDto.getBeginTime(), earlyWarningDto.getFinishTime());
            dateType = "DATE_FORMAT(warning_time, '%Y-%m')";
        }else if(dayType==4){
            //年统计
            betweenDays = GetBetweenDayUtils.getYearBetweenDate(earlyWarningDto.getBeginTime(), earlyWarningDto.getFinishTime());
            dateType = "DATE_FORMAT(warning_time, '%Y')";
        }
        //报警数量统计
        warningCount = earlyWarningDao.findWarningCountByDay(earlyWarningDto,dateType);
        for (int i = 0; i < warningCount.size(); i++) {
            Map map = warningCount.get(i);
            String date = map.get("day").toString();
            Integer noDeal = earlyWarningDao.findNoDealCountByDay(date, earlyWarningDto,dateType);
            if(ToolUtil.isEmpty(noDeal)){
                noDeal  = 0;
            }
            map.put("noDeal",noDeal);
            Integer deal = earlyWarningDao.findDealCountByDay(date, earlyWarningDto,dateType);
            if(ToolUtil.isEmpty(deal)){
                deal  = 0;
            }
            map.put("deal",deal);
            map.put("processing",Integer.parseInt(map.get("sum").toString())-noDeal-deal);
            map.put("treatmentRate",Math.round(Double.parseDouble(deal.toString())/Double.parseDouble(map.get("sum").toString())*100)+"%");
        }
        List<String> day = warningCount.stream().map((Map timeMap) -> timeMap.get("day").toString()).collect(Collectors.toList());
        List<Map> addWarningCount = new ArrayList<>();
        betweenDays.forEach(betweenDay -> {
            if(!day.contains(betweenDay)){
                Map<String,String> map = new HashMap<>();
                map.put("day",betweenDay);
                map.put("sum",0+"");
                map.put("deal",0+"");
                map.put("noDeal",0+"");
                map.put("processing",0+"");
                map.put("treatmentRate",0+"");
                addWarningCount.add(map);
            }
        });
        warningCount.addAll(addWarningCount);
        warningCount.sort((v1, v2) -> v1.get("day").toString().compareTo(v2.get("day").toString()));
        String isDealJson = JSON.toJSONString(warningCount);
        //报警类型统计 各种类型所占总数
        isDealCount =  JSON.parseArray(isDealJson,Map.class);

        for (Map map : warningCount) {
            map.remove("deal");
            map.remove("noDeal");
            map.remove("processing");
            map.remove("treatmentRate");
        }
        Map<String,Object> map = new HashMap<>();
        map.put("sum",sum);
        map.put("warningTypeCount",warningTypeCount);
        map.put("warningCount",warningCount);
        map.put("warningTypeNumber",warningTypeNumber);
        map.put("dayCount",isDeal);
        map.put("isDealCount",isDealCount);
        return new Result(true, StatusCode.OK, "查询报警统计成功",map);
    }

    @Override
    public Integer misreport(Integer id) {
        return earlyWarningDao.misreport(id);
    }

    @Resource
    private NodeDao nodeDao;

    public Map<String,Object> getDataBySiteId(EarlyWarningDto earlyWarningDto){
        String warningType = earlyWarningDto.getWarningType();
        Integer stackId = earlyWarningDto.getStackId();
        Integer areaId = earlyWarningDto.getAreaId();
        String affirm = earlyWarningDto.getIsAffirm();
        String pidRoute = earlyWarningDto.getPidRoute();
        String warningGrade = earlyWarningDto.getWarningGrade();
        EarlyWarningVo earlyWarningVo = new EarlyWarningVo();
        earlyWarningVo.setWarningType(warningType);
        earlyWarningVo.setWarningTime(earlyWarningDao.findNewTime(affirm,warningGrade,warningType,pidRoute));
        if(ToolUtil.isNotEmpty(stackId)){
            earlyWarningVo.setMaterialCode(maintainPlanDao.findMaintainMaxIdByStackId(stackId));
            earlyWarningVo.setSiteName(warehouseDao.findStackById(stackId).getSiteName());
        }
        if(ToolUtil.isNotEmpty(areaId)){
            WareAreaVo wareArea = warehouseDao.findWareArea(areaId);
            earlyWarningVo.setSiteName(wareArea.getSiteName());
        }
        List<EarlyWarningVo> warningDatas = new ArrayList<>();
        EarlyWarningVo warningInfo = new EarlyWarningVo();
        if (warningType.equals("01")){
            warningDatas = earlyWarningDao.findWarningDataWarningTypeIsOne(affirm, warningGrade, pidRoute);
            warningDatas.forEach(warningData -> {
                switch (warningData.getWarningType()){
                    case "01":
                        warningData.setWarningNews("设备电量电量低");
                        break;
                    case "02":
                        warningData.setWarningNews("设备信号强度差");
                        break;
                    case "03":
                        warningData.setWarningNews("设备丢包");
                        break;
                }
            });
            //预警记录
            warningInfo.setWarningTime(earlyWarningVo.getWarningTime());
            warningInfo.setWarningType("01");
            warningInfo.setWarningNews("故障");
        }else{
            warningDatas = earlyWarningDao.findWarningDataWarningTypeIsNotOne(affirm, warningGrade,warningType, pidRoute);
            EarlyWarningVo earlyWarningInfo = getEarlyWarningVo(warningType,pidRoute,stackId);
            warningDatas.forEach(warningData -> {
                warningData.setUpperOne(earlyWarningInfo.getUpperOne());
                warningData.setUpperTwo(earlyWarningInfo.getUpperTwo());
                warningData.setUpperThree(earlyWarningInfo.getUpperThree());
                warningData.setLower(earlyWarningInfo.getLower());
            });
            Integer id = warningDatas.get(0).getId();
            warningInfo = earlyWarningDao.findById(id);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("warningInfo",warningInfo);
        map.put("earlyWarningVo",earlyWarningVo);
        map.put("warningDatas",warningDatas);
        return map;
    }

    public EarlyWarningVo getEarlyWarningVo(String warningType,String pidRoute,Integer stackId){
        EarlyWarningVo earlyWarningInfo = new EarlyWarningVo();
        String column = "";
        if(warningType.equals("06") || warningType.equals("08") || warningType.equals("10") || warningType.equals("12") || warningType.equals("13")){
            //04:垛内甲虫预警,06:垛内氧气预警,08:垛内温度预警,10:垛内湿度预警,12:包芯温度预警,13:平均包芯高于环境预警
            if(warningType.equals("06")){
                column = "stack_oxygen_upper_one as upperOne,stack_oxygen_upper_two as upperTwo,stack_oxygen_upper_three as upperThree,stack_oxygen_lower as lower";
            }else if(warningType.equals("08")){
                column = "stack_temp_upper_one as upperOne,stack_temp_upper_two as upperTwo,stack_temp_upper_three as upperThree,stack_temp_upper_lower as lower";
            }else if(warningType.equals("10")){
                column = "stack_moisture_upper_one as upperOne,stack_moisture_upper_two as upperTwo,stack_moisture_upper_three as upperThree,stack_moisture_upper_lower as lower";
            }else if(warningType.equals("12")){
                column = "temp_core_chip_upper_one as upperOne,temp_core_chip_upper_two as upperTwo,temp_core_chip_upper_three as upperThree,temp_core_chip_upper_lower as lower";
            }else{
                column = "core_temp_high_env_temp_one as upperOne,core_temp_high_env_temp_two as upperTwo,core_temp_high_env_temp_three as upperThree,core_temp_low_env_temp as lower";
            }
            earlyWarningInfo = alarmValueSettingDao.findAlarmValueSettingByStackId(column,stackId);
        }else if(warningType.equals("07") || warningType.equals("09") || warningType.equals("11")){
            //05:仓间甲虫预警,07:仓间氧气预警,09:仓间温度预警,11:仓间湿度预警,14:仓间粉螟预警
            if(warningType.equals("07")){
                column = "warehouse_oxygen_upper_one as upperOne,warehouse_oxygen_upper_two as upperTwo,warehouse_oxygen_upper_three as upperThree,warehouse_oxygen_lower as lower";
            }else if(warningType.equals("09")){
                column = "warehouse_temp_upper_one as upperOne,warehouse_temp_upper_two as upperTwo,warehouse_temp_upper_three as upperThree,warehouse_temp_upper_lower as lower";
            }else{
                column = "warehouse_moisture_upper_one as upperOne,warehouse_moisture_upper_two as upperTwo,warehouse_moisture_upper_three as upperThree,warehouse_moisture_upper_lower as lower";
            }
            earlyWarningInfo = wareAreaAlarmSettingDao.findAlarmValueSettingByStackId(column,pidRoute);
        }else {
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
        return earlyWarningInfo;
    }
    @Override
    public Result findEarlyWarningBySiteId(EarlyWarningDto earlyWarningDto) {
        Date endTime = earlyWarningDto.getEndTime();
        //结束时间不为空
        if(ToolUtil.isNotEmpty(endTime)){
            earlyWarningDto.setEndTime(new Date(endTime.getTime() + 24 * 60 * 60 * 1000 - 1));
        }
        //开始时间和结束时间为空时，开始时间为当月1号 00:00:00,结束时间为当月最后一天 23:59:59
        if(ToolUtil.isEmpty(earlyWarningDto.getStartTime()) && ToolUtil.isEmpty(earlyWarningDto.getEndTime())){
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            earlyWarningDto.setStartTime(calendar.getTime());
            calendar.add(Calendar.MONTH, 1);
            earlyWarningDto.setEndTime(new Date(calendar.getTime().getTime() - 1000));
        }
        Map<String, Object> map = getDataBySiteId(earlyWarningDto);
        return new Result<>(true, StatusCode.OK, "查询成功",map);
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
        Date endTime = earlyWarningDto.getEndTime();
        //结束时间不为空
        if(ToolUtil.isNotEmpty(endTime)){
            earlyWarningDto.setEndTime(new Date(endTime.getTime() + 24 * 60 * 60 * 1000 - 1));
        }
        //开始时间和结束时间为空时，开始时间为当月1号 00:00:00,结束时间为当月最后一天 23:59:59
        if(ToolUtil.isEmpty(earlyWarningDto.getStartTime()) && ToolUtil.isEmpty(earlyWarningDto.getEndTime())){
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            earlyWarningDto.setStartTime(calendar.getTime());
            calendar.add(Calendar.MONTH, 1);
            earlyWarningDto.setEndTime(new Date(calendar.getTime().getTime() - 1000));
        }
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
            WarningCountVo warningCountVo = earlyWarningDao.findEarlyWarningCountByOne(earlyWarningDto).get(0);
            warningCount = earlyWarningDao.findEarlyWarningCount(earlyWarningDto);
            if(ToolUtil.isNotEmpty(warningCountVo.getOneWarningCount()) && ToolUtil.isNotEmpty(warningCountVo.getTwoWarningCount()) && ToolUtil.isNotEmpty(warningCountVo.getThreeWarningCount())){
                warningCount.add(0,warningCountVo);
            }
            warningType = warningCount.get(0).getWarningType();
        }
        List<EarlyWarningVo> earlyWarningVos;
        List<EarlyWarningVo> warningDatas;
        EarlyWarningVo earlyWarningVo = new EarlyWarningVo();
        EarlyWarningVo warningInfo = new EarlyWarningVo();
        Map<String,Object> map = new HashMap<>();
        if(ToolUtil.isNotEmpty(warningCount)){
            earlyWarningDto.setWarningType(warningType);
            Map<String, Object> data = getData(earlyWarningDto);
            map.put("warningCount",warningCount);
            map.put("earlyWarningVos",data.get("earlyWarningVos"));
            map.put("warningInfo",data.get("warningInfo"));
            map.put("earlyWarningVo",data.get("earlyWarningVo"));
            map.put("warningDatas",data.get("warningDatas"));
            return new Result<>(true, StatusCode.OK, "查询成功",map);
        }else{
            warningCount = new ArrayList<>();
            earlyWarningVos = new ArrayList<>();
            warningDatas = new ArrayList<>();
            map.put("warningCount",warningCount);
            map.put("earlyWarningVos",earlyWarningVos);
            map.put("earlyWarningVo",earlyWarningVo);
            map.put("warningDatas",warningDatas);
            map.put("warningInfo",warningInfo);
            return new Result<>(true, StatusCode.OK, "查询成功,无预警信息",map);
        }
    }

    @Override
    public Result findEarlyWarningByWarningType(EarlyWarningDto earlyWarningDto) {
        Date endTime = earlyWarningDto.getEndTime();
        //结束时间不为空
        if(ToolUtil.isNotEmpty(endTime)){
            earlyWarningDto.setEndTime(new Date(endTime.getTime() + 24 * 60 * 60 * 1000 - 1));
        }
        //开始时间和结束时间为空时，开始时间为当月1号 00:00:00,结束时间为当月最后一天 23:59:59
        if(ToolUtil.isEmpty(earlyWarningDto.getStartTime()) && ToolUtil.isEmpty(earlyWarningDto.getEndTime())){
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, 0);
            calendar.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            earlyWarningDto.setStartTime(calendar.getTime());
            calendar.add(Calendar.MONTH, 1);
            earlyWarningDto.setEndTime(new Date(calendar.getTime().getTime() - 1000));
        }
        Map<String, Object> map = getData(earlyWarningDto);
        return new Result<>(true, StatusCode.OK, "查询成功",map);
    }

    public Map<String,Object> getData(EarlyWarningDto earlyWarningDto){
        String warningType = earlyWarningDto.getWarningType();
        List<EarlyWarningVo> earlyWarningVos;
        if(warningType.equals("01")){
            earlyWarningVos = earlyWarningDao.findEarlyWarningByOne(earlyWarningDto);
            earlyWarningVos.forEach(p -> p.setWarningNews("设备故障"));
        }else{
            earlyWarningVos = earlyWarningDao.findEarlyWarningByNotOne(earlyWarningDto);
        }
        if(ToolUtil.isEmpty(earlyWarningVos)){
            return new HashMap<>();
        }
        EarlyWarningVo earlyWarning = earlyWarningVos.get(0);
        earlyWarningDto.setPidRoute(earlyWarning.getSiteRoute());
        earlyWarningDto.setStackId(earlyWarning.getStackId());
        earlyWarningDto.setAreaId(earlyWarning.getAreaId());
        earlyWarningDto.setIsAffirm(earlyWarning.getAffirm());
        earlyWarningDto.setWarningGrade(earlyWarning.getWarningGrade()+"");
        Map<String,Object> map = new HashMap<>();
        Map<String, Object> data = getDataBySiteId(earlyWarningDto);
        map.put("earlyWarningVos",earlyWarningVos);
        map.put("warningInfo",data.get("warningInfo"));
        map.put("earlyWarningVo",data.get("earlyWarningVo"));
        map.put("warningDatas",data.get("warningDatas"));
        return map;
    }

}
