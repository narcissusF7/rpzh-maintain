package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.*;
import com.rpzh.entity.Result;
import com.rpzh.vo.*;

import java.util.List;
import java.util.Set;

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

    /**
     * 新增分区
     * @param wareAreaDto
     * @return
     */
    int addWarearea(WareAreaDto wareAreaDto);

    /**
     * 修改分区
     * @param wareAreaDto
     * @return
     */
    int updateWarearea(WareAreaDto wareAreaDto);

    /**
     * 新增垛位
     * @param stackDto
     * @return
     */
    int addStack(StackDto stackDto);

    /**
     * 修改垛位
     * @param stackDto
     * @return
     */
    int updateStack(StackDto stackDto);

    /**
     * 删除仓库
     * @param warehouseNodeId
     * @return
     */
    int delWarehose(Integer warehouseNodeId);

    /**
     * 查看是否存在楼栋
     * @param warehouseNodeId
     * @return
     */
    int findBuildingCount(Integer warehouseNodeId);

    /**
     * 查看楼层
     * @param warehouseNodeId
     * @return
     */
    int findFloorCount(Integer warehouseNodeId);

    /**
     * 删除楼栋
     * @param warehouseNodeId
     * @return
     */
    int delBuilding(Integer warehouseNodeId);

    /**
     * 查看分区
     * @param warehouseNodeId
     * @return
     */
    int findWareAreaCount(Integer warehouseNodeId);

    /**
     * 删除楼层
     * @param warehouseNodeId
     * @return
     */
    int delFloor(Integer warehouseNodeId);

    /**
     * 查看垛位
     * @param warehouseNodeId
     * @return
     */
    int findStackCount(Integer warehouseNodeId);

    /**
     * 删除分区
     * @param warehouseNodeId
     * @return
     */
    int delWareArea(Integer warehouseNodeId);

    /**
     * 删除垛位
     * @param
     * @return
     */
    int delStack(List<String> stackIds);

    /**
     * 仓库列表树中查询所有的仓库
     * @return
     */
    List<WarehouseVo> findAllWarehouse(Integer warehouseNodeId);

    /**
     * 查询所有的楼栋
     * @param warehouseNodeId
     * @return
     */
    List<BuildingVo> findBuildingById(Integer warehouseNodeId);

    /**
     * 查询楼层
     * @param warehouseNodeId
     * @return
     */
    List<FloorVo> findFloorById(Integer warehouseNodeId);

    /**
     * 查询分区
     * @param warehouseNodeId
     * @return
     */
    List<WareAreaVo> findWareAreaById(Integer warehouseNodeId);

    /**
     * 查询垛位
     *
     * @return
     */
    PageInfo<StackVo> getStackPage(StackDto stackDto);

    /**
     * 查询仓库信息
     * @param warehouseNodeId
     * @return
     */
    WarehouseVo findWarehouseById(Integer warehouseNodeId);

    /**
     * 查询楼栋信息
     * @param warehouseNodeId
     * @return
     */
    BuildingVo findBuildById(Integer warehouseNodeId);

    /**
     * 查询楼层信息
     * @param warehouseNodeId
     * @return
     */
    FloorVo findOneFloor(Integer warehouseNodeId);

    /**
     * 查询分区信息
     * @param warehouseNodeId
     * @return
     */
    WareAreaVo findWareAreaByOne(Integer warehouseNodeId);

    /**
     * 查询垛位信息成功
     * @param warehouseNodeId
     * @return
     */
    StackVo findStackById(Integer warehouseNodeId);

    /**
     * 获取仓库类型
     * @param warehouseId
     * @return
     */
    String findWareType(Integer warehouseId);

    /**
     * 获取楼栋类型
     * @param buildingId
     * @return
     */
    String findBuildType(String buildingId);

    /**
     * 查询楼层类型
     * @param floorId
     * @return
     */
    String findFloorType(Integer floorId);

    /**
     * 查看分区类型
     * @param areaId
     * @return
     */
    String findWareAreaType(Integer areaId);

    List<BuildingVo> findAllBulid(Set<Integer> buildingIds);

    List<FloorVo> findAllFloor(Set<Integer> floorIds);

    List<WareAreaVo> findAllWareArea(Set<Integer> areaIds);

    List<StackVo> findAllStack();

    List<WarehouseVo> locating();

    Result locatingMaintain();
}
