package com.rpzh.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import java.lang.reflect.Field;
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

    @Resource
    private RedisTemplate redisTemplate;

    public static String MAINTAIN ="MAINTAIN_";

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

    public AlarmValueSettingVo getWareArea(Field[] fields,Integer areaId){
        String pidRoute = warehouseDao.findWareArea(areaId).getPidRoute() + ",[" + WarehouseNodeType.WAREAREA_NODE.getType() + ":" + areaId + "]";
        String[] pidRoutes = pidRoute.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
        AlarmValueSettingVo alarmValueSettingVo1 = new AlarmValueSettingVo();
        //从下往上找
        for (int i = pidRoutes.length - 1; i >= 0; i--) {
            String[] route = pidRoutes[i].split(":");
            if (ToolUtil.isNotEmpty(redisTemplate.opsForHash().entries(MAINTAIN + route[0] + route[1]))) {
                alarmValueSettingVo1 = JSON.parseObject(JSON.toJSONString(redisTemplate.boundHashOps(MAINTAIN + route[0] + route[1]).entries()), AlarmValueSettingVo.class);
                break;
            }
        }
        if (ToolUtil.isEmpty(alarmValueSettingVo1)) {
            for (String routes : pidRoutes) {
                String[] route = routes.split(":");
                alarmValueSettingVo1 = wareAreaAlarmSettingDao.findBySite(route[0] + route[1]);
                if (ToolUtil.isNotEmpty(alarmValueSettingVo1)) {
                    for (Field field : fields) {
                        try {
                            field.setAccessible(true);
                            //把每个字段的值存到redis中  对象为MAINTAIN_加养护阶段id主键 ，key为字段名 value为字段值
                            redisTemplate.opsForHash().put(MAINTAIN + route[0] + route[1], field.getName(), field.get(alarmValueSettingVo1));
                        } catch (IllegalAccessException exception) {
                            exception.printStackTrace();
                        }
                    }
                    break;
                }
            }
        }
        return alarmValueSettingVo1;
    }

    public AlarmValueSettingVo getStack(Field[] fields,Integer stageId,Integer alarmValueSettingId){
        AlarmValueSettingVo alarmValueSettingVo1 = new AlarmValueSettingVo();
            if (ToolUtil.isNotEmpty(redisTemplate.opsForHash().entries(MAINTAIN + stageId))) {
                alarmValueSettingVo1 = JSON.parseObject(JSON.toJSONString(redisTemplate.boundHashOps(MAINTAIN + stageId).entries()), AlarmValueSettingVo.class);
            } else {
                alarmValueSettingVo1 = alarmValueSettingDao.findAlarmValueSettingById(alarmValueSettingId);
                //获取所有的字段
                for (Field field : fields) {
                    try {
                        field.setAccessible(true);
                        //把每个字段的值存到redis中  对象为MAINTAIN_加养护阶段id主键 ，key为字段名 value为字段值
                        redisTemplate.opsForHash().put(MAINTAIN + stageId, field.getName(), field.get(alarmValueSettingVo1));
                    } catch (IllegalAccessException exception) {
                        exception.printStackTrace();
                    }
                }
            }
         return alarmValueSettingVo1;
    }

    public Map<String,Object> getDataBySiteId(EarlyWarningDto earlyWarningDto){
        String warningType = earlyWarningDto.getWarningType();
        Field[] fields = AlarmValueSettingVo.class.getDeclaredFields();
        Integer stackId = earlyWarningDto.getStackId();
        Integer areaId = earlyWarningDto.getAreaId();
        String affirm = earlyWarningDto.getIsAffirm();
        String warningTime = earlyWarningDto.getTime();
        List<EarlyWarningVo> warningDatas = new ArrayList<>();
        EarlyWarningVo earlyWarningVo = new EarlyWarningVo();
        EarlyWarningVo warningInfo = new EarlyWarningVo();
        WareAreaVo wareArea = warehouseDao.findWareArea(areaId);
        if(warningType.equals("01")){
            earlyWarningVo.setWarningType("01");
            earlyWarningVo.setWarningTime(warningTime);
            List<String> type = new ArrayList<>();
            type.add("01");type.add("02");type.add("03");
            if(ToolUtil.isNotEmpty(stackId)){
                earlyWarningVo.setSiteName(warehouseDao.findStackById(stackId).getSiteName());
                earlyWarningVo.setMaterialCode(maintainPlanDao.findMaintainStackId(stackId).getMaterialCode());
                warningDatas = earlyWarningDao.findWarningDataByNode(affirm, stackId, null,type);
            }
            if(ToolUtil.isNotEmpty(areaId)){
                earlyWarningVo.setSiteName(wareArea.getSiteName());
                warningDatas = earlyWarningDao.findWarningDataByNode(affirm,null,areaId,type);
            }
            for (EarlyWarningVo warningData : warningDatas) {
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
            }
            //预警记录
            warningInfo.setWarningTime(warningTime);
            warningInfo.setWarningType("01");
            warningInfo.setWarningNews("故障");
        }
        //05:仓间甲虫预警,07:仓间氧气预警,09:仓间温度预警,11:仓间湿度预警,14:仓间粉螟预警
        else if(warningType.equals("05") || warningType.equals("07") || warningType.equals("09") || warningType.equals("11") || warningType.equals("14")){
            earlyWarningVo.setWarningType(warningType);
            earlyWarningVo.setWarningTime(warningTime);
            earlyWarningVo.setSiteName(wareArea.getSiteName());
            String pidRoute = wareArea.getPidRoute() + ",[" + WarehouseNodeType.WAREAREA_NODE.getType() + ":" + areaId + "]";
            if(warningType.equals("07") || warningType.equals("09") || warningType.equals("11")){
                List type = Arrays.asList(warningType);
                AlarmValueSettingVo alarmValueSettingVo = getWareArea(fields,areaId);
                warningDatas = earlyWarningDao.findWarningDataByNode(affirm,null,areaId,type);
                //07:仓间氧气预警,09:仓间温度预警,11:仓间湿度预警
                switch (warningType){
                    case "07":
                        warningDatas.forEach((warningData)->{
                            warningData.setUpperOne(alarmValueSettingVo.getWarehouseOxygenUpperOne());
                            warningData.setUpperTwo(alarmValueSettingVo.getWarehouseOxygenUpperTwo());
                            warningData.setUpperThree(alarmValueSettingVo.getWarehouseOxygenUpperThree());
                            warningData.setLower(alarmValueSettingVo.getWarehouseOxygenLower());
                        });
                        break;
                    case "09":
                        warningDatas.forEach((warningData)->{
                            warningData.setUpperOne(alarmValueSettingVo.getWarehouseTempUpperOne());
                            warningData.setUpperTwo(alarmValueSettingVo.getWarehouseTempUpperTwo());
                            warningData.setUpperThree(alarmValueSettingVo.getWarehouseTempUpperThree());
                            warningData.setLower(alarmValueSettingVo.getWarehouseTempLower());
                        });
                        break;
                    case "11":
                        warningDatas.forEach((warningData)->{
                            warningData.setUpperOne(alarmValueSettingVo.getWarehouseMoistureUpperOne());
                            warningData.setUpperTwo(alarmValueSettingVo.getWarehouseMoistureUpperTwo());
                            warningData.setUpperThree(alarmValueSettingVo.getWarehouseMoistureUpperThree());
                            warningData.setLower(alarmValueSettingVo.getWarehouseMoistureLower());
                        });
                        break;
                }
            }
            if(warningType.equals("05") || warningType.equals("14")){
                warningDatas = earlyWarningDao.findWarningDataByArea(affirm,areaId,warningType);
                PestMonitoringRuleVo pestStackMonitor = pestMonitoringDao.findPestStackMonitor(pidRoute);
                String[] pidRoutes = pidRoute.split(",");
                if(ToolUtil.isEmpty(pestStackMonitor)){
                    pidRoute = pidRoutes[0] + "," + pidRoutes[1] + "," + pidRoutes[2] + ","  + pidRoutes[3];
                    pestStackMonitor = pestMonitoringDao.findPestStackMonitor(pidRoute);
                }
                if(ToolUtil.isEmpty(pestStackMonitor)){
                    pidRoute = pidRoutes[0] + "," + pidRoutes[1] + "," + pidRoutes[2];
                    pestStackMonitor = pestMonitoringDao.findPestStackMonitor(pidRoute);
                }
                if(ToolUtil.isEmpty(pestStackMonitor)){
                    pidRoute = pidRoutes[0] + "," + pidRoutes[1] ;
                    pestStackMonitor = pestMonitoringDao.findPestStackMonitor(pidRoute);
                }
                if(ToolUtil.isEmpty(pestStackMonitor)){
                    pestStackMonitor = pestMonitoringDao.findPestMonitor();
                }
                PestMonitoringRuleVo pestStackMonitor1 = pestStackMonitor;
                warningDatas.forEach((warningData)->{
                    warningData.setUpperOne(pestStackMonitor1.getValueOne());
                    warningData.setUpperTwo(pestStackMonitor1.getValueTwo());
                    warningData.setUpperThree(pestStackMonitor1.getValueThree());
                });
            }
            Integer id = warningDatas.get(0).getId();
            warningInfo = earlyWarningDao.findById(id);
        }
        //04:垛内甲虫预警,06:垛内氧气预警,08:垛内温度预警,10:垛内湿度预警,12:包芯温度预警,13:平均包芯高于环境预警
        else{
            earlyWarningVo.setWarningType(warningType);
            earlyWarningVo.setWarningTime(warningTime);
            StackVo stack = warehouseDao.findStackById(stackId);
            earlyWarningVo.setSiteName(stack.getSiteName());
            earlyWarningVo.setMaterialCode(maintainPlanDao.findMaintainStackId(stackId).getMaterialCode());
            String pidRoute = stack.getPidRoute() + ",[" + WarehouseNodeType.STACK_NODE.getType() + ":" + stackId + "]";
            if(warningType.equals("06") || warningType.equals("08") || warningType.equals("10") || warningType.equals("12") || warningType.equals("13")){
                List type = Arrays.asList(warningType);
                warningDatas = earlyWarningDao.findWarningDataByNode(affirm,stackId,null,type);
                MaintainPlanVo maintainPlanVo = alarmValueSettingDao.findByMaintainStackId(stackId);
                AlarmValueSettingVo alarmValueSettingVo = getStack(fields, maintainPlanVo.getStageId(),maintainPlanVo.getAlarmValueSettingId());
                switch (warningType){
                    case "06":
                        warningDatas.forEach((warningData)->{
                            warningData.setUpperOne(alarmValueSettingVo.getStackOxygenUpperOne());
                            warningData.setUpperTwo(alarmValueSettingVo.getStackOxygenUpperTwo());
                            warningData.setUpperThree(alarmValueSettingVo.getStackOxygenUpperThree());
                            warningData.setLower(alarmValueSettingVo.getStackOxygenLower());
                        });
                        break;
                    case "07":
                        warningDatas.forEach((warningData)->{
                            warningData.setUpperOne(alarmValueSettingVo.getStackTempUpperOne());
                            warningData.setUpperTwo(alarmValueSettingVo.getStackTempUpperTwo());
                            warningData.setUpperThree(alarmValueSettingVo.getStackTempUpperThree());
                            warningData.setLower(alarmValueSettingVo.getStackTempLower());
                        });
                        break;
                    case "10":
                        warningDatas.forEach((warningData)->{
                            warningData.setUpperOne(alarmValueSettingVo.getStackMoistureUpperOne());
                            warningData.setUpperTwo(alarmValueSettingVo.getStackMoistureUpperTwo());
                            warningData.setUpperThree(alarmValueSettingVo.getStackMoistureUpperThree());
                            warningData.setLower(alarmValueSettingVo.getStackMoistureLower());
                        });
                        break;
                    case "12":
                        warningDatas.forEach((warningData)->{
                            warningData.setUpperOne(alarmValueSettingVo.getTempCoreChipUpperOne());
                            warningData.setUpperTwo(alarmValueSettingVo.getTempCoreChipUpperTwo());
                            warningData.setUpperThree(alarmValueSettingVo.getTempCoreChipUpperThree());
                            warningData.setLower(alarmValueSettingVo.getTempCoreChipLower());
                        });
                        break;
                    case "13":
                        warningDatas.forEach((warningData)->{
                            warningData.setUpperOne(alarmValueSettingVo.getCoreTempHighEnvTempOne());
                            warningData.setUpperTwo(alarmValueSettingVo.getCoreTempHighEnvTempTwo());
                            warningData.setUpperThree(alarmValueSettingVo.getCoreTempHighEnvTempThree());
                            warningData.setLower(alarmValueSettingVo.getCoreTempLowEnvTemp());
                        });
                        break;
                }
            }
            if(warningType.equals("04")){
                warningDatas = earlyWarningDao.findWarningDataByStack(affirm,stackId,warningType);
                PestMonitoringRuleVo pestStackMonitor = pestMonitoringDao.findPestStackMonitor(pidRoute);
                String[] pidRoutes = pidRoute.split(",");
                if(ToolUtil.isEmpty(pestStackMonitor)){
                    pidRoute = pidRoutes[0] + "," + pidRoutes[1] + "," + pidRoutes[2] + ","  + pidRoutes[3] + ","  + pidRoutes[4];
                    pestStackMonitor = pestMonitoringDao.findPestStackMonitor(pidRoute);
                }
                if(ToolUtil.isEmpty(pestStackMonitor)){
                    pidRoute = pidRoutes[0] + "," + pidRoutes[1] + "," + pidRoutes[2] + ","  + pidRoutes[3];
                    pestStackMonitor = pestMonitoringDao.findPestStackMonitor(pidRoute);
                }
                if(ToolUtil.isEmpty(pestStackMonitor)){
                    pidRoute = pidRoutes[0] + "," + pidRoutes[1] + "," + pidRoutes[2];
                    pestStackMonitor = pestMonitoringDao.findPestStackMonitor(pidRoute);
                }
                if(ToolUtil.isEmpty(pestStackMonitor)){
                    pidRoute = pidRoutes[0] + "," + pidRoutes[1] ;
                    pestStackMonitor = pestMonitoringDao.findPestStackMonitor(pidRoute);
                }
                if(ToolUtil.isEmpty(pestStackMonitor)){
                    pestStackMonitor = pestMonitoringDao.findPestMonitor();
                }
                PestMonitoringRuleVo pestStackMonitor1 = pestStackMonitor;
                warningDatas.forEach((warningData)->{
                    warningData.setUpperOne(pestStackMonitor1.getValueOne());
                    warningData.setUpperTwo(pestStackMonitor1.getValueTwo());
                    warningData.setUpperThree(pestStackMonitor1.getValueThree());
                });
            }
            Integer id = warningDatas.get(0).getId();
            warningInfo = earlyWarningDao.findById(id);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("warningInfo",warningInfo);
        map.put("earlyWarningVo",earlyWarningVo);
        map.put("warningDatas",warningDatas);
        return map;
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
        Field[] fields = AlarmValueSettingVo.class.getDeclaredFields();
        Integer areaId = earlyWarningDto.getAreaId();
        Integer stackId = earlyWarningDto.getStackId();
        EarlyWarningVo earlyWarningVo = new EarlyWarningVo();
        Map<String, Object> map = new HashMap<>();
        String nodeId = earlyWarningDto.getNodeId();
        String time = earlyWarningDao.findMaxTime(nodeId);
        AlarmValueSettingVo alarmValueSettingVo = new AlarmValueSettingVo();
        if (ToolUtil.isNotEmpty(areaId)) {
            alarmValueSettingVo = getWareArea(fields, areaId);
            //07:仓间氧气预警,09:仓间温度预警,11:仓间湿度预警
            switch (warningType) {
                case "07":
                    earlyWarningVo.setUpperOne(alarmValueSettingVo.getWarehouseOxygenUpperOne());
                    earlyWarningVo.setUpperTwo(alarmValueSettingVo.getWarehouseOxygenUpperTwo());
                    earlyWarningVo.setUpperThree(alarmValueSettingVo.getWarehouseOxygenUpperThree());
                    earlyWarningVo.setLower(alarmValueSettingVo.getWarehouseOxygenLower());
                    break;
                case "09":
                    earlyWarningVo.setUpperOne(alarmValueSettingVo.getWarehouseTempUpperOne());
                    earlyWarningVo.setUpperTwo(alarmValueSettingVo.getWarehouseTempUpperTwo());
                    earlyWarningVo.setUpperThree(alarmValueSettingVo.getWarehouseTempUpperThree());
                    earlyWarningVo.setLower(alarmValueSettingVo.getWarehouseTempLower());
                    break;
                case "11":
                    earlyWarningVo.setUpperOne(alarmValueSettingVo.getWarehouseMoistureUpperOne());
                    earlyWarningVo.setUpperTwo(alarmValueSettingVo.getWarehouseMoistureUpperTwo());
                    earlyWarningVo.setUpperThree(alarmValueSettingVo.getWarehouseMoistureUpperThree());
                    earlyWarningVo.setLower(alarmValueSettingVo.getWarehouseMoistureLower());
                    break;
            }
        }
        if (ToolUtil.isNotEmpty(stackId)) {
            MaintainPlanVo maintainPlanVo = maintainPlanDao.findMaintainPlanByStack(stackId);
            alarmValueSettingVo = getStack(fields, maintainPlanVo.getStageId(),maintainPlanVo.getAlarmValueSettingId());
            switch (warningType) {
                case "06":
                    earlyWarningVo.setUpperOne(alarmValueSettingVo.getStackOxygenUpperOne());
                    earlyWarningVo.setUpperTwo(alarmValueSettingVo.getStackOxygenUpperTwo());
                    earlyWarningVo.setUpperThree(alarmValueSettingVo.getStackOxygenUpperThree());
                    earlyWarningVo.setLower(alarmValueSettingVo.getStackOxygenLower());
                    break;
                case "07":
                    earlyWarningVo.setUpperOne(alarmValueSettingVo.getStackTempUpperOne());
                    earlyWarningVo.setUpperTwo(alarmValueSettingVo.getStackTempUpperTwo());
                    earlyWarningVo.setUpperThree(alarmValueSettingVo.getStackTempUpperThree());
                    earlyWarningVo.setLower(alarmValueSettingVo.getStackTempLower());
                    break;
                case "10":
                    earlyWarningVo.setUpperOne(alarmValueSettingVo.getStackMoistureUpperOne());
                    earlyWarningVo.setUpperTwo(alarmValueSettingVo.getStackMoistureUpperTwo());
                    earlyWarningVo.setUpperThree(alarmValueSettingVo.getStackMoistureUpperThree());
                    earlyWarningVo.setLower(alarmValueSettingVo.getStackMoistureLower());
                    break;
                case "12":
                    earlyWarningVo.setUpperOne(alarmValueSettingVo.getTempCoreChipUpperOne());
                    earlyWarningVo.setUpperTwo(alarmValueSettingVo.getTempCoreChipUpperTwo());
                    earlyWarningVo.setUpperThree(alarmValueSettingVo.getTempCoreChipUpperThree());
                    earlyWarningVo.setLower(alarmValueSettingVo.getTempCoreChipLower());
                    break;
                case "13":
                    earlyWarningVo.setUpperOne(alarmValueSettingVo.getCoreTempHighEnvTempOne());
                    earlyWarningVo.setUpperTwo(alarmValueSettingVo.getCoreTempHighEnvTempTwo());
                    earlyWarningVo.setUpperThree(alarmValueSettingVo.getCoreTempHighEnvTempThree());
                    earlyWarningVo.setLower(alarmValueSettingVo.getCoreTempLowEnvTemp());
                    break;
                }
            map.put("maintainPlan", maintainPlanVo);
        }
        map.put("earlyWarningVo", earlyWarningVo);
        NodeTypeEnum nodeType = NodeTypeEnum.getNodeType(earlyWarningDto.getNodeType());
        List<NodeData> nodeData = nodeDao.findNodeData(nodeId, nodeType.getTableName(), nodeType.getTableColumn());
        map.put("nodeData", nodeData);
        map.put("time", time);
        return new Result<>(true, StatusCode.OK, "查询成功,无预警信息", map);
    }

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
        List<EarlyWarningVo> earlyWarningVos;
        List<EarlyWarningVo> warningDatas;
        List<WarningCountVo> warningCount = earlyWarningDao.findEarlyWarningCount(earlyWarningDto);
        EarlyWarningVo earlyWarningVo = new EarlyWarningVo();
        EarlyWarningVo warningInfo = new EarlyWarningVo();
        Map<String,Object> map = new HashMap<>();
        if(warningCount.size()==0){
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
        String warningType = earlyWarningDto.getWarningType();
        //将设备电量故障(01),设备信号强度故障(02),设备丢包故障(03)归类为设备故障,预警类型(01-14)是按顺序排列
        if(ToolUtil.isEmpty(warningType)) {
            Integer oneWarning=0;
            if(warningCount.get(0).getWarningType().equals("01")||warningCount.get(0).getWarningType().equals("02")||warningCount.get(0).getWarningType().equals("03")){
                oneWarning = oneWarning + warningCount.get(0).getOneWarningCount();
                warningCount.get(0).setWarningType("01");
            }
            if(warningCount.get(2).getWarningType().equals("03")){
                oneWarning = oneWarning + warningCount.get(2).getOneWarningCount();
                warningCount.remove(2);
            }
            if(warningCount.get(1).getWarningType().equals("03") || warningCount.get(1).getWarningType().equals("02")) {
                oneWarning = oneWarning + warningCount.get(2).getOneWarningCount();
                warningCount.remove(1);
            }
            if(warningCount.get(0).getWarningType().equals("01")){
                warningCount.get(0).setOneWarningCount(oneWarning);
                warningCount.add(warningCount.get(0)) ;
                warningCount.remove(0) ;
            }
            warningType = warningCount.get(0).getWarningType();
        }
        earlyWarningDto.setWarningType(warningType);
        Map<String, Object> data = getData(earlyWarningDto);
        map.put("warningCount",warningCount);
        map.put("earlyWarningVos",data.get("earlyWarningVos"));
        map.put("warningInfo",data.get("warningInfo"));
        map.put("earlyWarningVo",data.get("earlyWarningVo"));
        map.put("warningDatas",data.get("warningDatas"));
        return new Result<>(true, StatusCode.OK, "查询成功",map);
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
            earlyWarningVos = earlyWarningDao.findEarlyWarningByNode(earlyWarningDto);
            earlyWarningVos.forEach(p -> p.setWarningNews("设备故障"));
            EarlyWarningVo earlyWarning = earlyWarningVos.get(0);
            earlyWarningDto.setStackId(earlyWarning.getStackId());
            earlyWarningDto.setAreaId(earlyWarning.getAreaId());
            earlyWarningDto.setIsAffirm(earlyWarning.getAffirm());
            earlyWarningDto.setTime(earlyWarning.getWarningTime());
        }
        //05:仓间甲虫预警,07:仓间氧气预警,09:仓间温度预警,11:仓间湿度预警,14:仓间粉螟预警
        else if(warningType.equals("05") || warningType.equals("07") || warningType.equals("09") || warningType.equals("11") || warningType.equals("14")){
            earlyWarningVos = earlyWarningDao.findEarlyWarningByAreaType(earlyWarningDto);
            EarlyWarningVo earlyWarning = earlyWarningVos.get(0);
            earlyWarningDto.setAreaId(earlyWarning.getAreaId());
            earlyWarningDto.setIsAffirm(earlyWarning.getAffirm());
            earlyWarningDto.setTime(earlyWarning.getWarningTime());
        }
        //04:垛内甲虫预警,06:垛内氧气预警,08:垛内温度预警,10:垛内湿度预警,12:包芯温度预警,13:平均包芯高于环境预警
        else{
            earlyWarningVos = earlyWarningDao.findEarlyWarningByStackType(earlyWarningDto);
            System.out.println(earlyWarningVos);
            EarlyWarningVo earlyWarning = earlyWarningVos.get(0);
            earlyWarningDto.setStackId(earlyWarning.getStackId());
            earlyWarningDto.setAreaId(earlyWarning.getAreaId());
            earlyWarningDto.setIsAffirm(earlyWarning.getAffirm());
            earlyWarningDto.setTime(earlyWarning.getWarningTime());
        }
        Map<String,Object> map = new HashMap<>();
        Map<String, Object> data = getDataBySiteId(earlyWarningDto);
        map.put("earlyWarningVos",earlyWarningVos);
        map.put("warningInfo",data.get("warningInfo"));
        map.put("earlyWarningVo",data.get("earlyWarningVo"));
        map.put("warningDatas",data.get("warningDatas"));
        return map;
    }

}
