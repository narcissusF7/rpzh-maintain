package com.rpzh.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.util.StringUtil;
import com.rpzh.dao.AlarmSettingTemplateDao;
import com.rpzh.dao.WareAreaAlarmSettingDao;
import com.rpzh.dao.WarehouseDao;
import com.rpzh.dto.AlarmValueSettingDto;
import com.rpzh.dto.LogAlarmSettingDto;
import com.rpzh.dto.WareAreaAlarmSettingDto;
import com.rpzh.dto.WarehouseDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.WareAreaAlarmSettingService;
import com.rpzh.util.IpUtils;
import com.rpzh.util.RedisUtil;
import com.rpzh.util.SiteRouteUtil;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.LogAlarmSettingVo;
import com.rpzh.vo.WareAreaAlarmSettingVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

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

    public boolean get(WareAreaAlarmSettingDto wareAreaAlarmSettingDto){
        if(wareAreaAlarmSettingDto.getWarehouseMoistureLower() >= wareAreaAlarmSettingDto.getWarehouseMoistureUpperOne() || wareAreaAlarmSettingDto.getWarehouseMoistureUpperOne() >= wareAreaAlarmSettingDto.getWarehouseMoistureUpperTwo() ||
                wareAreaAlarmSettingDto.getWarehouseMoistureUpperTwo() >= wareAreaAlarmSettingDto.getWarehouseMoistureUpperThree()){
            return true;
        }
        if(wareAreaAlarmSettingDto.getWarehouseTempLower() >= wareAreaAlarmSettingDto.getWarehouseTempUpperOne() || wareAreaAlarmSettingDto.getWarehouseTempUpperOne() >= wareAreaAlarmSettingDto.getWarehouseTempUpperTwo() ||
                wareAreaAlarmSettingDto.getWarehouseTempUpperTwo() >= wareAreaAlarmSettingDto.getWarehouseTempUpperThree()){
            return true;
        }
        if(wareAreaAlarmSettingDto.getWarehouseOxygenLower() >= wareAreaAlarmSettingDto.getWarehouseOxygenUpperOne() || wareAreaAlarmSettingDto.getWarehouseOxygenUpperOne() >= wareAreaAlarmSettingDto.getWarehouseOxygenUpperTwo() ||
                wareAreaAlarmSettingDto.getWarehouseOxygenUpperTwo() >= wareAreaAlarmSettingDto.getWarehouseOxygenUpperThree()){
            return true;
        }
        return false;
    }

    @Override
    public Result insertWareAreaAlarmSetting(WareAreaAlarmSettingDto wareAreaAlarmSettingDto) {
        if(ToolUtil.isEmpty(wareAreaAlarmSettingDto.getPidRoute()) || ToolUtil.isEmpty(wareAreaAlarmSettingDto.getStackId())){
            return new Result(false, StatusCode.ERROR, "位置不能为空!");
        }
        if(getFlag(wareAreaAlarmSettingDto)){
            return new Result(false, StatusCode.ERROR, "值不能为空!");
        }
        if(get(wareAreaAlarmSettingDto)){
            return new Result(false, StatusCode.ERROR, "下限预警低于上限一级预警，上限一级预警低于上限二级预警，上限二级预警低于上限三级预警!");
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

    @Autowired
    private RedisUtil redisUtil;
    @Override
    public PageInfo<WareAreaAlarmSettingVo> findWareAreaAlarmSettingInfo(WareAreaAlarmSettingDto wareAreaAlarmSettingDto) {
        createDefaultRule();
        String pidRoute = wareAreaAlarmSettingDto.getPidRoute();
        Integer stackId = wareAreaAlarmSettingDto.getStackId();
        if(StringUtil.isNotEmpty(pidRoute)){
            wareAreaAlarmSettingDto.setPidRoute(SiteRouteUtil.route(pidRoute,stackId+""));
        }
        PageHelper.startPage(wareAreaAlarmSettingDto.getPage(), wareAreaAlarmSettingDto.getSize());
        List<WareAreaAlarmSettingVo> wareAreaAlarmSettingInfos = wareAreaAlarmSettingDao.findWareAreaAlarmSettingInfo(wareAreaAlarmSettingDto);
        for (WareAreaAlarmSettingVo wareAreaAlarmSetting : wareAreaAlarmSettingInfos) {
            Integer siteId = wareAreaAlarmSetting.getStackId();
            String createUser = wareAreaAlarmSetting.getCreateUser();
            wareAreaAlarmSetting.setCreateUser((String)redisUtil.hget("USER_"+createUser,"nickname") + "(" + createUser + ")");
            switch (wareAreaAlarmSetting.getSiteType()){
                case "04":
                    wareAreaAlarmSetting.setSiteName(warehouseDao.findWareArea(siteId).getSiteName());
                    break;
                case "03":
                    wareAreaAlarmSetting.setSiteName(warehouseDao.findFloor(siteId).getSiteName());
                    break;
                case "02":
                    wareAreaAlarmSetting.setSiteName(warehouseDao.findBuilding(siteId+"").getSiteName());
                    break;
                case "01":
                    wareAreaAlarmSetting.setSiteName(warehouseDao.findWarehouse(siteId).getSiteName());
                    break;
            }
        }
        return new PageInfo<>(wareAreaAlarmSettingInfos);
    }

    public void createDefaultRule(){
        if(wareAreaAlarmSettingDao.findCountByWarehouseId()>0) {
            List<Integer> warehouseIds = new ArrayList<>(warehouseDao.findAllWarehouseIds());
            List<Integer> siteIds = new ArrayList<>(wareAreaAlarmSettingDao.findWarehouseIds());
            warehouseIds.removeAll(siteIds);
            String username = TokenDecode.getUserInfo().get("username");
            for (Integer warehouseId : warehouseIds) {
                WareAreaAlarmSettingDto wareAreaAlarmSettingDto1 = new WareAreaAlarmSettingDto(10d,11d,12d,9d,20d,21d,22d,19d,32d,33d,34d,31d,"01",warehouseId,username,username);
                wareAreaAlarmSettingDao.insertWareAreaAlarmSetting(wareAreaAlarmSettingDto1);
            }
        }

    }

    @Resource
    private WarehouseDao warehouseDao;
    @Resource
    private RedisTemplate redisTemplate;
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
        if(get(wareAreaAlarmSettingDto)){
            return new Result(false, StatusCode.ERROR, "下限预警低于上限一级预警，上限一级预警低于上限二级预警，上限二级预警低于上限三级预警!");
        }
        System.out.println(wareAreaAlarmSettingDto.getWarehouseMoistureLower());
        System.out.println(wareAreaAlarmSettingDto.getWarehouseOxygenLower());
        System.out.println(wareAreaAlarmSettingDto.getWarehouseTempLower());
        Date date = new Date();
        String updateUser = TokenDecode.getUserInfo().get("username");
        wareAreaAlarmSettingDto.setUpdateUser(updateUser);
        wareAreaAlarmSettingDto.setUpdatedTime(date);
        WareAreaAlarmSettingVo wareAreaAlarmSetting = wareAreaAlarmSettingDao.findSiteIdAndSiteType(wareAreaAlarmSettingDto.getId());
        wareAreaAlarmSettingDao.updateWareAreaAlarmSetting(wareAreaAlarmSettingDto);
        List<String> fieldNames = new ArrayList<>(Arrays.asList("warehouseOxygenUpperThree","warehouseOxygenUpperTwo","warehouseOxygenUpperOne","warehouseOxygenLower","warehouseTempUpperThree","warehouseTempUpperTwo","warehouseTempUpperOne","warehouseTempLower","warehouseMoistureUpperThree","warehouseMoistureUpperTwo","warehouseMoistureUpperOne","warehouseMoistureLower"));
        List<Double> nodeDatas = new ArrayList<>(Arrays.asList(wareAreaAlarmSettingDto.getWarehouseOxygenUpperThree(),wareAreaAlarmSettingDto.getWarehouseOxygenUpperTwo(),wareAreaAlarmSettingDto.getWarehouseOxygenUpperOne(),wareAreaAlarmSettingDto.getWarehouseOxygenLower(),wareAreaAlarmSettingDto.getWarehouseTempUpperThree(),wareAreaAlarmSettingDto.getWarehouseTempUpperTwo(),
                wareAreaAlarmSettingDto.getWarehouseTempUpperOne(),wareAreaAlarmSettingDto.getWarehouseTempLower(),wareAreaAlarmSettingDto.getWarehouseMoistureUpperThree(),wareAreaAlarmSettingDto.getWarehouseMoistureUpperTwo(),wareAreaAlarmSettingDto.getWarehouseMoistureUpperOne(),wareAreaAlarmSettingDto.getWarehouseMoistureLower()));
        String key = "MAINTAIN_" + wareAreaAlarmSetting.getSiteType() + wareAreaAlarmSetting.getStackId();
        if(redisTemplate.hasKey(key)){
            for (int i = 0; i < fieldNames.size(); i++) {
                redisTemplate.opsForHash().put(key, fieldNames.get(i), nodeDatas.get(i));
            }
        }
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
