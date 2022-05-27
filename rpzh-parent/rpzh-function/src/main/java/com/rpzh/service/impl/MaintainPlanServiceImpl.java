package com.rpzh.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.*;
import com.github.pagehelper.util.StringUtil;
import com.rpzh.dao.*;
import com.rpzh.dto.*;
import com.rpzh.entity.*;
import com.rpzh.feign.UserFeign;
import com.rpzh.service.MaintainPlanService;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MaintainPlanServiceImpl implements MaintainPlanService {

    @Resource
    private MaintainPlanDao maintainPlanDao;

    @Resource
    private AlarmValueSettingDao alarmValueSettingDao;

    @Resource
    private UserFeign userFeign;

    @Resource
    private NodeDao nodeDao;

    /**
     * 新增养护计划
     *
     * @param maintainPlanDto 养护计划信息
     * @return success or failure
     */
    @Override
    public int insertMaintainPlan(MaintainPlanDto maintainPlanDto){
        List<Integer> principalIds = maintainPlanDto.getPrincipalName();
        maintainPlanDto.setPrincipalId(String.join(",",principalIds.stream().map(String::valueOf).collect(Collectors.toList())));
        maintainPlanDto.setName(String.join(",",userFeign.findUsernameById(principalIds).getData()));
        String materialCodeYear = maintainPlanDto.getMaterialCodeYear();
        List<String> materialCodeYearJson = JSON.parseArray(materialCodeYear, String.class);
        materialCodeYear = JSON.toJSONString(JSON.parse(materialCodeYearJson.toString()));
        List<MaterialCodeYearDto> materialCodeYearDtos = JSON.parseArray(materialCodeYear, MaterialCodeYearDto.class);
        String materialCode ="";
        String tobaccoYear = "";
        for (int i = 0; i < materialCodeYearDtos.size(); i++) {
            if(i==0){
                materialCode =  materialCodeYearDtos.get(i).getId();
                tobaccoYear =  materialCodeYearDtos.get(i).getTobaccoYear().replaceAll("\"","").replaceAll("\\[","").replaceAll("\\]","");
            }
            if(i!=0) {
                materialCode = materialCode + "," + materialCodeYearDtos.get(i).getId();
                tobaccoYear = tobaccoYear + ";" + materialCodeYearDtos.get(i).getTobaccoYear().replace("\"","").replaceAll("\\[","").replaceAll("\\]","");
            }
        }
        maintainPlanDto.setTobaccoYear(tobaccoYear);
        maintainPlanDto.setMaterialCode(materialCode);
        Date nowDate = new Date();
        String createUser = TokenDecode.getUserInfo().get("username");
        AlarmValueSettingDto alarmValueSettingDto = new AlarmValueSettingDto(maintainPlanDto);
        maintainPlanDto.setCreateUser(createUser);
        maintainPlanDto.setCreatedTime(nowDate);
        Integer alarmValueSettingId = alarmValueSettingDto.getAlarmValueSettingId();
        if(alarmValueSettingId!=null){
            alarmValueSettingDto.setUpdateUser(createUser);
            alarmValueSettingDto.setUpdatedTime(nowDate);
            alarmValueSettingDao.updateAlarmValueSetting(alarmValueSettingDto,alarmValueSettingId);
        }else{
            alarmValueSettingDto.setCreateUser(createUser);
            alarmValueSettingDto.setCreatedTime(nowDate);
            alarmValueSettingDao.insertAlarmValueSetting(alarmValueSettingDto);
            maintainPlanDto.setAlarmValueSettingId(alarmValueSettingDao.findMaxId());
        }
        long l = nowDate.getTime() % (1000 * 60 * 60 * 24);
        maintainPlanDto.setOxygenReductionTime(new Date(maintainPlanDto.getOxygenReductionTime().getTime() + l));
        Date date = new Date(maintainPlanDto.getAftercareDateStart().getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l);
        maintainPlanDto.setAftercareDateEnd(date);
        maintainPlanDto.setAftercareDateStart(new Date(maintainPlanDto.getAftercareDateStart().getTime() + l));
        maintainPlanDto.setExpireTime(new Date(maintainPlanDto.getExpireTime().getTime() + l));
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
        }
        return new Result(true, StatusCode.OK, "删除成功!");
    }


    @Override
    public MaintainPlanVo findMaintainPlan(Integer id) {
        MaintainPlanVo maintainPlanVo = maintainPlanDao.findMaintainPlan(id);
        String materialCodeYear = maintainPlanVo.getMaterialCodeYear();
        List<MaterialCodeYearVo> materialCodeYearVos = JSON.parseArray(materialCodeYear, MaterialCodeYearVo.class);
        List<MaterialCodeYearDto> materialCodeYearDtos = JSON.parseArray(materialCodeYear, MaterialCodeYearDto.class);
        for (int i = 0; i < materialCodeYearVos.size(); i++) {
            String[] tobaccoYear = materialCodeYearDtos.get(i).getTobaccoYear().replaceAll("\"","").replaceAll("\\[","").replaceAll("\\]","").split(",");
            materialCodeYearVos.get(i).setTobaccoYear(Arrays.asList(tobaccoYear));
        }
        maintainPlanVo.setUsername(maintainPlanVo.getUsername());
        maintainPlanVo.setPrincipalName(Arrays.asList(maintainPlanVo.getPrincipalId().split(",")).stream().map(Integer::parseInt).collect(Collectors.toList()));
        maintainPlanVo.setMaterialCodeYearVos(materialCodeYearVos);
        Integer alarmValueSettingId = maintainPlanVo.getAlarmValueSettingId();
        AlarmValueSettingVo alarmValueSettingVo = alarmValueSettingDao.findAlarmValueSettingById(alarmValueSettingId);
        maintainPlanVo.setAlarmValueSettingVo(alarmValueSettingVo);
        return maintainPlanVo;
    }

    @Override
    public Integer updateMaintainPlan(MaintainPlanDto maintainPlanDto) {
        List<Integer> principalIds = maintainPlanDto.getPrincipalName();
        maintainPlanDto.setPrincipalId(String.join(",",principalIds.stream().map(String::valueOf).collect(Collectors.toList())));
        maintainPlanDto.setName(String.join(",",userFeign.findUsernameById(principalIds).getData()));
        String updateUser = TokenDecode.getUserInfo().get("username");
        maintainPlanDto.setUpdateUser(updateUser);
        maintainPlanDto.setUpdatedTime(new Date());
        long l =new Date().getTime() % (1000 * 60 * 60 * 24);
        Date date = new Date(maintainPlanDto.getAftercareDateStart().getTime() + maintainPlanDto.getMaintainDays() * 24 * 60 * 60 * 1000l);
        maintainPlanDto.setAftercareDateEnd(date);
        maintainPlanDto.setOxygenReductionTime(new Date(maintainPlanDto.getOxygenReductionTime().getTime() + l));
        maintainPlanDto.setAftercareDateStart(new Date(maintainPlanDto.getAftercareDateStart().getTime() + l));
        maintainPlanDto.setExpireTime(new Date(maintainPlanDto.getExpireTime().getTime() + l));
        maintainPlanDao.updateMaintainPlan(maintainPlanDto);
        maintainPlanDto.setStageId(maintainPlanDao.findMaintainPlan(maintainPlanDto.getId()).getStageId());
        maintainPlanDao.updateMaintainPlanStage(maintainPlanDto);
        AlarmValueSettingDto alarmValueSettingDto = new AlarmValueSettingDto(maintainPlanDto);
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
                maintainPlanVo.setChangeStatus("未转段");
            }
            if (maintainPlanVo.getChangeStatus().equals("1")) {
                maintainPlanVo.setChangeStatus("已转段");
            }
        }
        return new PageInfo(maintainPlanVos);
    }

    @Override
    public Integer endMaintainPlan(MaintainPlanDto maintainPlanDto) {
        Date updateTime = new Date();
        String updateUser = TokenDecode.getUserInfo().get("username");
        Integer maintainPlanId = maintainPlanDto.getId();
        Integer result =  maintainPlanDao.endMaintainPlan(updateUser,updateTime,maintainPlanId);
        return result;
    }

    @Override
    public Integer deleteMaintainStage(List<Integer> ids) {
        maintainPlanDao.deleteMaintainPlanById(ids);
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

        List<Integer> principalIds = maintainPlanDto.getPrincipalName();
        maintainPlanDto.setPrincipalId(String.join(",",principalIds.stream().map(String::valueOf).collect(Collectors.toList())));
        maintainPlanDto.setName(String.join(",",userFeign.findUsernameById(principalIds).getData()));
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
        maintainPlanDto.setAftercareDateEnd(date1);
        AlarmValueSettingDto alarmValueSettingDto = new AlarmValueSettingDto(maintainPlanDto);
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
    public Result findExpireCollectTable(MaintainPlanDto maintainPlanDto) {
        PageHelper.startPage(maintainPlanDto.getPage(), maintainPlanDto.getSize());
        if(StringUtil.isNotEmpty(maintainPlanDto.getEndTime())){
            maintainPlanDto.setEndTime(maintainPlanDto.getEndTime().replace("00:00:00","23:59:59"));
        }
        Date date = new Date(new Date().getTime() / (1000 * 60 * 60 * 24) * (1000 * 60 * 60 * 24));
        maintainPlanDto.setCreatedTime(date);
        List<MaintainPlanDto> test = maintainPlanDao.findVersionAndAftercareDateEnd();
        List<MaintainPlanVo> maintainPlanVos = new ArrayList<>();
        for (MaintainPlanDto maintainPlanDto1 : test) {
            maintainPlanDto.setId(maintainPlanDto1.getId());
            MaintainPlanVo maintainPlanVo1;
            if(date.getTime() < maintainPlanDto1.getAftercareDateEnd().getTime() && maintainPlanDto1.getVersion()>1){
                maintainPlanDto.setVersion(maintainPlanDto1.getVersion()-1);
                maintainPlanVo1 = maintainPlanDao.findExpireCollectTable(maintainPlanDto);
                maintainPlanVo1.setChangeStatus("已转段");
                maintainPlanVos.add(maintainPlanVo1);
            }
            if(date.getTime() > maintainPlanDto1.getAftercareDateEnd().getTime()){
                maintainPlanDto.setVersion(maintainPlanDto1.getVersion());
                maintainPlanVo1 = maintainPlanDao.findExpireCollectTable(maintainPlanDto);
                maintainPlanVo1.setChangeStatus("未转段");
                maintainPlanVo1.setCreatedTime(null);
                maintainPlanVos.add(maintainPlanVo1);
            }
        }
        PageInfo maintainPlan = new PageInfo(maintainPlanVos);
        return new Result<>(true, StatusCode.OK, "养护到期汇总列表查询成功",maintainPlan);
    }

    @Override
    public Integer isExistStackId(Integer stackId) {
        return maintainPlanDao.isExistStackId(stackId);
    }

    @Override
    @Transactional
    public Integer moveStack(MaintainPlanDto maintainPlanDto) {
        List<NodeDto> nodeDtoList = maintainPlanDto.getNodeDtoList();
        Integer stackId = maintainPlanDto.getStackId();
        Integer count = maintainPlanDao.findStackCountByStackId(stackId);
        if (count > 0) {
            return -1;
        } else {
            Integer flag = maintainPlanDao.updateStackByStackId(maintainPlanDto.getId(), stackId);
            if (flag > 0) {
                nodeDtoList.forEach((nodeDto) -> {
                    nodeDto.setStackId(stackId);
                    nodeDao.updateNode(nodeDto);
                });
            }
            return flag;
        }
    }
}
