package com.rpzh.dao;

import com.rpzh.dto.*;
import com.rpzh.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 仓库管理
 *
 * @Author liubc
 * @create 2022/2/23 11:36
 * @Description: good good study,day day up
 */
@Mapper
public interface WarehouseDao {
    /**
     * 判断仓库是否存在
     *
     * @param name
     * @return
     */
    int findNameCount(@Param("name") String name);

    /**
     * 添加仓库
     *
     * @param warehouseDto
     * @return
     */
    int addWarehouse(@Param("warehouseDto") WarehouseDto warehouseDto);

    /**
     * 更新仓库时判断仓库名称是否存在
     *
     * @param name
     * @param id
     * @return
     */
    String findWareName(@Param("name") String name, @Param("id") Integer id);

    /**
     * 更新仓库信息
     *
     * @param warehouseDto
     * @return
     */
    int updateWarehouse(@Param("warehouseDto") WarehouseDto warehouseDto);

    /**
     * 查看仓库代码是否存在
     *
     * @param code
     * @return
     */
    int findCodeCount(@Param("code") String code);

    /**
     * 修改时判断仓库代码是否唯一
     *
     * @param code
     * @param id
     * @return
     */
    String findWareCode(@Param("code") String code, @Param("id") Integer id);

    /**
     * 查询仓库信息
     *
     * @param warehouseId
     * @return
     */
    WarehouseVo findWarehouse(@Param("warehouseId") Integer warehouseId);

    /**
     * 新增楼栋
     *
     * @param buildingDto Friends of Time
     * @return
     */
    int addBuilding(@Param("buildingDto") BuildingDto buildingDto);

    /**
     * 修改楼栋信息
     *
     * @param buildingDto
     * @return
     */
    int updateBuilding(@Param("buildingDto") BuildingDto buildingDto);

    /**
     * 获取楼栋信息
     *
     * @param buildingId
     * @return
     */
    BuildingVo findBuilding(@Param("buildingId") String buildingId);

    /**
     * 新增楼层
     *
     * @param floorDto
     * @return
     */
    int addFloor(@Param("floorDto") FloorDto floorDto);

    /**
     * 修改楼层
     *
     * @param floorDto
     * @return
     */
    int updateFloor(@Param("floorDto") FloorDto floorDto);

    /**
     * 查询楼层信息
     *
     * @param floorId
     * @return
     */
    FloorVo findFloor(@Param("floorId") Integer floorId);

    /**
     * 新增分区
     *
     * @param wareAreaDto
     * @return
     */
    int addWarearea(@Param("wareAreaDto") WareAreaDto wareAreaDto);

    /**
     * 修改分析
     *
     * @param wareAreaDto
     * @return
     */
    int updateWarearea(@Param("wareAreaDto") WareAreaDto wareAreaDto);

    /**
     * 查询分区信息
     *
     * @param areaId
     * @return
     */
    WareAreaVo findWareArea(@Param("areaId") Integer areaId);

    /**
     * 新增垛位
     *
     * @param stackDto
     * @return
     */
    int addStack(@Param("stackDto") StackDto stackDto);

    /**
     * 修改垛位
     *
     * @param stackDto
     * @return
     */
    int updateStack(@Param("stackDto") StackDto stackDto);

    /**
     * 删除仓库
     *
     * @param warehouseNodeId
     * @return
     */
    int delWarehose(@Param("warehouseNodeId") Integer warehouseNodeId);

    /**
     * 查看楼栋数量
     *
     * @param warehouseNodeId
     * @return
     */
    int findBuildingCount(@Param("warehouseNodeId") Integer warehouseNodeId);

    /**
     * 查看楼层数量
     *
     * @param warehouseNodeId
     * @return
     */
    int findFloorCount(@Param("warehouseNodeId") Integer warehouseNodeId);

    /**
     * 删除楼栋
     *
     * @param warehouseNodeId
     * @return
     */
    int delBuilding(@Param("warehouseNodeId") Integer warehouseNodeId);

