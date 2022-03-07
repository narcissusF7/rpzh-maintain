package com.rpzh.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.AlarmValueSettingDao;
import com.rpzh.dao.MaintainPlanDao;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.MaintainPlanService;
import dto.MaintainPlanDto;
import org.springframework.stereotype.Service;
import pojo.AlarmValueSetting;
import pojo.MaintainPlan;
import vo.MaintainPlanVo;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Service
public class MaintainPlanServiceImpl implements MaintainPlanService {

    @Resource
    private MaintainPlanDao maintainPlanDao;

    @Resource
    private AlarmValueSettingDao alarmValueSettingDao;

    /**
     * 新增养护计划
     *
     * @param maintainPlanDto 养护计划信息
     * @return success or failure
     */
    @Override
    public int insert(MaintainPlanDto maintainPlanDto) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        MaintainPlan maintainPlan = ToolUtil.sqlBeanTransition(
                maintainPlanDto, new MaintainPlan());
        AlarmValueSetting alarmValueSetting = ToolUtil.sqlBeanTransition(
                maintainPlanDto, new AlarmValueSetting());
        int addAlarmValueSettingResult = alarmValueSettingDao.insertSelective(alarmValueSetting);
        maintainPlan.setAlarmValueSettingId(alarmValueSetting.getId());
        return maintainPlanDao.insertSelective(maintainPlan);
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
     * @param id 养护计划id
     * @return success or failure
     */
    @Override
    public Integer deleteMaintainPlanInfo(Integer id) {
        return maintainPlanDao.deleteByPrimaryKey(id);
    }
}
