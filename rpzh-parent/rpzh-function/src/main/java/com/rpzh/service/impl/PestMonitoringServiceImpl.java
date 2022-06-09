package com.rpzh.service.impl;

import com.rpzh.constant.NodeConstant;
import com.rpzh.dao.GeneralSettingDao;
import com.rpzh.dao.PestMonitoringDao;
import com.rpzh.dto.PestBoardDto;
import com.rpzh.dto.PestMonitoringRuleDto;
import com.rpzh.pojo.GeneralSetting;
import com.rpzh.service.PestMonitoringService;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.PestMonitoringRuleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PestMonitoringServiceImpl implements PestMonitoringService {

    @Autowired
    PestMonitoringDao pestMonitoringDao;

    @Autowired
    GeneralSettingDao generalSettingDao;

    /**
     * 添加虫情板
     * @param pestBoardDto
     * @return
     */
    @Override
    public Integer addPestBoard(PestBoardDto pestBoardDto) {
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        pestBoardDto.setCreatedUser(username);
        //根据选择的位置的路由id  判断是仓间还是垛内设备
        Integer siteLevel = pestBoardDto.getSiteRoute().split(",").length;
        pestBoardDto.setSiteType(siteLevel==4? NodeConstant.NODE_WARE_AREA:NodeConstant.NODE_STACK);
        //拼接成自己的路由id
        pestBoardDto.setSiteRoute(pestBoardDto.getSiteRoute()+",[0"+siteLevel+":"+pestBoardDto.getSiteId()+"]");
        Integer addFalg = pestMonitoringDao.addPestBoard(pestBoardDto);
        return addFalg;
    }

    /**
     * 删除虫情板
     * @param ids
     * @return
     */
    @Override
    public Integer deletePestBoard(Integer[] ids) {
        Integer delFalg = pestMonitoringDao.deletePestBoard(ids);
        return delFalg;
    }

    /**
     * 修改虫情规则配置
     * @param pestMonitoringRuleDto
     * @return
     */
    @Override
    public Integer updatePestRule(PestMonitoringRuleDto pestMonitoringRuleDto) {
        //虫情预警值 预警颜色修改
        Integer updateFlag =  pestMonitoringDao.updatePestMonitor(pestMonitoringRuleDto);

        //虫情板替换周期修改  该值在通用表中
        GeneralSetting generalSetting = new GeneralSetting();
        generalSetting.setSettingName("replacePeriod");
        generalSetting.setSettingValue(pestMonitoringRuleDto.getReplacePeriod()+"");
        generalSettingDao.updateGeneralSetting(generalSetting);
        //虫情巡查时间修改
        Integer[] weekPatrol = pestMonitoringRuleDto.getWeekPatrol();
        Map<Integer,Integer> map = new HashMap<>();
        for (int i = 0; i < weekPatrol.length; i++) {
            map.put(i+1,weekPatrol[i]);
        }
        pestMonitoringDao.updateWeekPatrol(map);

        //特殊垛位删除
        pestMonitoringDao.delSpecialStack(Arrays.asList(pestMonitoringRuleDto.getDelIds()));

        //特殊垛位 如果数据库存在  就修改 否则就添加
        Integer[] stackIds = pestMonitoringRuleDto.getStackIds();
        List<Integer> list = pestMonitoringDao.findSpecialStackId(Arrays.asList(stackIds));
        for (int i = 0; i < stackIds.length; i++) {
            String[] split = pestMonitoringRuleDto.getStackWarningValues()[i].split("|");
            if (list.contains(stackIds[i])){
                pestMonitoringDao.updateSpecialStack(stackIds[i],split[0],split[1],split[2]);
            }else {
                pestMonitoringDao.addSpecialStack(stackIds[i],split[0],split[1],split[2]);
            }
        }
        return updateFlag;
    }

    @Override
    public PestMonitoringRuleVo findPestRule() {


        //虫情巡查时间查询
        Map<String, Integer> weekPatrol = pestMonitoringDao.findWeekPatrol();

        //虫情板替换周期查询
        GeneralSetting generalSetting = new GeneralSetting();
        generalSetting.setSettingName("replacePeriod");
        Integer settingValue = Integer.parseInt(generalSettingDao.findGeneralSetting(generalSetting).get(0).getSettingValue());

        //虫情预警值 预警颜色查询
        Map<String,String> pestMonitorValues =  pestMonitoringDao.findPestMonitorValue();

        //查询特殊垛位
        List<Map<String ,String>> specialStacks = pestMonitoringDao.findSpecialStack();

        PestMonitoringRuleVo pestMonitoringRuleVo = new PestMonitoringRuleVo(weekPatrol,settingValue,pestMonitorValues,specialStacks);
        return pestMonitoringRuleVo;
    }

}
