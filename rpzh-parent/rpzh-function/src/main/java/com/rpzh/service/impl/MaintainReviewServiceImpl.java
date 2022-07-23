package com.rpzh.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.MaintainPlanDao;
import com.rpzh.dao.MaintainReviewDao;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.entity.ToolUtil;
import com.rpzh.feign.UserFeign;
import com.rpzh.pojo.EarlyWarning;
import com.rpzh.pojo.NodeData;
import com.rpzh.service.MaintainReviewService;
import com.rpzh.util.GetBetweenDayUtils;
import com.rpzh.vo.EarlyWarningVo;
import com.rpzh.vo.MaintainPlanVo;
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
    @Resource
    private UserFeign userFeign;
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
                column = "t3.pid_route like concat('%',#{maintainPlanDto.pidRoute},'%')";
            }
        }
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        List<MaintainPlanVo> maintainPlanVos = maintainReviewDao.findReviewTable(maintainPlanDto, column);
        maintainPlanVos.forEach(maintainPlanVo -> {
            List<String> principalIds = Arrays.asList(maintainPlanVo.getPrincipalId().split(","));
            List<Integer> principalId = principalIds.stream().map(Integer::parseInt).collect(Collectors.toList());
            maintainPlanVo.setPrincipalName(String.join(",",userFeign.findUsernameById(principalId)));
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
        String tableName = "";
        String columnName = "";
        if("01".equals(tableType)){
            tableName = "tb_node_o2_history";
            columnName = "oxygen";
        }else if("02".equals(tableType)){
            tableName = "tb_node_temp_core_chip_history";
            columnName = "temp";
        }else if("03".equals(tableType)){
            tableName = "tb_node_env_history";
            columnName = "temp";
        }else{
            tableName = "tb_node_env_history";
            columnName = "humiture";
        }
        List<Map> dataReviews = maintainReviewDao.findDataReview(tableName, columnName, id, startTime, endTime);
        List<String> day = dataReviews.stream().map((Map map) -> map.get("day").toString()).collect(Collectors.toList());
        if(StringUtil.isNotEmpty(startTime) || StringUtil.isNotEmpty(endTime)){
            List<String> months = GetBetweenDayUtils.getMonthBetweenDate(startTime, endTime);
            months.forEach(month -> {
                if(!day.contains(month)){
                    Map map = new HashMap();
                    map.put("avgData",0);
                    map.put("minData",0);
                    map.put("maxData",0);
                    map.put("day",month);
                    dataReviews.add(map);
                }
            });
        }
        dataReviews.sort((v1, v2) -> v1.get("day").toString().compareTo(v2.get("day").toString()));
        return dataReviews;
    }

    @Override
    public MaintainPlanVo findMaintainBaseInfo(Integer id) {
        MaintainPlanVo maintainBaseInfo = maintainReviewDao.findMaintainBaseInfo(id);
        List<String> principalIds = Arrays.asList(maintainBaseInfo.getPrincipalId().split(","));
        List<Integer> principalId = principalIds.stream().map(Integer::parseInt).collect(Collectors.toList());
        maintainBaseInfo.setPrincipalName(String.join(",",userFeign.findUsernameById(principalId)));
        return maintainBaseInfo;
    }
}
