package com.rpzh.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.WareAreaAlarmSettingDao;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.WareAreaAlarmSettingDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.WareAreaAlarmSettingService;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.WareAreaAlarmSettingVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/06/08/10:51
 * @Description: 功能
 */
@Service
public class WareAreaAlarmSettingServiceImpl implements WareAreaAlarmSettingService {
    @Resource
    private WareAreaAlarmSettingDao wareAreaAlarmSettingDao;
    @Override
    public void insertWareAreaAlarmSetting(WareAreaAlarmSettingDto wareAreaAlarmSettingDto) {
        wareAreaAlarmSettingDto.setSiteType("0"+wareAreaAlarmSettingDto.getLevel());
        Date date = new Date();
        String createUser = TokenDecode.getUserInfo().get("username");
        wareAreaAlarmSettingDto.setUpdateUser(createUser);
        wareAreaAlarmSettingDto.setCreateUser(createUser);
        wareAreaAlarmSettingDto.setUpdatedTime(date);
        wareAreaAlarmSettingDto.setCreatedTime(date);
        wareAreaAlarmSettingDao.insertWareAreaAlarmSetting(wareAreaAlarmSettingDto);
    }

    @Override
    public Result deleteWareAreaAlarmSetting(List<Integer> ids) {
        if(ToolUtil.isEmpty(ids)){
            return new Result(false, StatusCode.ERROR, "仓间预警规则ids不能为空!");
        }
        wareAreaAlarmSettingDao.deleteIds(ids);
        return new Result(true, StatusCode.OK, "删除仓间预警规则模板成功!");
    }

    @Override
    public PageInfo<WareAreaAlarmSettingVo> findWareAreaAlarmSettingInfo(WareAreaAlarmSettingDto wareAreaAlarmSettingDto) {
        String endTime = wareAreaAlarmSettingDto.getEndTime();
        if(ToolUtil.isNotEmpty(endTime)){
            wareAreaAlarmSettingDto.setEndTime(endTime.replace("00:00:00","23:59:59"));
        }
        PageHelper.startPage(wareAreaAlarmSettingDto.getPage(), wareAreaAlarmSettingDto.getSize());
        List<WareAreaAlarmSettingVo> wareAreaAlarmSettingInfos = wareAreaAlarmSettingDao.findWareAreaAlarmSettingInfo(wareAreaAlarmSettingDto);
        return new PageInfo<>(wareAreaAlarmSettingInfos);
    }

    @Override
    public Result findWareAreaAlarmSettingById(WareAreaAlarmSettingDto wareAreaAlarmSettingDto) {
        Integer id = wareAreaAlarmSettingDto.getId();
        if(ToolUtil.isEmpty(id)){
            return new Result(false, StatusCode.ERROR, "仓间预警规则id不能为空!");
        }
        WareAreaAlarmSettingVo wareAreaAlarmSettingVo = wareAreaAlarmSettingDao.findWareAreaAlarmSettingById(wareAreaAlarmSettingDto);
        wareAreaAlarmSettingVo.setSiteName(wareAreaAlarmSettingDto.getSiteName());
        return new Result(true, StatusCode.OK, "修改前回显成功!",wareAreaAlarmSettingVo);
    }

    @Override
    public void updateWareAreaAlarmSetting(WareAreaAlarmSettingDto wareAreaAlarmSettingDto) {
        wareAreaAlarmSettingDto.setSiteType("0"+wareAreaAlarmSettingDto.getLevel());
        Date date = new Date();
        String updateUser = TokenDecode.getUserInfo().get("username");
        wareAreaAlarmSettingDto.setUpdateUser(updateUser);
        wareAreaAlarmSettingDto.setUpdatedTime(date);
        wareAreaAlarmSettingDao.updateWareAreaAlarmSetting(wareAreaAlarmSettingDto);
    }
}
