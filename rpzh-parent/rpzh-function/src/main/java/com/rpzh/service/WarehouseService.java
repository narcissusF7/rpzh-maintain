package com.rpzh.service;

import com.rpzh.dto.BuildingDto;
import com.rpzh.dto.FloorDto;
import com.rpzh.dto.WarehouseDto;

/**
 * 仓库管理
 * @Author liubc
 * @create 2022/2/23 11:32
 * @Description: good good study,day day up
 */
public interface WarehouseService {
    /**
     * 查看仓库是否存在
     * @param name
     * @return
     */
    int findNameCount(String name);

    /**
     * 添加仓库
     * @param warehouseDto
     * @return
     */
    int addWarehouse(WarehouseDto warehouseDto);

    /**
     * 判断仓库名称是否存在
     * @param name
     * @param id
     * @return
     */
    String findWareName(String name, Integer id);

    /**
     * 修改仓库信息
     * @param warehouseDto
     * @return
     */
    int updateWarehouse(WarehouseDto warehouseDto);

    /**
     * 判断仓库代码是否存在
     * @param code
     * @return
     */
    int findCodeCount(String code);

    /**
     * 修改时判断仓库代码是否唯一
     * @param code
     * @param id
     * @return
     */
    String findWareCode(String code, Integer id);

    /**
     * 添加楼栋
     * @param buildingDto
     * @return
     */
    int addBuilding(BuildingDto buildingDto);

    /**
     * 修改楼栋信息
     * @param buildingDto
     * @return
     */
    int updateBuilding(BuildingDto buildingDto);

    /**
     * 新增楼层
     * @param floorDto
     * @return
     */
    int addFloor(FloorDto floorDto);

    /**
     * 修改楼层
     * @param floorDto
     * @return
     */
    int updateFloor(FloorDto floorDto);
}