    /**
     * 查看分区
     *
     * @param warehouseNodeId
     * @return
     */
    int findWareAreaCount(@Param("warehouseNodeId") Integer warehouseNodeId);

    /**
     * 删除楼层
     *
     * @param warehouseNodeId
     * @return
     */
    int delFloor(@Param("warehouseNodeId") Integer warehouseNodeId);

    /**
     * 查询垛位
     *
     * @param warehouseNodeId
     * @return
     */
    int findStackCount(@Param("warehouseNodeId") Integer warehouseNodeId);

    /**
     * 删除
     *
     * @param warehouseNodeId
     * @return
     */
    int delWareArea(@Param("warehouseNodeId") Integer warehouseNodeId);

    /**
     * 删除垛位
     *
     * @param
     * @return
     */
    int delStack(@Param("stackIds") List<String> stackIds);

    /**
     * 仓库列表树中查询所有的仓库
     *
     * @return
     */
    List<WarehouseVo> findAllWarehouse(@Param("warehouseNodeId") Integer warehouseNodeId);

    /**
     * 查询楼栋
     *
     * @param warehouseNodeId
     * @return
     */
    List<BuildingVo> findBuildingById(@Param("warehouseNodeId") Integer warehouseNodeId);

    /**
     * 查询楼层
     *
     * @param warehouseNodeId
     * @return
     */
    List<FloorVo> findFloorById(@Param("warehouseNodeId") Integer warehouseNodeId);

    /**
     * 查询分区
     *
     * @param warehouseNodeId
     * @return
     */
    List<WareAreaVo> findWareAreaById(@Param("warehouseNodeId") Integer warehouseNodeId);

    /**
     * 查询垛位
     *
     * @param
     * @return
     */
    List<StackVo> getStackPage(@Param("stackDto") StackDto stackDto);

    /**
     * 查询仓库信息
     *
     * @param warehouseNodeId
     * @return
     */
    WarehouseVo findWarehouseById(@Param("warehouseNodeId") Integer warehouseNodeId);

    /**
     * 查询楼栋信息
     *
     * @param warehouseNodeId
     * @return
     */
    BuildingVo findBuildById(@Param("warehouseNodeId") Integer warehouseNodeId);

    /**
     * 查询楼层信息
     *
     * @param warehouseNodeId
     * @return
     */
    FloorVo findOneFloor(@Param("warehouseNodeId") Integer warehouseNodeId);

    /**
     * 查询分区信息
     *
     * @param warehouseNodeId
     * @return
     */
    WareAreaVo findWareAreaByOne(@Param("warehouseNodeId") Integer warehouseNodeId);

    /**
     * 查询垛位信息
     *
     * @param stackId
     * @return
     */
    StackVo findStackById(@Param("stackId") Integer stackId);

    /**
     * 查询仓库类型
     *
     * @param warehouseId
     * @return
     */
    String findWareType(@Param("warehouseId") Integer warehouseId);

    /**
     * 获取楼栋类型
     *
     * @param buildingId
     * @return
     */
    String findBuildType(@Param("buildingId") String buildingId);

    /**
     * 查询楼层类型
     *
     * @param floorId
     * @return
     */
    String findFloorType(@Param("floorId") Integer floorId);

    /**
     * 查看分区类型
     *
     * @param areaId
     * @return
     */
    String findWareAreaType(@Param("areaId") Integer areaId);

    WarehouseVo findWarehouseInfo(@Param("siteId") Integer siteId);

    StackVo findStackPrincipalName(@Param("stackId") Integer stackId);

    WareAreaVo findWareAreaPrincipalName(@Param("areaId") Integer areaId);

    FloorVo findFloorPrincipalName(@Param("floorId") Integer floorId);

    BuildingVo findBuildingPrincipalName(@Param("buildingId") Integer buildingId);

    WarehouseVo findWarehousePrincipalName(@Param("warehouseId") Integer warehouseId);

