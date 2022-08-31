package com.rpzh.service.impl;
import com.alibaba.fastjson.*;
import com.github.pagehelper.*;
import com.github.pagehelper.util.StringUtil;
import com.rpzh.dao.*;
import com.rpzh.dto.*;
import com.rpzh.entity.*;
import com.rpzh.enums.WarehouseNodeType;
import com.rpzh.service.AlarmSettingTemplateService;
import com.rpzh.service.MaintainPlanService;
import com.rpzh.util.RedisUtil;
import com.rpzh.util.SiteRouteUtil;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
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

    //redis中用户key前缀
    private static final String USER = "USER_ID_";
    private static final String USER_ID = "MAINTIAN_USER_ID_USERNAME";
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
        //获取负责人ids
        List<Integer> principalIds = maintainPlanDto.getPrincipalIds().stream().distinct().collect(Collectors.toList());
        //从redis中获取账号,并拼接负责人账号
        maintainPlanDto.setPrincipalName(getUsername(principalIds));
        //将物料代码转成字符串,并设置到maintainPlanDto中
        List<String> materialCodes = maintainPlanDto.getMaterialCodes();
        maintainPlanDto.setMaterialCode(String.join(",",maintainPlanDto.getMaterialCodes()));
        //获取创建人
        String createUser = TokenDecode.getUserInfo().get("username");
        //设置创建人
        maintainPlanDto.setCreateUser(createUser);
        try {
            //设置养护到期时间,并格式化
            maintainPlanDto.setAftercareDateEnd(new Date(new SimpleDateFormat("yyyy-MM-dd").parse(maintainPlanDto.getAftercareDateStart()).getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l));
        }catch (Exception e){
            e.printStackTrace();
        }
        //创建养护计划
        maintainPlanDao.insertMaintainPlan(maintainPlanDto);
        //将养护计划Dto中的预警数值转为预警规则Dto中的预警数值
        AlarmValueSettingDto alarmValueSettingDto = new AlarmValueSettingDto(maintainPlanDto);
        //将预警数值插入到预警表中
        alarmValueSettingDao.insertAlarmValueSetting(alarmValueSettingDto);
        //设置养护计划id
        maintainPlanDto.setId(maintainPlanDto.getId()+1);
        //存储物料代码、养护数量、养护计划id、垛位id、仓间id、层id、楼栋id、仓库id的集合
        List<MaintainPlanDto> maintainPlanDtos = new ArrayList<>();
        //根据垛位id和物料代码查询各个物料代码的数量
        List<MaterialVo> materialVos = materialDao.findCount(stackId, materialCodes);
        //宁波测试用
        if(ToolUtil.isEmpty(materialVos)){
            for (String materialCode : materialCodes) {
                MaintainPlanDto maintainPlanDto1 = new MaintainPlanDto(materialCode,12d,maintainPlanDto.getId(),stackId,maintainPlanDto.getPidRoute(),maintainPlanDto.getMaintenceStyle(),maintainPlanDto.getPhases());
                maintainPlanDtos.add(maintainPlanDto1);
            }
        }
        if(ToolUtil.isNotEmpty(materialVos)) {
            //将新建养护计划中的所有物料代码以及各自的数量插入到养护数量表中，方便养护数量模板的统计
            for (MaterialVo materialVo : materialVos) {
                MaintainPlanDto maintainPlanDto1 = new MaintainPlanDto(materialVo.getMaterialCode(),materialVo.getMaintainCount(),maintainPlanDto.getId(),stackId,maintainPlanDto.getPidRoute(),maintainPlanDto.getMaintenceStyle(),maintainPlanDto.getPhases());
                maintainPlanDtos.add(maintainPlanDto1);
            }
            //养护数量信息批次插入养护数量表中
            maintainPlanDao.insertMaintainCount(maintainPlanDtos);
        }
        //设置预警规则id
        maintainPlanDto.setAlarmValueSettingId(alarmValueSettingDto.getAlarmValueSettingId()+1);
        //插入到养护阶段表中
        maintainPlanDao.insertMaintainStage(maintainPlanDto);
        //更新养护计划表中的养护阶段id
        return maintainPlanDao.updateMaintainPlanStageId(maintainPlanDto.getId(),maintainPlanDto.getStageId()+1);
    }

    /*
     * 查询所有的预警规则名称
     * */
    @Override
    public List<String> findAlarmSettingTemplateList(){
        return alarmSettingTemplateDao.findAlarmSettingTemplateList();
    }

    /*
     * 判断预警名称是否已存在
     * */
    @Override
    public Result findCountByName(String earlyWarningRuleName) {
        Integer id = alarmSettingTemplateDao.findCountByName(earlyWarningRuleName);
        if(ToolUtil.isNotEmpty(id)){
            return new Result(false, StatusCode.ERROR, "该预警名称已存在!");
        }
        return new Result(true, StatusCode.OK, "该预警名称不存在");
    }

    /*
     * 根据预警规则名称预警信息
     * */
    @Override
    public AlarmValueSettingVo findAlarmSettingTemplate(String earlyWarningRuleName,String phases) {
        //根据预警规则名称获取预警模板数值
        AlarmValueSettingVo alarmSettingTemplate = alarmSettingTemplateDao.findAlarmSettingTemplate(earlyWarningRuleName,phases);
        if(ToolUtil.isEmpty(alarmSettingTemplate)){
            //查询出来为空,则new一个返回给前端
            alarmSettingTemplate = new AlarmValueSettingVo();
        }
        return alarmSettingTemplate;
    }

    /*
     * 判断该垛位存在存在养护计划
     * */
    @Override
    public Integer isExistStackId(Integer stackId) {
        return maintainPlanDao.isExistStackId(stackId);
    }

    /**
     * 查询所有养护计划
     */
    @Override
    public PageInfo<MaintainPlanVo> findMaintainPlanInfo(MaintainPlanDto maintainPlanDto) {
        //AlarmSettingTemplateServiceImpl.createDefaultRule()
        //宁波测试用
        if(maintainPlanDao.findCountByMaintainPlan()==0){
            MaintainPlanDto maintainPlanDto1 = new MaintainPlanDto();
            maintainPlanDto1.setStackId(86);
            maintainPlanDto1.setMaterialCodes(Arrays.asList("11111111,22222222"));
            maintainPlanDto1.setPidRoute("[00:0],[01:2],[02:4],[03:14],[04:32],[05:86]");
            List<Integer> ids = new ArrayList<>();
            ids.add(161);
            maintainPlanDto1.setPrincipalIds(ids);
            maintainPlanDto1.setMaintenceStyle("常规整仓养护方式");
            maintainPlanDto1.setPhases("充氮杀虫阶段");
            maintainPlanDto1.setMaintainDays(11);
            maintainPlanDto1.setAftercareDateStart("2022-08-23");
            maintainPlanDto1.setMaintainCount(146d);
            maintainPlanDto1.setOxygenReductionTime("8");
            maintainPlanDto1.setExpireTime("2022-08-26");
            maintainPlanDto1.setEarlyWarningRuleName("默认模板-常规整仓养护方式");
            maintainPlanDto1.setStackOxygenUpperOne(14d);
            maintainPlanDto1.setStackOxygenUpperTwo(15d);
            maintainPlanDto1.setStackOxygenUpperThree(16d);
            maintainPlanDto1.setStackOxygenLower(13d);
            maintainPlanDto1.setTempCoreChipUpperOne(33d);
            maintainPlanDto1.setTempCoreChipUpperTwo(34d);
            maintainPlanDto1.setTempCoreChipUpperThree(35d);
            maintainPlanDto1.setTempCoreChipLower(32d);
            maintainPlanDto1.setStackTempUpperOne(43d);
            maintainPlanDto1.setStackTempUpperTwo(44d);
            maintainPlanDto1.setStackTempUpperThree(45d);
            maintainPlanDto1.setStackTempLower(42d);
            maintainPlanDto1.setStackMoistureUpperOne(23d);
            maintainPlanDto1.setStackMoistureUpperTwo(24d);
            maintainPlanDto1.setStackMoistureUpperThree(25d);
            maintainPlanDto1.setStackMoistureLower(22d);
            maintainPlanDto1.setCoreTempHighEnvTempOne(3d);
            maintainPlanDto1.setCoreTempHighEnvTempTwo(4d);
            maintainPlanDto1.setCoreTempHighEnvTempThree(5d);
            maintainPlanDto1.setCoreTempLowEnvTemp(2d);
            insertMaintainPlan1(maintainPlanDto1);
            maintainPlanDto1.setMaintenceStyle("常规整仓养护方式");
            maintainPlanDto1.setPhases("充氮杀虫阶段");
            maintainPlanDto1.setStackId(26);
            maintainPlanDto1.setMaterialCodes(Arrays.asList("33333333,44444444"));
            maintainPlanDto1.setPidRoute("[00:0],[01:2],[02:4],[03:12],[04:26],[05:26]");
            insertMaintainPlan1(maintainPlanDto1);
        }
        //获取位置路由
        String pidRoute = maintainPlanDto.getPidRoute();
        List<Integer> stackIds = new ArrayList<>();
        if(StringUtil.isNotEmpty(pidRoute)) {
            if (pidRoute.contains("04")) {
                //选择的位置是垛位
                stackIds.add(maintainPlanDto.getStackId());
            }else{
                //选择的位置不是垛位       查询满足位置条件的所有垛位
                stackIds = maintainPlanDao.findStackIdBySiteRoute(SiteRouteUtil.route(pidRoute, maintainPlanDto.getStackId() + ""));
            }
        }
        //分页查询
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        //分页查询养护计划
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findMaintainPlanInfo(maintainPlanDto,stackIds,stackIds.size());
        return new PageInfo(maintainPlanVos);
    }

    public int insertMaintainPlan1(MaintainPlanDto maintainPlanDto){
        //获取垛位id
        Integer stackId = maintainPlanDto.getStackId();
        //获取负责人ids
        List<Integer> principalIds = maintainPlanDto.getPrincipalIds().stream().distinct().collect(Collectors.toList());
        //从redis中获取账号,并拼接负责人账号
        maintainPlanDto.setPrincipalName(getUsername(principalIds));
        //将物料代码转成字符串,并设置到maintainPlanDto中
        List<String> materialCodes = maintainPlanDto.getMaterialCodes();
        maintainPlanDto.setMaterialCode(String.join(",",maintainPlanDto.getMaterialCodes()));
        //获取创建人
        String createUser = TokenDecode.getUserInfo().get("username");
        //设置创建人
        maintainPlanDto.setCreateUser(createUser);
        try {
            //设置养护到期时间,并格式化
            maintainPlanDto.setAftercareDateEnd(new Date(new SimpleDateFormat("yyyy-MM-dd").parse(maintainPlanDto.getAftercareDateStart()).getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l));
        }catch (Exception e){
            e.printStackTrace();
        }
        //创建养护计划
        maintainPlanDao.insertMaintainPlan(maintainPlanDto);
        //将养护计划Dto中的预警数值转为预警规则Dto中的预警数值
        AlarmValueSettingDto alarmValueSettingDto = new AlarmValueSettingDto(maintainPlanDto);
        //将预警数值插入到预警表中
        alarmValueSettingDao.insertAlarmValueSetting(alarmValueSettingDto);
        //设置养护计划id
        maintainPlanDto.setId(maintainPlanDto.getId()+1);
        //存储物料代码、养护数量、养护计划id、垛位id、仓间id、层id、楼栋id、仓库id的集合
        List<MaintainPlanDto> maintainPlanDtos = new ArrayList<>();
        //根据垛位id和物料代码查询各个物料代码的数量
        List<MaterialVo> materialVos = materialDao.findCount(stackId, materialCodes);
        //宁波测试用
        if(ToolUtil.isEmpty(materialVos)){
            for (String materialCode : materialCodes) {
                MaintainPlanDto maintainPlanDto1 = new MaintainPlanDto(materialCode,12d,maintainPlanDto.getId(),stackId,maintainPlanDto.getPidRoute(),maintainPlanDto.getMaintenceStyle(),maintainPlanDto.getPhases());
                maintainPlanDtos.add(maintainPlanDto1);
            }
        }
        if(ToolUtil.isNotEmpty(materialVos)) {
            //将新建养护计划中的所有物料代码以及各自的数量插入到养护数量表中，方便养护数量模板的统计
            for (MaterialVo materialVo : materialVos) {
                MaintainPlanDto maintainPlanDto1 = new MaintainPlanDto(materialVo.getMaterialCode(),materialVo.getMaintainCount(),maintainPlanDto.getId(),stackId,maintainPlanDto.getPidRoute(),maintainPlanDto.getMaintenceStyle(),maintainPlanDto.getPhases());
                maintainPlanDtos.add(maintainPlanDto1);
            }
            //养护数量信息批次插入养护数量表中
            maintainPlanDao.insertMaintainCount(maintainPlanDtos);
        }
        //设置预警规则id
        maintainPlanDto.setAlarmValueSettingId(alarmValueSettingDto.getAlarmValueSettingId()+1);
        //插入到养护阶段表中
        maintainPlanDao.insertMaintainStage(maintainPlanDto);
        //更新养护计划表中的养护阶段id
        return maintainPlanDao.updateMaintainPlanStageId(maintainPlanDto.getId(),maintainPlanDto.getStageId()+1);
    }
    /**
     * 删除/批量删除养护计划
     */
    @Override
    public Result deleteMaintainPlanInfo(List<Integer> ids) {
        //根据ids查询位置路由和垛位id,垛位id用来拼接全位置路由,位置路由用来查询是否存在未处理的任务,
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findPidRoute(ids);
        List<String> pidRoutes = new ArrayList<>();
        maintainPlanVos.forEach(maintainPlanVo -> {
            //拼接全位置路由
            pidRoutes.add(SiteRouteUtil.route(maintainPlanVo.getPidRoute(),maintainPlanVo.getStackId()+""));
        });
        //查询所选的养护计划是否存在未处理的任务
        Integer count = maintainPlanDao.findCountBySiteRoute(pidRoutes);
        //发现有一个养护计划存在未处理的任务,则禁止删除
        if(count>0){
            return new Result(false, StatusCode.ERROR, "所选的养护计划存在未处理的任务,请先处理在删除");
        }
        //查询满足条件的预警ids和阶段ids
        List<Map> idInfos = maintainPlanDao.findAlarmValueSettingId(ids);
        //筛选所有预警ids
        List<Integer> alarmValueSettingIds = idInfos.stream().map((Map map) -> Integer.parseInt(map.get("alarmValueSettingId").toString())).collect(Collectors.toList());
        //筛选所有阶段ids
        List<Integer> stageIds = idInfos.stream().map((Map map) -> Integer.parseInt(map.get("id").toString())).collect(Collectors.toList());
        for (Integer stageId : stageIds) {
            //删除redis中的养护信息
            if(ToolUtil.isNotEmpty(redisTemplate.opsForHash().entries(MAINTAIN + stageId))){
                redisTemplate.delete(MAINTAIN + stageId);
            }
        }
        //删除养护数量表数据
        maintainPlanDao.deleteMaintainCount(ids);
        //删除养护计划表信息
        maintainPlanDao.deleteMaintainPlanById(ids);
        if(ToolUtil.isNotEmpty(alarmValueSettingIds)) {
            //删除预警表信息
            alarmValueSettingDao.deleteIds(alarmValueSettingIds);
        }
        //删除养护阶段表信息
        maintainPlanDao.deleteMaintainPlanStageById(ids);
        return new Result(true, StatusCode.OK, "删除成功!");
    }

    /*
     * 根据养护id查询养护详情
     * */
    @Override
    public MaintainPlanVo findMaintainPlan(Integer id) {
        //根据养护计划id查询养护信息
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findMaintainPlan(id);
        //将物料代码转为list类型传给前端
        maintainPlanVo.setMaterialCodes(Arrays.asList(maintainPlanVo.getMaterialCode().split(",")));
        //设置负责人(用户名加账号)
        maintainPlanVo.setPrincipalName(getUserName(maintainPlanVo.getPrincipalName()));
        //根据养护计划id查询各阶段信息
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findDetails(id);
        maintainPlanVos.forEach(maintainPlan->{
            //设置各阶段的负责人(用户名加账号)
            maintainPlan.setPrincipalName(getUserName(maintainPlan.getPrincipalName()));
        });
        maintainPlanVo.setMaintainPlanVos(maintainPlanVos);//将各阶段信息加入到养护信息中
        //根据预警规则id查询预警信息
        AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingDao.findAlarmValueSettingById(maintainPlanVo.getAlarmValueSettingId());;
        //调用方法向上查找仓间预警数值
        maintainPlanVo.setAlarmValueSettingVo(getAlarmValueSetting(alarmValueSettingVo,maintainPlanVo.getPidRoute()));
        List<String> airNodes = airConditioningDao.findAirConditioningData(maintainPlanVo.getAreaId());//获取该仓间的所有空调编号
        if(ToolUtil.isNotEmpty(airNodes)) {
            Map<String, Double> map = nodeDao.findNowNodeData(airNodes);//从空调上获取仓间当前的温湿度
            maintainPlanVo.setWarehouseTempNow(Double.parseDouble(String.format("%.2f", map.get("temp"))));//当前的仓间温度
            maintainPlanVo.setWarehouseMoistureNow(Double.parseDouble(String.format("%.2f", map.get("humiture"))));//当前的仓间湿度
        }
        if(ToolUtil.isNotEmpty(id)) {
            Map<String, Double> stackEnv = nodeDao.findStackEnv(id);//根据养护计划id查询垛位当前的温湿度
            maintainPlanVo.setStackTempNow(Double.parseDouble(String.format("%.2f", stackEnv.get("temp"))));//当前的垛位温度
            maintainPlanVo.setStackMoistureNow(Double.parseDouble(String.format("%.2f", stackEnv.get("humiture"))));//当前的垛位温度
            Double stackCore = nodeDao.findStackCore(id);//根据养护计划id查询垛位当前的包芯温度
            maintainPlanVo.setTempCoreChipNow(Double.parseDouble(String.format("%.2f", stackCore)));//当前的平均包芯温度
            maintainPlanVo.setCoreTempHighEnvTempNow(Double.parseDouble(String.format("%.2f", stackCore - stackEnv.get("temp"))));//当前的平均包芯温度高于环境温度
            maintainPlanVo.setStackOxygenNow(Double.parseDouble(String.format("%.2f", nodeDao.findO2StackData(id))));//根据养护计划id查询垛位当前的氧气
            maintainPlanVo.setWarehouseOxygenNow(Double.parseDouble(String.format("%.2f", nodeDao.findO2AreaData(maintainPlanVo.getPidRoute()))));//根据位置路由查询仓间当前的氧气
        }
        return maintainPlanVo;
    }

    /*
     * 设置负责人(用户名加账号)
     * */
    public String getUserName(String name){
        List<String> usernames = Arrays.asList(name.split(","));
        String userNames = "";
        for (int i = 0; i < usernames.size(); i++) {
            if(i==0){
                //从redis中取出用户名
                userNames = (String)redisUtil.hget("USER_"+usernames.get(i),"nickname") + "(" + usernames.get(i) + ")";
            }else{
                userNames = userNames + ","+ (String)redisUtil.hget("USER_"+usernames.get(i),"nickname") + "(" + usernames.get(i) + ")";
            }
        }
        return userNames;
    }

    /*
     * 更改前回显
     * */
    @Override
    public MaintainPlanVo beforeUpdateMaintainPlan(Integer id) {
        //根据养护计划id查询养护信息
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findMaintainPlan(id);
        maintainPlanVo.setPidRoute(SiteRouteUtil.route(maintainPlanVo.getPidRoute(),maintainPlanVo.getStackId()+""));
        //设置负责人id
        maintainPlanVo.setPrincipalIds(getPrincipalIds(maintainPlanVo.getPrincipalName()));
        Integer stackId = maintainPlanVo.getStackId();
        //获取最新的养护数量
        maintainPlanVo.setMaintainCount(materialDao.getMaintainCount(stackId));
        //获取最新的物料代码
        maintainPlanVo.setMaterialCodes(materialDao.findMaterialCode(stackId));
        //根据垛位id查询位置路由
        String pidRoute = warehouseDao.findPidRoute(stackId).getPidRoute();
        //设置全位置路由
        maintainPlanVo.setPidRoute(SiteRouteUtil.route(pidRoute,stackId+""));
        //获取预警规则id
        Integer alarmValueSettingId = maintainPlanVo.getAlarmValueSettingId();
        AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingDao.findAlarmValueSettingById(alarmValueSettingId);
        alarmValueSettingVo = getAlarmValueSetting(alarmValueSettingVo,pidRoute);
        maintainPlanVo.setAlarmValueSettingVo(alarmValueSettingVo);
        return maintainPlanVo;
    }

    /*
     * 获取负责人id集合
     * */
    public List<Integer> getPrincipalIds(String name){
        List<String> names = Arrays.asList(name.split(","));
        String userName = "";
        List<Integer> principalIds =new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            principalIds.add((Integer) redisUtil.hget("USER_"+names.get(i),"userId"));//从redis中取出负责人id
        }
        return principalIds;
    }

    /*
     * 设置负责人(用户名)
     * */
    public String getUsername(List<Integer> principalIds){
        String username = "";
        for (int i = 0; i < principalIds.size(); i++) {
            if(i==0){
                username = (String) redisUtil.hget("MAINTIAN_USER_ID_USERNAME", "USER_ID_" + principalIds.get(i));
            }else{
                username = username + "," +  (String) redisUtil.hget("MAINTIAN_USER_ID_USERNAME", "USER_ID_" + principalIds.get(i));
            }
        }
        return username;
    }
    /*
     * 转段前回显
     * */
    @Override
    public MaintainPlanVo beforeChangeMaintainPlanStage(Integer id) {
        MaintainPlanVo maintainPlanVo = maintainPlanDao.beforeChangeMaintainPlan(id);//根据养护计划id查询养护信息
        Integer stackId = maintainPlanVo.getStackId();
        maintainPlanVo.setPidRoute(SiteRouteUtil.route(maintainPlanVo.getPidRoute(), stackId +""));//拼接全位置路由
        maintainPlanVo.setMaterialCodes(materialDao.findMaterialCode(stackId));//查询转段时的阶段所对应的物料代码(list)
        maintainPlanVo.setMaterialCode(StringUtils.join(maintainPlanVo.getMaterialCodes(),","));//查询转段时的阶段所对应的物料代码(string)
        maintainPlanVo.setMaintainCount(materialDao.getMaintainCount(stackId));//查询转段时的阶段所对应的养护数量
        maintainPlanVo.setStackName(maintainPlanVo.getStackName());//垛位名称
        maintainPlanVo.setPrincipalIds(getPrincipalIds(maintainPlanVo.getPrincipalName()));//负责人id
        maintainPlanVo.setPersonId(maintainPlanVo.getPrincipalIds().get(0));//垛位负责人id
        maintainPlanVo.setMaintenceStyle(maintainPlanVo.getMaintenceStyle());//养护方式
        return maintainPlanVo;
    }

    /*
     * 转段时的养护阶段列表
     * */
    @Override
    public List<String> beforeChangePhases(Integer stackId,String maintenceStyle) {
        List<String> phases = new ArrayList<>(Arrays.asList("养护阶段","充氮杀虫阶段","磷化铝杀虫阶段"));
        if(maintenceStyle.equals("机械调控方式")) {
            phases.add("充氮保质阶段");
        }
        List<String> maintainPlanPhases = maintainPlanDao.findPhases(stackId, maintenceStyle);//该养护计划已存在的养护阶段
        if(ToolUtil.isNotEmpty(maintainPlanPhases)) {
            phases.removeAll(maintainPlanPhases);
        }
        return phases;
    }

    /*
     * 更改养护计划
     * */
    @Override
    public Integer updateMaintainPlan(MaintainPlanDto maintainPlanDto) {
        AlarmValueSettingVo alarmValueSettingVo = new AlarmValueSettingVo();
        BeanUtils.copyProperties(maintainPlanDto,alarmValueSettingVo);//将养护计划Dto中的预警数值复制到预警规则Vo中的预警数值
        Field[] fields = AlarmValueSettingVo.class.getDeclaredFields();//获取 AlarmValueSettingVo中所有的字段
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                //修改redis缓存中的每个字段的值       对象为MAINTAIN_加养护阶段id主键 ，key为字段名 value为字段值
                redisTemplate.opsForHash().put(MAINTAIN + maintainPlanDto.getStageId(), field.getName(), field.get(alarmValueSettingVo));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //根据用户id从redis中获取账号,并拼接负责人账号
        maintainPlanDto.setPrincipalName(getUsername(maintainPlanDto.getPrincipalIds().stream().distinct().collect(Collectors.toList())));
        //获取list类型的物料代码转为string类型
        maintainPlanDto.setMaterialCode(String.join(",", maintainPlanDto.getMaterialCodes()));
        //获取修改人的账号
        maintainPlanDto.setUpdateUser(TokenDecode.getUserInfo().get("username"));
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //设置到期转段的日期
            maintainPlanDto.setAftercareDateEnd(new Date(sdf.parse(maintainPlanDto.getAftercareDateStart()+" 00:00:00").getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l));
        }catch (Exception e){
            e.printStackTrace();
        }
        //修改养护计划信息
        maintainPlanDao.updateMaintainPlan(maintainPlanDto);
        //修改养护阶段信息
        maintainPlanDao.updateMaintainPlanStage(maintainPlanDto);
        List<Integer> ids = new ArrayList<>(Arrays.asList(maintainPlanDto.getId()));
        List<MaintainPlanDto> maintainPlanDtos = new ArrayList<>();
        //获取垛位id
        Integer stackId = maintainPlanDto.getStackId();
        //根据垛位id和物料代码查询各个物料代码的数量
        List<MaterialVo> materialVos = materialDao.findCount(stackId, maintainPlanDto.getMaterialCodes());
        //宁波测试用
        /*maintainPlanDao.deleteMaintainCount(ids);//删除养护数量表数据
        if(ToolUtil.isNotEmpty(materialVos)) {
            for (MaterialVo materialVo : materialVos) {
                //将新建养护计划中的所有物料代码以及各自的数量插入到养护数量表中，方便养护数量模板的统计
                MaintainPlanDto maintainPlanDto1 = new MaintainPlanDto(materialVo.getMaterialCode(), materialVo.getMaintainCount(), maintainPlanDto.getId(), stackId, maintainPlanDto.getPidRoute(), maintainPlanDto.getMaintenceStyle(), maintainPlanDto.getPhases());
                maintainPlanDtos.add(maintainPlanDto1);
            }
        }
        //养护数量信息批次插入养护数量表中
        maintainPlanDao.insertMaintainCount(maintainPlanDtos);*/
        //将养护计划Dto中的预警数值转为预警规则Dto中的预警数值
        AlarmValueSettingDto alarmValueSettingDto = new AlarmValueSettingDto(maintainPlanDto);
        //更新预警数值
        Integer result  =alarmValueSettingDao.updateAlarmValueSetting(alarmValueSettingDto);
        return result;
    }

    /*
     * 开垛使用
     * */
    @Override
    public Result endMaintainPlan(MaintainPlanDto maintainPlanDto){
        List<Integer> ids = new ArrayList<>(Arrays.asList(maintainPlanDto.getId()));
        //根据ids查询位置路由和垛位id,垛位id用来拼接全位置路由,位置路由用来查询是否存在未处理的任务,
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findPidRoute(ids).get(0);
        List<String> pidRoutes = new ArrayList<>();
        pidRoutes.add(SiteRouteUtil.route(maintainPlanVo.getPidRoute(),maintainPlanVo.getStackId()+""));//拼接全位置路由
        Integer count = maintainPlanDao.findCountBySiteRoute(pidRoutes);//查询所选的养护计划是否存在未处理的任务
        //发现有一个养护计划存在未处理的任务,则禁止删除
        if(count>0){
            return new Result(false, StatusCode.ERROR, "所选的养护计划存在未处理的任务,请先处理再开垛");
        }
        AlarmValueSettingVo alarmValueSettingVo = new AlarmValueSettingVo();
        //将养护计划Dto中的预警数值复制到预警规则Vo中的预警数值
        BeanUtils.copyProperties(maintainPlanDto,alarmValueSettingVo);
        Field[] fields = AlarmValueSettingVo.class.getDeclaredFields();//获取 AlarmValueSettingVo中所有的字段
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                //修改redis缓存中的每个字段的值       对象为MAINTAIN_加养护阶段id主键 ，key为字段名 value为字段值
                redisTemplate.opsForHash().put(MAINTAIN + maintainPlanDto.getStageId(), field.getName(), field.get(alarmValueSettingVo));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String updateUser = TokenDecode.getUserInfo().get("username");
        maintainPlanDao.deleteMaintainCount(ids);//删除养护数量表数据
        materialDao.updateMaterialStatus(maintainPlanVo.getStackId());//把物料表中相关垛位的物料代码的状态改为开垛状态
        try {
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            long createdTime = maintainPlanDao.findCreatedTime(maintainPlanDto.getId()).getTime();//最后一阶段创建时的时间
            long nowTime = sdf.parse(sdf.format(new Date())).getTime();//当前时间
            long maintainDays = (nowTime - createdTime)/(1000 * 24 * 60 * 60);//最后一阶段养护的天数
            maintainPlanDao.endMaintainPlanStage(updateUser,maintainPlanDto.getId(),maintainDays);//修改最后一阶段的更新者、养护天数
        } catch (Exception e) {
            e.printStackTrace();
        }
        maintainPlanDao.endMaintainPlan(maintainPlanDto.getId());//将养护计划开垛
        return new Result(true, StatusCode.OK, "开垛使用成功!");
    }

    /*
     * 转段
     * */
    @Override
    public Result changeMaintainPlanStage(MaintainPlanDto maintainPlanDto) {
        List<String> pidRoutes = new ArrayList<>();
        Integer stackId = maintainPlanDto.getStackId();
        pidRoutes.add(SiteRouteUtil.route(maintainPlanDto.getPidRoute(),stackId+""));
        //根据ids查询位置路由和垛位id,垛位id用来拼接全位置路由,位置路由用来查询是否存在未处理的任务,
        Integer count = maintainPlanDao.findCountBySiteRoute(pidRoutes);
        //发现有一个养护计划存在未处理的任务,则禁止删除
        if(count>0){
            return new Result(false, StatusCode.ERROR, "所选的养护计划存在未处理的任务,请先处理在转段");
        }
        nodeDao.findNodeIds(stackId).forEach(nodeId -> {
            if(ToolUtil.isNotEmpty(redisTemplate.opsForValue().get("MAINTAIN_CACHE_NODE_"+nodeId))){
                //删除redis缓存中的设备数据
                redisTemplate.delete("MAINTAIN_CACHE_NODE_"+nodeId);
            }
        });
        AlarmValueSettingVo alarmValueSettingVo = new AlarmValueSettingVo();
        BeanUtils.copyProperties(maintainPlanDto,alarmValueSettingVo);//将养护计划Dto中的预警数值复制到预警规则Vo中的预警数值
        Field[] fields = AlarmValueSettingVo.class.getDeclaredFields();//获取 AlarmValueSettingVo中所有的字段
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                //修改redis缓存中的每个字段的值       对象为MAINTAIN_加养护阶段id主键 ，key为字段名 value为字段值
                redisTemplate.opsForHash().put(MAINTAIN + maintainPlanDto.getStageId(), field.getName(), field.get(alarmValueSettingVo));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //从redis中获取账号,并拼接负责人账号
        maintainPlanDto.setPrincipalName(getUsername(maintainPlanDto.getPrincipalIds().stream().distinct().collect(Collectors.toList())));
        //将物料代码转成字符串,并设置到maintainPlanDto中
        List<String> materialCodes = maintainPlanDto.getMaterialCodes();
        maintainPlanDto.setMaterialCode(String.join(",", materialCodes));
        String createUser = TokenDecode.getUserInfo().get("username");//获取用户账号
        maintainPlanDto.setCreateUser(createUser);
        maintainPlanDto.setUpdateUser(createUser);
        maintainPlanDto.setVersion(0);//标记为不是新建养护计划,而是转段（月底养护报表会用到）
        AlarmValueSettingDto alarmValueSettingDto = new AlarmValueSettingDto(maintainPlanDto);
        alarmValueSettingDao.insertAlarmValueSetting(alarmValueSettingDto);//将预警数值插入到预警表中
        maintainPlanDto.setAlarmValueSettingId(alarmValueSettingDto.getAlarmValueSettingId()+1);//设置预警规则id
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //设置养护到期时间,并格式化
            maintainPlanDto.setAftercareDateEnd(new Date(sdf.parse(maintainPlanDto.getAftercareDateStart()+" 00:00:00").getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l));
        }catch (Exception e){
            e.printStackTrace();
        }
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findStageIdById(maintainPlanDto.getId());//根据养护计划id查询上一阶段的养护阶段id和上一阶段的创建时间
        maintainPlanDto.setStageId(maintainPlanVo.getStageId());//上一阶段的养护阶段id
        long createdTime = maintainPlanVo.getCreatedTime().getTime();//上一阶段的创建时间
        maintainPlanDao.insertMaintainStage(maintainPlanDto);//将当前阶段的信息插入阶段表中
        maintainPlanDto.setMaintainDays(new Long((new Date().getTime() - createdTime) / (1000 * 60 * 60 * 24)).intValue() + 1);//计算上一阶段的养护天数
        maintainPlanDto.setAftercareDateEnd(new Date());//设置上一阶段的转段时间
        String pidRoute = SiteRouteUtil.route(maintainPlanDto.getPidRoute(),stackId+"");//拼接全位置路由
        List<Integer> ids = new ArrayList<>(Arrays.asList(maintainPlanDto.getId()));
        List<MaintainPlanDto> maintainPlanDtos = new ArrayList<>();
        List<MaterialVo> materialVos = materialDao.findCount(stackId, materialCodes);//根据垛位id和物料代码查询各个物料代码的数量
        //宁波测试用
        /*maintainPlanDao.deleteMaintainCount(ids);//删除养护数量表数据
        for (MaterialVo materialVo : materialVos) {
            //将新建养护计划中的所有物料代码以及各自的数量插入到养护数量表中，方便养护数量模板的统计
            MaintainPlanDto maintainPlanDto1 = new MaintainPlanDto(materialVo.getMaterialCode(), materialVo.getMaintainCount(), maintainPlanDto.getId(), stackId, pidRoute, maintainPlanDto.getMaintenceStyle(), maintainPlanDto.getPhases());
            maintainPlanDtos.add(maintainPlanDto1);
        }
        maintainPlanDao.insertMaintainCount(maintainPlanDtos);//养护数量信息批次插入养护数量表中*/
        maintainPlanDao.changeMaintainPlanStage(maintainPlanDto);//修改上一阶段的信息
        maintainPlanDto.setStageId(maintainPlanDto.getStageId()+1);//设置当前的阶段id
        Integer result = maintainPlanDao.changeMaintainPlan(maintainPlanDto);//转段
        return new Result(true, StatusCode.OK, "转段成功!");
    }

    /*
     * 养护到期汇总中的详情
     * */
    @Override
    public MaintainPlanVo findMaintainPlanDetails(Integer id,Integer stackId) {
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findDetailsNew(id);//根据养护计划id查询当前阶段基本信息
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findDetails(id);//根据养护计划id查询已创建的阶段信息
        maintainPlanVos.forEach(maintainPlan->{
            maintainPlan.setPrincipalName(getUserName(maintainPlan.getPrincipalName()));//设置各阶段的负责人(用户名加账号)
        });
        maintainPlanVo.setMaintainPlanVos(maintainPlanVos);
        return maintainPlanVo;
    }

    /*
     * 历史汇总单中的详情
     * */
    @Override
    public MaintainPlanVo findEndMaintainPlanDetails(Integer id) {
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findMaintainHistoryById(id);//根据养护计划id查询当前阶段基本信息
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findDetails(id);//根据养护计划id查询全部阶段信息
        maintainPlanVos.forEach(maintainPlan->{
            maintainPlan.setPrincipalName(getUserName(maintainPlan.getPrincipalName()));//设置各阶段的负责人(用户名加账号)
        });
        maintainPlanVo.setMaintainPlanVos(maintainPlanVos);
        maintainPlanVo.setMaintainDays(maintainPlanDao.findMaintainDays(id));//设置总养护天数
        return maintainPlanVo;
    }

    /*
     * 养护到期汇总列表
     * */
    @Override
    public PageInfo<MaintainPlanVo> findExpireCollectTable(MaintainPlanDto maintainPlanDto) {
        if(StringUtil.isNotEmpty(maintainPlanDto.getEndTime())){
            maintainPlanDto.setEndTime(maintainPlanDto.getEndTime().replace("00:00:00","23:59:59"));//设置结束时间
        }
        String pidRoute = maintainPlanDto.getPidRoute();//获取位置路由
        List<Integer> stackIds = new ArrayList<>();
        if(StringUtil.isNotEmpty(pidRoute)) {
            if (pidRoute.contains("04")) {
                stackIds.add(maintainPlanDto.getStackId());//选择的位置是垛位
            }else{
                stackIds = maintainPlanDao.findStackIdBySiteRoute(SiteRouteUtil.route(pidRoute, maintainPlanDto.getStackId() + ""));//选择的位置是非垛位
            }
        }
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findExpireCollectTable(maintainPlanDto,stackIds,stackIds.size());//分页查询养护到期汇总列表
        maintainPlanVos.forEach(maintainPlanVo -> {
            maintainPlanVo.setMaterialCode(maintainPlanVo.getMaterialCode().replaceAll(",","、"));//将物料代码中的,替换成、,使得导出时不出错
        });
        return new PageInfo<>(maintainPlanVos);
    }

    /*
     * 月底养护报表
     * */
    @Override
    public PageInfo<MaintainPlanVo> findMaintainMonthEndReport(MaintainPlanDto maintainPlanDto) {
        //开始时间和结束时间为空,则默认当前一个月
        if(ToolUtil.isEmpty(maintainPlanDto.getStartTime()) && ToolUtil.isEmpty(maintainPlanDto.getEndTime())){
            SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calast = Calendar.getInstance();
            calast.set(Calendar.DAY_OF_MONTH, calast.getActualMaximum(Calendar.DAY_OF_MONTH));
            maintainPlanDto.setEndTime(sm.format(calast.getTime())+ " 23:59:59");
            calast.set(Calendar.DAY_OF_MONTH, calast.getActualMinimum(Calendar.DAY_OF_MONTH));
            maintainPlanDto.setStartTime(sm.format(calast.getTime())+ " 00:00:00");
        }
        String pidRoute = maintainPlanDto.getPidRoute();//获取位置路由
        List<Integer> stackIds = new ArrayList<>();
        if(StringUtil.isNotEmpty(pidRoute)) {
            if (pidRoute.contains("04")) {
                stackIds.add(maintainPlanDto.getStackId());//选择的位置是垛位
            }else{
                stackIds = maintainPlanDao.findStackIdBySiteRoute(SiteRouteUtil.route(pidRoute, maintainPlanDto.getStackId() + ""));//选择的位置是非垛位
            }
        }
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        List<MaintainPlanVo> maintainMonthEndReport = maintainPlanDao.findMaintainMonthEndReport(maintainPlanDto,stackIds,stackIds.size());//分页查询月底养护报表
        maintainMonthEndReport.forEach(maintainPlanVo -> {
            maintainPlanVo.setMaterialCode(maintainPlanVo.getMaterialCode().replaceAll(",","、"));//将物料代码中的,替换成、,使得导出时不出错
        });
        return new PageInfo(maintainMonthEndReport);
    }

    /*
     * 历史汇总单
     * */
    @Override
    public PageInfo<MaintainPlanVo> findMaintainHistoryCollect(MaintainPlanDto maintainPlanDto) {
        String pidRoute = maintainPlanDto.getPidRoute();//获取位置路由
        List<Integer> stackIds = new ArrayList<>();
        if(StringUtil.isNotEmpty(pidRoute)) {
            if (pidRoute.contains("04")) {
                stackIds.add(maintainPlanDto.getStackId());//选择的位置是垛位
            }else{
                stackIds = maintainPlanDao.findStackIdBySiteRoute(SiteRouteUtil.route(pidRoute, maintainPlanDto.getStackId() + ""));//选择的位置是非垛位
            }
        }
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findMaintainHistoryCollect(maintainPlanDto,stackIds,stackIds.size());//分页查询历史汇总单
        maintainPlanVos.forEach(maintainPlanVo -> {
            maintainPlanVo.setMaintainDays(maintainPlanDao.findMaintainDays(maintainPlanVo.getId()));//设置总养护天数
        });
        return new PageInfo(maintainPlanVos);
    }

    /*@Override
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
    }*/

    /*
     * 养护数量
     * */
    @Override
    public Map<String,Object> findMaintainCountReport(MaintainCountDto maintainCountDto) {
        Double sum = 0d;
        //00:根节点,01:仓库, 02: 楼号, 03: 层, 04: 区, 05: 垛位',
        String pidRoute = maintainCountDto.getPidRoute();
        Integer stackId = maintainCountDto.getStackId();
        String materialCode = maintainCountDto.getMaterialCode();
        String maintenceStyle = maintainCountDto.getMaintenceStyle();
        String phases = maintainCountDto.getPhases();
        List<MaintainCountVo> maintainCountVos = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        //位置不为空
        if(ToolUtil.isNotEmpty(pidRoute) && ToolUtil.isNotEmpty(stackId)){
            String column="";
            switch (pidRoute.split(",").length){
                case 1: column = "t1.warehouse_id";break;
                case 2: column = "t1.buliding_id";break;
                case 3: column = "t1.floor_id";break;
                case 4: column = "t1.area_id";break;
                case 5: column = "t1.stack_id";break;
            }
            PageHelper.startPage(maintainCountDto.getPage(), maintainCountDto.getSize());
            maintainCountVos = maintainPlanDao.findMaintainCountBySiteName(maintainCountDto,column);
            map.put("sum",maintainPlanDao.findCount(maintainCountDto,column));
            map.put("export",JSON.toJSONString(maintainCountVos));
            map.put("list",new PageInfo(maintainCountVos));
            return map;
        }
        //位置为空且物料代码也为空
        if(ToolUtil.isEmpty(pidRoute) && ToolUtil.isEmpty(stackId) && ToolUtil.isEmpty(materialCode)){
            PageHelper.startPage(maintainCountDto.getPage(), maintainCountDto.getSize());
            maintainCountVos = maintainPlanDao.findMaintainCountBySiteName(maintainCountDto,"");
            map.put("sum",maintainPlanDao.findCount(maintainCountDto,""));
            map.put("export",JSON.toJSONString(maintainCountVos));
            map.put("list",new PageInfo(maintainCountVos));
            return map;
        }
        //位置为空而物料代码不为空而位置
        if(ToolUtil.isEmpty(pidRoute) && ToolUtil.isEmpty(stackId) && ToolUtil.isNotEmpty(materialCode)){
            sum = maintainPlanDao.findCount(maintainCountDto,"");
            if(ToolUtil.isEmpty(sum)){
                maintainCountVos = new ArrayList<>();
                map.put("sum",0);
                map.put("list",new PageInfo(maintainCountVos));
                return map;
            }
            List<Integer> ids = maintainPlanDao.findIds(maintainCountDto);
            PageHelper.startPage(maintainCountDto.getPage(), maintainCountDto.getSize());
            maintainCountVos = maintainPlanDao.findFirst(ids);
            List<MaintainCountVo> maintainCountOnes = new PageInfo<MaintainCountVo>(maintainCountVos).getList();
            Set<Integer> buildingIds = new HashSet<>();
            maintainCountOnes.forEach(maintainCountOne -> { buildingIds.add(maintainCountOne.getBuildingId()); });
            List<MaintainCountVo> maintainCountTwos = maintainPlanDao.findTwo(ids,buildingIds);
            Set<Integer> floorIds = new HashSet<>();
            maintainCountTwos.forEach(maintainCountTwo -> { floorIds.add(maintainCountTwo.getFloorId()); });
            List<MaintainCountVo> maintainCountThrees = maintainPlanDao.findThree(ids,floorIds);
            Set<Integer> areaIds = new HashSet<>();
            maintainCountThrees.forEach(maintainCountThree -> { areaIds.add(maintainCountThree.getAreaId()); });
            List<MaintainCountVo> maintainCountFours = maintainPlanDao.findFour(ids,areaIds);
            Set<Integer> stackIds = new HashSet<>();
            maintainCountFours.forEach(maintainCountFour -> { stackIds.add(maintainCountFour.getStackId()); });
            List<MaintainCountVo> maintainCountFives = maintainPlanDao.findFive(ids,stackIds);
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
            map.put("sum",sum);
            map.put("export",JSON.toJSONString(maintainCountVos));
            map.put("list",new PageInfo(maintainCountOnes));
            return map;
        }
        return new HashMap<>();
    }

    /*
     * 根据物流代码、养护方式、养护阶段查询预警数值
     * */
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
            alarmValueSettingDto.setMaterialCode(null);
            alarmValueSettingDto.setEarlyWarningRuleName("默认模板-"+alarmValueSettingDto.getMaintenceStyle());
            alarmValueSettingVo = alarmValueSettingDao.findByMaterialCode(alarmValueSettingDto);
        }
        //向上查找仓间预警数值
        String pidRoute = alarmValueSettingDto.getPidRoute();
        alarmValueSettingVo = getAlarmValueSetting(alarmValueSettingVo,pidRoute);
        return alarmValueSettingVo;
    }

    /*
     * 向上查找仓间预警数值
     * */
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
