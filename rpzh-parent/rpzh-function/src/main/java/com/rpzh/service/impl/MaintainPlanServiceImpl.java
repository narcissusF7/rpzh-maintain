package com.rpzh.service.impl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.*;
import com.github.pagehelper.util.StringUtil;
import com.rpzh.dao.*;
import com.rpzh.dto.*;
import com.rpzh.entity.*;
import com.rpzh.enums.WarehouseNodeType;
import com.rpzh.service.MaintainPlanService;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.util.RedisUtil;
import com.rpzh.util.SiteRouteUtil;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
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

    //redis中用户key前缀
    private static final String USER = "USER_ID_";
    @Autowired
    private RedisUtil redisUtil;

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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = new Date(sdf.parse(maintainPlanDto.getAftercareDateStart()+" 00:00:00").getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l);
            maintainPlanDto.setAftercareDateEnd(date);
        }catch (Exception e){
            e.printStackTrace();
        }
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
            maintainPlanDto1.setStackId(stackId);
            maintainPlanDto1.setAreaId(SiteRouteUtil.getSiteId(pidRoute,"04"));
            maintainPlanDto1.setFloorId(SiteRouteUtil.getSiteId(pidRoute,"03"));
            maintainPlanDto1.setBuildingId(SiteRouteUtil.getSiteId(pidRoute,"02"));
            maintainPlanDto1.setWarehouseId(SiteRouteUtil.getSiteId(pidRoute,"01"));
            maintainPlanDto1.setMaintenceStyle(maintenceStyle);
            maintainPlanDto1.setPhases(phases);
            maintainPlanDto1.setNowDate(nowDate);
            maintainPlanDto1.setNowDate(nowDate);
            maintainPlanDtos.add(maintainPlanDto1);
        }
        if(ToolUtil.isNotEmpty(maintainPlanDtos)) {
            maintainPlanDao.insertMaintainCount(maintainPlanDtos);
        }
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
    public Result findCountByName(String earlyWarningRuleName) {
        Integer id = alarmSettingTemplateDao.findCountByName(earlyWarningRuleName);
        if(ToolUtil.isNotEmpty(id)){
            return new Result(false, StatusCode.ERROR, "该预警名称已存在!");
        }
        return new Result(true, StatusCode.OK, "");
    }

    @Override
    public AlarmValueSettingVo findAlarmSettingTemplate(String earlyWarningRuleName) {
        String user = TokenDecode.getUserInfo().get("username");
        String phases = redisTemplate.opsForValue().get(user + "phases").toString();
        //根据预警规则名称获取预警模板数值
        AlarmValueSettingVo alarmSettingTemplate = alarmSettingTemplateDao.findAlarmSettingTemplate(earlyWarningRuleName,phases);
        if(ToolUtil.isEmpty(alarmSettingTemplate)){
            alarmSettingTemplate = new AlarmValueSettingVo();
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
            if (!pidRoute.contains("04")) {
                maintainPlanDto.setPidRoute(SiteRouteUtil.route(pidRoute,maintainPlanDto.getStackId()+""));
                maintainPlanDto.setStackId(null);
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
        List<Integer> alarmValueSettingIds = maintainPlanDao.findAlarmValueSettingId(ids);
        for (Integer id : ids) {
            maintainPlanDao.deleteMaintainCount(id);
        }
        maintainPlanDao.deleteMaintainPlanById(ids);
        if(ToolUtil.isNotEmpty(alarmValueSettingIds)) {
            alarmValueSettingDao.deleteIds(alarmValueSettingIds);
        }
        maintainPlanDao.deleteMaintainPlanStageById(ids);
        return new Result(true, StatusCode.OK, "删除成功!");
    }

    @Override
    public MaintainPlanVo findMaintainPlan(Integer id) {
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findMaintainPlan(id);
        maintainPlanVo.setMaterialCodes(Arrays.asList(maintainPlanVo.getMaterialCode().split(",")));
        List<String> principalIds = Arrays.asList(maintainPlanVo.getPrincipalId().split(","));
        String principalName = "";
        for (String pid : principalIds) {
            principalName = principalName + (String)redisUtil.hget(USER+"username", USER+pid);
        }
        maintainPlanVo.setPrincipalName(principalName);
        maintainPlanVo.setPrincipalIds(principalIds.stream().map(Integer::parseInt).collect(Collectors.toList()));
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findDetails(id);
        maintainPlanVos.forEach(maintainPlan->{
            List<String> peopleIds = Arrays.asList(maintainPlan.getPrincipalId().split(","));
            String peopleName = "";
            for (String pid : peopleIds) {
                peopleName = peopleName + (String)redisUtil.hget(USER+"username", USER+pid);
            }
            maintainPlan.setPrincipalName(peopleName);
        });
        maintainPlanVo.setMaintainPlanVos(maintainPlanVos);
        Integer alarmValueSettingId = maintainPlanVo.getAlarmValueSettingId();
        AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingDao.findAlarmValueSettingById(alarmValueSettingId);;
        String pidRoute = maintainPlanVo.getPidRoute();
        alarmValueSettingVo = getAlarmValueSetting(alarmValueSettingVo,pidRoute);
        maintainPlanVo.setAlarmValueSettingVo(alarmValueSettingVo);
        Integer areaId = maintainPlanVo.getAreaId();
        List<String> airNodes = airConditioningDao.findAirConditioningData(areaId);
        if(ToolUtil.isNotEmpty(airNodes)) {
            Map<String, Double> map = nodeDao.findNowNodeData(airNodes);
            maintainPlanVo.setWarehouseTempNow(Double.parseDouble(String.format("%.2f", map.get("temp"))));
            maintainPlanVo.setWarehouseMoistureNow(Double.parseDouble(String.format("%.2f", map.get("humiture"))));
        }
        if(ToolUtil.isNotEmpty(id)) {
            Map<String, Double> stackEnv = nodeDao.findStackEnv(id);
            maintainPlanVo.setStackTempNow(Double.parseDouble(String.format("%.2f", stackEnv.get("temp"))));
            maintainPlanVo.setStackMoistureNow(Double.parseDouble(String.format("%.2f", stackEnv.get("humiture"))));
            Double stackCore = nodeDao.findStackCore(id);
            maintainPlanVo.setTempCoreChipNow(Double.parseDouble(String.format("%.2f", stackCore)));
            maintainPlanVo.setCoreTempHighEnvTempNow(Double.parseDouble(String.format("%.2f", stackCore - stackEnv.get("temp"))));
            maintainPlanVo.setStackOxygenNow(Double.parseDouble(String.format("%.2f", nodeDao.findO2StackData(id))));
            maintainPlanVo.setWarehouseOxygenNow(Double.parseDouble(String.format("%.2f", nodeDao.findO2AreaData(pidRoute))));
        }
        return maintainPlanVo;
    }

    @Override
    public MaintainPlanVo beforeUpdateMaintainPlan(Integer id) {
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findMaintainPlan(id);
        if(ToolUtil.isNotEmpty(maintainPlanVo)){
            return new MaintainPlanVo();
        }
        List<String> principalIds = Arrays.asList(maintainPlanVo.getPrincipalId().split(","));
        List<Integer> principalId = principalIds.stream().map(Integer::parseInt).collect(Collectors.toList());
        maintainPlanVo.setPrincipalIds(principalId);
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
        alarmValueSettingVo = getAlarmValueSetting(alarmValueSettingVo,pidRoute);
        maintainPlanVo.setAlarmValueSettingVo(alarmValueSettingVo);
        String createUser = TokenDecode.getUserInfo().get("username");
        System.out.println(maintainPlanVo.getPhases());
        redisTemplate.opsForValue().set(createUser+"phases",maintainPlanVo.getPhases());
        return maintainPlanVo;
    }

    @Override
    public MaintainPlanVo beforeChangeMaintainPlanStage(Integer id) {
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findMaintainPlan(id);
        List<String> principalIds = Arrays.asList(maintainPlanVo.getPrincipalId().split(","));
        List<Integer> principalId = principalIds.stream().map(Integer::parseInt).collect(Collectors.toList());
        Integer stackId = maintainPlanVo.getStackId();
        MaintainPlanVo maintainPlanVo1 = new MaintainPlanVo();
        String pidRoute = warehouseDao.findPidRoute(maintainPlanVo.getStackId()).getPidRoute();
        maintainPlanVo1.setPersonId(principalId.get(0));
        maintainPlanVo1.setAlarmValueSettingId(maintainPlanVo.getAlarmValueSettingId());
        maintainPlanVo1.setStackId(maintainPlanVo.getStackId());
        maintainPlanVo1.setPidRoute(pidRoute);
        maintainPlanVo1.setMaterialCodes(materialDao.findMaterialCode(stackId));
        maintainPlanVo1.setMaterialCode(StringUtils.join(maintainPlanVo1.getMaterialCodes(),","));
        maintainPlanVo1.setMaintainCount(materialDao.getMaintainCount(stackId));
        maintainPlanVo1.setStackName(maintainPlanVo.getStackName());
        maintainPlanVo1.setPrincipalIds(principalId);
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

    @Override
    public Integer updateMaintainPlan(MaintainPlanDto maintainPlanDto) {
        if(ToolUtil.isNotEmpty(redisTemplate.opsForHash().entries(MAINTAIN + maintainPlanDto.getStageId()))){
            redisTemplate.opsForHash().delete(MAINTAIN + maintainPlanDto.getStageId());
        }
        List<Integer> principalIds = maintainPlanDto.getPrincipalIds();
        List<String> materialCodes = maintainPlanDto.getMaterialCodes();
        Set<Integer> pids = new HashSet<>();
        pids.addAll(principalIds);
        Integer stackId = maintainPlanDto.getStackId();
        maintainPlanDto.setPrincipalId(StringUtils.join(pids,","));
        maintainPlanDto.setMaterialCode(String.join(",", materialCodes));
        String updateUser = TokenDecode.getUserInfo().get("username");
        Date nowDate = new Date();
        maintainPlanDto.setUpdateUser(updateUser);
        maintainPlanDto.setUpdatedTime(nowDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = new Date(sdf.parse(maintainPlanDto.getAftercareDateStart()+" 00:00:00").getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l);
            maintainPlanDto.setAftercareDateEnd(date);
        }catch (Exception e){
            e.printStackTrace();
        }
        maintainPlanDao.updateMaintainPlan(maintainPlanDto);
        maintainPlanDao.updateMaintainPlanStage(maintainPlanDto);
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
            maintainPlanDto1.setStackId(stackId);
            maintainPlanDto1.setAreaId(SiteRouteUtil.getSiteId(pidRoute,"04"));
            maintainPlanDto1.setFloorId(SiteRouteUtil.getSiteId(pidRoute,"03"));
            maintainPlanDto1.setBuildingId(SiteRouteUtil.getSiteId(pidRoute,"02"));
            maintainPlanDto1.setWarehouseId(SiteRouteUtil.getSiteId(pidRoute,"01"));
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
        Date createdTime = maintainPlanDao.findCreatedTime(maintainPlanId);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createdTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTime(new Date());
        nowCalendar.set(Calendar.HOUR_OF_DAY, 0);
        nowCalendar.set(Calendar.MINUTE, 0);
        nowCalendar.set(Calendar.SECOND, 0);
        long maintainDays = (nowCalendar.getTime().getTime() - calendar.getTime().getTime())/(1000 * 24 * 60 * 60);
        Integer result =  maintainPlanDao.endMaintainPlan(updateUser,updateTime,maintainPlanId,maintainDays);
        return result;
    }

    @Override
    public Integer changeMaintainPlanStage(MaintainPlanDto maintainPlanDto) {
        if(ToolUtil.isNotEmpty(redisTemplate.opsForHash().entries(MAINTAIN + maintainPlanDto.getStageId()))){
            redisTemplate.opsForHash().delete(MAINTAIN + maintainPlanDto.getStageId());
        }
        List<Integer> principalIds = maintainPlanDto.getPrincipalIds();
        Set<Integer> pids = new HashSet<>();
        pids.addAll(principalIds);
        List<String> materialCodes = maintainPlanDto.getMaterialCodes();
        maintainPlanDto.setMaterialCode(String.join(",", materialCodes));
        maintainPlanDto.setPrincipalId(StringUtils.join(pids,","));
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            maintainPlanDto.setAftercareDateEnd(new Date(sdf.parse(maintainPlanDto.getAftercareDateStart()+" 00:00:00").getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l));
        }catch (Exception e){
            e.printStackTrace();
        }
        maintainPlanDao.insertMaintainStage(maintainPlanDto);
        maintainPlanDto.setStageId(maintainPlanDao.findStageIdById(maintainPlanDto.getId()));
        Date createdTime = maintainPlanDao.findMaintainPlan(maintainPlanDto.getId()).getCreatedTime();
        int day = new Long((new Date().getTime() - createdTime.getTime()) / (1000 * 60 * 60 * 24)).intValue() + 1;
        maintainPlanDto.setMaintainDays(day);
        maintainPlanDto.setAftercareDateEnd(date);
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
            maintainPlanDto1.setStackId(stackId);
            maintainPlanDto1.setAreaId(SiteRouteUtil.getSiteId(pidRoute,"04"));
            maintainPlanDto1.setFloorId(SiteRouteUtil.getSiteId(pidRoute,"03"));
            maintainPlanDto1.setBuildingId(SiteRouteUtil.getSiteId(pidRoute,"02"));
            maintainPlanDto1.setWarehouseId(SiteRouteUtil.getSiteId(pidRoute,"01"));
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
            String principalName = "";
            for (String pid : principalIds) {
                principalName = principalName + (String)redisUtil.hget(USER+"username", USER+pid);
            }
            maintainPlan.setPrincipalName(principalName);
        });
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findDetailsNew(id);
        maintainPlanVo.setMaintainPlanVos(maintainPlanVos);
        return maintainPlanVo;
    }

    @Override
    public MaintainPlanVo findEndMaintainPlanDetails(Integer id) {
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findDetails(id);
        maintainPlanVos.forEach(maintainPlan->{
            List<String> principalIds = Arrays.asList(maintainPlan.getPrincipalId().split(","));
            String principalName = "";
            for (String pid : principalIds) {
                principalName = principalName + (String)redisUtil.hget(USER+"username", USER+pid);
            }
            maintainPlan.setPrincipalName(principalName);
        });
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findMaintainHistoryById(id);
        maintainPlanVo.setMaintainPlanVos(maintainPlanVos);
        maintainPlanVo.setMaintainDays(maintainPlanDao.findMaintainDays(id));
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
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findMaintainHistoryCollect(maintainPlanDto);
        maintainPlanVos.forEach(maintainPlanVo -> {
            Integer maintainDays = maintainPlanDao.findMaintainDays(maintainPlanVo.getId());
            maintainPlanVo.setMaintainDays(maintainDays);
        });
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
        String createUser = TokenDecode.getUserInfo().get("username");
        redisTemplate.opsForValue().set(createUser+"phases",alarmValueSettingDto.getPhases());
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
        alarmValueSettingVo = getAlarmValueSetting(alarmValueSettingVo,pidRoute);
        return alarmValueSettingVo;
    }

    public AlarmValueSettingVo getAlarmValueSetting(AlarmValueSettingVo alarmValueSettingVo,String pidRoute){
       List<String> pidRoutes = Arrays.asList(pidRoute.replaceAll("\\[","").replaceAll("\\]","").split(","));
        pidRoutes.sort((v1, v2) -> v2.compareTo(v1));
        AlarmValueSettingVo alarmValueSettingVo1=null;
        //从下往上找
        for (String routes : pidRoutes) {
            String[] route = routes.split(":");
            alarmValueSettingVo1 = wareAreaAlarmSettingDao.findBySite(route[0]+route[1]);
            if(ToolUtil.isNotEmpty(alarmValueSettingVo1)) {
                break;
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
        return alarmValueSettingVo;
    }
}
