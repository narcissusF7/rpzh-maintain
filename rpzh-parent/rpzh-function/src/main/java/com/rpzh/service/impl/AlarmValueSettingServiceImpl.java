package com.rpzh.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.AlarmValueSettingDao;
import com.rpzh.dao.MaintainPlanDao;
import com.rpzh.dao.MaterialDao;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.MaterialCodeYearDto;
import com.rpzh.dto.SettingColorAndDayDto;
import com.rpzh.entity.AlertSettingSetDefault;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.AlarmValueSettingService;
import com.rpzh.util.StringUtils;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.SettingColorAndDayVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Author: zyq
 * @Date: 2022/05/07/15:01
 * @Description: 功能
 */
@Service
public class AlarmValueSettingServiceImpl implements AlarmValueSettingService {
    @Resource
    private AlarmValueSettingDao alarmValueSettingDao;
    @Resource
    private MaintainPlanDao maintainPlanDao;
    @Resource
    private MaterialDao materialDao;
    @Override
    public AlarmValueSettingVo findMaintainPlanInfo(AlarmValueSettingDto alarmValueSettingDto) {
        String materialCodeYear = alarmValueSettingDto.getMaterialCodeYear();
        if(!materialCodeYear.contains("[")){
            materialCodeYear = "[" + materialCodeYear;
        }
        if(!materialCodeYear.contains("]")){
            materialCodeYear = materialCodeYear + "]";
        }
        List<String> materialCodeYearJson = JSON.parseArray(materialCodeYear, String.class);
        Collections.sort(materialCodeYearJson);
        alarmValueSettingDto.setMaterialCodeYear(materialCodeYearJson.toString());
        AlarmValueSettingVo alarmValueSetting = alarmValueSettingDao.findAlarmValueSetting(alarmValueSettingDto);
        if(alarmValueSetting!=null){
            return alarmValueSetting;
        }
        alarmValueSetting = new AlarmValueSettingVo();
        alarmValueSetting.setStackOxygenUpperOne(AlertSettingSetDefault.stackOxygenUpperOne);
        alarmValueSetting.setStackOxygenUpperTwo(AlertSettingSetDefault.stackOxygenUpperTwo);
        alarmValueSetting.setStackOxygenUpperThree(AlertSettingSetDefault.stackOxygenUpperThree);
        alarmValueSetting.setStackOxygenLower(AlertSettingSetDefault.stackOxygenLower);
        alarmValueSetting.setWarehouseOxygenUpperOne(AlertSettingSetDefault.warehouseOxygenUpperOne);
        alarmValueSetting.setWarehouseOxygenUpperTwo(AlertSettingSetDefault.warehouseOxygenUpperTwo);
        alarmValueSetting.setWarehouseOxygenUpperThree(AlertSettingSetDefault.warehouseOxygenUpperThree);
        alarmValueSetting.setWarehouseOxygenLower(AlertSettingSetDefault.warehouseOxygenLower);
        alarmValueSetting.setTempCoreChipUpperOne(AlertSettingSetDefault.tempCoreChipUpperOne);
        alarmValueSetting.setTempCoreChipUpperTwo(AlertSettingSetDefault.tempCoreChipUpperTwo);
        alarmValueSetting.setTempCoreChipUpperThree(AlertSettingSetDefault.tempCoreChipUpperThree);
        alarmValueSetting.setTempCoreChipLower(AlertSettingSetDefault.tempCoreChipLower);
        alarmValueSetting.setStackTempUpperOne(AlertSettingSetDefault.stackTempUpperOne);
        alarmValueSetting.setStackTempUpperTwo(AlertSettingSetDefault.stackTempUpperTwo);
        alarmValueSetting.setStackTempUpperThree(AlertSettingSetDefault.stackTempUpperThree);
        alarmValueSetting.setStackTempLower(AlertSettingSetDefault.stackTempLower);
        alarmValueSetting.setStackMoistureUpperOne(AlertSettingSetDefault.stackMoistureUpperOne);
        alarmValueSetting.setStackMoistureUpperTwo(AlertSettingSetDefault.stackMoistureUpperTwo);
        alarmValueSetting.setStackMoistureUpperThree(AlertSettingSetDefault.stackMoistureUpperThree);
        alarmValueSetting.setStackMoistureLower(AlertSettingSetDefault.stackMoistureLower);
        alarmValueSetting.setWarehouseTempUpper0ne(AlertSettingSetDefault.warehouseTempUpperOne);
        alarmValueSetting.setWarehouseTempUpperTwo(AlertSettingSetDefault.warehouseTempUpperTwo);
        alarmValueSetting.setWarehouseTempUpperThree(AlertSettingSetDefault.warehouseTempUpperThree);
        alarmValueSetting.setWarehouseTempLower(AlertSettingSetDefault.warehouseTempLower);
        alarmValueSetting.setWarehouseMoistureUpperOne(AlertSettingSetDefault.warehouseMoistureUpperOne);
        alarmValueSetting.setWarehouseMoistureUpperTwo(AlertSettingSetDefault.warehouseMoistureUpperTwo);
        alarmValueSetting.setWarehouseMoistureUpperThree(AlertSettingSetDefault.warehouseMoistureUpperThree);
        alarmValueSetting.setWarehouseMoistureLower(AlertSettingSetDefault.warehouseMoistureLower);
        alarmValueSetting.setCoreTempHighEnvTempOne(AlertSettingSetDefault.coreTempHighEnvTempOne);
        alarmValueSetting.setCoreTempHighEnvTempTwo(AlertSettingSetDefault.coreTempHighEnvTempTwo);
        alarmValueSetting.setCoreTempHighEnvTempThree(AlertSettingSetDefault.coreTempHighEnvTempThree);
        alarmValueSetting.setCoreTempLowEnvTemp(AlertSettingSetDefault.coreTempLowEnvTemp);
        return alarmValueSetting;
    }

