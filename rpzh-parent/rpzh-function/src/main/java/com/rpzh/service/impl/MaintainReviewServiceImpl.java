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
import com.rpzh.vo.MaintainPlanVo;
import org.springframework.stereotype.Service;

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
    private MaintainPlanDao maintainPlanDao;
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
        System.out.println(maintainPlanInfos);
        Set<Integer> stageIds = maintainPlanInfos.stream().map(MaintainPlanVo::getId).collect(Collectors.toSet());
        List<Double> oxygenData = maintainReviewDao.findOxygenOrCoreData("oxygen", "tb_node_o2_history", stageIds);
        System.out.println(oxygenData);
        List<Double> coreData = maintainReviewDao.findOxygenOrCoreData("temp", "tb_node_temp_core_chip_history", stageIds);
        System.out.println(coreData);
        List<Map<String, Double>> envData = maintainReviewDao.findEnvData(stageIds);
        System.out.println(envData);
        for (int i = 0; i < maintainPlanInfos.size(); i++) {
            maintainPlanInfos.get(i).setStackOxygenNow(oxygenData.get(i));
            maintainPlanInfos.get(i).setCoreTempHighEnvTempNow(coreData.get(i));
            maintainPlanInfos.get(i).setStackTempNow(envData.get(i).get("temp"));
            maintainPlanInfos.get(i).setStackMoistureNow(envData.get(i).get("humiture"));
        }
        return maintainPlanInfos;
    }

    @Override
    public List<EarlyWarning> findEarlyWarningReview(Integer id) {
        Integer stackId = maintainPlanDao.findMaintainPlan(id).getStackId();
        System.out.println(stackId);
        return null;
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
