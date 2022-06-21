package com.rpzh.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.AlarmSettingTemplateDao;
import com.rpzh.dto.AlarmSettingTemplateDto;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.LogAlarmSettingDto;
import com.rpzh.dto.SettingColorAndDayDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.AlarmSettingTemplateService;
import com.rpzh.util.IpUtils;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.AlarmSettingTemplateVo;
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
    public Result deleteAlarmSettingTemplate(List<String> materialCodes) {
        if(ToolUtil.isEmpty(materialCodes)){
            return new Result(false, StatusCode.ERROR, "预警模板ids不能为空!");
        }
        List<Integer> ids = alarmSettingTemplateDao.findMaterialCodes(materialCodes);
        alarmSettingTemplateDao.delete(ids);
        return new Result(true, StatusCode.OK, "删除预警设置成功!");
    }

    @Override
    public void insertAlarmSettingTemplate(AlarmSettingTemplateDto alarmSettingTemplateDto) {
        String earlyWarningRuleName = alarmSettingTemplateDto.getEarlyWarningRuleName();
        String materialCode = String.join(",",alarmSettingTemplateDto.getMaterialCodes());
        Date date = new Date();
        String createUser = TokenDecode.getUserInfo().get("username");
        String alarmValueSettingDto1 = alarmSettingTemplateDto.getAlarmValueSettingDto();
        List<AlarmValueSettingDto> alarmValueSettingDtos = JSON.parseArray(alarmValueSettingDto1, AlarmValueSettingDto.class);
        String ipAddr = IpUtils.getIpAddr(Objects.requireNonNull(IpUtils.getRequest()));
        for (AlarmValueSettingDto alarmValueSettingDto : alarmValueSettingDtos) {
            LogAlarmSettingDto logAlarmSettingDto = new LogAlarmSettingDto();
            alarmValueSettingDto.setMaterialCode(materialCode);
            alarmValueSettingDto.setEarlyWarningRuleName(earlyWarningRuleName);
            alarmValueSettingDto.setCreatedTime(date);
            alarmValueSettingDto.setCreateUser(createUser);
            alarmValueSettingDto.setUpdatedTime(date);
            alarmValueSettingDto.setUpdateUser(createUser);
            alarmSettingTemplateDao.insertAlarmSettingTemplate(alarmValueSettingDto);
            logAlarmSettingDto.setEarlyWarningRuleName(earlyWarningRuleName);
            logAlarmSettingDto.setLogDate(date);
            logAlarmSettingDto.setLogUserName(createUser);
            logAlarmSettingDto.setLogType(INSERTTYPE);
            logAlarmSettingDto.setPhases(alarmValueSettingDto.getPhases());
            logAlarmSettingDto.setLogIp(ipAddr);
            logAlarmSettingDto.setMaterialCode(materialCode);
            alarmSettingTemplateDao.insertLogAlarmSetting(logAlarmSettingDto);
        }
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
    public AlarmSettingTemplateVo findAlarmSettingTemplateByMaterialCode(String materialCode, String maintenceStyle) {
        AlarmValueSettingVo alarmValueSettingVo = alarmSettingTemplateDao.findNameByMaintenceStyle(materialCode, maintenceStyle);
        AlarmSettingTemplateVo alarmSettingTemplateVo = new AlarmSettingTemplateVo();
        alarmSettingTemplateVo.setEarlyWarningRuleName(alarmValueSettingVo.getEarlyWarningRuleName());
        alarmSettingTemplateVo.setMaterialCodes(Arrays.asList(alarmValueSettingVo.getMaterialCodes()));
        List<AlarmValueSettingVo> alarmSettingTemplates = alarmSettingTemplateDao.findAlarmSettingTemplateByMaterialCode(materialCode, maintenceStyle);
        alarmSettingTemplateVo.setAlarmValueSettingDtos(alarmSettingTemplates);
        return alarmSettingTemplateVo;
    }

    @Override
    public void updateAlarmSettingTemplate(AlarmSettingTemplateDto alarmSettingTemplateDto) {
        String earlyWarningRuleName = alarmSettingTemplateDto.getEarlyWarningRuleName();
        List<String> materialCodes = alarmSettingTemplateDto.getMaterialCodes();
        String materialCode = String.join(",", materialCodes);
        Date date = new Date();
        String createUser = TokenDecode.getUserInfo().get("username");
        String alarmValueSettingDto1 = alarmSettingTemplateDto.getAlarmValueSettingDto();
        List<AlarmValueSettingDto> alarmValueSettingDtos = JSON.parseArray(alarmValueSettingDto1, AlarmValueSettingDto.class);
        String ipAddr = IpUtils.getIpAddr(Objects.requireNonNull(IpUtils.getRequest()));
        for (int i = 0; i < alarmValueSettingDtos.size(); i++) {
            AlarmValueSettingDto alarmValueSettingDto = alarmValueSettingDtos.get(i);
            AlarmValueSettingVo alarmValueSettingVo = alarmSettingTemplateDao.findAlarmSettingTemplateById(alarmValueSettingDto.getAlarmValueSettingId());
            if(ToolUtil.isNotEmpty(alarmValueSettingVo)) {
                boolean flag = alarmValueSettingVo.getEarlyWarningRuleName().equalsIgnoreCase(earlyWarningRuleName) && alarmValueSettingVo.getMaterialCodes().equals(materialCode);
                boolean o2StackFlag = alarmValueSettingVo.getStackOxygenUpperOne().equals(alarmValueSettingDto.getStackOxygenUpperOne()) && alarmValueSettingVo.getStackOxygenUpperTwo().equals(alarmValueSettingDto.getStackOxygenUpperTwo()) &&
                        alarmValueSettingVo.getStackOxygenUpperThree().equals(alarmValueSettingDto.getStackOxygenUpperThree()) && alarmValueSettingVo.getStackOxygenLower().equals(alarmValueSettingDto.getStackOxygenLower());
                boolean tempCoreFlag = alarmValueSettingVo.getTempCoreChipUpperOne().equals(alarmValueSettingDto.getTempCoreChipUpperOne()) && alarmValueSettingVo.getTempCoreChipUpperTwo().equals(alarmValueSettingDto.getTempCoreChipUpperTwo()) &&
                        alarmValueSettingVo.getTempCoreChipUpperThree().equals(alarmValueSettingDto.getTempCoreChipUpperThree()) && alarmValueSettingVo.getTempCoreChipLower().equals(alarmValueSettingDto.getTempCoreChipLower());
                boolean tempStackFlag = alarmValueSettingVo.getStackTempUpperOne().equals(alarmValueSettingDto.getStackTempUpperOne()) && alarmValueSettingVo.getStackTempUpperTwo().equals(alarmValueSettingDto.getStackTempUpperTwo()) &&
                        alarmValueSettingVo.getStackTempUpperThree().equals(alarmValueSettingDto.getStackTempUpperThree()) && alarmValueSettingVo.getStackTempLower().equals(alarmValueSettingDto.getStackTempLower());
                boolean moistureStackFlag = alarmValueSettingVo.getStackMoistureUpperOne().equals(alarmValueSettingDto.getStackMoistureUpperOne()) && alarmValueSettingVo.getStackMoistureUpperTwo().equals(alarmValueSettingDto.getStackMoistureUpperTwo()) &&
                        alarmValueSettingVo.getStackMoistureUpperThree().equals(alarmValueSettingDto.getStackMoistureUpperThree()) && alarmValueSettingVo.getStackMoistureLower().equals(alarmValueSettingDto.getStackMoistureLower());
                boolean coreHighEnvFlag = alarmValueSettingVo.getCoreTempHighEnvTempOne().equals(alarmValueSettingDto.getCoreTempHighEnvTempOne()) && alarmValueSettingVo.getCoreTempHighEnvTempTwo().equals(alarmValueSettingDto.getCoreTempHighEnvTempTwo()) &&
                        alarmValueSettingVo.getCoreTempHighEnvTempThree().equals(alarmValueSettingDto.getCoreTempHighEnvTempThree()) && alarmValueSettingVo.getCoreTempLowEnvTemp().equals(alarmValueSettingDto.getCoreTempLowEnvTemp());
                if (!(o2StackFlag && tempCoreFlag && tempStackFlag && moistureStackFlag && coreHighEnvFlag && flag)) {
                    alarmValueSettingDto.setMaterialCode(materialCode);
                    alarmValueSettingDto.setEarlyWarningRuleName(earlyWarningRuleName);
                    alarmValueSettingDto.setUpdatedTime(date);
                    alarmValueSettingDto.setUpdateUser(createUser);
                    alarmSettingTemplateDao.updateAlarmSettingTemplate(alarmValueSettingDto);
                    LogAlarmSettingDto logAlarmSettingDto = new LogAlarmSettingDto();
                    logAlarmSettingDto.setEarlyWarningRuleName(earlyWarningRuleName);
                    logAlarmSettingDto.setLogDate(date);
                    logAlarmSettingDto.setLogUserName(createUser);
                    logAlarmSettingDto.setLogType(UPDATETYPE);
                    logAlarmSettingDto.setPhases(alarmValueSettingDto.getPhases());
                    logAlarmSettingDto.setLogIp(ipAddr);
                    logAlarmSettingDto.setMaterialCode(materialCode);
                    alarmSettingTemplateDao.insertLogAlarmSetting(logAlarmSettingDto);
                }
            }
        }
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
}
