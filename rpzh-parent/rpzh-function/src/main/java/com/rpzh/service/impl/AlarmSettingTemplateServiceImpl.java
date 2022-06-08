package com.rpzh.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.AlarmSettingTemplateDao;
import com.rpzh.dto.AlarmSettingTemplateDto;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.SettingColorAndDayDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.AlarmSettingTemplateService;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.AlarmSettingTemplateVo;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.SettingColorAndDayVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/06/07/10:20
 * @Description: 功能
 */
@Service
public class AlarmSettingTemplateServiceImpl implements AlarmSettingTemplateService {
    @Resource
    private AlarmSettingTemplateDao alarmSettingTemplateDao;
    @Override
    public PageInfo<AlarmValueSettingVo> findAlarmSettingTemplateTable(AlarmValueSettingDto alarmValueSettingDto) {
        Integer size = alarmValueSettingDto.getSize();
        Integer page = alarmValueSettingDto.getPage();
        if(size ==null){
            size =10;
        }
        if(page==null){
            page = 1;
        }
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
        List<String> materialCodes = alarmSettingTemplateDto.getMaterialCodes();
        String materialCode = String.join(",",materialCodes);
        Date date = new Date();
        String createUser = TokenDecode.getUserInfo().get("username");
        String alarmValueSettingDto1 = alarmSettingTemplateDto.getAlarmValueSettingDto();
        List<AlarmValueSettingDto> alarmValueSettingDtos = JSON.parseArray(alarmValueSettingDto1, AlarmValueSettingDto.class);
        for (AlarmValueSettingDto alarmValueSettingDto : alarmValueSettingDtos) {
            alarmValueSettingDto.setMaterialCode(materialCode);
            alarmValueSettingDto.setEarlyWarningRuleName(earlyWarningRuleName);
            alarmValueSettingDto.setCreatedTime(date);
            alarmValueSettingDto.setCreateUser(createUser);
            alarmValueSettingDto.setUpdatedTime(date);
            alarmValueSettingDto.setUpdateUser(createUser);
            alarmSettingTemplateDao.insertAlarmSettingTemplate(alarmValueSettingDto);
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
        String materialCode = String.join(",",materialCodes);
        Date date = new Date();
        String createUser = TokenDecode.getUserInfo().get("username");
        String alarmValueSettingDto1 = alarmSettingTemplateDto.getAlarmValueSettingDto();
        List<AlarmValueSettingDto> alarmValueSettingDtos = JSON.parseArray(alarmValueSettingDto1, AlarmValueSettingDto.class);
        Integer count = alarmSettingTemplateDao.findCountByMaintenceStyle(materialCode, earlyWarningRuleName);
        if(count==0){
            for (AlarmValueSettingDto alarmValueSettingDto : alarmValueSettingDtos) {
                alarmValueSettingDto.setMaterialCode(materialCode);
                alarmValueSettingDto.setEarlyWarningRuleName(earlyWarningRuleName);
                alarmValueSettingDto.setUpdatedTime(date);
                alarmValueSettingDto.setUpdateUser(createUser);
                alarmSettingTemplateDao.updateAlarmSettingTemplate(alarmValueSettingDto);
            }
        }
        if(count>0){
            for (int i = 0; i < alarmValueSettingDtos.size(); i++) {
                AlarmValueSettingDto alarmValueSettingDto = alarmValueSettingDtos.get(i);
                AlarmValueSettingVo alarmValueSettingVo = alarmSettingTemplateDao.findAlarmSettingTemplateById(alarmValueSettingDto.getAlarmValueSettingId());
                boolean o2StackFlag = alarmValueSettingVo.getStackOxygenUpperOne().equals(alarmValueSettingDto.getStackOxygenUpperOne()) && alarmValueSettingVo.getStackOxygenUpperTwo().equals(alarmValueSettingDto.getStackOxygenUpperTwo()) &&
                        alarmValueSettingVo.getStackOxygenUpperThree ().equals(alarmValueSettingDto.getStackOxygenUpperThree ()) && alarmValueSettingVo.getStackOxygenLower().equals(alarmValueSettingDto.getStackOxygenLower());
                boolean tempCoreFlag = alarmValueSettingVo.getTempCoreChipUpperOne().equals(alarmValueSettingDto.getTempCoreChipUpperOne()) && alarmValueSettingVo.getTempCoreChipUpperTwo().equals(alarmValueSettingDto.getTempCoreChipUpperTwo()) &&
                        alarmValueSettingVo.getTempCoreChipUpperThree ().equals(alarmValueSettingDto.getTempCoreChipUpperThree ()) && alarmValueSettingVo.getTempCoreChipLower().equals(alarmValueSettingDto.getTempCoreChipLower());
                boolean tempStackFlag =alarmValueSettingVo.getStackTempUpperOne().equals(alarmValueSettingDto.getStackTempUpperOne()) && alarmValueSettingVo.getStackTempUpperTwo().equals(alarmValueSettingDto.getStackTempUpperTwo())&&
                        alarmValueSettingVo.getStackTempUpperThree().equals(alarmValueSettingDto.getStackTempUpperThree())&& alarmValueSettingVo.getStackTempLower().equals(alarmValueSettingDto.getStackTempLower());
                boolean moistureStackFlag =alarmValueSettingVo.getStackMoistureUpperOne().equals(alarmValueSettingDto.getStackMoistureUpperOne()) && alarmValueSettingVo.getStackMoistureUpperTwo().equals(alarmValueSettingDto.getStackMoistureUpperTwo()) &&
                        alarmValueSettingVo.getStackMoistureUpperThree().equals(alarmValueSettingDto.getStackMoistureUpperThree()) && alarmValueSettingVo.getStackMoistureLower().equals(alarmValueSettingDto.getStackMoistureLower());
                boolean coreHighEnvFlag = alarmValueSettingVo.getCoreTempHighEnvTempOne().equals(alarmValueSettingDto.getCoreTempHighEnvTempOne()) && alarmValueSettingVo.getCoreTempHighEnvTempTwo().equals(alarmValueSettingDto.getCoreTempHighEnvTempTwo()) &&
                        alarmValueSettingVo.getCoreTempHighEnvTempThree().equals(alarmValueSettingDto.getCoreTempHighEnvTempThree()) && alarmValueSettingVo.getCoreTempLowEnvTemp().equals(alarmValueSettingDto.getCoreTempLowEnvTemp());
                if(!(o2StackFlag && tempCoreFlag && tempStackFlag && moistureStackFlag && coreHighEnvFlag)){
                    alarmValueSettingDto.setUpdatedTime(date);
                    alarmValueSettingDto.setUpdateUser(createUser);
                    alarmSettingTemplateDao.updateAlarmSettingTemplate(alarmValueSettingDto);
                }
            }
        }
    }

}
