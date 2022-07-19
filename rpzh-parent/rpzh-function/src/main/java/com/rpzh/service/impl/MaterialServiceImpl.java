package com.rpzh.service.impl;

import com.rpzh.dao.*;
import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.WarehouseNodeType;
import com.rpzh.service.MaterialService;
import com.rpzh.vo.*;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Resource
    private RedisTemplate redisTemplate;

    public static String MAINTAIN ="MAINTAIN_";

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
        if(ToolUtil.isNotEmpty(redisTemplate.opsForValue().get(MAINTAIN + stackId+"_principalId"))){
            Integer principalId = Integer.parseInt(redisTemplate.opsForValue().get(MAINTAIN + stackId + "_principalId").toString());
            materialVo.setPrincipalId(principalId);
        }
        if(ToolUtil.isEmpty(redisTemplate.opsForValue().get(MAINTAIN + stackId+"_principalId"))) {
            StackVo stackVo = warehouseDao.findStackPrincipalName(stackId);
            if (!ToolUtil.isEmpty(stackVo.getPrincipalId())) {
                materialVo.setPrincipalId(stackVo.getPrincipalId());
            }
            WareAreaVo wareAreaVo = warehouseDao.findWareAreaPrincipalName(stackVo.getAreaId());
            if (ToolUtil.isNotEmpty(wareAreaVo.getPrincipalId())) {
                materialVo.setPrincipalId(wareAreaVo.getPrincipalId());
            }
            FloorVo floorVo = warehouseDao.findFloorPrincipalName(wareAreaVo.getFloorId());
            if (ToolUtil.isEmpty(wareAreaVo.getPrincipalId()) && ToolUtil.isNotEmpty(floorVo.getPrincipalId())) {
                materialVo.setPrincipalId(floorVo.getPrincipalId());
            }
            BuildingVo buildingVo = warehouseDao.findBuildingPrincipalName(floorVo.getBuildingId());
            if (ToolUtil.isEmpty(wareAreaVo.getPrincipalId()) && ToolUtil.isEmpty(floorVo.getPrincipalId()) && ToolUtil.isNotEmpty(buildingVo.getPrincipalId())) {
                materialVo.setPrincipalId(buildingVo.getPrincipalId());
            }
            WarehouseVo warehouseVo = warehouseDao.findWarehousePrincipalName(buildingVo.getWarehouseId());
            if (ToolUtil.isEmpty(wareAreaVo.getPrincipalId()) && ToolUtil.isEmpty(floorVo.getPrincipalId())
                    && ToolUtil.isEmpty(buildingVo.getPrincipalId()) && ToolUtil.isNotEmpty(warehouseVo.getPrincipalId())) {
                materialVo.setPrincipalId(warehouseVo.getPrincipalId());
            }
            redisTemplate.opsForValue().set(MAINTAIN + stackId + "_principalId",materialVo.getPrincipalId()+"");
        }
        String pidRoute = warehouseDao.findPidRoute(stackId).getPidRoute() + ",[" + WarehouseNodeType.STACK_NODE.getType() + ":" + stackId + "]";
        materialVo.setPidRoute(pidRoute);
        return materialVo;
    }

    @Override
    public List<String> findAllMaterialCodes() {
        return materialDao.findAllMaterialCodes();
    }

}
