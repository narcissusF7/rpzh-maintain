package com.rpzh.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.*;
import com.github.pagehelper.util.StringUtil;
import com.rpzh.dao.*;
import com.rpzh.dto.*;
import com.rpzh.entity.*;
import com.rpzh.enums.WarehouseNodeType;
import com.rpzh.feign.UserFeign;
import com.rpzh.pojo.NodeData;
import com.rpzh.service.MaintainPlanService;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Field;
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
        //获取垛位id
        Integer stackId = maintainPlanDto.getStackId();
        //负责人id
        maintainPlanDto.setPrincipalId(StringUtils.join(maintainPlanDto.getPrincipalIds(),","));
        //物料代码
        List<String> materialCodes = maintainPlanDto.getMaterialCodes();
        maintainPlanDto.setMaterialCode(String.join(",",maintainPlanDto.getMaterialCodes()));
        //养护计划创建的时间
        Date nowDate = new Date();
        //创建养护计划的用户
        String createUser = TokenDecode.getUserInfo().get("username");
        maintainPlanDto.setCreateUser(createUser);
        maintainPlanDto.setCreatedTime(nowDate);
        //结束当前阶段的时间
        Date date  = new Date(nowDate.getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l);
        maintainPlanDto.setAftercareDateEnd(date);
        //新建养护计划
        maintainPlanDao.insertMaintainPlan(maintainPlanDto);
        //获取当前养护计划的id
        Integer id =maintainPlanDao.findIdByStackId(stackId);
        //将新建养护计划中的所有物料代码以及各自的数量插入到养护数量表中，方便养护数量模板的统计
        String maintenceStyle = maintainPlanDto.getMaintenceStyle();
        String phases = maintainPlanDto.getPhases();
        String pidRoute = maintainPlanDto.getPidRoute();
        List<MaintainPlanDto> maintainPlanDtos = new ArrayList<>();
        List<MaterialVo> materialVos = materialDao.findCount(stackId, materialCodes);
        for (MaterialVo materialVo : materialVos) {
            MaintainPlanDto maintainPlanDto1 = new MaintainPlanDto();
            maintainPlanDto1.setMaterialCode(materialVo.getMaterialCode());
            maintainPlanDto1.setMaintainCount(materialVo.getMaintainCount());
            maintainPlanDto1.setMaintainPlanId(id);
            maintainPlanDto1.setStackId(Integer.parseInt(pidRoute.split("05:")[1].split("]")[0]));
            maintainPlanDto1.setAreaId(Integer.parseInt(pidRoute.split("04:")[1].split("],\\[05")[0]));
            maintainPlanDto1.setFloorId(Integer.parseInt(pidRoute.split("03:")[1].split("],\\[04")[0]));
            maintainPlanDto1.setBuildingId(Integer.parseInt(pidRoute.split("02:")[1].split("],\\[03")[0]));
            maintainPlanDto1.setWarehouseId(Integer.parseInt(pidRoute.split("01:")[1].split("],\\[02")[0]));
            maintainPlanDto1.setMaintenceStyle(maintenceStyle);
            maintainPlanDto1.setPhases(phases);
            maintainPlanDto1.setNowDate(nowDate);
            maintainPlanDtos.add(maintainPlanDto1);
        }
        maintainPlanDao.insertMaintainCount(maintainPlanDtos);
        //将数值插入到预警规则表中
        maintainPlanDto.setId(id);
        AlarmValueSettingDto alarmValueSettingDto = new AlarmValueSettingDto(maintainPlanDto);
        alarmValueSettingDao.insertAlarmValueSetting(alarmValueSettingDto);
        //获取新创建的预警规则id
        Integer alarmValueSettingId = alarmValueSettingDao.findIdByEarlyWarningRuleName(alarmValueSettingDto.getEarlyWarningRuleName());
        maintainPlanDto.setAlarmValueSettingId(alarmValueSettingId);
        //插入到养护阶段表中
        maintainPlanDao.insertMaintainStage(maintainPlanDto);
        //获取新创建的养护阶段id
        Integer stageId = maintainPlanDao.findStageIdById(id);
        //更新养护计划表中的养护阶段id
        return maintainPlanDao.updateMaintainPlanStageId(stageId,id);
    }

    @Override
    public List<String> findAlarmSettingTemplateList(){
        return alarmSettingTemplateDao.findAlarmSettingTemplateList();
    }
    @Override
    public AlarmValueSettingVo findAlarmSettingTemplate(String earlyWarningRuleName) {
        //根据预警规则名称获取预警模板数值
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
        //判断该垛位存在存在养护计划
        return maintainPlanDao.isExistStackId(stackId);
    }

    /**
     * 查询所有养护计划
     *
     * @param maintainPlanDto
     * @return success or failure
     */
    @Override
    public PageInfo<MaintainPlanVo> findMaintainPlanInfo(MaintainPlanDto maintainPlanDto) {
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        maintainPlanDto.setNowDate(new Date());
        String pidRoute = maintainPlanDto.getPidRoute();
        if(StringUtil.isNotEmpty(pidRoute)) {
            int length = pidRoute.split(",").length;
            if (length < 5) {
                pidRoute = pidRoute + ",[0" + length + ":" + maintainPlanDto.getStackId() + "]";
                maintainPlanDto.setStackId(null);
                maintainPlanDto.setPidRoute(pidRoute);
            }
        }
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
                redisTemplate.delete(MAINTAIN + stageId);
            }
        }
        for (Integer id : ids) {
            maintainPlanDao.deleteMaintainCount(id);
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
        List<String> principalIds = Arrays.asList(maintainPlanVo.getPrincipalId().split(","));
        List<Integer> principalId = principalIds.stream().map(Integer::parseInt).collect(Collectors.toList());
        maintainPlanVo.setPrincipalName(userFeign.findUsernameById(principalId).getData().toString().replaceAll("\\[","").replaceAll("\\]",""));
        maintainPlanVo.setPrincipalIds(principalIds);
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findDetails(id);
        maintainPlanVos.forEach(maintainPlan->{
            maintainPlan.setPrincipalName(userFeign.findUsernameById(principalId).getData().toString().replaceAll("\\[","").replaceAll("\\]",""));
        });
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
        //01：无线氧气传感器,03：无线温湿度传感器,04： 无线包芯温度传感器,
        Integer stackId = maintainPlanVo.getStackId();
        StackVo stackVo = warehouseDao.findPidRoute(stackId);
        String pidRoute = stackVo.getPidRoute();
        alarmValueSettingVo = getAlarmValueSetting(alarmValueSettingVo,pidRoute,fields);
        maintainPlanVo.setAlarmValueSettingVo(alarmValueSettingVo);
        Integer areaId = stackVo.getAreaId();
        List<String> conditioningDatas = airConditioningDao.findAirConditioningData(areaId);
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
            NodeData nodeData;
            for (String conditioningData : conditioningDatas) {
                nodeData = new ObjectMapper().convertValue(redisTemplate.opsForValue().get(MAINTAIN + conditioningData), NodeData.class);
                humitureSum = humitureSum + nodeData.getHumiture();
                tempSum = tempSum + nodeData.getTemp();
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
        List<String> principalIds = Arrays.asList(maintainPlanVo.getPrincipalId().split(","));
        maintainPlanVo.setPrincipalIds(principalIds);
        maintainPlanVo.setPrincipalId(null);
        Integer stackId = maintainPlanVo.getStackId();
        maintainPlanVo.setMaintainCount(materialDao.getMaintainCount(stackId));
        maintainPlanVo.setMaterialCode(null);
        maintainPlanVo.setPrincipalName(null);
        maintainPlanVo.setMaterialCodes(materialDao.findMaterialCode(stackId));
        String pidRoute = warehouseDao.findPidRoute(stackId).getPidRoute();
        maintainPlanVo.setPidRoute(pidRoute + ",[" + WarehouseNodeType.STACK_NODE.getType() + ":" + stackId + "]");
        Integer alarmValueSettingId = maintainPlanVo.getAlarmValueSettingId();
        AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingDao.findAlarmValueSettingById(alarmValueSettingId);
        Field[] fields = AlarmValueSettingVo.class.getDeclaredFields();
        alarmValueSettingVo = getAlarmValueSetting(alarmValueSettingVo,pidRoute,fields);
        maintainPlanVo.setAlarmValueSettingVo(alarmValueSettingVo);
        return maintainPlanVo;
    }

    @Override
    public MaintainPlanVo beforeChangeMaintainPlanStage(Integer id) {
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findMaintainPlan(id);
        List<String> principalIds = Arrays.asList(maintainPlanVo.getPrincipalId().split(","));
        List<Integer> principalId = principalIds.stream().map(Integer::parseInt).collect(Collectors.toList());
        maintainPlanVo.setPrincipalName(String.join(",",userFeign.findUsernameById(principalId).getData().toString()));
        maintainPlanVo.setPrincipalIds(principalIds);
        MaintainPlanVo maintainPlanVo1 = new MaintainPlanVo();
        String pidRoute = warehouseDao.findPidRoute(maintainPlanVo.getStackId()).getPidRoute();
        maintainPlanVo1.setAlarmValueSettingId(maintainPlanVo.getAlarmValueSettingId());
        maintainPlanVo1.setStackId(maintainPlanVo.getStackId());
        maintainPlanVo1.setPidRoute(pidRoute);
        maintainPlanVo1.setMaterialCode(maintainPlanVo.getMaterialCode());
        maintainPlanVo1.setMaterialCodes(Arrays.asList(maintainPlanVo.getMaterialCode().split(",")));
        maintainPlanVo1.setStackName(maintainPlanVo.getStackName());
        maintainPlanVo1.setPrincipalIds(Arrays.asList(maintainPlanVo.getPrincipalId().split(",")));
        maintainPlanVo1.setMaintenceStyle(maintainPlanVo.getMaintenceStyle());
        return maintainPlanVo1;
    }

    @Override
    public List<String> beforeChangePhases(Integer stackId,String maintenceStyle) {
        List<String> phases = new ArrayList<>();
        phases.add("养护阶段");
        phases.add("充氮杀虫阶段");
        phases.add("磷化铝杀虫阶段");
        if(maintenceStyle.equals("机械调控方式")) {
            phases.add("充氮保质阶段");
        }
        List<String> phases1 = maintainPlanDao.findPhases(stackId, maintenceStyle);
        for (String s : phases1) {
            if(phases.contains(s)){
                phases.remove(s);
            }
        }
        return phases;
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
            Object nodeInfo = redisTemplate.opsForValue().get(MAINTAIN + nodeId);
            if(ToolUtil.isNotEmpty(nodeInfo)){
            NodeData nodeData = JSON.parseObject(nodeInfo.toString(), NodeData.class);
                switch (nodeType) {
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
        List<String> materialCodes = maintainPlanDto.getMaterialCodes();
        Integer stackId = maintainPlanDto.getStackId();
        maintainPlanDto.setPrincipalId(StringUtils.join(principalIds,","));
        maintainPlanDto.setMaterialCode(String.join(",", materialCodes));
        String updateUser = TokenDecode.getUserInfo().get("username");
        Date nowDate = new Date();
        maintainPlanDto.setUpdateUser(updateUser);
        maintainPlanDto.setUpdatedTime(nowDate);
        Date date = new Date(nowDate.getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l);
        maintainPlanDto.setAftercareDateEnd(date);
        Integer r = maintainPlanDao.updateMaintainPlan(maintainPlanDto);
        System.out.println(r);
        Integer k = maintainPlanDao.updateMaintainPlanStage(maintainPlanDto);
        System.out.println(k);
        String maintenceStyle = maintainPlanDto.getMaintenceStyle();
        String phases = maintainPlanDto.getPhases();
        String pidRoute = maintainPlanDto.getPidRoute();
        Integer id = maintainPlanDto.getId();
        maintainPlanDao.deleteMaintainCount(id);
        List<MaintainPlanDto> maintainPlanDtos = new ArrayList<>();
        List<MaterialVo> materialVos = materialDao.findCount(stackId, materialCodes);
        for (MaterialVo materialVo : materialVos) {
            MaintainPlanDto maintainPlanDto1 = new MaintainPlanDto();
            maintainPlanDto1.setMaterialCode(materialVo.getMaterialCode());
            maintainPlanDto1.setMaintainCount(materialVo.getMaintainCount());
            maintainPlanDto1.setMaintainPlanId(id);
            maintainPlanDto1.setStackId(Integer.parseInt(pidRoute.split("05:")[1].split("]")[0]));
            maintainPlanDto1.setAreaId(Integer.parseInt(pidRoute.split("04:")[1].split("],\\[05")[0]));
            maintainPlanDto1.setFloorId(Integer.parseInt(pidRoute.split("03:")[1].split("],\\[04")[0]));
            maintainPlanDto1.setBuildingId(Integer.parseInt(pidRoute.split("02:")[1].split("],\\[03")[0]));
            maintainPlanDto1.setWarehouseId(Integer.parseInt(pidRoute.split("01:")[1].split("],\\[02")[0]));
            maintainPlanDto1.setMaintenceStyle(maintenceStyle);
            maintainPlanDto1.setPhases(phases);
            maintainPlanDto1.setNowDate(nowDate);
            maintainPlanDtos.add(maintainPlanDto1);
        }
        maintainPlanDao.insertMaintainCount(maintainPlanDtos);
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
        maintainPlanDao.deleteMaintainCount(maintainPlanId);
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
        List<String> materialCodes = maintainPlanDto.getMaterialCodes();
        maintainPlanDto.setMaterialCode(String.join(",", materialCodes));
        maintainPlanDto.setPrincipalId(StringUtils.join(principalIds,","));
        String createUser = TokenDecode.getUserInfo().get("username");
        Date date = new Date();
        maintainPlanDto.setCreateUser(createUser);
        maintainPlanDto.setCreatedTime(date);
        maintainPlanDto.setUpdatedTime(date);
        maintainPlanDto.setUpdateUser(createUser);
        maintainPlanDto.setVersion(0);
        AlarmValueSettingDto alarmValueSettingDto = new AlarmValueSettingDto(maintainPlanDto);
        alarmValueSettingDao.changeStatus(alarmValueSettingDto);
        alarmValueSettingDao.insertAlarmValueSetting(alarmValueSettingDto);
        maintainPlanDto.setAlarmValueSettingId(alarmValueSettingDao.findIdByEarlyWarningRuleName(maintainPlanDto.getEarlyWarningRuleName()));
        date = new Date(new Date().getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l);
        maintainPlanDto.setAftercareDateEnd(date);
        maintainPlanDao.insertMaintainStage(maintainPlanDto);
        maintainPlanDto.setStageId(maintainPlanDao.findStageIdById(maintainPlanDto.getId()));
        Date createdTime = maintainPlanDao.findMaintainPlan(maintainPlanDto.getId()).getCreatedTime();
        int day = new Long((new Date().getTime() - createdTime.getTime()) / (1000 * 60 * 60 * 24)).intValue() + 1;
        maintainPlanDto.setMaintainDays(day);
        maintainPlanDto.setAftercareDateEnd(new Date());
        String maintenceStyle = maintainPlanDto.getMaintenceStyle();
        String phases = maintainPlanDto.getPhases();
        String pidRoute = maintainPlanDto.getPidRoute()  + ",[" + WarehouseNodeType.STACK_NODE.getType() + ":" + maintainPlanDto.getStackId() + "]";
        Integer id = maintainPlanDto.getId();
        Integer stackId = maintainPlanDto.getStackId();
        maintainPlanDao.deleteMaintainCount(id);
        List<MaintainPlanDto> maintainPlanDtos = new ArrayList<>();
        List<MaterialVo> materialVos = materialDao.findCount(stackId, materialCodes);
        for (MaterialVo materialVo : materialVos) {
            MaintainPlanDto maintainPlanDto1 = new MaintainPlanDto();
            maintainPlanDto1.setMaterialCode(materialVo.getMaterialCode());
            maintainPlanDto1.setMaintainCount(materialVo.getMaintainCount());
            maintainPlanDto1.setMaintainPlanId(id);
            maintainPlanDto1.setStackId(Integer.parseInt(pidRoute.split("05:")[1].split("]")[0]));
            maintainPlanDto1.setAreaId(Integer.parseInt(pidRoute.split("04:")[1].split("],\\[05")[0]));
            maintainPlanDto1.setFloorId(Integer.parseInt(pidRoute.split("03:")[1].split("],\\[04")[0]));
            maintainPlanDto1.setBuildingId(Integer.parseInt(pidRoute.split("02:")[1].split("],\\[03")[0]));
            maintainPlanDto1.setWarehouseId(Integer.parseInt(pidRoute.split("01:")[1].split("],\\[02")[0]));
            maintainPlanDto1.setMaintenceStyle(maintenceStyle);
            maintainPlanDto1.setPhases(phases);
            maintainPlanDto1.setNowDate(date);
            maintainPlanDtos.add(maintainPlanDto1);
        }
        maintainPlanDao.insertMaintainCount(maintainPlanDtos);
        Integer result = maintainPlanDao.changeMaintainPlanStage(maintainPlanDto);
        return result;
    }

    @Override
    public MaintainPlanVo findMaintainPlanDetails(Integer id,Integer stackId) {
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findDetails(id);
        maintainPlanVos.forEach(maintainPlan->{
            List<String> principalIds = Arrays.asList(maintainPlan.getPrincipalId().split(","));
            List<Integer> principalId = principalIds.stream().map(Integer::parseInt).collect(Collectors.toList());
            maintainPlan.setPrincipalName(userFeign.findUsernameById(principalId).getData().toString().replaceAll("\\[","").replaceAll("\\]",""));
        });
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findDetailsNew(id,stackId);
        maintainPlanVo.setMaintainPlanVos(maintainPlanVos);
        return maintainPlanVo;
    }

    @Override
    public PageInfo<MaintainPlanVo> findExpireCollectTable(MaintainPlanDto maintainPlanDto) {
        if(StringUtil.isNotEmpty(maintainPlanDto.getEndTime())){
            maintainPlanDto.setEndTime(maintainPlanDto.getEndTime().replace("00:00:00","23:59:59"));
        }
        String pidRoute = maintainPlanDto.getPidRoute();
        if(StringUtil.isNotEmpty(pidRoute)) {
            int length = pidRoute.split(",").length;
            if (length < 5) {
                pidRoute = pidRoute + ",[0" + length + ":" + maintainPlanDto.getStackId() + "]";
                maintainPlanDto.setStackId(null);
                maintainPlanDto.setPidRoute(pidRoute);
            }
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
        String pidRoute = maintainPlanDto.getPidRoute();
        if(StringUtil.isNotEmpty(pidRoute)) {
            int length = pidRoute.split(",").length;
            if (length < 5) {
                pidRoute = pidRoute + ",[0" + length + ":" + maintainPlanDto.getStackId() + "]";
                maintainPlanDto.setStackId(null);
                maintainPlanDto.setPidRoute(pidRoute);
            }
        }
        List<MaintainPlanVo> maintainMonthEndReport = maintainPlanDao.findMaintainMonthEndReport(maintainPlanDto);
        return new PageInfo(maintainMonthEndReport);
    }

    @Override
    public PageInfo<MaintainPlanVo> findMaintainHistoryCollect(MaintainPlanDto maintainPlanDto) {
        if(ToolUtil.isNotEmpty(maintainPlanDto.getPidRoute())) {
            maintainPlanDto.setPidRoute(maintainPlanDto.getPidRoute() + ",[0" + maintainPlanDto.getPidRoute().split(",").length + ":" + maintainPlanDto.getStackId() + "]");
        }
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        String pidRoute = maintainPlanDto.getPidRoute();
        if(StringUtil.isNotEmpty(pidRoute)) {
            int length = pidRoute.split(",").length;
            if (length < 5) {
                pidRoute = pidRoute + ",[0" + length + ":" + maintainPlanDto.getStackId() + "]";
                maintainPlanDto.setStackId(null);
                maintainPlanDto.setPidRoute(pidRoute);
            }
        }
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findMaintainHistoryCollect(maintainPlanDto);
        for (MaintainPlanVo maintainPlanVo : maintainPlanVos) {
                maintainPlanVo.setChangeStatus("已开垛");
        }
        return new PageInfo(maintainPlanVos);
    }

    @Override
    @Transactional
    public Result moveStack(MaintainPlanDto maintainPlanDto) {

        List<String> nodeDtoList = maintainPlanDto.getNodeDtoList();
        List<NodeDto> nodeDtos = new ArrayList<>();
        nodeDtoList.forEach(nodeDtostr ->
                nodeDtos.add(JSONObject.parseObject(nodeDtostr,NodeDto.class)));
        if (maintainPlanDto.getStackId().equals(nodeDtos.get(0).getStackId())) {
            return new Result(false, StatusCode.ERROR, "垛位没有移动!");
        } else {
            //新的垛位id
            Integer stackId = maintainPlanDto.getStackId();
            //新的位置路由
            String siteRoute = maintainPlanDto.getPidRoute()+",[05:"+stackId+"]";
            //新的位置名称
            String siteName = maintainPlanDto.getSiteName();

            //判断目标垛位是否存在养护计划
            Integer count = maintainPlanDao.findStackCountByStackId(stackId);
            if (count > 0) {
                return new Result<>(false, StatusCode.ERROR, "该垛位已有养护计划!");
            } else {
                //修改养护计划
                Integer flag = maintainPlanDao.updateStackByStackId(maintainPlanDto.getId(), stackId);
                if (flag > 0) {
                    //修改养护数量表的垛位信息
                    maintainPlanDto.setAreaId(Integer.parseInt(siteRoute.split("04:")[1].split("],\\[05")[0]));
                    maintainPlanDto.setFloorId(Integer.parseInt(siteRoute.split("03:")[1].split("],\\[04")[0]));
                    maintainPlanDto.setBuildingId(Integer.parseInt(siteRoute.split("02:")[1].split("],\\[03")[0]));
                    maintainPlanDto.setWarehouseId(Integer.parseInt(siteRoute.split("01:")[1].split("],\\[02")[0]));
                    maintainPlanDao.updateMaintainCount(maintainPlanDto);
                    //修改垛位信息
                    nodeDtos.forEach((nodeDto) -> {
                        nodeDto.setSiteId(stackId);
                        nodeDto.setStackId(stackId);
                        nodeDto.setSiteRoute(siteRoute);
                        nodeDto.setGatherSite(siteName);
                        nodeDto.setStack(siteName);
                        nodeDao.updateNode(nodeDto);
                    });
                    return new Result(true, StatusCode.OK, "移垛成功!");
                }
                return new Result(false, StatusCode.ERROR, "养护计划修改失败!");
            }
        }
    }

    @Override
    public Map<String,Object> findMaintainCountReport(MaintainCountDto maintainCountDto) {
        Double sum = 0d;
        //00:根节点,01:仓库, 02: 楼号, 03: 层, 04: 区, 05: 垛位',
        String pidRoute = maintainCountDto.getPidRoute();
        Integer stackId = maintainCountDto.getStackId();
        String materialCode = maintainCountDto.getMaterialCode();
        String maintenceStyle = maintainCountDto.getMaintenceStyle();
        String phases = maintainCountDto.getPhases();
        String key = "not";
        if(StringUtil.isNotEmpty(materialCode) || StringUtil.isNotEmpty(maintenceStyle) || StringUtil.isNotEmpty(phases) || StringUtil.isNotEmpty(pidRoute)){
            key = maintenceStyle + phases + pidRoute + materialCode;
        }
        List<MaintainCountVo> maintainCountVos = new ArrayList<MaintainCountVo>();
        Map<String,Object> map = new HashMap<>();
        //位置不为空
        if(ToolUtil.isNotEmpty(pidRoute) && ToolUtil.isNotEmpty(stackId)){
            int length = pidRoute.split(",").length;
            String column="";
            switch (length){
                case 1: column = "t1.warehouse_id";break;
                case 2: column = "t1.buliding_id";break;
                case 3: column = "t1.floor_id";break;
                case 4: column = "t1.area_id";break;
                case 5: column = "t1.stack_id";break;
            }
            sum = maintainPlanDao.findCount(maintainCountDto,column);
            maintainCountDto.setPidRoute(pidRoute);
            PageHelper.startPage(maintainCountDto.getPage(), maintainCountDto.getSize());
            maintainCountVos = maintainPlanDao.findMaintainCountBySiteName(maintainCountDto,column);
            maintainCountVos.forEach(maintainCountVo -> {
                maintainCountVo.setChildren(new ArrayList<>());
            });
            redisTemplate.opsForValue().set(key,JSON.toJSONString(maintainCountVos));
            map.put("sum",sum);
            map.put("export",JSON.toJSONString(maintainCountVos));
            map.put("list",new PageInfo(maintainCountVos));
            return map;
        }
        //位置为空且物料代码也为空
        if(ToolUtil.isEmpty(pidRoute) && ToolUtil.isEmpty(stackId) && ToolUtil.isEmpty(materialCode)){
            sum = maintainPlanDao.findCount(maintainCountDto,"");
            PageHelper.startPage(maintainCountDto.getPage(), maintainCountDto.getSize());
            maintainCountVos = maintainPlanDao.findMaintainCountBySiteName(maintainCountDto,"t1.stack_id");
            maintainCountVos.forEach(maintainCountVo -> {
                maintainCountVo.setChildren(new ArrayList<>());
            });
            redisTemplate.opsForValue().set(key,JSON.toJSONString(maintainCountVos));
            map.put("sum",sum);
            map.put("export",JSON.toJSONString(maintainCountVos));
            map.put("list",new PageInfo(maintainCountVos));
            return map;
        }
        //物料代码不为空而位置为空
        if(ToolUtil.isEmpty(pidRoute) && ToolUtil.isEmpty(stackId) && ToolUtil.isNotEmpty(materialCode)){
            sum = maintainPlanDao.findCount(maintainCountDto,"");
            if(ToolUtil.isEmpty(sum)){
                maintainCountVos = new ArrayList<>();
                map.put("sum",0);
                map.put("list",new PageInfo(maintainCountVos));
                return map;
            }
            PageHelper.startPage(maintainCountDto.getPage(), maintainCountDto.getSize());
            maintainCountVos = maintainPlanDao.findFirst(maintainCountDto);
            List<MaintainCountVo> maintainCountOnes = new PageInfo<MaintainCountVo>(maintainCountVos).getList();
            Set<Integer> buildingIds = new HashSet<>();
            maintainCountOnes.forEach(maintainCountOne -> { buildingIds.add(maintainCountOne.getBuildingId()); });
            List<MaintainCountVo> maintainCountTwos = maintainPlanDao.findTwo(maintainCountDto,buildingIds);
            Set<Integer> floorIds = new HashSet<>();
            maintainCountTwos.forEach(maintainCountTwo -> { floorIds.add(maintainCountTwo.getFloorId()); });
            List<MaintainCountVo> maintainCountThrees = maintainPlanDao.findThree(maintainCountDto,floorIds);
            Set<Integer> areaIds = new HashSet<>();
            maintainCountThrees.forEach(maintainCountThree -> { areaIds.add(maintainCountThree.getAreaId()); });
            List<MaintainCountVo> maintainCountFours = maintainPlanDao.findFour(maintainCountDto,areaIds);
            Set<Integer> stackIds = new HashSet<>();
            maintainCountFours.forEach(maintainCountFour -> { stackIds.add(maintainCountFour.getStackId()); });
            List<MaintainCountVo> maintainCountFives = maintainPlanDao.findFive(maintainCountDto,stackIds);
            maintainCountFives.forEach(maintainCountVo -> {
                maintainCountVo.setId("05"+maintainCountVo.getId());
            });
            maintainCountFours.stream().forEach(maintainCountVo ->{
                maintainCountVo.setId("04"+maintainCountVo.getId());
                maintainCountFives.stream().collect(Collectors.groupingBy(maintainCountFive -> maintainCountFive.getAreaId()+maintainCountFive.getMaterialCode())).forEach((keys, value)->{
                    if ((maintainCountVo.getAreaId()+maintainCountVo.getMaterialCode()).equals(keys)){
                        maintainCountVo.setChildren(value);
                    }
                });
            });
            maintainCountThrees.stream().forEach(maintainCountVo ->{
                maintainCountVo.setId("03"+maintainCountVo.getId());
                maintainCountFours.stream().collect(Collectors.groupingBy(maintainCountFive -> maintainCountFive.getFloorId()+maintainCountFive.getMaterialCode())).forEach((keys, value)->{
                    if ((maintainCountVo.getFloorId()+maintainCountVo.getMaterialCode()).equals(keys)){
                        maintainCountVo.setChildren(value);
                    }
                });
            });
            maintainCountTwos.stream().forEach(maintainCountVo ->{
                maintainCountVo.setId("02"+maintainCountVo.getId());
                maintainCountThrees.stream().collect(Collectors.groupingBy(maintainCountFive -> maintainCountFive.getBuildingId()+maintainCountFive.getMaterialCode())).forEach((keys, value)->{
                    if ((maintainCountVo.getBuildingId()+maintainCountVo.getMaterialCode()).equals(keys)){
                        maintainCountVo.setChildren(value);
                    }
                });
            });
            maintainCountOnes.stream().forEach(maintainCountVo ->{
                maintainCountVo.setId("01"+maintainCountVo.getId());
                maintainCountTwos.stream().collect(Collectors.groupingBy(maintainCountFive -> maintainCountFive.getWarehouseId()+maintainCountFive.getMaterialCode())).forEach((keys, value)->{
                    if ((maintainCountVo.getWarehouseId()+maintainCountVo.getMaterialCode()).equals(keys)){
                        maintainCountVo.setChildren(value);
                    }
                });
            });
            redisTemplate.opsForValue().set(key,JSON.toJSONString(maintainCountOnes));
            map.put("sum",sum);
            map.put("export",JSON.toJSONString(maintainCountVos));
            map.put("list",new PageInfo(maintainCountOnes));
            return map;
        }
        return new HashMap<>();
    }

    @Override
    public AlarmValueSettingVo findAlarmValueSetting(AlarmValueSettingDto alarmValueSettingDto) {
        AlarmValueSettingVo alarmValueSettingVo = null;
        List<String> materialCodes = alarmValueSettingDto.getMaterialCodes();
        for (String materialCode : materialCodes) {
            alarmValueSettingDto.setMaterialCode(materialCode);
            alarmValueSettingVo = alarmValueSettingDao.findByMaterialCode(alarmValueSettingDto);
            if(ToolUtil.isNotEmpty(alarmValueSettingVo)){
                break;
            }
        }
        if(ToolUtil.isEmpty(alarmValueSettingVo)){
            alarmValueSettingVo = new AlarmValueSettingVo();
        }
        //向上查找仓间预警数值
        String pidRoute = alarmValueSettingDto.getPidRoute();
        Field[] fields = AlarmValueSettingVo.class.getDeclaredFields();
        alarmValueSettingVo = getAlarmValueSetting(alarmValueSettingVo,pidRoute,fields);
        return alarmValueSettingVo;
    }

    public AlarmValueSettingVo getAlarmValueSetting(AlarmValueSettingVo alarmValueSettingVo,String pidRoute,Field[] fields){
        String[] pidRoutes = pidRoute.replaceAll("\\[","").replaceAll("\\]","").split(",");
        AlarmValueSettingVo alarmValueSettingVo1=null;
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
        if(ToolUtil.isNotEmpty(alarmValueSettingVo1)){
            alarmValueSettingVo.setWarehouseOxygenUpperOne(alarmValueSettingVo1.getWarehouseOxygenUpperOne());
            alarmValueSettingVo.setWarehouseOxygenUpperTwo(alarmValueSettingVo1.getWarehouseOxygenUpperTwo());
            alarmValueSettingVo.setWarehouseOxygenUpperThree(alarmValueSettingVo1.getWarehouseOxygenUpperThree());
            alarmValueSettingVo.setWarehouseOxygenLower(alarmValueSettingVo1.getWarehouseOxygenLower());
            alarmValueSettingVo.setWarehouseTempUpperOne(alarmValueSettingVo1.getWarehouseTempUpperOne());
            alarmValueSettingVo.setWarehouseTempUpperTwo(alarmValueSettingVo1.getWarehouseTempUpperTwo());
            alarmValueSettingVo.setWarehouseTempUpperThree(alarmValueSettingVo1.getWarehouseTempUpperThree());
            alarmValueSettingVo.setWarehouseTempLower(alarmValueSettingVo1.getWarehouseTempLower());
            alarmValueSettingVo.setWarehouseMoistureUpperOne(alarmValueSettingVo1.getWarehouseMoistureUpperOne());
            alarmValueSettingVo.setWarehouseMoistureUpperTwo(alarmValueSettingVo1.getWarehouseMoistureUpperTwo());
            alarmValueSettingVo.setWarehouseMoistureUpperThree(alarmValueSettingVo1.getWarehouseMoistureUpperThree());
            alarmValueSettingVo.setWarehouseMoistureLower(alarmValueSettingVo1.getWarehouseMoistureLower());
        }
        if(ToolUtil.isEmpty(alarmValueSettingVo1)){
            alarmValueSettingVo.setWarehouseOxygenUpperOne(AlertSettingSetDefault.warehouseOxygenUpperOne);
            alarmValueSettingVo.setWarehouseOxygenUpperTwo(AlertSettingSetDefault.warehouseOxygenUpperTwo);
            alarmValueSettingVo.setWarehouseOxygenUpperThree(AlertSettingSetDefault.warehouseOxygenUpperThree);
            alarmValueSettingVo.setWarehouseOxygenLower(AlertSettingSetDefault.warehouseOxygenLower);
            alarmValueSettingVo.setWarehouseTempUpperOne(AlertSettingSetDefault.warehouseTempUpperOne);
            alarmValueSettingVo.setWarehouseTempUpperTwo(AlertSettingSetDefault.warehouseTempUpperTwo);
            alarmValueSettingVo.setWarehouseTempUpperThree(AlertSettingSetDefault.warehouseTempUpperThree);
            alarmValueSettingVo.setWarehouseTempLower(AlertSettingSetDefault.warehouseTempLower);
            alarmValueSettingVo.setWarehouseMoistureUpperOne(AlertSettingSetDefault.warehouseMoistureUpperOne);
            alarmValueSettingVo.setWarehouseMoistureUpperTwo(AlertSettingSetDefault.warehouseMoistureUpperTwo);
            alarmValueSettingVo.setWarehouseMoistureUpperThree(AlertSettingSetDefault.warehouseMoistureUpperThree);
            alarmValueSettingVo.setWarehouseMoistureLower(AlertSettingSetDefault.warehouseMoistureLower);
        }
        return alarmValueSettingVo;
    }
}
