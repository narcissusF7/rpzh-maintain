package com.rpzh.service.impl;

import com.rpzh.dao.*;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.MaterialService;
import com.rpzh.util.RedisUtil;
import com.rpzh.util.SiteRouteUtil;
import com.rpzh.vo.*;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/05/07/15:12
 * @Description: 功能
 */
@Service
public class MaterialServiceImpl implements MaterialService {
    @Resource
    private MaterialDao materialDao;
    @Resource
    private WarehouseDao warehouseDao;

    @Override
    public MaterialVo findMaterialInfo(Integer stackId) {
        MaterialVo materialVo = new MaterialVo();
        StackVo stackVo = warehouseDao.findStackById(stackId);
        String nameCn = stackVo.getNameCn();
        List<String> materialCodes = materialDao.findMaterialCode(nameCn);
        if(ToolUtil.isEmpty(materialCodes)){
            return materialVo;
        }
        materialVo.setMaintainCount(materialDao.getAllMaintainCount(nameCn));
        materialVo.setMaterialCodes(materialCodes);
        materialVo.setPrincipalId(getPrincipalName(stackId));
        materialVo.setPidRoute(SiteRouteUtil.route(stackVo.getPidRoute(),stackId+""));
        return materialVo;
    }

    @Override
    public List<String> findAllMaterialCodes() {
        return materialDao.findAllMaterialCodes();
    }

    @Resource
    private RedisUtil redisUtil;
    public Integer getPrincipalName(Integer stackId){
        StackVo stackVo = warehouseDao.findStackPrincipalName(stackId);
        if (ToolUtil.isNotEmpty(stackVo.getPrincipalName())) {
            return Integer.parseInt(redisUtil.hget("USER_"+stackVo.getPrincipalName(),"userId").toString());
        }
        WareAreaVo wareAreaVo = warehouseDao.findWareAreaPrincipalName(stackVo.getAreaId());
        if (ToolUtil.isNotEmpty(wareAreaVo.getPrincipalName())) {
            return Integer.parseInt(redisUtil.hget("USER_"+wareAreaVo.getPrincipalName(),"userId").toString());
        }
        FloorVo floorVo = warehouseDao.findFloorPrincipalName(wareAreaVo.getFloorId());
        if (ToolUtil.isEmpty(wareAreaVo.getPrincipalName()) && ToolUtil.isNotEmpty(floorVo.getPrincipalName())) {
            return Integer.parseInt(redisUtil.hget("USER_"+floorVo.getPrincipalName(),"userId").toString());
        }
        BuildingVo buildingVo = warehouseDao.findBuildingPrincipalName(floorVo.getBuildingId());
        if (ToolUtil.isEmpty(wareAreaVo.getPrincipalName()) && ToolUtil.isEmpty(floorVo.getPrincipalName()) && ToolUtil.isNotEmpty(buildingVo.getPrincipalName())) {
            return Integer.parseInt(redisUtil.hget("USER_"+buildingVo.getPrincipalName(),"userId").toString());
        }
        WarehouseVo warehouseVo = warehouseDao.findWarehousePrincipalName(buildingVo.getWarehouseId());
        return Integer.parseInt(redisUtil.hget("USER_"+warehouseVo.getPrincipalName(),"userId").toString());
    }
}
