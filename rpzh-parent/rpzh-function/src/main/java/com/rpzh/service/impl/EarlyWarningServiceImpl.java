package com.rpzh.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.*;
import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.entity.*;
import com.rpzh.enums.NodeTypeEnum;
import com.rpzh.enums.WarehouseNodeType;
import com.rpzh.pojo.NodeData;
import com.rpzh.service.EarlyWarningService;
import com.rpzh.util.DateTypeUtils;
import com.rpzh.util.StringUtils;
import com.rpzh.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

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
        Integer stackId = earlyWarningDto.getStackId();
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
            earlyWarningDto.setStartTime(new Date(calendar.getTime().getTime()));
            calendar.add(Calendar.MONTH, 1);
            earlyWarningDto.setEndTime(new Date(calendar.getTime().getTime() - 1000));
        }
        //根据位置路由和位置id设置仓间id和垛位id
        if(StringUtils.isNotEmpty(pidRoute)) {
            int length = pidRoute.split(",").length;
            List<Integer> stackIds = new ArrayList<>();
            switch (length) {
                case 4:
                    stackIds = warehouseDao.findStackByAreId(stackId);
                    if (ToolUtil.isEmpty(stackIds)) {
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
        }
        //查询预警总数量
        earlyWarningDto.setCount(earlyWarningDao.findSum(earlyWarningDto));
        //报警类型所占的百分比
        List<WarningOrDealVo> count = earlyWarningDao.findCountByWarningType(earlyWarningDto);
        if(count.size()>0) {
            for (int i = 1; i <= 3; i++) {
                if (!count.get(i-1).getWarningType().equals("0" + i)) {
                    WarningOrDealVo warningOrDealVo = new WarningOrDealVo();
                    warningOrDealVo.setWarningType("0" + i);
                    warningOrDealVo.setPercentage(0d);
                    count.add(i - 1, warningOrDealVo);
                }
            }
            WarningOrDealVo warningOrDealVo = count.get(0);
            warningOrDealVo.setPercentage(count.get(0).getPercentage() + count.get(1).getPercentage() + count.get(2).getPercentage());
            count.remove(2);
            count.remove(1);
            if(warningOrDealVo.getPercentage()==0d){
                count.remove(0);
            }
        }
        //已处理和未处理所占的比率
        List<WarningOrDealVo> dealCount = earlyWarningDao.findCountByDeal(earlyWarningDto);
        //查询各种报警类型的数量
        List<WarningOrDealVo> countInfo =getMonthCountInfo();
        //报警数量统计
        List<WarningOrDealVo> dayCount = earlyWarningDao.findCountByDay(earlyWarningDto);
        Integer dayType = earlyWarningDto.getDayType();
        if(dayCount.size()>0) {
            Map<String, WarningOrDealVo> listDate = DateTypeUtils.getDayCountList(earlyWarningDto.getStartTime(), earlyWarningDto.getEndTime(), dayType);
            for (WarningOrDealVo warningOrDealVo : dayCount) {
                listDate.put(warningOrDealVo.getDayType(), warningOrDealVo);
            }
            dayCount = new ArrayList<>();
            for (Map.Entry<String, WarningOrDealVo> map : listDate.entrySet()) {
                dayCount.add(map.getValue());
            }
        }
        //报警处理率统计
        List<WarningOrDealVo> isDealCount = earlyWarningDao.findCountByIsDeal(earlyWarningDto);
        if(isDealCount.size()>0) {
            Map<String, WarningOrDealVo> listDate = DateTypeUtils.getIsDealCountList(earlyWarningDto.getStartTime(), earlyWarningDto.getEndTime(), dayType);
            for (WarningOrDealVo warningOrDealVo : isDealCount) {
                listDate.put(warningOrDealVo.getDayType()+warningOrDealVo.getDeal(), warningOrDealVo);
            }
            isDealCount = new ArrayList<>();
            for (Map.Entry<String, WarningOrDealVo> map : listDate.entrySet()) {
                isDealCount.add(map.getValue());
            }
        }
        Collections.sort(isDealCount, new Comparator<WarningOrDealVo>(){
            public int compare(WarningOrDealVo o1, WarningOrDealVo o2) {

                if( Integer.parseInt(o2.getDayType())<Integer.parseInt(o1.getDayType()))
                    return 1;
                else if(Integer.parseInt(o2.getDayType())==Integer.parseInt(o1.getDayType())){
                    if(o1.getDeal()>(o2.getDeal()) )
                        return 1;
                    else
                        return -1;
                }
                else
                    return -1;
            }
        });

        Map<String,Object> map = new HashMap<>();
        map.put("count",earlyWarningDto.getCount());
        map.put("count_type",count);
        map.put("countInfo",countInfo);
        map.put("dealCount",dealCount);
        map.put("dayCount",dayCount);
        map.put("isDealCount",isDealCount);
        return new Result(true, StatusCode.OK, "查询报警统计成功",map);
    }

    @Override
    public Integer misreport(Integer id) {
        return earlyWarningDao.misreport(id);
    }

    @Resource
    private NodeDao nodeDao;
    @Override
    public Result findNodeData(EarlyWarningDto earlyWarningDto) {
        String warningType = earlyWarningDto.getWarningType();
        String pidRoute = "";
        Field[] fields = AlarmValueSettingVo.class.getDeclaredFields();
        Integer areaId = earlyWarningDto.getAreaId();
        Integer stackId = earlyWarningDto.getStackId();
        EarlyWarningVo earlyWarningVo = new EarlyWarningVo();
        AlarmValueSettingVo alarmValueSettingVo1 = null;
        Map<String,Object> map =new HashMap<>();
        String nodeId = earlyWarningDto.getNodeId();
        String time = earlyWarningDao.findMaxTime(nodeId);
        if(ToolUtil.isNotEmpty(areaId)) {
            pidRoute = warehouseDao.findWareArea(areaId).getPidRoute() + ",[" + WarehouseNodeType.WAREAREA_NODE.getType() + ":" + areaId + "]";
            if (warningType.equals("07") || warningType.equals("09") || warningType.equals("11")) {
                String[] pidRoutes = pidRoute.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
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
                AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingVo1;
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
        }
        if(ToolUtil.isNotEmpty(stackId)){
            MaintainPlanVo maintainPlanVo = maintainPlanDao.findMaintainPlanByStack(stackId);
            if(warningType.equals("06") || warningType.equals("08") || warningType.equals("10") || warningType.equals("12") || warningType.equals("13")){
                if(ToolUtil.isNotEmpty(redisTemplate.opsForHash().entries(MAINTAIN + maintainPlanVo.getStageId()))){
                    alarmValueSettingVo1 = JSON.parseObject(JSON.toJSONString(redisTemplate.boundHashOps(MAINTAIN + maintainPlanVo.getStageId()).entries()),AlarmValueSettingVo.class);
                }else {
                    alarmValueSettingVo1 = alarmValueSettingDao.findAlarmValueSettingById(maintainPlanVo.getAlarmValueSettingId());
                    //获取所有的字段
                    for (Field field : fields) {
                        try {
                            field.setAccessible(true);
                            //把每个字段的值存到redis中  对象为MAINTAIN_加养护阶段id主键 ，key为字段名 value为字段值
                            redisTemplate.opsForHash().put(MAINTAIN + maintainPlanVo.getStageId(), field.getName(), field.get(alarmValueSettingVo1));
                        } catch (IllegalAccessException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
                AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingVo1;
                switch (warningType){
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
            }
            map.put("maintainPlan",maintainPlanVo);
        }
        map.put("earlyWarningVo",earlyWarningVo);
        NodeTypeEnum nodeType = NodeTypeEnum.getNodeType(earlyWarningDto.getNodeType());
        List<NodeData> nodeData = nodeDao.findNodeData(nodeId, nodeType.getTableName(), nodeType.getTableColumn());
        map.put("nodeData",nodeData);
        map.put("time",time);
        return new Result<>(true, StatusCode.OK, "查询成功,无预警信息",map);
    }

    public List<WarningOrDealVo> getMonthCountInfo(){
        List<WarningOrDealVo> countInfo =earlyWarningDao.findCountByMonth();
        List<String> type = new ArrayList<>();
        countInfo.forEach((countVo) ->{type.add(countVo.getWarningType());});
        for (int i = 1; i < 15;i++) {
            String flag= "0" + i;
            if(i>=10){
                flag= "" + i;
            }
            if (!type.contains(flag)) {
                WarningOrDealVo warningOrDealVo = new WarningOrDealVo();
                warningOrDealVo.setWarningType(flag);
                warningOrDealVo.setCount(0);
                countInfo.add(warningOrDealVo);
            }
        }
        countInfo.sort((v1, v2) -> v1.getWarningType().compareTo(v2.getWarningType()));
        countInfo.get(0).setCount(countInfo.get(0).getCount()+countInfo.get(1).getCount()+countInfo.get(2).getCount());
        countInfo.remove(2);
        countInfo.remove(1);
        return countInfo;
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
        List<EarlyWarningVo> warningDatas = new ArrayList<>();
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
                earlyWarningVo.setSiteName(warehouseDao.findWareArea(areaId).getSiteName());
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
            WareAreaVo wareArea = warehouseDao.findWareArea(areaId);
            earlyWarningVo.setSiteName(wareArea.getSiteName());
            String pidRoute = wareArea.getPidRoute() + ",[" + WarehouseNodeType.WAREAREA_NODE.getType() + ":" + areaId + "]";
            AlarmValueSettingVo alarmValueSettingVo1=null;
            if(warningType.equals("07") || warningType.equals("09") || warningType.equals("11")){
                List type = Arrays.asList(warningType);
                warningDatas = earlyWarningDao.findWarningDataByNode(affirm,null,areaId,type);
                String[] pidRoutes = pidRoute.replaceAll("\\[","").replaceAll("\\]","").split(",");
                //从下往上找
                for (int i = pidRoutes.length - 1; i >= 0; i--) {
                    String[] route = pidRoutes[i].split(":");
                    if(ToolUtil.isNotEmpty(redisTemplate.opsForHash().entries(MAINTAIN + route[0]+route[1]))){
                        alarmValueSettingVo1 = JSON.parseObject(JSON.toJSONString(redisTemplate.boundHashOps(MAINTAIN + route[0]+route[1]).entries()),AlarmValueSettingVo.class);
                        break;
                    }
                }
                if(ToolUtil.isEmpty(alarmValueSettingVo1)){
                    for (String routes : pidRoutes) {
                        String[] route = routes.split(":");
                        alarmValueSettingVo1 = wareAreaAlarmSettingDao.findBySite(route[0]+route[1]);
                        if(ToolUtil.isNotEmpty(alarmValueSettingVo1)){
                            for (Field field : fields) {
                                try {
                                    field.setAccessible(true);
                                    //把每个字段的值存到redis中  对象为MAINTAIN_加养护阶段id主键 ，key为字段名 value为字段值
                                    redisTemplate.opsForHash().put(MAINTAIN + route[0]+route[1], field.getName(), field.get(alarmValueSettingVo1));
                                } catch (IllegalAccessException exception) {
                                    exception.printStackTrace();
                                }
                            }
                            break;
                        }
                    }
                }
                AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingVo1;
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
            AlarmValueSettingVo alarmValueSettingVo1=null;
            if(warningType.equals("06") || warningType.equals("08") || warningType.equals("10") || warningType.equals("12") || warningType.equals("13")){
                List type = Arrays.asList(warningType);
                warningDatas = earlyWarningDao.findWarningDataByNode(affirm,stackId,null,type);
                MaintainPlanVo maintainPlanVo = alarmValueSettingDao.findByMaintainStackId(stackId);
                if(ToolUtil.isNotEmpty(redisTemplate.opsForHash().entries(MAINTAIN + maintainPlanVo.getStageId()))){
                    alarmValueSettingVo1 = JSON.parseObject(JSON.toJSONString(redisTemplate.boundHashOps(MAINTAIN + maintainPlanVo.getStageId()).entries()),AlarmValueSettingVo.class);
                }else {
                    alarmValueSettingVo1 = alarmValueSettingDao.findAlarmValueSettingById(maintainPlanVo.getAlarmValueSettingId());
                    //获取所有的字段
                    for (Field field : fields) {
                        try {
                            field.setAccessible(true);
                            //把每个字段的值存到redis中  对象为MAINTAIN_加养护阶段id主键 ，key为字段名 value为字段值
                            redisTemplate.opsForHash().put(MAINTAIN + maintainPlanVo.getStageId(), field.getName(), field.get(alarmValueSettingVo1));
                        } catch (IllegalAccessException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
                AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingVo1;
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
}
