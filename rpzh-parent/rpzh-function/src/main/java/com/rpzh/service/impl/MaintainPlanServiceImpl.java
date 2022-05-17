package com.rpzh.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.*;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.entity.AlertSettingSetDefault;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.MaintainPlanService;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.WarehouseVo;
import org.apache.xmlbeans.impl.piccolo.util.DuplicateKeyException;
import org.springframework.stereotype.Service;
import com.rpzh.pojo.AlarmValueSetting;
import com.rpzh.pojo.MaintainPlan;
import com.rpzh.vo.MaintainPlanVo;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MaintainPlanServiceImpl implements MaintainPlanService {

    @Resource
    private MaintainPlanDao maintainPlanDao;

    @Resource
    private AlarmValueSettingDao alarmValueSettingDao;

    @Resource
    private MaterialDao materialDao;

    @Resource
    private WarehouseDao warehouseDao;

    /**
     * 新增养护计划
     *
     * @param maintainPlanDto 养护计划信息
     * @return success or failure
     */
    @Override
    public int insertMaintainPlan(MaintainPlanDto maintainPlanDto){
        String createUser = TokenDecode.getUserInfo().get("username");
        AlarmValueSettingDto alarmValueSettingDto = maintainPlanDto.getAlarmValueSettingDto();
        maintainPlanDto.setCreateUser(createUser);
        maintainPlanDto.setCreatedTime(new Date());
        Integer alarmValueSettingId = maintainPlanDto.getAlarmValueSettingId();
        alarmValueSettingDto.setMaterialCodeYear(maintainPlanDto.getMaterialCodeYear());
        if(alarmValueSettingId!=null){
            alarmValueSettingDto.setUpdateUser(createUser);
            alarmValueSettingDto.setUpdatedTime(new Date());
            alarmValueSettingDao.updateAlarmValueSetting(alarmValueSettingDto,alarmValueSettingId);
        }else{
            alarmValueSettingDto.setCreateUser(createUser);
            alarmValueSettingDto.setCreatedTime(new Date());
            alarmValueSettingDto.setMaintenceStyle(maintainPlanDto.getMaintenceStyle());
            alarmValueSettingDto.setPhases(maintainPlanDto.getPhases());
            alarmValueSettingDto.setTobaccoYear(maintainPlanDto.getTobaccoYear());
            alarmValueSettingDto.setMaterialCode(maintainPlanDto.getMaterialCode());
            alarmValueSettingDao.insertAlarmValueSetting(alarmValueSettingDto);
            maintainPlanDto.setAlarmValueSettingId(alarmValueSettingDao.findMaxId());
        }
        Date date = new Date(maintainPlanDto.getAftercareDateStart().getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l);
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        maintainPlanDto.setAftercareDateEnd(date);
        maintainPlanDao.insertMaintainPlan(maintainPlanDto);
        Integer id =maintainPlanDao.findMaxPlanId();
        maintainPlanDto.setMaintainPlanId(id);
        maintainPlanDao.insertMaintainStage(maintainPlanDto);
        Integer stageId = maintainPlanDao.findMaxStageId();
        return maintainPlanDao.updateMaintainPlanStageId(stageId,id);
    }

    /**
     * 查询养护计划
     *
     * @param maintainPlanDto 养护计划信息
     * @return success or failure
     */
    @Override
    public PageInfo<MaintainPlanVo> findMaintainPlanInfo(MaintainPlanDto maintainPlanDto) {
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findMaintainPlanInfo(maintainPlanDto);
        return new PageInfo<>(maintainPlanVos);
    }

    /**
     * 删除养护计划
     *
     * @param stageIds 养护计划id
     * @return success or failure
     */
    @Override
    public Result deleteMaintainPlanInfo(List<Integer> stageIds) {
        for (Integer stageId : stageIds) {
            MaintainPlanVo maintainPlan = maintainPlanDao.findVersion(stageId);
            Integer version = maintainPlan.getVersion();
            Integer alarmValueSettingId = maintainPlan.getAlarmValueSettingId();
            Integer id = maintainPlan.getId();
            if(version == 1){
                maintainPlanDao.deleteMaintainPlan(stageId);
            }
            if(version != 1){
                version =version -1;
                Integer stageId1 = maintainPlanDao.findStageId(id, version);
                maintainPlanDao.updateChange(stageId1,"0");
                maintainPlanDao.updateStageId(id,stageId1);
            }
            maintainPlanDao.deleteMaintainPlanStage(stageId);
            alarmValueSettingDao.delete(alarmValueSettingId);
        }
        return new Result(true, StatusCode.OK, "删除成功!");
    }


    @Override
    public MaintainPlanVo findMaintainPlan(Integer id) {
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findMaintainPlan(id);
        Integer alarmValueSettingId = maintainPlanVo.getAlarmValueSettingId();
        AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingDao.findAlarmValueSettingById(alarmValueSettingId);
        maintainPlanVo.setAlarmValueSettingVo(alarmValueSettingVo);
        return maintainPlanVo;
    }

    @Override
    public Integer updateMaintainPlan(MaintainPlanDto maintainPlanDto) {
        String updateUser = TokenDecode.getUserInfo().get("username");
        maintainPlanDto.setUpdateUser(updateUser);
        maintainPlanDto.setUpdatedTime(new Date());
        Date date = new Date(maintainPlanDto.getAftercareDateStart().getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l);
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        maintainPlanDto.setAftercareDateEnd(date);
        maintainPlanDao.updateMaintainPlan(maintainPlanDto);
        maintainPlanDao.updateMaintainPlanStage(maintainPlanDto);
        AlarmValueSettingDto alarmValueSettingDto = maintainPlanDto.getAlarmValueSettingDto();
        alarmValueSettingDto.setUpdateUser(updateUser);
        alarmValueSettingDto.setUpdatedTime(new Date());
        Integer alarmValueSettingId = maintainPlanDto.getAlarmValueSettingId();
        Integer result  =alarmValueSettingDao.updateAlarmValueSetting(alarmValueSettingDto,alarmValueSettingId);
        return result;
    }

    @Override
    public PageInfo<MaintainPlanVo> findMaintainCountReport(MaintainPlanDto maintainPlanDto) {
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        List<MaintainPlanVo> maintainCountReport = maintainPlanDao.findMaintainCountReport(maintainPlanDto);
        return new PageInfo(maintainCountReport);
    }

    @Override
    public PageInfo<MaintainPlanVo> findMaintainMonthEndReport(MaintainPlanDto maintainPlanDto) {
        if(ToolUtil.isEmpty(maintainPlanDto.getStartTime()) && ToolUtil.isEmpty(maintainPlanDto.getEndTime())){
            SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calast = Calendar.getInstance();
            calast.set(Calendar.DAY_OF_MONTH, calast.getActualMaximum(Calendar.DAY_OF_MONTH));
            maintainPlanDto.setEndTime(sm.format(calast.getTime())+ " 23:59:59");
            calast.set(Calendar.DAY_OF_MONTH, calast.getActualMinimum(Calendar.DAY_OF_MONTH));
            maintainPlanDto.setStartTime(sm.format(calast.getTime())+ " 00:00:00");
        }
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        List<MaintainPlanVo> maintainMonthEndReport = maintainPlanDao.findMaintainMonthEndReport(maintainPlanDto);
        return new PageInfo(maintainMonthEndReport);
    }

    @Override
    public PageInfo<MaintainPlanVo> findMaintainHistoryCollect(MaintainPlanDto maintainPlanDto) {
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findMaintainHistoryCollect(maintainPlanDto);
        for (MaintainPlanVo maintainPlanVo : maintainPlanVos) {
            if (maintainPlanVo.getChangeStatus().equals("0")) {
                maintainPlanVo.setChangeStatus("未变更");
            }
            if (maintainPlanVo.getChangeStatus().equals("1")) {
                maintainPlanVo.setChangeStatus("已变更");
            }
        }
        return new PageInfo(maintainPlanVos);
    }

    @Override
    public Integer endMaintainPlan(MaintainPlanDto maintainPlanDto) {
        Date updateTime = new Date();
        String updateUser = TokenDecode.getUserInfo().get("username");
        Integer maintainPlanId = maintainPlanDto.getId();
        maintainPlanDao.endMaintainPlan(updateUser,updateTime,maintainPlanId);
        Integer alarmValueSettingId = maintainPlanDto.getAlarmValueSettingId();
        Integer result = alarmValueSettingDao.updateStatus(alarmValueSettingId);
        return result;
    }

    @Override
    public Integer deleteMaintainStage(List<Integer> ids) {
        maintainPlanDao.deleteMaintainPlanById(ids);
        List<Integer> alarmValueSettingIds = maintainPlanDao.findAlarmValueSettingId(ids);
        alarmValueSettingDao.deleteIds(alarmValueSettingIds);
        maintainPlanDao.deleteMaintainPlanStageById(ids);
        return 1;
    }

    @Override
    public MaintainPlanVo findMaintainPlanDetails(Integer id) {
        List<MaintainPlanVo> maintainPlanVos = maintainPlanDao.findDetails(id);
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findDetailsNew(id);
        maintainPlanVo.setMaintainPlanVos(maintainPlanVos);
        return maintainPlanVo;
    }

    @Override
    public Integer changeMaintainPlanStage(MaintainPlanDto maintainPlanDto) {
        String createUser = TokenDecode.getUserInfo().get("username");
        Date date = new Date();
        MaintainPlanVo maintainPlan = maintainPlanDao.findMaintainPlan(maintainPlanDto.getId());
        maintainPlanDto.setCreateUser(createUser);
        maintainPlanDto.setCreatedTime(date);
        maintainPlanDto.setVersion(maintainPlan.getVersion()+1);
        maintainPlanDto.setMaintainPlanId(maintainPlanDto.getId());
        int days=0;
        if(((date.getTime() - maintainPlan.getAftercareDateStart().getTime()) % (1000*3600*24))!=0){
            days = (int) ((date.getTime() - maintainPlan.getAftercareDateStart().getTime()) / (1000*3600*24))+1;
        }
        if(((date.getTime() - maintainPlan.getAftercareDateStart().getTime()) % (1000*3600*24))==0){
            days = (int) ((date.getTime() - maintainPlan.getAftercareDateStart().getTime()) / (1000*3600*24));
        }
        Date date1 = new Date(maintainPlanDto.getAftercareDateStart().getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l);
        date1.setHours(0);
        date1.setMinutes(0);
        date1.setSeconds(0);
        maintainPlanDto.setAftercareDateEnd(date1);
        AlarmValueSettingDto alarmValueSettingDto = maintainPlanDto.getAlarmValueSettingDto();
        alarmValueSettingDto.setCreateUser(createUser);
        alarmValueSettingDto.setCreatedTime(date);
        Integer alarmValueSettingId = maintainPlanDto.getAlarmValueSettingId();
        alarmValueSettingDto.setPhases(maintainPlanDto.getPhases());
        alarmValueSettingDao.updateAlarmValueSetting(alarmValueSettingDto,alarmValueSettingId);
        maintainPlanDao.insertMaintainStage(maintainPlanDto);
        maintainPlanDto.setStageId(maintainPlanDao.findMaxStageId());
        maintainPlanDto.setMaintainDays(days);
        maintainPlanDto.setUpdatedTime(date);
        maintainPlanDto.setUpdateUser(createUser);
        Integer result = maintainPlanDao.changeMaintainPlanStage(maintainPlanDto);
        return result;
    }

    @Override
    public PageInfo<MaintainPlanVo> findExpireCollectTable(MaintainPlanDto maintainPlanDto) {
        Date date = new Date();
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        maintainPlanDto.setCreatedTime(date);
        List<MaintainPlanDto> test = maintainPlanDao.findVersionAndAftercareDateEnd();
        List<MaintainPlanVo> maintainPlanVos = new ArrayList<>();
        for (MaintainPlanDto maintainPlanDto1 : test) {
            maintainPlanDto.setVersion(maintainPlanDto1.getVersion());
            maintainPlanDto.setId(maintainPlanDto1.getId());
            maintainPlanDto.setFlag(date.getTime() < maintainPlanDto1.getAftercareDateEnd().getTime()?1:0);
            MaintainPlanVo maintainPlanVo1 = maintainPlanDao.findExpireCollectTable(maintainPlanDto);
            if(date.getTime() < maintainPlanDto1.getAftercareDateEnd().getTime()){
                maintainPlanVo1.setChangeStatus("已转段");
            }
            if(date.getTime() >= maintainPlanDto1.getAftercareDateEnd().getTime()){
                maintainPlanVo1.setChangeStatus("未转段");
            }
            maintainPlanVos.add(maintainPlanVo1);
        }
        PageInfo maintainPlan = new PageInfo(maintainPlanVos);
        return maintainPlan;
    }

    @Override
    public Integer isExistStackId(Integer stackId) {
        return maintainPlanDao.isExistStackId(stackId);
    }
}
