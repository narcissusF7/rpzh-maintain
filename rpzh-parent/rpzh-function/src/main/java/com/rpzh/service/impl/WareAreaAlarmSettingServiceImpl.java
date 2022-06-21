package com.rpzh.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.AlarmSettingTemplateDao;
import com.rpzh.dao.WareAreaAlarmSettingDao;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.LogAlarmSettingDto;
import com.rpzh.dto.WareAreaAlarmSettingDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.WareAreaAlarmSettingService;
import com.rpzh.util.IpUtils;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.AlarmValueSettingVo;
import com.rpzh.vo.LogAlarmSettingVo;
import com.rpzh.vo.WareAreaAlarmSettingVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Author: zyq
 * @Date: 2022/06/08/10:51
 * @Description: 功能
 */
@Service
public class WareAreaAlarmSettingServiceImpl implements WareAreaAlarmSettingService {
    @Resource
    private WareAreaAlarmSettingDao wareAreaAlarmSettingDao;
    @Resource
    private AlarmSettingTemplateDao alarmSettingTemplateDao;
    private final String INSERTTYPE = "添加";

    private final String UPDATETYPE = "修改";

    @Override
    public void insertWareAreaAlarmSetting(WareAreaAlarmSettingDto wareAreaAlarmSettingDto) {
        String siteType = "0" + wareAreaAlarmSettingDto.getLevel();
        wareAreaAlarmSettingDto.setSiteType(siteType);
        Date date = new Date();
        String createUser = TokenDecode.getUserInfo().get("username");
        wareAreaAlarmSettingDto.setUpdateUser(createUser);
        wareAreaAlarmSettingDto.setCreateUser(createUser);
        wareAreaAlarmSettingDto.setUpdatedTime(date);
        wareAreaAlarmSettingDto.setCreatedTime(date);
        wareAreaAlarmSettingDao.insertWareAreaAlarmSetting(wareAreaAlarmSettingDto);
        Integer id = wareAreaAlarmSettingDao.findBySite(siteType + wareAreaAlarmSettingDto.getStackId()).getId();
        LogAlarmSettingDto logAlarmSettingDto = new LogAlarmSettingDto();
        String ipAddr = IpUtils.getIpAddr(Objects.requireNonNull(IpUtils.getRequest()));
        logAlarmSettingDto.setLogDate(date);
        logAlarmSettingDto.setLogUserName(createUser);
        logAlarmSettingDto.setLogType(INSERTTYPE);
        logAlarmSettingDto.setAlarmValueSettingId(id);
        logAlarmSettingDto.setLogIp(ipAddr);
        alarmSettingTemplateDao.insertLogAlarmSetting(logAlarmSettingDto);
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
        LogAlarmSettingDto logAlarmSettingDto = new LogAlarmSettingDto();
        String ipAddr = IpUtils.getIpAddr(Objects.requireNonNull(IpUtils.getRequest()));
        logAlarmSettingDto.setLogDate(date);
        logAlarmSettingDto.setLogUserName(updateUser);
        logAlarmSettingDto.setLogType(UPDATETYPE);
        logAlarmSettingDto.setAlarmValueSettingId(wareAreaAlarmSettingDto.getId());
        logAlarmSettingDto.setLogIp(ipAddr);
        alarmSettingTemplateDao.insertLogAlarmSetting(logAlarmSettingDto);
    }

    @Override
    public Result findLogData(LogAlarmSettingDto logAlarmSettingDto) {
        if(StringUtils.isEmpty(logAlarmSettingDto.getAlarmValueSettingId())){
            return new Result<>(false, StatusCode.ERROR, "仓间预警id不能为空!");
        }
        PageHelper.startPage(logAlarmSettingDto.getPage(), logAlarmSettingDto.getSize());
        List<LogAlarmSettingVo> logData = wareAreaAlarmSettingDao.findLogData(logAlarmSettingDto);
        return new Result<>(true, StatusCode.OK, "查询仓间操作日志成功!",new PageInfo(logData));
    }
}