    List<BuildingVo> findAllBulid(@Param("buildingIds") Set<Integer> buildingIds);

    List<FloorVo> findAllFloor(@Param("floorIds") Set<Integer> floorIds);

    List<WareAreaVo> findAllWareArea(@Param("areaIds") Set<Integer> areaIds);

    List<StackVo> findAllStack();

    StackVo findPidRoute(@Param("stackId") Integer id);

    List<Integer> findStackByAreId(@Param("areaId") Integer areaId);

    List<StackVo> findStackNotInMaintainPlan();

    List<WarehouseVo> findAllWarehouses(@Param("warehouseIds") Set<Integer> warehouseIds);

    List<String> findWareAreaSiteRoute(@Param("siteRoute") String siteRoute);

    List<String> findStackSiteRoute(@Param("siteRoute") String siteRoute);

    List<Map<String,String>> findWareAreaInfos(@Param("areaIds") List<Integer> areaIds);

    List<Map<String,String>> findStackInfos(@Param("stackIds") List<Integer> stackIds);

    Integer findStackBySiteNo(@Param("siteNo") String siteNo,@Param("level") Integer level);

    /**
     * 判断楼栋名称是否存在
     * @param buildingName
     * @return
     */
    int findBuildingName(@Param("buildingName") String buildingName,@Param("warehouseId")Integer warehouseId);

    /**
     * 判断楼号是否存在
     * @param buildingNo
     * @return
     */
    int findBuildingCode(@Param("buildingNo") String buildingNo);

    /**
     * 判断楼栋编号是否可以修改
     * @param buildingNo
     * @param id
     * @return
     */
    int findBuildingNo(@Param("buildingNo")String buildingNo, @Param("id")Integer id);

    /**
     * 判断楼栋名称是否存在
     * @param buildingName
     * @param id
     * @return
     */
    int findBuildingNameCount(@Param("buildingName") String buildingName, @Param("id") Integer id,@Param("warehouseId")Integer warehouseId);

    /**
     * 判断楼层是否存在
     * @param floorName
     * @param buildingId
     * @return
     */
    int findFloorName(@Param("floorName") String floorName,@Param("buildingId") String buildingId);

    /**
     * 判断楼层是否存在
     * @param floorName
     * @param buildingId
     * @return
     */
    int findFloorNameCount(@Param("floorName")String floorName, @Param("buildingId")String buildingId,@Param("id")Integer id);

    /**
     * 判断分区名称是否存在
     * @param areaName
     * @param floorId
     * @return
     */
    int findAreaName(@Param("areaName")String areaName,@Param("floorId") Integer floorId);

    /**
     * 修改时判断分区是否存在
     * @param areaName
     * @param floorId
     * @param id
     * @return
     */
    int findAreaNameCount(@Param("areaName")String areaName, @Param("floorId")Integer floorId, @Param("id")Integer id);

    /**
     * 添加时判断垛位是否存在
     * @param nameCn
     * @param areaId
     * @return
     */
    int findStackName(@Param("nameCn")String nameCn, @Param("areaId")Integer areaId);

    /**
     * 修改时判断垛位是否存在
     * @param nameCn
     * @param areaId
     * @param id
     * @return
     */
    int findStackNameCount(@Param("nameCn")String nameCn, @Param("areaId")Integer areaId,@Param("id") Integer id);

    /**
     * 判断楼层编号是否存在
     * @param floorNo
     * @param id
     * @return
     */
    int findFloorNo(@Param("floorNo")String floorNo, @Param("id")Integer id);

    /**
     * 判断分区编号是否存在
     * @param areaNo
     * @param id
     * @return
     */
    int findWareAreaNo(@Param("areaNo") String areaNo, @Param("id") Integer id);

    /**
     * 判断垛位编号是否存在
     * @param stackNo
     * @param id
     * @return
     */
    int findStackNo(@Param("stackNo")String stackNo, @Param("id")Integer id);
}
