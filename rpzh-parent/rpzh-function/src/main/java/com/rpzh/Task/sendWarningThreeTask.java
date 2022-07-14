package com.rpzh.Task;

/**
 * @Author: zyq
 * @Date: 2022/06/16/16:27
 * @Description: 三级每两个小时推送一次至任务结束
 */
import com.github.pagehelper.util.StringUtil;
import com.rpzh.dao.EarlyWarningDao;
import com.rpzh.dao.MaintainPlanDao;
import com.rpzh.dao.WarehouseDao;
import com.rpzh.entity.ToolUtil;
import com.rpzh.feign.UserFeign;
import com.rpzh.util.SendMessageUtil;
import com.rpzh.util.StringUtils;
import com.rpzh.vo.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Data
@Slf4j
@Component
@EnableAsync
public class sendWarningThreeTask{

    @Resource
    private EarlyWarningDao earlyWarningDao;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private WarehouseDao warehouseDao;

    @Resource
    private UserFeign userFeign;

    @Resource
    private MaintainPlanDao maintainPlanDao;

    @Scheduled(cron="0 0 0/2 * * ? ")
    public void configureTasks() {
        List<Integer> stack = earlyWarningDao.findStack();
        for (Integer stackId : stack) {
            List<EarlyWarningVo> earlyWarningVos = earlyWarningDao.findSendStackByWarningThree(stackId);
            if(earlyWarningVos.size()==0){
                return;
            }
            String siteName = warehouseDao.findStackById(stackId).getSiteName();
            EarlyWarningVo earlyWarningVo = earlyWarningVos.get(0);
            String info = "位置:" + siteName +" " + getWarningType(earlyWarningVo.getWarningType()) + " "  + getWarningGrade(earlyWarningVo.getWarningGrade()) + " "+ "当前值 " + earlyWarningVo.getWarningVal();
            if(ToolUtil.isNotEmpty(earlyWarningVo.getNodeId())){
                info = "位置:" + siteName +" " + getWarningType(earlyWarningVo.getWarningType()) + " " + getNodeType(earlyWarningVo.getNodeType())+" " + getWarningGrade(earlyWarningVo.getWarningGrade()) + " 当前值 " + earlyWarningVo.getNodeId() + " " +  earlyWarningVo.getWarningVal();
            }
            for (int i = 1; i < earlyWarningVos.size(); i++) {
                if(earlyWarningVos.get(i-1).getWarningType().equals(earlyWarningVos.get(i).getWarningType())){
                    if(ToolUtil.isNotEmpty(earlyWarningVos.get(i).getNodeId())){
                        if(earlyWarningVos.get(i-1).getWarningGrade().equals(earlyWarningVos.get(i).getWarningGrade())) {
                            info = info + ";" + earlyWarningVos.get(i).getNodeId() + " " + earlyWarningVos.get(i).getWarningVal();
                        }else{
                            info = info + ";" + getWarningGrade(earlyWarningVos.get(i).getWarningGrade()) + " 当前值 " + earlyWarningVos.get(i).getNodeId() + " " + earlyWarningVos.get(i).getWarningVal();
                        }
                    }else{
                        info = info + ";" + earlyWarningVos.get(i).getWarningVal();
                    }
                }else{
                    if(ToolUtil.isNotEmpty(earlyWarningVos.get(i).getNodeId())) {
                        info = info + "   " + getWarningType(earlyWarningVos.get(i).getWarningType()) + " " + getNodeType(earlyWarningVos.get(i).getNodeType()) + " " + getWarningGrade(earlyWarningVos.get(i).getWarningGrade()) + " 当前值 " + earlyWarningVos.get(i).getNodeId() + " " + earlyWarningVos.get(i).getWarningVal();
                    }else{
                        info = info + "   " + getWarningType(earlyWarningVos.get(i).getWarningType()) + " "  + getWarningGrade(earlyWarningVos.get(i).getWarningGrade()) + " 当前值 " + earlyWarningVos.get(i).getWarningVal();
                    }
                }
            }
            log.info(info);
            String principalId = maintainPlanDao.findPrincipalId(stackId);
            if(StringUtil.isNotEmpty(principalId)) {
                String number = userFeign.findPhoneNumberById(principalId).getData().toString();
                /*MessageVo messageVo = SendMessageUtil.sendSms(number, "每天第一个固定时间点推送短信", info);
                //当为0的时候发送成功
                log.info(messageVo.getResult());*/
            }
        }
        List<Integer> area = earlyWarningDao.findArea();
        for (Integer areaId : area) {
            List<EarlyWarningVo> earlyWarningVos = earlyWarningDao.findSendStackByWarningThree(areaId);
            if(earlyWarningVos.size()==0){
                return;
            }
            String siteName = warehouseDao.findWareArea(areaId).getSiteName();
            EarlyWarningVo earlyWarningVo = earlyWarningVos.get(0);
            String info = "位置:" + siteName +" " + getWarningType(earlyWarningVo.getWarningType()) + " "  + getWarningGrade(earlyWarningVo.getWarningGrade()) + " "+ "当前值 " + earlyWarningVo.getWarningVal();
            if(ToolUtil.isNotEmpty(earlyWarningVo.getNodeId())){
                info = "位置:" + siteName +" " + getWarningType(earlyWarningVo.getWarningType()) + " " + getNodeType(earlyWarningVo.getNodeType())+" " + getWarningGrade(earlyWarningVo.getWarningGrade()) + " 当前值 " + earlyWarningVo.getNodeId() + " " +  earlyWarningVo.getWarningVal();
            }
            for (int i = 1; i < earlyWarningVos.size(); i++) {
                if(earlyWarningVos.get(i-1).getWarningType().equals(earlyWarningVos.get(i).getWarningType())){
                    if(ToolUtil.isNotEmpty(earlyWarningVos.get(i).getNodeId())){
                        if(earlyWarningVos.get(i-1).getWarningGrade().equals(earlyWarningVos.get(i).getWarningGrade())) {
                            info = info + ";" + earlyWarningVos.get(i).getNodeId() + " " + earlyWarningVos.get(i).getWarningVal();
                        }else{
                            info = info + ";" + getWarningGrade(earlyWarningVos.get(i).getWarningGrade()) + " 当前值 " + earlyWarningVos.get(i).getNodeId() + " " + earlyWarningVos.get(i).getWarningVal();
                        }
                    }else{
                        info = info + ";" + earlyWarningVos.get(i).getWarningVal();
                    }
                }else {
                    if(ToolUtil.isNotEmpty(earlyWarningVos.get(i).getNodeId())) {
                        info = info + "   " + getWarningType(earlyWarningVos.get(i).getWarningType()) + " " + getNodeType(earlyWarningVos.get(i).getNodeType()) + " " + getWarningGrade(earlyWarningVos.get(i).getWarningGrade()) + " 当前值 " + earlyWarningVos.get(i).getNodeId() + " " + earlyWarningVos.get(i).getWarningVal();
                    }else{
                        info = info + "   " + getWarningType(earlyWarningVos.get(i).getWarningType()) + " "  + getWarningGrade(earlyWarningVos.get(i).getWarningGrade()) + " 当前值 " + earlyWarningVos.get(i).getWarningVal();
                    }
                }
            }
            log.info(info);
            Integer principalId=0;
            WareAreaVo wareAreaVo = warehouseDao.findWareAreaPrincipalName(areaId);
            if (!ToolUtil.isEmpty(wareAreaVo.getPrincipalId())) {
                principalId = wareAreaVo.getPrincipalId();
            }
            FloorVo floorVo = warehouseDao.findFloorPrincipalName(wareAreaVo.getFloorId());
            if (StringUtils.isEmpty(wareAreaVo.getPrincipalName()) && !StringUtils.isEmpty(floorVo.getPrincipalName())) {
                principalId = floorVo.getPrincipalId();
            }
            BuildingVo buildingVo = warehouseDao.findBuildingPrincipalName(floorVo.getBuildingId());
            if (StringUtils.isEmpty(wareAreaVo.getPrincipalName()) && StringUtils.isEmpty(floorVo.getPrincipalName()) && !StringUtils.isEmpty(buildingVo.getPrincipalName())) {
                principalId = buildingVo.getPrincipalId();
            }
            WarehouseVo warehouseVo = warehouseDao.findWarehousePrincipalName(buildingVo.getWarehouseId());
            if (StringUtils.isEmpty(wareAreaVo.getPrincipalName()) && StringUtils.isEmpty(floorVo.getPrincipalName())
                    && StringUtils.isEmpty(buildingVo.getPrincipalName()) && !StringUtils.isEmpty(warehouseVo.getPrincipalName())) {
                principalId = buildingVo.getPrincipalId();
            }
            String number = userFeign.findPhoneNumberById(principalId+"").getData().toString();
            /*MessageVo messageVo = SendMessageUtil.sendSms(number, "每天第一个固定时间点推送短信", info);
            //当为0的时候发送成功
            log.info(messageVo.getResult());*/
        }
    }

    public String getWarningType(String type){
        switch (type){
            case "04": return "垛内甲虫预警";
            case "05": return "仓间甲虫预警";
            case "06": return "垛内氧气预警";
            case "07": return "仓间氧气预警";
            case "08": return "垛内温度预警";
            case "09": return "仓间温度预警";
            case "10": return "垛内湿度预警";
            case "11": return "仓间湿度预警";
            case "12": return "包芯温度预警";
            case "13": return "平均包芯高于环境预警";
            case "14": return "仓间粉螟预警";
            default: return "";
        }
    }
    public String getNodeType(String type){
        switch (type){
            case "01": return "氧气传感器";
            case "03": return "温湿度传感器";
            case "04": return "包芯温度传感器";
            default: return "";
        }
    }

    public String getWarningGrade(Integer warningGrade){
        switch (warningGrade){
            case 1: return "一级预警";
            case 2: return "二级预警";
            case 3: return "三级预警";
            default: return "";
        }
    }
}
