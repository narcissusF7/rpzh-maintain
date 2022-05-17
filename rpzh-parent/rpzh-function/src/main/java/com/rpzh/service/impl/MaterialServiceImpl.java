package com.rpzh.service.impl;

import com.rpzh.dao.MaintainPlanDao;
import com.rpzh.dao.MaterialDao;
import com.rpzh.dao.WarehouseDao;
import com.rpzh.service.MaterialService;
import com.rpzh.util.StringUtils;
import com.rpzh.vo.*;
import org.apache.ibatis.annotations.Param;
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
    public MaterialVo getMaterialInfoBySiteId(Integer stackId) {
        MaterialVo materialVo = materialDao.getinfoBySiteId(stackId);
        if(materialVo==null){
            return null;
        }
        List<String> materialBatches = materialDao.getMaterialBatch(stackId);
        String materialBatch = "";
        for (int i = 0; i < materialBatches.size(); i++) {
            if(i==0){
                materialBatch = materialBatches.get(i);
            }
            if(i!=0){
                materialBatch =materialBatch+","+ materialBatches.get(i);
            }
        }
        materialVo.setPrincipalName("");
        WareAreaVo wareAreaVo = warehouseDao.findWareAreaPrincipalName(stackId);
        if(!StringUtils.isEmpty(wareAreaVo.getPrincipalName())){
           materialVo.setPrincipalName(wareAreaVo.getPrincipalName());
        }
        FloorVo floorVo = warehouseDao.findFloorPrincipalName(wareAreaVo.getFloorId());
        if(StringUtils.isEmpty(wareAreaVo.getPrincipalName()) && !StringUtils.isEmpty(floorVo.getPrincipalName())){
            materialVo.setPrincipalName(floorVo.getPrincipalName());
        }
        BuildingVo buildingVo = warehouseDao.findBuildingPrincipalName(floorVo.getBuildingId());
        if(StringUtils.isEmpty(wareAreaVo.getPrincipalName()) && StringUtils.isEmpty(floorVo.getPrincipalName()) && !StringUtils.isEmpty(buildingVo.getPrincipalName())){
            materialVo.setPrincipalName(buildingVo.getPrincipalName());
        }
        WarehouseVo warehouseVo = warehouseDao.findWarehousePrincipalName(buildingVo.getWarehouseId());
        if(StringUtils.isEmpty(wareAreaVo.getPrincipalName()) && StringUtils.isEmpty(floorVo.getPrincipalName())
                && StringUtils.isEmpty(buildingVo.getPrincipalName()) && !StringUtils.isEmpty(warehouseVo.getPrincipalName())){
            materialVo.setPrincipalName(warehouseVo.getPrincipalName());
        }
        return materialVo;
    }

    @Override
    public List<String> findMaterialCode(Integer stackId) {
        List<String> materialCodes = materialDao.findMaterialCode(stackId);
        return materialCodes;
    }

    @Override
    public List<String> findYear(Integer stackId, String materialCode) {
        List<String> year = materialDao.findYear(stackId, materialCode);
        return year;
    }
}
