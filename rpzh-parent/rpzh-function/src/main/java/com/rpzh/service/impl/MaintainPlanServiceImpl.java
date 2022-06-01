package com.rpzh.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.*;
import com.github.pagehelper.util.StringUtil;
import com.rpzh.dao.*;
import com.rpzh.dto.*;
import com.rpzh.entity.*;
import com.rpzh.feign.UserFeign;
import com.rpzh.pojo.AlarmValueSetting;
import com.rpzh.pojo.NodeData;
import com.rpzh.service.MaintainPlanService;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.validator.constraints.pl.REGON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MaintainPlanServiceImpl implements MaintainPlanService {

    @Resource
    private MaintainPlanDao maintainPlanDao;

    @Resource
    private AlarmSettingTemplateDao alarmSettingTemplateDao;

    @Resource
    private AlarmValueSettingDao alarmValueSettingDao;

    @Resource
    private MaterialDao materialDao;

    @Resource
    private UserFeign userFeign;

    @Resource
    private NodeDao nodeDao;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private WarehouseDao warehouseDao;

    public static String MAINTAIN ="MAINTAIN_";

    @Resource
    private AirConditioningDao airConditioningDao;

    @Autowired
    private WareAreaAlarmSettingDao wareAreaAlarmSettingDao;

    /**
     * 新增养护计划
     *
     * @param maintainPlanDto 养护计划信息
     * @return success or failure
     */
    @Override
    public int insertMaintainPlan(MaintainPlanDto maintainPlanDto){
        List<Integer> principalIds = maintainPlanDto.getPrincipalIds();
        maintainPlanDto.setPrincipalId(principalIds.toString().replaceAll("\\[","").replaceAll("\\]","").replaceAll(" ",""));
        maintainPlanDto.setPrincipalName(String.join(",",userFeign.findUsernameById(principalIds).getData()));
        List<String> materialCodes = maintainPlanDto.getMaterialCodes();
        maintainPlanDto.setMaterialCode(String.join(",",materialCodes));
        Date nowDate = new Date();
        String createUser = TokenDecode.getUserInfo().get("username");
        maintainPlanDto.setCreateUser(createUser);
        maintainPlanDto.setCreatedTime(nowDate);
        String hours = new SimpleDateFormat("HH:mm:ss").format(new Date());
        maintainPlanDto.setOxygenReductionTime(maintainPlanDto.getOxygenReductionTime().replace("00:00:00", hours));
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = new Date(dayFormat.parse(maintainPlanDto.getAftercareDateStart()).getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l);
            maintainPlanDto.setAftercareDateEnd(date);
            maintainPlanDto.setAftercareDateStart(maintainPlanDto.getAftercareDateStart().replace("00:00:00", hours));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        maintainPlanDto.setExpireTime(maintainPlanDto.getExpireTime().replace("00:00:00", hours));
        maintainPlanDao.insertMaintainPlan(maintainPlanDto);
        Integer id =maintainPlanDao.findMaxPlanId(maintainPlanDto.getStackId());
        maintainPlanDto.setId(id);
        AlarmValueSettingDto alarmValueSettingDto = new AlarmValueSettingDto(maintainPlanDto);
        alarmValueSettingDao.insertAlarmValueSetting(alarmValueSettingDto);
        Integer alarmValueSettingId = alarmValueSettingDao.findMaxId();
        maintainPlanDto.setAlarmValueSettingId(alarmValueSettingId);
        maintainPlanDao.insertMaintainStage(maintainPlanDto);
        Integer stageId = maintainPlanDao.findMaxStageId(id);
        return maintainPlanDao.updateMaintainPlanStageId(stageId,id);
    }

    @Override
    public AlarmValueSettingVo findAlarmSettingTemplate(String earlyWarningRuleName) {
        AlarmValueSettingVo alarmSettingTemplate = alarmSettingTemplateDao.findAlarmSettingTemplate(earlyWarningRuleName);
        if(ToolUtil.isEmpty(alarmSettingTemplate)){
            alarmSettingTemplate.setStackOxygenUpperOne(AlertSettingSetDefault.stackOxygenUpperOne);
            alarmSettingTemplate.setStackOxygenUpperTwo(AlertSettingSetDefault.stackOxygenUpperTwo);
            alarmSettingTemplate.setStackOxygenUpperThree(AlertSettingSetDefault.stackOxygenUpperThree);
            alarmSettingTemplate.setStackOxygenLower(AlertSettingSetDefault.stackOxygenLower);
            alarmSettingTemplate.setTempCoreChipUpperOne(AlertSettingSetDefault.tempCoreChipUpperOne);
            alarmSettingTemplate.setTempCoreChipUpperTwo(AlertSettingSetDefault.tempCoreChipUpperTwo);
            alarmSettingTemplate.setTempCoreChipUpperThree(AlertSettingSetDefault.tempCoreChipUpperThree);
            alarmSettingTemplate.setTempCoreChipLower(AlertSettingSetDefault.tempCoreChipLower);
            alarmSettingTemplate.setStackTempUpperOne(AlertSettingSetDefault.stackTempUpperOne);
            alarmSettingTemplate.setStackTempUpperTwo(AlertSettingSetDefault.stackTempUpperTwo);
            alarmSettingTemplate.setStackTempUpperThree(AlertSettingSetDefault.stackTempUpperThree);
            alarmSettingTemplate.setStackTempLower(AlertSettingSetDefault.stackTempLower);
            alarmSettingTemplate.setStackMoistureUpperOne(AlertSettingSetDefault.stackMoistureUpperOne);
            alarmSettingTemplate.setStackMoistureUpperTwo(AlertSettingSetDefault.stackMoistureUpperTwo);
            alarmSettingTemplate.setStackMoistureUpperThree(AlertSettingSetDefault.stackMoistureUpperThree);
            alarmSettingTemplate.setStackMoistureLower(AlertSettingSetDefault.stackMoistureLower);
            alarmSettingTemplate.setCoreTempHighEnvTempOne(AlertSettingSetDefault.coreTempHighEnvTempOne);
            alarmSettingTemplate.setCoreTempHighEnvTempTwo(AlertSettingSetDefault.coreTempHighEnvTempTwo);
            alarmSettingTemplate.setCoreTempHighEnvTempThree(AlertSettingSetDefault.coreTempHighEnvTempThree);
            alarmSettingTemplate.setCoreTempLowEnvTemp(AlertSettingSetDefault.coreTempLowEnvTemp);
        }
        return alarmSettingTemplate;
    }

    @Override
    public Integer isExistStackId(Integer stackId) {
        return maintainPlanDao.isExistStackId(stackId);
    }

    /**
     * 查询养护计划
     *
     * @param maintainPlanDto 养护计划信息
     * @return success or failure
     */
    @Override
    public PageInfo<MaintainPlanVo> findMaintainPlanInfo(MaintainPlanDto maintainPlanDto) {
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        maintainPlanDto.setNowDate(new Date());
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findMaintainPlanInfo(maintainPlanDto);
        return new PageInfo<>(maintainPlanVos);
    }

    /**
     * 删除养护计划
     *
     * @param ids 养护计划id
     * @return success or failure
     */
    @Override
    public Result deleteMaintainPlanInfo(List<Integer> ids) {
        List<Integer> stageIds = maintainPlanDao.findId(ids);
        for (Integer stageId : stageIds) {
            if(ToolUtil.isNotEmpty(redisTemplate.opsForHash().entries(MAINTAIN + stageId))){
                redisTemplate.opsForHash().delete(MAINTAIN + stageId);
            }
        }
        maintainPlanDao.deleteMaintainPlanById(ids);
        List<Integer> alarmValueSettingIds = maintainPlanDao.findAlarmValueSettingId(ids);
        alarmValueSettingDao.deleteIds(alarmValueSettingIds);
        maintainPlanDao.deleteMaintainPlanStageById(ids);
        return new Result(true, StatusCode.OK, "删除成功!");
    }

    @Override
    public MaintainPlanVo findMaintainPlan(Integer id) {
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findMaintainPlan(id);
        maintainPlanVo.setMaterialCodes(Arrays.asList(maintainPlanVo.getMaterialCode().split(",")));
        maintainPlanVo.setPrincipalName(maintainPlanVo.getPrincipalName());
        maintainPlanVo.setPrincipalIds(Arrays.asList(maintainPlanVo.getPrincipalId().split(",")));
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findDetails(id);
        maintainPlanVo.setMaintainPlanVos(maintainPlanVos);
        Integer alarmValueSettingId = maintainPlanVo.getAlarmValueSettingId();
        AlarmValueSettingVo alarmValueSettingVo;
        Field[] fields = AlarmValueSettingVo.class.getDeclaredFields();
        if(ToolUtil.isNotEmpty(redisTemplate.opsForHash().entries(MAINTAIN + maintainPlanVo.getStageId()))){
            alarmValueSettingVo = JSON.parseObject(JSON.toJSONString(redisTemplate.boundHashOps(MAINTAIN + maintainPlanVo.getStageId()).entries()),AlarmValueSettingVo.class);
        }else {
            alarmValueSettingVo = alarmValueSettingDao.findAlarmValueSettingById(alarmValueSettingId);
            //获取所有的字段
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    //把每个字段的值存到redis中  对象为MAINTAIN_加养护阶段id主键 ，key为字段名 value为字段值
                    redisTemplate.opsForHash().put(MAINTAIN + maintainPlanVo.getStageId(), field.getName(), field.get(alarmValueSettingVo));
                } catch (IllegalAccessException exception) {
                    exception.printStackTrace();
                }
            }
        }
        maintainPlanVo.setAlarmValueSettingVo(alarmValueSettingVo);
        //01：无线氧气传感器,03：无线温湿度传感器,04： 无线包芯温度传感器,
        alarmValueSettingVo = null;
        Integer stackId = maintainPlanVo.getStackId();
        StackVo stackVo = warehouseDao.findPidRoute(stackId);
        String pidRoute = stackVo.getPidRoute();
        String[] pidRoutes =pidRoute.replaceAll("\\[","").replaceAll("\\]","").split(",");
        for (String routes : pidRoutes) {
            String[] route = routes.split(":");
            if(ToolUtil.isNotEmpty(redisTemplate.opsForHash().entries(MAINTAIN + route[0]+route[1]))){
                alarmValueSettingVo = JSON.parseObject(JSON.toJSONString(redisTemplate.boundHashOps(MAINTAIN + MAINTAIN + route[0]+route[1]).entries()),AlarmValueSettingVo.class);
                break;
            }
        }
        AlarmValueSettingVo alarmValueSettingVo1 = new AlarmValueSettingVo();
        if(ToolUtil.isEmpty(alarmValueSettingVo)){
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
        log.debug("alarmValueSettingVo1:{}",alarmValueSettingVo1);
        Integer areaId = stackVo.getAreaId();
        List<NodeData> conditioningDatas = airConditioningDao.findAirConditioningData(areaId);
        Map<String, Object> nodeDate = getNodeDate("05", stackId, null);
        if(ToolUtil.isNotEmpty(nodeDate)) {
            maintainPlanVo.setStackOxygenNow(Double.parseDouble(nodeDate.get("o2Avg").toString()));
            maintainPlanVo.setTempCoreChipNow(Double.parseDouble(nodeDate.get("coreTempAvg").toString()));
            maintainPlanVo.setStackTempNow(Double.parseDouble(nodeDate.get("tempAvg").toString()));
            maintainPlanVo.setStackMoistureNow(Double.parseDouble(nodeDate.get("humitureAvg").toString()));
            maintainPlanVo.setCoreTempHighEnvTempNow(Double.parseDouble(nodeDate.get("coreTempHighEnvTemp").toString()));
        }
        nodeDate = getNodeDate("04", null, pidRoute);
        if(ToolUtil.isNotEmpty(nodeDate)) {
            maintainPlanVo.setWarehouseOxygenNow(Double.parseDouble(nodeDate.get("o2Avg").toString()));
            Double tempSum = Double.parseDouble(nodeDate.get("tempSum").toString());
            Double humitureSum = Double.parseDouble(nodeDate.get("humitureSum").toString());
            Integer tempCount = Integer.parseInt(nodeDate.get("tempCount").toString());
            for (NodeData conditioningData : conditioningDatas) {
                tempSum = tempSum + conditioningData.getTemp();
                humitureSum = humitureSum + conditioningData.getHumiture();
            }
            Integer size = tempCount + conditioningDatas.size();
            Double tempAvg = size > 0 ? tempSum / (double) size : 0d;
            Double humitureAvg = size > 0 ? humitureSum / (double) size : 0d;
            maintainPlanVo.setWarehouseTempNow(Double.parseDouble(String.format("%.2f", tempAvg)));
            maintainPlanVo.setWarehouseMoistureNow(Double.parseDouble(String.format("%.2f", humitureAvg)));
        }
        return maintainPlanVo;
    }

    @Override
    public MaintainPlanVo beforeUpdateMaintainPlan(Integer id) {
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findMaintainPlan(id);
        Integer stackId = maintainPlanVo.getStackId();
        maintainPlanVo.setMaintainCount(materialDao.getMaintainCount(stackId));
        maintainPlanVo.setMaterialCodes(materialDao.findMaterialCode(stackId));
        maintainPlanVo.setMaterialName(String.join(",",materialDao.getMaterialName(stackId)));
        maintainPlanVo.setPrincipalIds(Arrays.asList(maintainPlanVo.getPrincipalId().split(",")));
        Integer alarmValueSettingId = maintainPlanVo.getAlarmValueSettingId();
        AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingDao.findAlarmValueSettingById(alarmValueSettingId);
        maintainPlanVo.setAlarmValueSettingVo(alarmValueSettingVo);
        return maintainPlanVo;
    }

    public Map<String,Object> getNodeDate(String siteType,Integer stackId,String pidRoute){
        List<NodeVo> nodeVos = nodeDao.findNodeIdBySiteType(siteType, stackId, pidRoute);
        if(nodeVos.size()==0){
            return null;
        }
        Double o2Sum = 0d;
        Double tempSum = 0d;
        Double humitureSum = 0d;
        Double coreTempSum = 0d;
        int o2Count = 0,tempCount = 0,coreTempCount = 0;
        for (NodeVo nodeVo : nodeVos) {
            String nodeType = nodeVo.getNodeType();
            String nodeId = nodeVo.getNodeId();
            NodeData nodeData = new ObjectMapper().convertValue(redisTemplate.opsForValue().get(MAINTAIN + nodeId), NodeData.class);
            switch (nodeType){
                case "01":
                    o2Sum = o2Sum + nodeData.getOxygen();
                    o2Count = o2Count + 1;
                    break;
                case "03":
                    coreTempSum = coreTempSum + nodeData.getTemp();
                    coreTempCount = coreTempCount + 1;
                    break;
                case "04":
                    tempSum = tempSum + nodeData.getTemp();
                    humitureSum = humitureSum + nodeData.getHumiture();
                    tempCount = tempCount + 1;
                    break;
                default:
                    break;
            }
        }
        Map<String,Object> nodeDataMap = new HashMap<>();
        Double o2Avg = o2Count>0 ? o2Sum/ (double)o2Count:0d;
        nodeDataMap.put("o2Avg",String.format("%.2f",o2Avg));
        if(siteType.equals("04")){
            nodeDataMap.put("tempSum",tempSum);
            nodeDataMap.put("humitureSum",humitureSum);
            nodeDataMap.put("tempCount",tempCount);
        }
        if(siteType.equals("05")) {
            Double tempAvg = tempCount > 0 ? tempSum / (double) tempCount : 0d;
            nodeDataMap.put("tempAvg", String.format("%.2f",tempAvg));
            Double humitureAvg = tempCount > 0 ? humitureSum / (double) tempCount : 0d;
            nodeDataMap.put("humitureAvg", String.format("%.2f",humitureAvg));
            Double coreTempAvg = coreTempCount > 0 ? coreTempSum / (double) coreTempCount : 0d;
            nodeDataMap.put("coreTempAvg", String.format("%.2f",coreTempAvg));
            nodeDataMap.put("coreTempHighEnvTemp", String.format("%.2f",coreTempAvg-tempAvg));
        }
        return nodeDataMap;
    }



    @Override
    public Integer updateMaintainPlan(MaintainPlanDto maintainPlanDto) {
        if(ToolUtil.isNotEmpty(redisTemplate.opsForHash().entries(MAINTAIN + maintainPlanDto.getStageId()))){
            redisTemplate.opsForHash().delete(MAINTAIN + maintainPlanDto.getStageId());
        }
        List<Integer> principalIds = maintainPlanDto.getPrincipalIds();
        maintainPlanDto.setPrincipalId(principalIds.toString().replaceAll("\\[","").replaceAll("\\]","").replaceAll(" ",""));
        maintainPlanDto.setPrincipalName(String.join(",",userFeign.findUsernameById(principalIds).getData()));
        maintainPlanDto.setMaterialCode(String.join(",",maintainPlanDto.getMaterialCodes()));
        String updateUser = TokenDecode.getUserInfo().get("username");
        Date nowDate = new Date();
        maintainPlanDto.setUpdateUser(updateUser);
        maintainPlanDto.setUpdatedTime(nowDate);
        String hours = new SimpleDateFormat("HH:mm:ss").format(nowDate);
        maintainPlanDto.setOxygenReductionTime(maintainPlanDto.getOxygenReductionTime().replace("00:00:00", hours));
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = new Date(dayFormat.parse(maintainPlanDto.getAftercareDateStart()).getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l);
            maintainPlanDto.setAftercareDateEnd(date);
            maintainPlanDto.setAftercareDateStart(maintainPlanDto.getAftercareDateStart().replace("00:00:00", hours));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        maintainPlanDto.setExpireTime(maintainPlanDto.getExpireTime().replace("00:00:00", hours));
        maintainPlanDao.updateMaintainPlan(maintainPlanDto);
        maintainPlanDao.updateMaintainPlanStage(maintainPlanDto);
        AlarmValueSettingDto alarmValueSettingDto = new AlarmValueSettingDto(maintainPlanDto);
        Integer result  =alarmValueSettingDao.updateAlarmValueSetting(alarmValueSettingDto);
        return result;
    }

    @Override
    public Integer endMaintainPlan(MaintainPlanDto maintainPlanDto) {
        if(ToolUtil.isNotEmpty(redisTemplate.opsForHash().entries(MAINTAIN + maintainPlanDto.getStageId()))){
            redisTemplate.opsForHash().delete(MAINTAIN + maintainPlanDto.getStageId());
        }
        Date updateTime = new Date();
        String updateUser = TokenDecode.getUserInfo().get("username");
        Integer maintainPlanId = maintainPlanDto.getId();
        materialDao.updateMaterialStatus(maintainPlanDto.getStackId());
        Integer result =  maintainPlanDao.endMaintainPlan(updateUser,updateTime,maintainPlanId);
        return result;
    }

    @Override
    public Integer changeMaintainPlanStage(MaintainPlanDto maintainPlanDto) {
        if(ToolUtil.isNotEmpty(redisTemplate.opsForHash().entries(MAINTAIN + maintainPlanDto.getStageId()))){
            redisTemplate.opsForHash().delete(MAINTAIN + maintainPlanDto.getStageId());
        }
        List<Integer> principalIds = maintainPlanDto.getPrincipalIds();
        maintainPlanDto.setPrincipalId(principalIds.toString().replaceAll("\\[","").replaceAll("\\]","").replaceAll(" ",""));
        maintainPlanDto.setPrincipalName(String.join(",",userFeign.findUsernameById(principalIds).getData()));
        String createUser = TokenDecode.getUserInfo().get("username");
        Date date = new Date();
        maintainPlanDto.setCreateUser(createUser);
        maintainPlanDto.setCreatedTime(date);
        maintainPlanDto.setUpdatedTime(date);
        maintainPlanDto.setUpdateUser(createUser);
        maintainPlanDto.setVersion(0);
        AlarmValueSettingDto alarmValueSettingDto = new AlarmValueSettingDto(maintainPlanDto);
        alarmValueSettingDao.updateAlarmValueSetting(alarmValueSettingDto);
        alarmValueSettingDao.insertAlarmValueSetting(alarmValueSettingDto);
        maintainPlanDto.setAlarmValueSettingId(alarmValueSettingDao.findMaxId());
        String hours = new SimpleDateFormat("HH:mm:ss").format(new Date());
        maintainPlanDto.setOxygenReductionTime(maintainPlanDto.getOxygenReductionTime().replace("00:00:00", hours));
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = new Date(dayFormat.parse(maintainPlanDto.getAftercareDateStart()).getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l);
            maintainPlanDto.setAftercareDateEnd(date);
            maintainPlanDto.setAftercareDateStart(maintainPlanDto.getAftercareDateStart().replace("00:00:00", hours));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        maintainPlanDto.setExpireTime(maintainPlanDto.getExpireTime().replace("00:00:00", hours));
        maintainPlanDao.insertMaintainStage(maintainPlanDto);
        maintainPlanDto.setStageId(maintainPlanDao.findMaxStageId(maintainPlanDto.getId()));
        Date aftercareDateStart = maintainPlanDao.findMaintainPlan(maintainPlanDto.getId()).getAftercareDateStart();
        int day = new Long((new Date().getTime() - aftercareDateStart.getTime()) / (1000 * 60 * 60 * 24)).intValue() + 1;
        maintainPlanDto.setMaintainDays(day);
        maintainPlanDto.setAftercareDateEnd(new Date());
        Integer result = maintainPlanDao.changeMaintainPlanStage(maintainPlanDto);
        return result;
    }

    @Override
    public MaintainPlanVo findMaintainPlanDetails(Integer id,Integer stackId) {
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findDetails(id);
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findDetailsNew(id,stackId);
        maintainPlanVo.setMaintainPlanVos(maintainPlanVos);
        return maintainPlanVo;
    }

    @Override
    public PageInfo<MaintainPlanVo> findExpireCollectTable(MaintainPlanDto maintainPlanDto) {
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        if(StringUtil.isNotEmpty(maintainPlanDto.getEndTime())){
            maintainPlanDto.setEndTime(maintainPlanDto.getEndTime().replace("00:00:00","23:59:59"));
        }
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        maintainPlanDto.setNowDate(new Date());
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findExpireCollectTable(maintainPlanDto);
        return new PageInfo<>(maintainPlanVos);

    }

    @Override
    public PageInfo<MaintainPlanVo> findMaintainMonthEndReport(MaintainPlanDto maintainPlanDto) {
        if(ToolUtil.isEmpty(maintainPlanDto.getStartTime()) && ToolUtil.isEmpty(maintainPlanDto.getEndTime())){
            SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calast = Calendar.getInstance();
            calast.set(Calendar.DAY_OF_MONTH, calast.getActualMaximum(Calendar.DAY_OF_MONTH));
            maintainPlanDto.setEndTime(sm.format(calast.getTime())+ " 23:59:59");
            calast.set(Calendar.DAY_OF_MONTH, calast.getActualMinimum(Calendar.DAY_OF_MONTH));
            maintainPlanDto.setStartTime(sm.format(calast.getTime())+ " 00:00:00");
        }
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        List<MaintainPlanVo> maintainMonthEndReport = maintainPlanDao.findMaintainMonthEndReport(maintainPlanDto);
        return new PageInfo(maintainMonthEndReport);
    }

    @Override
    public PageInfo<MaintainPlanVo> findMaintainHistoryCollect(MaintainPlanDto maintainPlanDto) {
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findMaintainHistoryCollect(maintainPlanDto);
        for (MaintainPlanVo maintainPlanVo : maintainPlanVos) {
            if (maintainPlanVo.getChangeStatus().equals("0")) {
                maintainPlanVo.setChangeStatus("未转段");
            }
            if (maintainPlanVo.getChangeStatus().equals("1")) {
                maintainPlanVo.setChangeStatus("已转段");
            }
        }
        return new PageInfo(maintainPlanVos);
    }

    @Override
    @Transactional
    public Integer moveStack(MaintainPlanDto maintainPlanDto) {
        List<NodeDto> nodeDtoList = maintainPlanDto.getNodeDtoList();
        Integer stackId = maintainPlanDto.getStackId();
        Integer count = maintainPlanDao.findStackCountByStackId(stackId);
        if (count > 0) {
            return -1;
        } else {
            Integer flag = maintainPlanDao.updateStackByStackId(maintainPlanDto.getId(), stackId);
            if (flag > 0) {
                nodeDtoList.forEach((nodeDto) -> {
                    nodeDto.setStackId(stackId);
                    nodeDao.updateNode(nodeDto);
                });
            }
            return flag;
        }
    }

    /*
    @Override
    public PageInfo<MaintainPlanVo> findMaintainCountReport(MaintainPlanDto maintainPlanDto) {
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        List<MaintainPlanVo> maintainCountReport = maintainPlanDao.findMaintainCountReport(maintainPlanDto);
        return new PageInfo(maintainCountReport);
    }
*/
}
