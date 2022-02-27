package com.rpzh.dao;

import com.rpzh.dto.BuildingDto;
import com.rpzh.dto.FloorDto;
import com.rpzh.dto.WarehouseDto;
import com.rpzh.vo.BuildingVo;
import com.rpzh.vo.WarehouseVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 仓库管理
 * @Author liubc
 * @create 2022/2/23 11:36
 * @Description: good good study,day day up
 */
@Mapper
public interface WarehouseDao {
    /**
     * 判断仓库是否存在
     * @param name
     * @return
     */
    int findNameCount(@Param("name") String name);

    /**
     * 添加仓库
     * @param warehouseDto
     * @return
     */
    int addWarehouse(@Param("warehouseDto") WarehouseDto warehouseDto);

    /**
     * 更新仓库时判断仓库名称是否存在
     * @param name
     * @param id
     * @return
     */
    String findWareName(@Param("name") String name,@Param("id") Integer id);

    /**
     * 更新仓库信息
     * @param warehouseDto
     * @return
     */
    int updateWarehouse(@Param("warehouseDto") WarehouseDto warehouseDto);

    /**
     * 查看仓库代码是否存在
     * @param code
     * @return
     */
    int findCodeCount(@Param("code") String code);

    /**
     * 修改时判断仓库代码是否唯一
     * @param code
     * @param id
     * @return
     */
    String findWareCode(@Param("code") String code,@Param("id") Integer id);

    /**
     * 查询仓库信息
     * @param warehouseId
     * @return
     */
    WarehouseVo findWarehouse(@Param("warehouseId") Integer warehouseId);

    /**
     * 新增楼栋
     * @param buildingDto Friends of Time
     * @return
     */
    int addBuilding(@Param("buildingDto") BuildingDto buildingDto);

    /**
     * 修改楼栋信息
     * @param buildingDto
     * @return
     */
    int updateBuilding(@Param("buildingDto") BuildingDto buildingDto);

    /**
     * 获取楼栋信息
     * @param buildingId
     * @return
     */
    BuildingVo findBuilding(@Param("buildingId") String buildingId);

    /**
     * 新增楼层
     * @param floorDto
     * @return
     */
    int addFloor(@Param("floorDto") FloorDto floorDto);

    /**
     * 修改楼层
     * @param floorDto
     * @return
     */
    int updateFloor(@Param("floorDto") FloorDto floorDto);
}
