package com.rpzh.service.impl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.AlarmSettingTemplateDao;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.LogAlarmSettingDto;
import com.rpzh.dto.SettingColorAndDayDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.AlarmSettingTemplateService;
import com.rpzh.util.*;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.LogAlarmSettingVo;
import com.rpzh.vo.SettingColorAndDayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Author: zyq
 * @Date: 2022/06/07/10:20
 * @Description: 预警模板
 */
@Service
public class AlarmSettingTemplateServiceImpl implements AlarmSettingTemplateService {
    @Resource
    private AlarmSettingTemplateDao alarmSettingTemplateDao;

    private final String INSERTTYPE = "预警规则添加";

    private final String UPDATETYPE = "预警规则修改";

    @Autowired
    RedisTemplate redisTemplate;
    private static final String CACHE_KEY_REDIS_MAINTAIN = "MAINTAIN_";
    @Autowired
    private RedisUtil redisUtil;

    /*
     * 查询预警列表
     */
    @Override
    public PageInfo<AlarmValueSettingVo> findAlarmSettingTemplateTable(AlarmValueSettingDto alarmValueSettingDto) {
        Integer size = alarmValueSettingDto.getSize();
        Integer page = alarmValueSettingDto.getPage();
        createDefaultRule();
        PageHelper.startPage(page, size);
        //分页查询预警信息
        List<AlarmValueSettingVo> alarmValueSettingVos = alarmSettingTemplateDao.findAlarmSettingTemplateTable(alarmValueSettingDto);
        for (AlarmValueSettingVo alarmValueSettingVo : alarmValueSettingVos) {
            alarmValueSettingVo.setCreateUser((String)redisUtil.hget("USER_"+alarmValueSettingVo.getCreateUser(),"nickname") + "(" + alarmValueSettingVo.getCreateUser() + ")");
        }
        return new PageInfo(alarmValueSettingVos);
    }

    public void createDefaultRule(){
        String createUser = TokenDecode.getUserInfo().get("username");
        if(alarmSettingTemplateDao.findAlarmSettingTemplateCount("默认模板-常规整仓养护方式")==0){
            AlarmValueSettingDto alarmValueSettingInfo = new AlarmValueSettingDto("默认模板-常规整仓养护方式", "常规整仓养护方式",  createUser,0);
            alarmSettingTemplateDao.insertAlarmSettingTemplate(alarmValueSettingInfo);
            Integer alarmValueSettingId = alarmValueSettingInfo.getAlarmValueSettingId()+1;
            AlarmValueSettingDto alarmValueSetting = new AlarmValueSettingDto(2d, 5d, 7d, 0d, 65d, 68d, 71d, 0d, 32d, 33d, 34d, 0d, 32d, 33d, 34d, 0d, 3d, 4d, 5d, 2d, "养护阶段", createUser,alarmValueSettingId);
            alarmSettingTemplateDao.insertAlarmSettingTemplateStage(alarmValueSetting);
            alarmValueSetting = new AlarmValueSettingDto(2d, 5d, 7d, 0d, 65d, 68d, 71d, 0d, 32d, 33d, 34d, 0d, 32d, 33d, 34d, 0d, 3d, 4d, 5d, 2d, "充氮杀虫阶段", createUser,alarmValueSettingId);
            alarmSettingTemplateDao.insertAlarmSettingTemplateStage(alarmValueSetting);
            alarmValueSetting = new AlarmValueSettingDto(2d, 5d, 7d, 0d, 65d, 68d, 71d, 0d, 32d, 33d, 34d, 0d, 32d, 33d, 34d, 0d, 3d, 4d, 5d, 2d, "磷化铝杀虫阶段", createUser,alarmValueSettingId);
            alarmSettingTemplateDao.insertAlarmSettingTemplateStage(alarmValueSetting);
        }
        if(alarmSettingTemplateDao.findAlarmSettingTemplateCount("默认模板-机械调控方式")==0){
            AlarmValueSettingDto alarmValueSettingInfo = new AlarmValueSettingDto("默认模板-机械调控方式", "机械调控方式",  createUser,0);
            alarmSettingTemplateDao.insertAlarmSettingTemplate(alarmValueSettingInfo);
            Integer alarmValueSettingId = alarmValueSettingInfo.getAlarmValueSettingId()+1;
            AlarmValueSettingDto alarmValueSetting = new AlarmValueSettingDto(2d, 5d, 7d, 0d, 65d, 68d, 71d, 0d, 32d, 33d, 34d, 0d, 32d, 33d, 34d, 0d, 3d, 4d, 5d, 2d, "养护阶段", createUser,alarmValueSettingId);
            alarmSettingTemplateDao.insertAlarmSettingTemplateStage(alarmValueSetting);
            alarmValueSetting = new AlarmValueSettingDto(2d, 5d, 7d, 0d, 65d, 68d, 71d, 0d, 32d, 33d, 34d, 0d, 32d, 33d, 34d, 0d, 3d, 4d, 5d, 2d, "充氮杀虫阶段", createUser,alarmValueSettingId);
            alarmSettingTemplateDao.insertAlarmSettingTemplateStage(alarmValueSetting);
            alarmValueSetting = new AlarmValueSettingDto(2d, 5d, 7d, 0d, 65d, 68d, 71d, 0d, 32d, 33d, 34d, 0d, 32d, 33d, 34d, 0d, 3d, 4d, 5d, 2d, "磷化铝杀虫阶段", createUser,alarmValueSettingId);
            alarmSettingTemplateDao.insertAlarmSettingTemplateStage(alarmValueSetting);
            alarmValueSetting = new AlarmValueSettingDto(2d, 5d, 7d, 0d, 65d, 68d, 71d, 0d, 32d, 33d, 34d, 0d, 32d, 33d, 34d, 0d, 3d, 4d, 5d, 2d,  "充氮保质阶段", createUser,alarmValueSettingId);
            alarmSettingTemplateDao.insertAlarmSettingTemplateStage(alarmValueSetting);
        }
    }

