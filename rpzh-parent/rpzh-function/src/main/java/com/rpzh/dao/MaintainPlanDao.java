package com.rpzh.dao;

import com.rpzh.dto.MaintainCountDto;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.vo.MaintainCountVo;
import com.rpzh.vo.StackVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.rpzh.vo.MaintainPlanVo;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Map;

@Mapper
public interface MaintainPlanDao {
    int deleteMaintainPlanById(@Param("ids") List<Integer> ids);

    int deleteMaintainPlanStageById(@Param("ids") List<Integer> ids);

    List<MaintainPlanVo> findMaintainPlanInfo(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto,@Param("stackIds")List<Integer> stackIds,@Param("size")Integer size);

    MaintainPlanVo findMaintainPlan(@Param("id") Integer id);

    MaintainPlanVo beforeChangeMaintainPlan(@Param("id") Integer id);

    Integer updateMaintainPlan(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    Integer updateMaintainPlanStage(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    Integer insertMaintainPlan(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    Integer insertMaintainStage(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    MaintainPlanVo findStageIdById(@Param("id") Integer id);

    int updateMaintainPlanStageId(@Param("id") Integer id,@Param("stageId")Integer stageId);

    List<MaintainPlanVo> findMaintainMonthEndReport(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto,@Param("stackIds")List<Integer> stackIds,@Param("size")Integer size);

    List<MaintainPlanVo> findMaintainHistoryCollect(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto,@Param("stackIds")List<Integer> stackIds,@Param("size")Integer size);

    Integer endMaintainPlan(@Param("maintainPlanId") Integer maintainPlanId);

    Integer endMaintainPlanStage(@Param("updateUser") String updateUser, @Param("maintainPlanId") Integer maintainPlanId, @Param("maintainDays") long maintainDays);

    List<Map> findAlarmValueSettingId(@Param("ids") List<Integer> ids);

    List<MaintainPlanVo> findDetails(@Param("id") Integer id);

    MaintainPlanVo findDetailsNew(@Param("id") Integer id);

    int changeMaintainPlanStage(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    int changeMaintainPlan(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    List<MaintainPlanVo> findExpireCollectTable(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto,@Param("stackIds")List<Integer> stackIds,@Param("size")Integer size);

    Integer isExistStackId(@Param("stackId") Integer stackId);

    Integer updateStackByStackId(@Param("id") Integer id, @Param("stackId") Integer stackId);

    Integer findStackCountByStackId(Integer stackId);

    MaintainPlanVo findMaintainStackId(@Param("stackId") Integer stackId);

    List<MaintainPlanVo> findPidRoute(@Param("ids") List<Integer> ids);

    int insertMaintainCount(@Param("maintainPlanDtos") List<MaintainPlanDto> maintainPlanDtos);

    int deleteMaintainCount(@Param("ids") List<Integer> ids);

    List<MaintainCountVo> findMaintainCountBySiteName(@Param("maintainCountDto") MaintainCountDto maintainCountDto, @Param("tableColumn") String tableColumn);

    List<MaintainCountVo> findFirst(@Param("ids")List<Integer> ids);

    Double findCount(@Param("maintainCountDto") MaintainCountDto maintainCountDto, @Param("tableColumn") String tableColumn);

    List<String> findPhases(@Param("stackId") Integer stackId, @Param("maintenceStyle") String maintenceStyle);

    int updateMaintainCount(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    List<MaintainCountVo> findTwo(@Param("ids")List<Integer> ids,@Param("siteIds") Set<Integer> siteIds);

    List<MaintainCountVo> findThree(@Param("ids")List<Integer> ids, @Param("siteIds") Set<Integer> siteIds);

    List<MaintainCountVo> findFour(@Param("ids")List<Integer> ids, @Param("siteIds") Set<Integer> siteIds);

    List<MaintainCountVo> findFive(@Param("ids")List<Integer> ids, @Param("siteIds") Set<Integer> siteIds);

    List<String> findSite(@Param("materialCode") String materialCode);

    List<Map<String, String>> findUsableStack();

    String findMaintainMaxIdByStackId(@Param("stackId") Integer stackId);

    Date findCreatedTime(@Param("maintainPlanId") Integer maintainPlanId);

    Integer findMaintainDays(@Param("id") Integer id);

    MaintainPlanVo findMaintainHistoryById(@Param("id") Integer id);

    MaintainPlanVo findMaintainPlanNow(@Param("stackId") Integer stackId);

    String findCurrentMaintainPlan(@Param("stackId")Integer stackId);

    Integer findCountBySiteRoute(@Param("siteRoutes")List<String> siteRoutes);

    List<StackVo> findStackNotInMaintainPlan();

    List<Integer> findStackIdBySiteRoute(@Param("siteRoute")String siteRoute);

    Integer findCountByMaintainPlan();

    List<Integer> findIds(@Param("maintainCountDto")MaintainCountDto maintainCountDto);

    String findPrincipalById(@Param("maintainPlanId") Integer maintainPlanId);
}
