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
import com.rpzh.util.IpUtils;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.LogAlarmSettingVo;
import com.rpzh.vo.SettingColorAndDayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Author: zyq
 * @Date: 2022/06/07/10:20
 * @Description: 功能
 */
@Service
public class AlarmSettingTemplateServiceImpl implements AlarmSettingTemplateService {
    @Resource
    private AlarmSettingTemplateDao alarmSettingTemplateDao;

    private final String INSERTTYPE = "添加";

    private final String UPDATETYPE = "修改";

    @Autowired
    RedisTemplate redisTemplate;
    private static final String CACHE_KEY_REDIS_MAINTAIN = "MAINTAIN_";
    @Override
    public PageInfo<AlarmValueSettingVo> findAlarmSettingTemplateTable(AlarmValueSettingDto alarmValueSettingDto) {
        Integer size = alarmValueSettingDto.getSize();
        Integer page = alarmValueSettingDto.getPage();
        PageHelper.startPage(page, size);
        List<AlarmValueSettingVo> alarmValueSettingVos = alarmSettingTemplateDao.findAlarmSettingTemplateTable(alarmValueSettingDto);
        return new PageInfo(alarmValueSettingVos);
    }

    @Override
    public Result deleteAlarmSettingTemplate(List<Integer> ids) {
        if(ToolUtil.isEmpty(ids)){
            return new Result(false, StatusCode.ERROR, "ids不能为空!");
        }
        alarmSettingTemplateDao.delete(ids);
        alarmSettingTemplateDao.deleteStage(ids);
        return new Result(true, StatusCode.OK, "删除预警设置成功!");
    }

    public boolean getFlag(AlarmValueSettingDto alarmValueSettingDto){
        return alarmValueSettingDto.getStackOxygenUpperOne() == null ||
        alarmValueSettingDto.getStackOxygenUpperThree() == null ||
        alarmValueSettingDto.getStackOxygenUpperTwo() == null ||
        alarmValueSettingDto.getStackOxygenLower() == null ||
        alarmValueSettingDto.getStackTempUpperOne() == null ||
        alarmValueSettingDto.getStackTempUpperTwo() == null ||
        alarmValueSettingDto.getStackTempUpperThree() == null ||
        alarmValueSettingDto.getStackTempLower() == null ||
        alarmValueSettingDto.getStackMoistureUpperOne() == null ||
        alarmValueSettingDto.getStackMoistureUpperThree() == null ||
        alarmValueSettingDto.getStackMoistureUpperTwo() == null ||
        alarmValueSettingDto.getStackMoistureLower() == null ||
        alarmValueSettingDto.getTempCoreChipUpperOne() == null ||
        alarmValueSettingDto.getTempCoreChipUpperThree() == null ||
        alarmValueSettingDto.getTempCoreChipUpperTwo() == null ||
        alarmValueSettingDto.getTempCoreChipLower() == null ||
        alarmValueSettingDto.getCoreTempHighEnvTempOne() == null ||
        alarmValueSettingDto.getCoreTempHighEnvTempThree() == null ||
        alarmValueSettingDto.getCoreTempHighEnvTempTwo() == null ||
        alarmValueSettingDto.getCoreTempLowEnvTemp() ==null;
    }

    @Override
    public Result insertAlarmSettingTemplate(AlarmValueSettingDto alarmValueSettingDto) {
        String materialCode = alarmValueSettingDto.getMaterialCode();
        if(getFlag(alarmValueSettingDto)){
            return new Result(false, StatusCode.ERROR, "值不能为空!");
        }
        Date date = new Date();
        String createUser = TokenDecode.getUserInfo().get("username");
        String ipAddr = IpUtils.getIpAddr(Objects.requireNonNull(IpUtils.getRequest()));
        LogAlarmSettingDto logAlarmSettingDto = new LogAlarmSettingDto();
        alarmValueSettingDto.setUpdatedTime(date);
        alarmValueSettingDto.setUpdateUser(createUser);
        if(ToolUtil.isNotEmpty(alarmValueSettingDto.getAlarmValueSettingId())){
            alarmSettingTemplateDao.updateAlarmSettingTemplate(alarmValueSettingDto);
            alarmSettingTemplateDao.updateAlarmSettingTemplateStage(alarmValueSettingDto);
        }else {
            Integer count = alarmSettingTemplateDao.findCountByNameAndPhases(alarmValueSettingDto.getEarlyWarningRuleName(),alarmValueSettingDto.getPhases());
            if(count>0){
                return new Result(false, StatusCode.ERROR, "已存在!");
            }
            alarmValueSettingDto.setCreatedTime(date);
            alarmValueSettingDto.setCreateUser(createUser);
            if(alarmSettingTemplateDao.findCountByName(alarmValueSettingDto.getEarlyWarningRuleName())<1) {
                alarmSettingTemplateDao.insertAlarmSettingTemplate(alarmValueSettingDto);
            }
            Integer templateId = alarmSettingTemplateDao.findTemplateId(alarmValueSettingDto);
            alarmValueSettingDto.setAlarmValueSettingId(templateId);
            alarmSettingTemplateDao.insertAlarmSettingTemplateStage(alarmValueSettingDto);
        }
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

    @Override
    public SettingColorAndDayVo findColorAndAlertUpgradeDay(){
        SettingColorAndDayVo settingColorAndDayVo = alarmSettingTemplateDao.findColorAndAlertUpgradeDay();
        if(ToolUtil.isNotEmpty(settingColorAndDayVo)){
            return settingColorAndDayVo;
        }
        return new SettingColorAndDayVo();
    }

    @Override
    public Result updateOrInsertColorAndAlertUpgradeDay(SettingColorAndDayDto settingColorAndDayDto) {
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

    @Override
    public Result updateAlarmSettingTemplate(AlarmValueSettingDto alarmValueSettingDto) {
        if(getFlag(alarmValueSettingDto)){
            return new Result(false, StatusCode.ERROR, "值不能为空!");
        }
        String earlyWarningRuleName = alarmValueSettingDto.getEarlyWarningRuleName();
        String materialCode = alarmValueSettingDto.getMaterialCode();
        Date date = new Date();
        String createUser = TokenDecode.getUserInfo().get("username");
        String ipAddr = IpUtils.getIpAddr(Objects.requireNonNull(IpUtils.getRequest()));
        alarmValueSettingDto.setMaterialCode(materialCode);
        alarmValueSettingDto.setEarlyWarningRuleName(earlyWarningRuleName);
        alarmValueSettingDto.setUpdatedTime(date);
        alarmValueSettingDto.setUpdateUser(createUser);
        alarmSettingTemplateDao.updateAlarmSettingTemplate(alarmValueSettingDto);
        alarmSettingTemplateDao.updateAlarmSettingTemplateStage(alarmValueSettingDto);
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

    @Override
    public Result findLogData(LogAlarmSettingDto logAlarmSettingDto) {
        if(StringUtils.isEmpty(logAlarmSettingDto.getMaterialCode()) || StringUtils.isEmpty(logAlarmSettingDto.getEarlyWarningRuleName())){
            return new Result<>(false, StatusCode.ERROR, "物料代码或预警规则名称不能为空!");
        }
        PageHelper.startPage(logAlarmSettingDto.getPage(), logAlarmSettingDto.getSize());
        List<LogAlarmSettingVo> logData = alarmSettingTemplateDao.findLogData(logAlarmSettingDto);
        return new Result<>(true, StatusCode.OK, "查询操作日志成功!",new PageInfo(logData));
    }

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
