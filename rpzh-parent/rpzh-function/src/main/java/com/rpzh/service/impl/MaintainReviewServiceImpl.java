package com.rpzh.service.impl;

import cn.hutool.core.date.DateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.MaintainReviewDao;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.MaintainReviewService;
import com.rpzh.util.*;
import com.rpzh.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.util.StringUtil;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: zyq
 * @Date: 2022/07/16/16:45
 * @Description: 功能
 */
@Service
public class MaintainReviewServiceImpl implements MaintainReviewService {
    @Resource
    private MaintainReviewDao maintainReviewDao;
    //redis中用户key前缀
    private static final String USER = "USER_ID_";
    @Autowired
    private RedisUtil redisUtil;
    @Override
    public PageInfo<MaintainPlanVo> findReviewTable(MaintainPlanDto maintainPlanDto) {
        String pidRoute = maintainPlanDto.getPidRoute();
        String column = "";
        if(ToolUtil.isNotEmpty(pidRoute)){
            if(pidRoute.contains("04")){
                column = "t3.id = #{maintainPlanDto.stackId}";
            }else if(!pidRoute.contains("04") && pidRoute.contains("03")){
                column = "t3.area_id = #{maintainPlanDto.stackId}";
            }else{
                maintainPlanDto.setPidRoute(maintainPlanDto.getPidRoute() + ",[0"+maintainPlanDto.getPidRoute().split(",").length+":" + maintainPlanDto.getStackId() + "]");
                column = "t3.pid_route like concat(concat('%',#{maintainPlanDto.pidRoute}),'%')";
            }
        }
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        List<MaintainPlanVo> maintainPlanVos = maintainReviewDao.findReviewTable(maintainPlanDto, column);
        maintainPlanVos.forEach(maintainPlanVo -> {
            List<String> names = Arrays.asList(maintainPlanVo.getPrincipalName().split(","));
            String userName = "";
            for (int i = 0; i < names.size(); i++) {
                if(i==0){
                    userName = (String)redisUtil.hget("USER_"+names.get(i),"nickname") + "(" + names.get(i) + ")";
                }else{
                    userName = userName + ","+ (String)redisUtil.hget("USER_"+names.get(i),"nickname") + "(" + names.get(i) + ")";
                }
            }
            maintainPlanVo.setPrincipalName(userName);
        });
        return new PageInfo<>(maintainPlanVos);
    }

    @Override
    public List<MaintainPlanVo> findMaintainPlanReview(Integer id) {
        List<MaintainPlanVo> maintainPlanInfos = maintainReviewDao.findMaintainPlanReview(id);
        Set<Integer> stageIds = maintainPlanInfos.stream().map(MaintainPlanVo::getId).collect(Collectors.toSet());
        List<Double> oxygenData = maintainReviewDao.findOxygenOrCoreData("oxygen", "tb_node_o2_history", stageIds);
        List<Double> coreData = maintainReviewDao.findOxygenOrCoreData("temp", "tb_node_temp_core_chip_history", stageIds);
        List<Map<String, Double>> envData = maintainReviewDao.findEnvData(stageIds);
        for (int i = 0; i < maintainPlanInfos.size(); i++) {
            maintainPlanInfos.get(i).setStackOxygenNow(oxygenData.get(i));
            maintainPlanInfos.get(i).setCoreTempHighEnvTempNow(coreData.get(i));
            maintainPlanInfos.get(i).setStackTempNow(envData.get(i).get("temp"));
            maintainPlanInfos.get(i).setStackMoistureNow(envData.get(i).get("humiture"));
        }
        return maintainPlanInfos;
    }

    @Override
    public List<EarlyWarningVo> findEarlyWarningReview(Integer id) {
        MaintainPlanVo maintainPlanVo = maintainReviewDao.findStackIdAndTime(id);
        List<EarlyWarningVo> earlyWarningReview = maintainReviewDao.findEarlyWarningReview(maintainPlanVo);
        return earlyWarningReview;
    }

    @Override
    public List<Map> findDataReview(String tableType,Integer id,String startTime,String endTime) {
        if(ToolUtil.isEmpty(startTime) || ToolUtil.isEmpty(endTime)){
            startTime = DateUtil.offsetMonth(new Date(), -4).toString();
            endTime = DateUtil.offsetMonth(new Date(), -1).toString();
        }
        String tableName = "";
        String columnName = "";
        List<Map> dataReviews = new ArrayList<>();
        if("01".equals(tableType)){
            dataReviews = maintainReviewDao.findDataReview("tb_node_o2_history","oxygen", "maintain_plan_id = '"+id +"'", startTime, endTime);
        }else if("02".equals(tableType)){
            dataReviews = maintainReviewDao.findDataReview("tb_node_temp_core_chip_history","temp", "maintain_plan_id = '"+id +"'", startTime, endTime);
        }else if("03".equals(tableType)){
            dataReviews = maintainReviewDao.findDataReview("tb_node_env_history","temp", "maintain_plan_id = '"+id +"'", startTime, endTime);
        }else if("04".equals(tableType)){
            dataReviews = maintainReviewDao.findDataReview("tb_node_env_history","humiture", "maintain_plan_id = '"+id +"'", startTime, endTime);
        }else if("05".equals(tableType)){
            Integer areaId = maintainReviewDao.findAreaId(id);
            dataReviews = maintainReviewDao.findDataReview("tb_air_conditioner_history","humiture", "stack_id = '"+areaId +"'", startTime, endTime);
        }else{
            Integer areaId = maintainReviewDao.findAreaId(id);
            dataReviews = maintainReviewDao.findDataReview("tb_air_conditioner_history","temp", "stack_id = '"+areaId +"'", startTime, endTime);
        }
        List<String> day = dataReviews.stream().map((Map map) -> map.get("day").toString()).collect(Collectors.toList());
        if(StringUtil.isNotEmpty(startTime) || StringUtil.isNotEmpty(endTime)){
            List<String> months = GetBetweenDayUtils.getMonthBetweenDate(startTime, endTime);
            for (String month : months) {
                if(!day.contains(month)){
                    Map map = new HashMap();
                    map.put("avgData",0);
                    map.put("minData",0);
                    map.put("maxData",0);
                    map.put("day",month);
                    dataReviews.add(map);
                }
            }
        }
        dataReviews.sort((v1, v2) -> v1.get("day").toString().compareTo(v2.get("day").toString()));
        return dataReviews;
    }

    @Override
    public MaintainPlanVo findMaintainBaseInfo(Integer id) {
        MaintainPlanVo maintainBaseInfo = maintainReviewDao.findMaintainBaseInfo(id);
        List<String> names = Arrays.asList(maintainBaseInfo.getPrincipalName().split(","));
        String userName = "";
        for (int i = 0; i < names.size(); i++) {
            if(i==0){
                userName = (String)redisUtil.hget("USER_"+names.get(i),"nickname") + "(" + names.get(i) + ")";
            }else{
                userName = userName + ","+ (String)redisUtil.hget("USER_"+names.get(i),"nickname") + "(" + names.get(i) + ")";
            }
        }
        maintainBaseInfo.setPrincipalName(userName);
        return maintainBaseInfo;
    }
}