    @Override
    public PageInfo<AlarmValueSettingVo> findAlarmValueSettingTable(AlarmValueSettingDto alarmValueSettingDto) {
        Integer size = alarmValueSettingDto.getSize();
        Integer page = alarmValueSettingDto.getPage();
        if(size ==null){
            size =10;
        }
        if(page==null){
            page = 0;
        }
        PageHelper.startPage(page, size);
        List<AlarmValueSettingVo> alarmValueSettingVos = alarmValueSettingDao.findAlarmValueSettingTable(alarmValueSettingDto);
        for (AlarmValueSettingVo alarmValueSettingVo : alarmValueSettingVos) {
            if(alarmValueSettingVo.getStatus().equals("1")){
                alarmValueSettingVo.setStatus("on");
            }
            if(alarmValueSettingVo.getStatus().equals("0")){
                alarmValueSettingVo.setStatus("off");
            }
        }
        return new PageInfo(alarmValueSettingVos);
    }

    @Override
    public Result changeAlarmValueSettingStatus(Integer id) {
        Integer count = maintainPlanDao.isExistAlarmValueSetting(id);
        if(count>0){
            return new Result(false, StatusCode.ERROR, "该预警设置已存在养护计划,不可修改状态!");
        }
        Integer result = alarmValueSettingDao.updateStatus(id);
        return new Result(true, StatusCode.OK, "修改预警状态成功!",result);
    }

    @Override
    public Result deleteAlarmValueSetting(List<Integer> ids) {
        Integer count=0;
        for (Integer id : ids) {
            count = maintainPlanDao.isExistAlarmValueSetting(id);
            if(count>0){
                return new Result(false, StatusCode.ERROR, "该预警设置已存在养护计划,不可删除!");
            }
            alarmValueSettingDao.delete(id);
        }
        return new Result(true, StatusCode.OK, "删除预警设置成功!");
    }

    @Override
    public List<String> findYear(String materialCode) {
        List<String> year = materialDao.findYear(null, materialCode);
        return year;
    }

    @Override
    public void insertAlarmValueSetting(AlarmValueSettingDto alarmValueSettingDto) {
        String materialCodeYear = alarmValueSettingDto.getMaterialCodeYear();
        if(!materialCodeYear.contains("[")){
            materialCodeYear = "[" + materialCodeYear;
        }
        if(!materialCodeYear.contains("]")){
            materialCodeYear = materialCodeYear + "]";
        }
        List<String> materialCodeYearJson = JSON.parseArray(materialCodeYear, String.class);
        materialCodeYear = JSON.toJSONString(JSON.parse(materialCodeYearJson.toString()));
        List<MaterialCodeYearDto> materialCodeYearDtos = JSON.parseArray(materialCodeYear, MaterialCodeYearDto.class);
        String materialCode = materialCodeYearDtos.get(0).getId();
        String tobaccoYear = materialCodeYearDtos.get(0).getTobaccoYear();
        for (int i = 1; i < materialCodeYearDtos.size(); i++) {
            materialCode = materialCode + "," + materialCodeYearDtos.get(i).getId();
            tobaccoYear =  tobaccoYear + ";" +materialCodeYearDtos.get(i).getTobaccoYear();
        }
        Collections.sort(materialCodeYearJson);
        alarmValueSettingDto.setMaterialCodeYear(materialCodeYearJson.toString());
        alarmValueSettingDto.setMaterialCode(materialCode);
        alarmValueSettingDto.setTobaccoYear(tobaccoYear);
        String createUser = TokenDecode.getUserInfo().get("username");
        alarmValueSettingDto.setCreateUser(createUser);
        alarmValueSettingDto.setCreatedTime(new Date());
        alarmValueSettingDao.insertAlarmValueSetting(alarmValueSettingDto);
    }