    /*
     * 删除预警模板设置
     */
    @Override
    public Result deleteAlarmSettingTemplate(List<Integer> ids) {
        if(ToolUtil.isEmpty(ids)){
            return new Result(false, StatusCode.ERROR, "ids不能为空!");
        }
        //根据id删除预警模板
        alarmSettingTemplateDao.delete(ids);
        //根据id删除预警阶段模板
        alarmSettingTemplateDao.deleteStage(ids);
        return new Result(true, StatusCode.OK, "删除预警设置成功!");
    }

    /*
     * 添加预警设置模板
     */
    @Override
    public Result insertAlarmSettingTemplate(AlarmValueSettingDto alarmValueSettingDto) {
        String materialCode = alarmValueSettingDto.getMaterialCode();
        //判断填写的预警值是否有少写
        if(GetBetweenDayUtils.getFlag(alarmValueSettingDto)){
            return new Result(false, StatusCode.ERROR, "预警值不能为空!");
        }
        if(GetBetweenDayUtils.get(alarmValueSettingDto)){
            return new Result(false, StatusCode.ERROR, "下限预警低于上限一级预警，上限一级预警低于上限二级预警，上限二级预警低于上限三级预警!");
        }
        Date date = new Date();
        //获取用户名
        String createUser = TokenDecode.getUserInfo().get("username");
        //获取ip地址
        String ipAddr = IpUtils.getIpAddr(Objects.requireNonNull(IpUtils.getRequest()));
        LogAlarmSettingDto logAlarmSettingDto = new LogAlarmSettingDto();
        alarmValueSettingDto.setUpdatedTime(date);
        alarmValueSettingDto.setUpdateUser(createUser);
        //判断新建时点击下一阶段后再点击上一阶段最后再点击下一阶段
        if(ToolUtil.isNotEmpty(alarmValueSettingDto.getAlarmValueSettingId())){
            //根据预警id更新预警表数据
            alarmSettingTemplateDao.updateAlarmSettingTemplate(alarmValueSettingDto);
            //根据预警id和阶段更新预警阶段表数据
            alarmSettingTemplateDao.updateAlarmSettingTemplateStage(alarmValueSettingDto);
        }else {
            //根据预警规则名称和阶段判断该预警规则名称该阶段是否存在
            Integer count = alarmSettingTemplateDao.findCountByNameAndPhases(alarmValueSettingDto.getEarlyWarningRuleName(),alarmValueSettingDto.getPhases());
            if(count>0){
                return new Result(false, StatusCode.ERROR, "已存在!");
            }
            alarmValueSettingDto.setCreatedTime(date);
            alarmValueSettingDto.setCreateUser(createUser);
            //根据预警规则名称获取预警规则id
            Integer alarmValueSettingId = alarmSettingTemplateDao.findCountByName(alarmValueSettingDto.getEarlyWarningRuleName());
            if(ToolUtil.isEmpty(alarmValueSettingId)) {
                //这是该预警规则名称存在,但该阶段不存在,将数据插入预警表中
                alarmSettingTemplateDao.insertAlarmSettingTemplate(alarmValueSettingDto);
                //新增后,再次根据预警规则名称获取预警规则id
                alarmValueSettingId = alarmValueSettingDto.getAlarmValueSettingId()+1;
            }
            alarmValueSettingDto.setAlarmValueSettingId(alarmValueSettingId);
            //将预警设置值插入预警阶段表中
            alarmSettingTemplateDao.insertAlarmSettingTemplateStage(alarmValueSettingDto);
        }
        //设置预警操作日志
        logAlarmSettingDto.setEarlyWarningRuleName(alarmValueSettingDto.getEarlyWarningRuleName());
        logAlarmSettingDto.setLogDate(date);
        logAlarmSettingDto.setLogUserName(createUser);
        logAlarmSettingDto.setLogType(INSERTTYPE);
        logAlarmSettingDto.setPhases(alarmValueSettingDto.getPhases());
        logAlarmSettingDto.setLogIp(ipAddr);
        logAlarmSettingDto.setMaterialCode(materialCode);
        alarmSettingTemplateDao.insertLogAlarmSetting(logAlarmSettingDto);
        return new Result(true, StatusCode.OK, "配置预警设置成功!");
    }

