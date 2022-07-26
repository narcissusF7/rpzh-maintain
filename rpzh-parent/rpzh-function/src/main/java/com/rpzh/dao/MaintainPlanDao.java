package com.rpzh.dao;

import com.rpzh.dto.MaintainCountDto;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.vo.MaintainCountVo;
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

    List<MaintainPlanVo> findMaintainPlanInfo(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    MaintainPlanVo findMaintainPlan(@Param("id") Integer id);

    Integer updateMaintainPlan(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    Integer updateMaintainPlanStage(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    Integer insertMaintainPlan(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    Integer insertMaintainStage(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    Integer findStageIdById(@Param("id") Integer id);

    Integer findIdByStackId(@Param("stackId") Integer stackId);

    int updateMaintainPlanStageId(@Param("stageId") Integer stageId, @Param("id") Integer id);

    List<MaintainPlanVo> findMaintainCountReport(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    List<MaintainPlanVo> findMaintainMonthEndReport(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    List<MaintainPlanVo> findMaintainHistoryCollect(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    Integer endMaintainPlan(@Param("updateUser") String updateUser, @Param("updatedTime") Date updateTime, @Param("maintainPlanId") Integer maintainPlanId, @Param("maintainDays") long maintainDays);

    List<Integer> findAlarmValueSettingId(@Param("ids") List<Integer> ids);

    MaintainPlanVo findVersion(@Param("stageId") Integer stageId);

    Integer findStageId(@Param("id") Integer id, @Param("version") Integer version);

    Integer updateChange(@Param("id") Integer id, @Param("change") String change);

    Integer updateStageId(@Param("id") Integer id, @Param("stageId") Integer stageId);

    List<MaintainPlanVo> findDetails(@Param("id") Integer id);

    MaintainPlanVo findDetailsNew(@Param("id") Integer id);

    int changeMaintainPlanStage(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    List<MaintainPlanVo> findExpireCollectTable(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    Integer isExistStackId(@Param("stackId") Integer stackId);

    Integer updateStackByStackId(@Param("id") Integer id, @Param("stackId") Integer stackId);

    Integer findStackCountByStackId(Integer stackId);

    MaintainPlanVo findMaintainStackId(@Param("stackId") Integer stackId);

    List<Integer> findId(@Param("ids") List<Integer> ids);

    int insertMaintainCount(@Param("maintainPlanDtos") List<MaintainPlanDto> maintainPlanDtos);

    int deleteMaintainCount(@Param("id") Integer id);

    List<MaintainCountVo> findMaintainCountBySiteName(@Param("maintainCountDto") MaintainCountDto maintainCountDto, @Param("tableColumn") String tableColumn);

    List<MaintainCountVo> findFirst(@Param("maintainCountDto") MaintainCountDto maintainCountDto);

    Double findCount(@Param("maintainCountDto") MaintainCountDto maintainCountDto, @Param("tableColumn") String tableColumn);

    List<String> findPhases(@Param("stackId") Integer stackId, @Param("maintenceStyle") String maintenceStyle);

    String findPrincipalId(@Param("stackId") Integer stackId);

    MaintainPlanVo findMaintainPlanByStack(@Param("stackId") Integer stackId);

    int updateMaintainCount(@Param("maintainPlanDto") MaintainPlanDto maintainPlanDto);

    List<MaintainCountVo> findTwo(@Param("maintainCountDto") MaintainCountDto maintainCountDto, @Param("siteIds") Set<Integer> siteIds);

    List<MaintainCountVo> findThree(@Param("maintainCountDto") MaintainCountDto maintainCountDto, @Param("siteIds") Set<Integer> siteIds);

    List<MaintainCountVo> findFour(@Param("maintainCountDto") MaintainCountDto maintainCountDto, @Param("siteIds") Set<Integer> siteIds);

    List<MaintainCountVo> findFive(@Param("maintainCountDto") MaintainCountDto maintainCountDto, @Param("siteIds") Set<Integer> siteIds);

    List<String> findSite(@Param("materialCode") String materialCode);

    List<Map<String, String>> findUsableStack();

    String findMaintainMaxIdByStackId(@Param("stackId") Integer stackId);

    Date findCreatedTime(@Param("maintainPlanId") Integer maintainPlanId);

    Integer findMaintainDays(@Param("id") Integer id);

    MaintainPlanVo findMaintainHistoryById(@Param("id") Integer id);

    MaintainPlanVo findMaintainPlanNow(@Param("id") Integer id,@Param("time")String time);
}