    @Override
    public Integer isExistEarlyWarningRuleName(String earlyWarningRuleName) {
        return alarmValueSettingDao.isExistEarlyWarningRuleName(earlyWarningRuleName);
    }

    @Override
    public AlarmValueSettingVo findAlarmValueSettingById(Integer id) {
        AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingDao.findAlarmValueSettingById(id);
        return alarmValueSettingVo;
    }

    @Override
    public Integer updateAlarmValueSetting(AlarmValueSettingDto alarmValueSettingDto) {
        String updateUser = TokenDecode.getUserInfo().get("username");
        alarmValueSettingDto.setUpdateUser(updateUser);
        alarmValueSettingDto.setUpdatedTime(new Date());
        Integer result = alarmValueSettingDao.updateAlarmValueSetting(alarmValueSettingDto, alarmValueSettingDto.getAlarmValueSettingId());
        return result;
    }

    @Override
    public String findEarlyWarningRuleName(Integer id) {
        return alarmValueSettingDao.findEarlyWarningRuleName(id);
    }

    @Override
    public SettingColorAndDayVo findColorAndAlertUpgradeDay(){
        SettingColorAndDayVo settingColorAndDayVo = alarmValueSettingDao.findColorAndAlertUpgradeDay();
        if(settingColorAndDayVo.getColorOne()==null){
            alarmValueSettingDao.insertColorAndAlertUpgradeDay("color",AlertSettingSetDefault.colorOne,"one");
            settingColorAndDayVo.setColorOne(AlertSettingSetDefault.colorOne);
        }
        if(settingColorAndDayVo.getColorTwo()==null){
            alarmValueSettingDao.insertColorAndAlertUpgradeDay("color",AlertSettingSetDefault.colorTwo,"two");
            settingColorAndDayVo.setColorTwo(AlertSettingSetDefault.colorTwo);
        }
        if(settingColorAndDayVo.getColorThree()==null){
            alarmValueSettingDao.insertColorAndAlertUpgradeDay("color",AlertSettingSetDefault.colorThree,"three");
            settingColorAndDayVo.setColorThree(AlertSettingSetDefault.colorThree);
        }
        if(settingColorAndDayVo.getOneToTwoDay()==null){
            alarmValueSettingDao.insertColorAndAlertUpgradeDay("warningThreshold",AlertSettingSetDefault.oneToTwoDay,"oneToTwo");
            settingColorAndDayVo.setOneToTwoDay(AlertSettingSetDefault.oneToTwoDay);
        }
        if(settingColorAndDayVo.getTwoToThreeDay()==null){
            alarmValueSettingDao.insertColorAndAlertUpgradeDay("warningThreshold",AlertSettingSetDefault.twoToThreeDay,"twoToThree");
            settingColorAndDayVo.setTwoToThreeDay(AlertSettingSetDefault.twoToThreeDay);
        }
        return settingColorAndDayVo;
    }

    @Override
    public Integer updateColorAndAlertUpgradeDay(SettingColorAndDayDto settingColorAndDayDto) {
        alarmValueSettingDao.updateColorAndAlertUpgradeDay(settingColorAndDayDto.getColorOne(),"color","one");
        alarmValueSettingDao.updateColorAndAlertUpgradeDay(settingColorAndDayDto.getColorTwo(),"color","two");
        alarmValueSettingDao.updateColorAndAlertUpgradeDay(settingColorAndDayDto.getColorThree(),"color","three");
        alarmValueSettingDao.updateColorAndAlertUpgradeDay(settingColorAndDayDto.getOneToTwoDay(),"warningThreshold","oneToTwo");
        Integer result = alarmValueSettingDao.updateColorAndAlertUpgradeDay(settingColorAndDayDto.getTwoToThreeDay(),"warningThreshold","twoToThree");
        return result;
    }
}
