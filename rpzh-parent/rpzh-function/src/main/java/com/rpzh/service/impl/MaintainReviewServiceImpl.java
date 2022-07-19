package com.rpzh.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.MaintainReviewDao;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.entity.ToolUtil;
import com.rpzh.feign.UserFeign;
import com.rpzh.service.MaintainReviewService;
import com.rpzh.vo.MaintainPlanVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
    public MaintainPlanVo findMiantainBaseInfo(Integer id) {
        return null;
    }

    @Override
    public List<MaintainPlanVo> findMaintainPlanReview(Integer id) {
        List<MaintainPlanVo> maintainPlanInfos = maintainReviewDao.findMaintainPlanReview(id);
        Date startTime = maintainPlanInfos.get(0).getCreatedTime();
        Date endTime = maintainPlanInfos.get(maintainPlanInfos.size()-1).getCreatedTime();

        return maintainPlanInfos;
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
