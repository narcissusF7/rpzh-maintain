package com.rpzh.service.impl;

import com.rpzh.dao.EarlyWarningDao;
import com.rpzh.dto.EarlyWarningDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.entity.WarningTypeDefault;
import com.rpzh.service.EarlyWarningService;
import com.rpzh.vo.EarlyWarningVo;
import com.rpzh.vo.WarningCountVo;
import com.rpzh.vo.WarningVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/05/20/14:46
 * @Description: 功能
 */
@Service
public class EarlyWarningServiceImpl implements EarlyWarningService {
    @Resource
    private EarlyWarningDao earlyWarningDao;

    @Override
    public Result findEarlyWarning(EarlyWarningDto earlyWarningDto) {
        List<WarningCountVo> warningCount = earlyWarningDao.findEarlyWarningCount(earlyWarningDto);
        if(warningCount.size()==0){
            return new Result<>(true, StatusCode.OK, "查询成功,无预警信息");
        }
        earlyWarningDto.setWarningType(warningCount.get(0).getWarningType());
        int a=0,b=0,c=0,d=0,f=0,e=0,g=0,h=0,j=0;
        for (WarningCountVo warningCountVo : warningCount) {
            String warningType = warningCountVo.getWarningType();
            switch (warningType){
                case "01":
                    warningCountVo.setWarningType(WarningTypeDefault.oneType);
                    a =1;
                    break;
                case "02":
                    warningCountVo.setWarningType(WarningTypeDefault.twoType);
                    b=2;
                    break;
                case "03":
                    warningCountVo.setWarningType(WarningTypeDefault.threeType);
                    c=3;
                    break;
                case "04":
                    warningCountVo.setWarningType(WarningTypeDefault.fourType);
                    d=4;
                    break;
                case "05":
                    warningCountVo.setWarningType(WarningTypeDefault.fiveType);
                    f=5;
                    break;
                case "06":
                    warningCountVo.setWarningType(WarningTypeDefault.sixType);
                    e=6;
                    break;
                case "07":
                    warningCountVo.setWarningType(WarningTypeDefault.sevenType);
                    g=7;
                    break;
                case "08":
                    warningCountVo.setWarningType(WarningTypeDefault.eightType);
                    h=8;
                    break;
                case "09":
                    warningCountVo.setWarningType(WarningTypeDefault.nineType);
                    j=9;
                    break;
                default:
                    warningCountVo.setWarningType("");
                    break;
            }
        }
        WarningCountVo warningCountVo = null;
        if(a==0){
            warningCountVo = new WarningCountVo();
            warningCountVo.setWarningType(WarningTypeDefault.oneType);
            warningCount.add(warningCountVo);
        }
        if(b==0){
            warningCountVo = new WarningCountVo();
            warningCountVo.setWarningType(WarningTypeDefault.twoType);
            warningCount.add(warningCountVo);
        }
        if(c==0){
            warningCountVo = new WarningCountVo();
            warningCountVo.setWarningType(WarningTypeDefault.threeType);
            warningCount.add(warningCountVo);
        }
        if(d==0){
            warningCountVo = new WarningCountVo();
            warningCountVo.setWarningType(WarningTypeDefault.fourType);
            warningCount.add(warningCountVo);
        }
        if(e==0){
            warningCountVo = new WarningCountVo();
            warningCountVo.setWarningType(WarningTypeDefault.fiveType);
            warningCount.add(warningCountVo);
        }
        if(f==0){
            warningCountVo = new WarningCountVo();
            warningCountVo.setWarningType(WarningTypeDefault.sixType);
            warningCount.add(warningCountVo);
        }
        if(g==0){
            warningCountVo = new WarningCountVo();
            warningCountVo.setWarningType(WarningTypeDefault.sevenType);
            warningCount.add(warningCountVo);
        }
        if(h==0){
            warningCountVo = new WarningCountVo();
            warningCountVo.setWarningType(WarningTypeDefault.eightType);
            warningCount.add(warningCountVo);
        }
        if(j==0){
            warningCountVo = new WarningCountVo();
            warningCountVo.setWarningType(WarningTypeDefault.nineType);
            warningCount.add(warningCountVo);
        }
        warningCountVo = null;
        WarningVo warningVo = new WarningVo();
        warningVo.setWarningCount(warningCount);
        List<EarlyWarningVo> earlyWarningVos = earlyWarningDao.findEarlyWarning(earlyWarningDto);
        for (EarlyWarningVo earlyWarning : earlyWarningVos) {
            earlyWarning.setWarningType(warningCount.get(0).getWarningType());
            String affirm = earlyWarning.getAffirm();
            switch (affirm){
                case "2":
                    earlyWarning.setAffirm(WarningTypeDefault.unconfirmed);
                    break;
                case "1":
                    earlyWarning.setAffirm(WarningTypeDefault.alerted);
                    break;
                case "0":
                    earlyWarning.setAffirm(WarningTypeDefault.misstate);
                    break;
                default:
                    earlyWarning.setAffirm("");
                    break;
            }
            String warningGrade = earlyWarning.getWarningGrade();
            switch (warningGrade){
                case "1":
                    earlyWarning.setWarningGrade(WarningTypeDefault.oneWanring);
                    break;
                case "2":
                    earlyWarning.setWarningGrade(WarningTypeDefault.twoWanring);
                    break;
                case "3":
                    earlyWarning.setWarningGrade(WarningTypeDefault.threeWanring);
                    break;
                default:
                    earlyWarning.setWarningGrade("");
                    break;
            }

        }
        warningVo.setEarlyWarning(earlyWarningVos);
        return new Result<>(true, StatusCode.OK, "查询成功",warningVo);
    }


}