    /*
     * 修改升级规则前回显
     */
    @Override
    public SettingColorAndDayVo findColorAndAlertUpgradeDay(){
        //查询升级规则
        SettingColorAndDayVo settingColorAndDayVo = alarmSettingTemplateDao.findColorAndAlertUpgradeDay();
        if(ToolUtil.isNotEmpty(settingColorAndDayVo)){
            return settingColorAndDayVo;
        }
        //不存在则new一个
        return new SettingColorAndDayVo();
    }

    /*
     * 修改或添加升级规则
     */
    @Override
    public Result updateOrInsertColorAndAlertUpgradeDay(SettingColorAndDayDto settingColorAndDayDto) {
        //查询是否已存在升级规则
        Integer count = alarmSettingTemplateDao.findCount();
        //删除掉缓存中的预警升级阈值
        redisTemplate.delete(CACHE_KEY_REDIS_MAINTAIN+"oneToTwo");
        redisTemplate.delete(CACHE_KEY_REDIS_MAINTAIN+"twoToThree");
        //存在则更新
        if(count>0){
            //更新一级颜色
            alarmSettingTemplateDao.updateColorAndAlertUpgradeDay(settingColorAndDayDto.getColorOne(),"color","one");
            //更新二级颜色
            alarmSettingTemplateDao.updateColorAndAlertUpgradeDay(settingColorAndDayDto.getColorTwo(),"color","two");
            //更新三级颜色
            alarmSettingTemplateDao.updateColorAndAlertUpgradeDay(settingColorAndDayDto.getColorThree(),"color","three");
            //更新一级升二级的天数
            alarmSettingTemplateDao.updateColorAndAlertUpgradeDay(settingColorAndDayDto.getOneToTwoDay(),"warningThreshold","oneToTwo");
            //更新二级升三级的天数
            alarmSettingTemplateDao.updateColorAndAlertUpgradeDay(settingColorAndDayDto.getTwoToThreeDay(),"warningThreshold","twoToThree");
            return new Result<>(true, StatusCode.OK, "修改升级规则成功!");
        }
        //新增一级颜色
        alarmSettingTemplateDao.insertColorAndAlertUpgradeDay("color",settingColorAndDayDto.getColorOne(),"one");
        //新增二级颜色
        alarmSettingTemplateDao.insertColorAndAlertUpgradeDay("color",settingColorAndDayDto.getColorTwo(),"two");
        //新增三级颜色
        alarmSettingTemplateDao.insertColorAndAlertUpgradeDay("color",settingColorAndDayDto.getColorThree(),"three");
        //新增一级升二级的天数
        alarmSettingTemplateDao.insertColorAndAlertUpgradeDay("warningThreshold",settingColorAndDayDto.getOneToTwoDay(),"oneToTwo");
        //新增二级升三级的天数
        alarmSettingTemplateDao.insertColorAndAlertUpgradeDay("warningThreshold",settingColorAndDayDto.getTwoToThreeDay(),"twoToThree");
        return new Result<>(true, StatusCode.OK, "添加升级规则成功!");
    }

    /*
     * 新建时在点击下一阶段后再点上一步时回显或者修改时预警设置各个阶段回显
     */
    @Override
    public Result findAlarmSettingTemplateByMaterialCode(String materialCode, String earlyWarningRuleName,String phases) {
        //根据物料代码、预警规则、阶段查询预警信息
        AlarmValueSettingVo alarmValueSettingVo = alarmSettingTemplateDao.findAlarmSettingTemplateByMaterialCodeAndPhases(materialCode, earlyWarningRuleName,phases);
        if(ToolUtil.isNotEmpty(alarmValueSettingVo)){
            //不为空,将物料代码转成数组,返回给前端
            alarmValueSettingVo.setMaterialCodes(Arrays.asList(alarmValueSettingVo.getMaterialCode()));
        }
        return new Result<>(true, StatusCode.OK, "预警设置修改前回显成功!",alarmValueSettingVo);
    }

