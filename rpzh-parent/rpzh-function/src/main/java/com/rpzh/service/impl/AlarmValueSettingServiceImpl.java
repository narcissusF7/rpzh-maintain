package com.rpzh.service.impl;

import com.alibaba.fastjson.JSON;
import com.rpzh.dao.AlarmValueSettingDao;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.MaterialCodeYearDto;
import com.rpzh.entity.AlertSettingSetDefault;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.AlarmValueSettingService;
import com.rpzh.util.StringUtils;
import com.rpzh.vo.AlarmValueSettingVo;
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
        AlarmValueSettingVo alarmValueSetting = alarmValueSettingDao.findAlarmValueSetting(alarmValueSettingDto);
        AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingDao.findColorAndAlertUpgradeDay();
        if(alarmValueSettingVo.getColorOne()==null){
            alarmValueSettingDao.insertColorAndAlertUpgradeDay("color",AlertSettingSetDefault.colorOne,"one");
            alarmValueSettingVo.setColorOne(AlertSettingSetDefault.colorOne);
        }
        if(alarmValueSettingVo.getColorTwo()==null){
            alarmValueSettingDao.insertColorAndAlertUpgradeDay("color",AlertSettingSetDefault.colorTwo,"two");
            alarmValueSettingVo.setColorTwo(AlertSettingSetDefault.colorTwo);
        }
        if(alarmValueSettingVo.getColorThree()==null){
            alarmValueSettingDao.insertColorAndAlertUpgradeDay("color",AlertSettingSetDefault.colorThree,"three");
            alarmValueSettingVo.setColorThree(AlertSettingSetDefault.colorThree);
        }
        if(alarmValueSettingVo.getOneToTwoDay()==null){
            alarmValueSettingDao.insertColorAndAlertUpgradeDay("warningThreshold",AlertSettingSetDefault.oneToTwoDay,"oneToTwo");
            alarmValueSettingVo.setOneToTwoDay(AlertSettingSetDefault.oneToTwoDay);
        }
        if(alarmValueSettingVo.getTwoToThreeDay()==null){
            alarmValueSettingDao.insertColorAndAlertUpgradeDay("warningThreshold",AlertSettingSetDefault.twoToThreeDay,"twoToThree");
            alarmValueSettingVo.setTwoToThreeDay(AlertSettingSetDefault.twoToThreeDay);
        }
        if(alarmValueSetting!=null){
            alarmValueSetting.setMaterialCode(materialCode);
            alarmValueSetting.setTobaccoYear(tobaccoYear);
            alarmValueSetting.setColorOne(alarmValueSettingVo.getColorOne());
            alarmValueSetting.setColorTwo(alarmValueSettingVo.getColorTwo());
            alarmValueSetting.setColorThree(alarmValueSettingVo.getColorThree());
            alarmValueSetting.setOneToTwoDay(alarmValueSettingVo.getOneToTwoDay());
            alarmValueSetting.setTwoToThreeDay(alarmValueSettingVo.getTwoToThreeDay());
            return alarmValueSetting;
        }
        alarmValueSetting = new AlarmValueSettingVo();
        alarmValueSetting.setMaterialCode(materialCode);
        alarmValueSetting.setTobaccoYear(tobaccoYear);
        alarmValueSetting.setColorOne(alarmValueSettingVo.getColorOne());
        alarmValueSetting.setColorTwo(alarmValueSettingVo.getColorTwo());
        alarmValueSetting.setColorThree(alarmValueSettingVo.getColorThree());
        alarmValueSetting.setOneToTwoDay(alarmValueSettingVo.getOneToTwoDay());
        alarmValueSetting.setTwoToThreeDay(alarmValueSettingVo.getTwoToThreeDay());
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
        return alarmValueSetting;
    }
}
