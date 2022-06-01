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
import java.util.ArrayList;
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
        MaterialVo materialVo = materialDao.getinfoBySiteId(stackId);
        if(materialVo==null){
            return null;
        }
        List<String> materialCodes = materialDao.findMaterialCode(stackId);
        if(materialCodes.size()>0) {
            materialVo.setMaterialCodes(materialCodes);
        }
        List<String> materialBatches = materialDao.getMaterialBatch(stackId);
        String materialBatch = "";
        if(materialBatches.size()>0) {
            for (int i = 0; i < materialBatches.size(); i++) {
                if (i == 0) {
                    materialBatch = materialBatches.get(i);
                }
                if (i != 0) {
                    materialBatch = materialBatch + "," + materialBatches.get(i);
                }
            }
            materialVo.setMaterialBatchNum(materialBatch);
        }
        materialVo.setPrincipalName("");
        materialVo.setPrincipalId(0);
        WareAreaVo wareAreaVo = warehouseDao.findWareAreaPrincipalName(stackId);
        if(!StringUtils.isEmpty(wareAreaVo.getPrincipalName())){
            materialVo.setPrincipalName(wareAreaVo.getPrincipalName());
            materialVo.setPrincipalId(wareAreaVo.getPrincipalId());
        }
        FloorVo floorVo = warehouseDao.findFloorPrincipalName(wareAreaVo.getFloorId());
        if(StringUtils.isEmpty(wareAreaVo.getPrincipalName()) && !StringUtils.isEmpty(floorVo.getPrincipalName())){
            materialVo.setPrincipalName(floorVo.getPrincipalName());
            materialVo.setPrincipalId(floorVo.getPrincipalId());
        }
        BuildingVo buildingVo = warehouseDao.findBuildingPrincipalName(floorVo.getBuildingId());
        if(StringUtils.isEmpty(wareAreaVo.getPrincipalName()) && StringUtils.isEmpty(floorVo.getPrincipalName()) && !StringUtils.isEmpty(buildingVo.getPrincipalName())){
            materialVo.setPrincipalName(buildingVo.getPrincipalName());
            materialVo.setPrincipalId(buildingVo.getPrincipalId());
        }
        WarehouseVo warehouseVo = warehouseDao.findWarehousePrincipalName(buildingVo.getWarehouseId());
        if(StringUtils.isEmpty(wareAreaVo.getPrincipalName()) && StringUtils.isEmpty(floorVo.getPrincipalName())
                && StringUtils.isEmpty(buildingVo.getPrincipalName()) && !StringUtils.isEmpty(warehouseVo.getPrincipalName())){
            materialVo.setPrincipalName(warehouseVo.getPrincipalName());
            materialVo.setPrincipalId(buildingVo.getPrincipalId());
        }
        return materialVo;
    }

}