    /*
     * 更改预警设置模板
     */
    @Override
    public Result updateAlarmSettingTemplate(AlarmValueSettingDto alarmValueSettingDto) {
        //判断填写的预警值是否有少写
        if(GetBetweenDayUtils.getFlag(alarmValueSettingDto)){
            return new Result(false, StatusCode.ERROR, "预警值不能为空!");
        }
        if(GetBetweenDayUtils.get(alarmValueSettingDto)){
            return new Result(false, StatusCode.ERROR, "下限预警低于上限一级预警，上限一级预警低于上限二级预警，上限二级预警低于上限三级预警!");
        }
        String earlyWarningRuleName = alarmValueSettingDto.getEarlyWarningRuleName();
        //根据预警名称查询预警规则id
        Integer aid = alarmSettingTemplateDao.findCountByName(earlyWarningRuleName);
        if(ToolUtil.isNotEmpty(aid)){
            //预警规则名称已存在(查询出来的预警规则id存在,但不是该预警规则的id)
            if(!aid.equals(alarmValueSettingDto.getAlarmValueSettingId())){
                return new Result(false, StatusCode.ERROR, "预警规则名称已存在!");
            }
        }
        String materialCode = alarmValueSettingDto.getMaterialCode();
        Date date = new Date();
        String createUser = TokenDecode.getUserInfo().get("username");
        //获取ip
        String ipAddr = IpUtils.getIpAddr(Objects.requireNonNull(IpUtils.getRequest()));
        alarmValueSettingDto.setMaterialCode(materialCode);
        alarmValueSettingDto.setEarlyWarningRuleName(earlyWarningRuleName);
        alarmValueSettingDto.setUpdatedTime(date);
        alarmValueSettingDto.setUpdateUser(createUser);
        //更新预警规则表
        alarmSettingTemplateDao.updateAlarmSettingTemplate(alarmValueSettingDto);
        //更新预警规则阶段表
        alarmSettingTemplateDao.updateAlarmSettingTemplateStage(alarmValueSettingDto);
        //设置操作日志
        LogAlarmSettingDto logAlarmSettingDto = new LogAlarmSettingDto();
        logAlarmSettingDto.setEarlyWarningRuleName(earlyWarningRuleName);
        logAlarmSettingDto.setLogDate(date);
        logAlarmSettingDto.setLogUserName(createUser);
        logAlarmSettingDto.setLogType(UPDATETYPE);
        logAlarmSettingDto.setPhases(alarmValueSettingDto.getPhases());
        logAlarmSettingDto.setLogIp(ipAddr);
        logAlarmSettingDto.setMaterialCode(materialCode);
        //插入操作日志
        alarmSettingTemplateDao.insertLogAlarmSetting(logAlarmSettingDto);
        return new Result<>(true, StatusCode.OK, "更改预警设置模板成功!");
    }

    /*
     * 查询操作日志
     */
    @Override
    public Result findLogData(LogAlarmSettingDto logAlarmSettingDto) {
        if(StringUtils.isEmpty(logAlarmSettingDto.getMaterialCode()) || StringUtils.isEmpty(logAlarmSettingDto.getEarlyWarningRuleName())){
            return new Result<>(false, StatusCode.ERROR, "物料代码或预警规则名称不能为空!");
        }
        PageHelper.startPage(logAlarmSettingDto.getPage(), logAlarmSettingDto.getSize());
        //分页查询操作日志
        List<LogAlarmSettingVo> logData = alarmSettingTemplateDao.findLogData(logAlarmSettingDto);
        return new Result<>(true, StatusCode.OK, "查询操作日志成功!",new PageInfo(logData));
    }

    /*
     * 预警设置修改前回显
     */
    @Override
    public AlarmValueSettingVo findAlarmSettingTemplate(Integer id) {
        //根据id查询预警设置值
        AlarmValueSettingVo alarmValueSetting = alarmSettingTemplateDao.findAlarmSettingTemplateById(id);
        if(ToolUtil.isNotEmpty(alarmValueSetting.getMaterialCode())) {
            alarmValueSetting.setMaterialCodes(Arrays.asList(alarmValueSetting.getMaterialCode().split(",")));
        }
        alarmValueSetting.setMaintenceStyle(alarmValueSetting.getMaintenceStyle());
        return alarmValueSetting;
    }
}
