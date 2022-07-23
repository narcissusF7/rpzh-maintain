package com.rpzh.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.util.StringUtil;
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
import com.rpzh.util.SiteRouteUtil;
import com.rpzh.util.TokenDecode;
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
    private final String INSERTTYPE = "仓间预警规则添加";

    private final String UPDATETYPE = "仓间预警规则修改";

    public boolean getFlag(WareAreaAlarmSettingDto wareAreaAlarmSettingDto){
        return wareAreaAlarmSettingDto.getWarehouseOxygenUpperOne() == null ||
                wareAreaAlarmSettingDto.getWarehouseOxygenUpperThree() == null ||
                wareAreaAlarmSettingDto.getWarehouseOxygenUpperTwo() == null ||
                wareAreaAlarmSettingDto.getWarehouseOxygenLower() == null ||
                wareAreaAlarmSettingDto.getWarehouseTempUpperOne() == null ||
                wareAreaAlarmSettingDto.getWarehouseTempUpperTwo() == null ||
                wareAreaAlarmSettingDto.getWarehouseTempUpperThree() == null ||
                wareAreaAlarmSettingDto.getWarehouseTempLower() == null ||
                wareAreaAlarmSettingDto.getWarehouseMoistureUpperOne() == null ||
                wareAreaAlarmSettingDto.getWarehouseMoistureUpperThree() == null ||
                wareAreaAlarmSettingDto.getWarehouseMoistureUpperTwo() == null ||
                wareAreaAlarmSettingDto.getWarehouseMoistureLower() == null;
    }
    @Override
    public Result insertWareAreaAlarmSetting(WareAreaAlarmSettingDto wareAreaAlarmSettingDto) {
        if(getFlag(wareAreaAlarmSettingDto)){
            return new Result(false, StatusCode.ERROR, "值不能为空!");
        }
        String pidRoute = wareAreaAlarmSettingDto.getPidRoute();
        String siteType = "0" + pidRoute.split(",").length;
        pidRoute = pidRoute + ",[" + siteType + ":" + wareAreaAlarmSettingDto.getStackId() + "]";
        if(wareAreaAlarmSettingDao.findCount(pidRoute)>0){
            return new Result(false, StatusCode.ERROR, "该位置已存在预警规则!");
        }
        wareAreaAlarmSettingDto.setPidRoute(pidRoute);
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
        return new Result(true, StatusCode.OK, "配置仓间预警规则模板成功!");
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
        String pidRoute = wareAreaAlarmSettingDto.getPidRoute();
        Integer stackId = wareAreaAlarmSettingDto.getStackId();
        if(StringUtil.isNotEmpty(pidRoute)){
            wareAreaAlarmSettingDto.setPidRoute(SiteRouteUtil.route(pidRoute,stackId+""));
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
    public Result updateWareAreaAlarmSetting(WareAreaAlarmSettingDto wareAreaAlarmSettingDto) {
        if(getFlag(wareAreaAlarmSettingDto)){
            return new Result(false, StatusCode.ERROR, "值不能为空!");
        }
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
        return new Result<>(true, StatusCode.OK, "修改仓间预警规则成功!");
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
