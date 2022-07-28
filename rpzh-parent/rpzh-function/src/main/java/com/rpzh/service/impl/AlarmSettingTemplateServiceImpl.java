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
 * @Description: 功能
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

    /*
    * 查询预警列表
    */
    @Override
    public PageInfo<AlarmValueSettingVo> findAlarmSettingTemplateTable(AlarmValueSettingDto alarmValueSettingDto) {
        Integer size = alarmValueSettingDto.getSize();
        Integer page = alarmValueSettingDto.getPage();
        PageHelper.startPage(page, size);
        List<AlarmValueSettingVo> alarmValueSettingVos = alarmSettingTemplateDao.findAlarmSettingTemplateTable(alarmValueSettingDto);
        return new PageInfo(alarmValueSettingVos);
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
            //更新
            alarmSettingTemplateDao.updateAlarmSettingTemplate(alarmValueSettingDto);
            alarmSettingTemplateDao.updateAlarmSettingTemplateStage(alarmValueSettingDto);
        }else {
            //判断该预警规则名称该阶段是否存在
            Integer count = alarmSettingTemplateDao.findCountByNameAndPhases(alarmValueSettingDto.getEarlyWarningRuleName(),alarmValueSettingDto.getPhases());
            if(count>0){
                return new Result(false, StatusCode.ERROR, "已存在!");
            }
            alarmValueSettingDto.setCreatedTime(date);
            alarmValueSettingDto.setCreateUser(createUser);
            //根据预警规则名称获取预警规则id
            Integer aid = alarmSettingTemplateDao.findCountByName(alarmValueSettingDto.getEarlyWarningRuleName());
            if(ToolUtil.isEmpty(aid)) {
                //这是该预警规则名称的第一阶段,将数据插入预警表中
                alarmSettingTemplateDao.insertAlarmSettingTemplate(alarmValueSettingDto);
                //根据预警规则名称获取预警规则id
                aid = alarmSettingTemplateDao.findCountByName(alarmValueSettingDto.getEarlyWarningRuleName());
            }
            alarmValueSettingDto.setAlarmValueSettingId(aid);
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
        SettingColorAndDayVo settingColorAndDayVo = alarmSettingTemplateDao.findColorAndAlertUpgradeDay();
        if(ToolUtil.isNotEmpty(settingColorAndDayVo)){
            return settingColorAndDayVo;
        }
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
        redisTemplate.delete(CACHE_KEY_REDIS_MAINTAIN+"TwoToThree");
        if(count>0){
            alarmSettingTemplateDao.updateColorAndAlertUpgradeDay(settingColorAndDayDto.getColorOne(),"color","one");
            alarmSettingTemplateDao.updateColorAndAlertUpgradeDay(settingColorAndDayDto.getColorTwo(),"color","two");
            alarmSettingTemplateDao.updateColorAndAlertUpgradeDay(settingColorAndDayDto.getColorThree(),"color","three");
            alarmSettingTemplateDao.updateColorAndAlertUpgradeDay(settingColorAndDayDto.getOneToTwoDay(),"warningThreshold","oneToTwo");
            alarmSettingTemplateDao.updateColorAndAlertUpgradeDay(settingColorAndDayDto.getTwoToThreeDay(),"warningThreshold","twoToThree");
            return new Result<>(true, StatusCode.OK, "修改升级规则成功!");
        }
        alarmSettingTemplateDao.insertColorAndAlertUpgradeDay("color",settingColorAndDayDto.getColorOne(),"one");
        alarmSettingTemplateDao.insertColorAndAlertUpgradeDay("color",settingColorAndDayDto.getColorTwo(),"two");
        alarmSettingTemplateDao.insertColorAndAlertUpgradeDay("color",settingColorAndDayDto.getColorThree(),"three");
        alarmSettingTemplateDao.insertColorAndAlertUpgradeDay("warningThreshold",settingColorAndDayDto.getOneToTwoDay(),"oneToTwo");
        alarmSettingTemplateDao.insertColorAndAlertUpgradeDay("warningThreshold",settingColorAndDayDto.getTwoToThreeDay(),"twoToThree");
        return new Result<>(true, StatusCode.OK, "添加升级规则成功!");
    }

    /*
    * 新建时在点击下一阶段后再点上一步时回显或者修改时预警设置各个阶段回显
    */
    @Override
    public Result findAlarmSettingTemplateByMaterialCode(String materialCode, String earlyWarningRuleName,String phases) {
        AlarmValueSettingVo alarmValueSettingVo;
        if(ToolUtil.isEmpty(phases)) {
            alarmValueSettingVo = alarmSettingTemplateDao.findNameByMaintenceStyle(materialCode, earlyWarningRuleName);
        }else{
            alarmValueSettingVo = alarmSettingTemplateDao.findAlarmSettingTemplateByMaterialCodeAndPhases(materialCode, earlyWarningRuleName,phases);
        }
        if(ToolUtil.isEmpty(alarmValueSettingVo)){
            alarmValueSettingVo = new AlarmValueSettingVo("1");
        }else{
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
        List<LogAlarmSettingVo> logData = alarmSettingTemplateDao.findLogData(logAlarmSettingDto);
        return new Result<>(true, StatusCode.OK, "查询操作日志成功!",new PageInfo(logData));
    }

    /*
    * 预警设置修改前回显
    */
    @Override
    public AlarmValueSettingVo findAlarmSettingTemplate(Integer id) {
        AlarmValueSettingVo alarmValueSetting = alarmSettingTemplateDao.findAlarmSettingTemplateById(id);
        AlarmValueSettingVo alarmValueSettingVo = new AlarmValueSettingVo();
        alarmValueSettingVo.setAlarmValueSettingId(alarmValueSetting.getAlarmValueSettingId());
        alarmValueSettingVo.setEarlyWarningRuleName(alarmValueSetting.getEarlyWarningRuleName());
        alarmValueSettingVo.setMaterialCodes(Arrays.asList(alarmValueSetting.getMaterialCode().split(",")));
        return alarmValueSettingVo;
    }
}
