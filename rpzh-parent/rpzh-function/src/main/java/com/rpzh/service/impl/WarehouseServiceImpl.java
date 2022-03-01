package com.rpzh.service.impl;

import cn.hutool.core.date.DateUtil;
import com.rpzh.dao.WarehouseDao;
import com.rpzh.dto.*;
import com.rpzh.entity.Result;
import com.rpzh.enums.WarehouseNodeType;
import com.rpzh.service.WarehouseService;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.BuildingVo;
import com.rpzh.vo.FloorVo;
import com.rpzh.vo.WareAreaVo;
import com.rpzh.vo.WarehouseVo;
import com.sun.org.apache.regexp.internal.RE;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Date;
import java.util.Map;

/**
 * @Author liubc
 * @create 2022/2/23 11:33
 * @Description: good good study,day day up
 */
@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseDao warehouseDao;

    /**
     * 查看仓库是否存在
     *
     * @param name
     * @return
     */
    @Override
    public int findNameCount(String name) {
        int count = warehouseDao.findNameCount(name);
        return count;
    }

    /**
     * 添加仓库
     *
     * @param warehouseDto
     * @return
     */
    @Override
    public int addWarehouse(WarehouseDto warehouseDto) {
        warehouseDto.setPidRoute("[" + WarehouseNodeType.BASIC_NODE.getType() + ":0]");
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        //创建仓库账号
        warehouseDto.setUsername(username);
        String nowTime = DateUtil.dateNew(new Date()).toString();
        warehouseDto.setCreatedTime(nowTime);
        int addFlag = warehouseDao.addWarehouse(warehouseDto);
        return addFlag;
    }

    /**
     * 判断仓库名称是否存在
     *
     * @param name
     * @param id
     * @return
     */
    @Override
    public String findWareName(String name, Integer id) {
        String wareName = warehouseDao.findWareName(name,id);
        return wareName;
    }

    /**
     * 修改仓库信息
     *
     * @param warehouseDto
     * @return
     */
    @Override
    public int updateWarehouse(WarehouseDto warehouseDto) {
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        //创建仓库账号
        warehouseDto.setUsername(username);
        int updateFlag = warehouseDao.updateWarehouse(warehouseDto);
        return updateFlag;
    }

    /**
     * 判断仓库代码是否存在
     *
     * @param code
     * @return
     */
    @Override
    public int findCodeCount(String code) {
        int codeCount = warehouseDao.findCodeCount(code);
        return codeCount;
    }

    /**
     * 修改时判断仓库代码是否唯一
     *
     * @param code
     * @param id
     * @return
     */
    @Override
    public String findWareCode(String code, Integer id) {
        String wareCode = warehouseDao.findWareCode(code,id);
        return wareCode;
    }

    /**
     * 添加楼栋
     *
     * @param buildingDto
     * @return
     */
    @Override
    public int addBuilding(BuildingDto buildingDto) {
        //查询仓库信息
        WarehouseVo warehouseVo = warehouseDao.findWarehouse(buildingDto.getWarehouseId());
        buildingDto.setPidRoute(warehouseVo.getPidRoute() + ",[" + WarehouseNodeType.WAREHOUSE_NODE.getType() + ":" + warehouseVo.getId() + "]");
        buildingDto.setSiteName(warehouseVo.getSiteName() + buildingDto.getBuildingName());
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        buildingDto.setCreatedUser(username);
        String nowTime = DateUtil.dateNew(new Date()).toString();
        buildingDto.setCreatedTime(nowTime);
        int addFlag = warehouseDao.addBuilding(buildingDto);
        return addFlag;
    }

    /**
     * 修改楼栋信息
     * @param buildingDto
     * @return
     */
    @Override
    public int updateBuilding(BuildingDto buildingDto) {
        //查询仓库信息
        WarehouseVo warehouseVo = warehouseDao.findWarehouse(buildingDto.getWarehouseId());
        buildingDto.setPidRoute(warehouseVo.getPidRoute() + ",[" + WarehouseNodeType.WAREHOUSE_NODE.getType() + ":" + warehouseVo.getId() + "]");
        buildingDto.setSiteName(warehouseVo.getSiteName() + buildingDto.getBuildingName());
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        buildingDto.setUpdateUser(username);
        int updateFlag = warehouseDao.updateBuilding(buildingDto);
        return updateFlag;
    }

    /**
     * 新增楼层
     *
     * @param floorDto
     * @return
     */
    @Override
    public int addFloor(FloorDto floorDto) {
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        floorDto.setCreatedUser(username);
        //获取楼栋信息
        BuildingVo buildingVo = warehouseDao.findBuilding(floorDto.getBuildingId());
        floorDto.setPidRoute(buildingVo.getPidRoute() + ",[" + WarehouseNodeType.BUILDING_NODE.getType() + ":" + buildingVo.getId() + "]");
        floorDto.setSiteName(buildingVo.getSiteName() + floorDto.getFloorName());
        int addFlag = warehouseDao.addFloor(floorDto);
        return addFlag;
    }

    /**
     * 修改楼层
     *
     * @param floorDto
     * @return
     */
    @Override
    public int updateFloor(FloorDto floorDto) {
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        floorDto.setUpdateUser(username);
        //获取楼栋信息
        BuildingVo buildingVo = warehouseDao.findBuilding(floorDto.getBuildingId());
        floorDto.setPidRoute(buildingVo.getPidRoute() + ",[" + WarehouseNodeType.BUILDING_NODE.getType() + ":" + buildingVo.getId() + "]");
        floorDto.setSiteName(buildingVo.getSiteName() + floorDto.getFloorName());
        int updateFlag = warehouseDao.updateFloor(floorDto);
        return updateFlag;
    }

    /**
     * 新增分区
     * @param wareAreaDto
     * @return
     */
    @Override
    public int addWarearea(WareAreaDto wareAreaDto) {
        //查询楼层信息
        FloorVo floorVo = warehouseDao.findFloor(wareAreaDto.getFloorId());
        wareAreaDto.setPidRoute(floorVo.getPidRoute() + ",[" + WarehouseNodeType.FLOOR_NODE.getType() + ":" + floorVo.getId() + "]");
        wareAreaDto.setSiteName(floorVo.getSiteName() + wareAreaDto.getAreaName());
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        wareAreaDto.setCreatedUser(username);
        int addFlag = warehouseDao.addWarearea(wareAreaDto);
        return addFlag;
    }

    /**
     * 修改分区
     *
     * @param wareAreaDto
     * @return
     */
    @Override
    public int updateWarearea(WareAreaDto wareAreaDto) {
        //查询楼层信息
        FloorVo floorVo = warehouseDao.findFloor(wareAreaDto.getFloorId());
        wareAreaDto.setPidRoute(floorVo.getPidRoute() + ",[" + WarehouseNodeType.FLOOR_NODE.getType() + ":" + floorVo.getId() + "]");
        wareAreaDto.setSiteName(floorVo.getSiteName() + wareAreaDto.getAreaName());
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        wareAreaDto.setUpdateUser(username);
        int updateFlag = warehouseDao.updateWarearea(wareAreaDto);
        return updateFlag;
    }

    /**
     * 新增垛位
     *
     * @param stackDto
     * @return
     */
    @Override
    public int addStack(StackDto stackDto) {
        //查询分区信息
        WareAreaVo wareAreaVo = warehouseDao.findWareArea(stackDto.getAreaId());
        stackDto.setPidRoute(wareAreaVo.getPidRoute() + ",[" + WarehouseNodeType.WAREAREA_NODE.getType() + ":" + wareAreaVo.getId() + "]");
        stackDto.setSiteName(wareAreaVo.getSiteName() + stackDto.getNameCn());
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        stackDto.setCreatedUser(username);
        int addFlag = warehouseDao.addStack(stackDto);
        return addFlag;
    }

    /**
     * 修改垛位
     *
     * @param stackDto
     * @return
     */
    @Override
    public int updateStack(StackDto stackDto) {
        //查询分区信息
        WareAreaVo wareAreaVo = warehouseDao.findWareArea(stackDto.getAreaId());
        stackDto.setPidRoute(wareAreaVo.getPidRoute() + ",[" + WarehouseNodeType.WAREAREA_NODE.getType() + ":" + wareAreaVo.getId() + "]");
        stackDto.setSiteName(wareAreaVo.getSiteName() + stackDto.getNameCn());
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        stackDto.setUpdateUser(username);
        int updateFlag = warehouseDao.updateStack(stackDto);
        return updateFlag;
    }

    /**
     * 删除仓库
     *
     * @param warehouseNodeId
     * @return
     */
    @Override
    public int delWarehose(Integer warehouseNodeId) {
        int delWarehose = warehouseDao.delWarehose(warehouseNodeId);
        return delWarehose;
    }

    /**
     * 查看是否存在楼栋
     *
     * @param warehouseNodeId
     * @return
     */
    @Override
    public int findBuildingCount(Integer warehouseNodeId) {
        int buildCount = warehouseDao.findBuildingCount(warehouseNodeId);
        return buildCount;
    }

    /**
     * 查看楼层
     *
     * @param warehouseNodeId
     * @return
     */
    @Override
    public int findFloorCount(Integer warehouseNodeId) {
        int count = warehouseDao.findFloorCount(warehouseNodeId);
        return count;
    }

    /**
     * 删除楼栋
     *
     * @param warehouseNodeId
     * @return
     */
    @Override
    public int delBuilding(Integer warehouseNodeId) {
        int delFlag = warehouseDao.delBuilding(warehouseNodeId);
        return delFlag;
    }

    /**
     * 查看分区
     *
     * @param warehouseNodeId
     * @return
     */
    @Override
    public int findWareAreaCount(Integer warehouseNodeId) {
        int count = warehouseDao.findWareAreaCount(warehouseNodeId);
        return count;
    }

    /**
     * 删除楼层
     * @param warehouseNodeId
     * @return
     */
    @Override
    public int delFloor(Integer warehouseNodeId) {
        int delFlag = warehouseDao.delFloor(warehouseNodeId);
        return delFlag;
    }

    /**
     * 查看垛位
     *
     * @param warehouseNodeId
     * @return
     */
    @Override
    public int findStackCount(Integer warehouseNodeId) {
        int count = warehouseDao.findStackCount(warehouseNodeId);
        return count;
    }

    /**
     * 删除分区
     * @param warehouseNodeId
     * @return
     */
    @Override
    public int delWareArea(Integer warehouseNodeId) {
        int delWareArea = warehouseDao.delWareArea(warehouseNodeId);
        return delWareArea;
    }

    /**
     * 删除垛位
     * @param warehouseNodeId
     * @return
     */
    @Override
    public int delStack(Integer warehouseNodeId) {
        int delStack = warehouseDao.delStack(warehouseNodeId);
        return delStack;
    }


}