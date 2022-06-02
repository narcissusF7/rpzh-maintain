package com.rpzh.service.impl;

import com.alibaba.fastjson.JSON;
import com.rpzh.dao.MaintainPlanDao;
import com.rpzh.dao.MaterialDao;
import com.rpzh.dao.WarehouseDao;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.MaterialService;
import com.rpzh.util.StringUtils;
import com.rpzh.vo.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        List<String> materialNames = materialDao.getMaterialName(stackId);
        String materialName ="";
        if (materialNames.size() > 0) {
            materialName=String.join(",", materialName);
        }
        materialVo.setMaterialName(materialName);
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

        if(ToolUtil.isNotEmpty(redisTemplate.opsForValue().get(MAINTAIN + stackId+"_principalName"))){
            String principal = redisTemplate.opsForValue().get(MAINTAIN + stackId + "_principalName").toString();
            materialVo.setPrincipalName(principal.split(",")[0]);
            materialVo.setPrincipalId(Integer.parseInt(principal.split(",")[1]));
        }
        if(ToolUtil.isEmpty(redisTemplate.opsForValue().get(MAINTAIN + stackId+"_principalName"))) {
            WareAreaVo wareAreaVo = warehouseDao.findWareAreaPrincipalName(stackId);
            if (!StringUtils.isEmpty(wareAreaVo.getPrincipalName())) {
                materialVo.setPrincipalName(wareAreaVo.getPrincipalName());
                materialVo.setPrincipalId(wareAreaVo.getPrincipalId());
            }
            FloorVo floorVo = warehouseDao.findFloorPrincipalName(wareAreaVo.getFloorId());
            if (StringUtils.isEmpty(wareAreaVo.getPrincipalName()) && !StringUtils.isEmpty(floorVo.getPrincipalName())) {
                materialVo.setPrincipalName(floorVo.getPrincipalName());
                materialVo.setPrincipalId(floorVo.getPrincipalId());
            }
            BuildingVo buildingVo = warehouseDao.findBuildingPrincipalName(floorVo.getBuildingId());
            if (StringUtils.isEmpty(wareAreaVo.getPrincipalName()) && StringUtils.isEmpty(floorVo.getPrincipalName()) && !StringUtils.isEmpty(buildingVo.getPrincipalName())) {
                materialVo.setPrincipalName(buildingVo.getPrincipalName());
                materialVo.setPrincipalId(buildingVo.getPrincipalId());
            }
            WarehouseVo warehouseVo = warehouseDao.findWarehousePrincipalName(buildingVo.getWarehouseId());
            if (StringUtils.isEmpty(wareAreaVo.getPrincipalName()) && StringUtils.isEmpty(floorVo.getPrincipalName())
                    && StringUtils.isEmpty(buildingVo.getPrincipalName()) && !StringUtils.isEmpty(warehouseVo.getPrincipalName())) {
                materialVo.setPrincipalName(warehouseVo.getPrincipalName());
                materialVo.setPrincipalId(buildingVo.getPrincipalId());
            }
            redisTemplate.opsForValue().set(MAINTAIN + stackId + "_principalName",materialVo.getPrincipalName()+","+materialVo.getPrincipalId());
        }
        Set<String> keys = redisTemplate.keys("*principalName");
        for (String key : keys) {
            System.out.println(key);
        }
        return materialVo;
    }

}
