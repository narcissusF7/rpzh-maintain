package com.rpzh.service.impl;

import com.rpzh.dao.*;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.MaterialService;
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
        Double maintainCount = materialDao.getMaintainCount(stackId);
        materialVo.setMaintainCount(maintainCount);
        if(materialVo==null){
            return null;
        }
        List<String> materialCodes = materialDao.findMaterialCode(stackId);
        if(materialCodes.size()>0) {
            materialVo.setMaterialCodes(materialCodes);
        }
        materialVo.setPrincipalId(getPrincipalName(stackId));
        materialVo.setPidRoute(SiteRouteUtil.route(warehouseDao.findPidRoute(stackId).getPidRoute(),stackId+""));
        return materialVo;
    }

    @Override
    public List<String> findAllMaterialCodes() {
        return materialDao.findAllMaterialCodes();
    }

    public Integer getPrincipalName(Integer stackId){
        StackVo stackVo = warehouseDao.findStackPrincipalName(stackId);
        if (!ToolUtil.isEmpty(stackVo.getPrincipalId())) {
            return stackVo.getPrincipalId();
        }
        WareAreaVo wareAreaVo = warehouseDao.findWareAreaPrincipalName(stackVo.getAreaId());
        if (ToolUtil.isNotEmpty(wareAreaVo.getPrincipalId())) {
            return wareAreaVo.getPrincipalId();
        }
        FloorVo floorVo = warehouseDao.findFloorPrincipalName(wareAreaVo.getFloorId());
        if (ToolUtil.isEmpty(wareAreaVo.getPrincipalId()) && ToolUtil.isNotEmpty(floorVo.getPrincipalId())) {
            return floorVo.getPrincipalId();
        }
        BuildingVo buildingVo = warehouseDao.findBuildingPrincipalName(floorVo.getBuildingId());
        if (ToolUtil.isEmpty(wareAreaVo.getPrincipalId()) && ToolUtil.isEmpty(floorVo.getPrincipalId()) && ToolUtil.isNotEmpty(buildingVo.getPrincipalId())) {
            return  buildingVo.getPrincipalId();
        }
        WarehouseVo warehouseVo = warehouseDao.findWarehousePrincipalName(buildingVo.getWarehouseId());
        return warehouseVo.getPrincipalId();
    }
}
