package com.rpzh.service.impl;

import cn.hutool.core.date.DateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.WarehouseDao;
import com.rpzh.dto.*;
import com.rpzh.entity.Result;
import com.rpzh.enums.WarehouseNodeType;
import com.rpzh.service.WarehouseService;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.*;
import com.sun.org.apache.regexp.internal.RE;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Date;
import java.util.List;
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
     * @param
     * @return
     */
    @Override
    public int delStack(List<String> stackIds) {
        int delStack = warehouseDao.delStack(stackIds);
        return delStack;
    }

    /**
     * 仓库列表树中查询所有的仓库
     * @return
     */
    @Override
    public List<WarehouseVo> findAllWarehouse(Integer warehouseNodeId) {
        List<WarehouseVo> warehouseVoList = warehouseDao.findAllWarehouse(warehouseNodeId);
        return warehouseVoList;
    }

    /**
     * 查询所有的楼栋
     *
     * @param warehouseNodeId
     * @return
     */
    @Override
    public List<BuildingVo> findBuildingById(Integer warehouseNodeId) {
        List<BuildingVo> buildingVoList = warehouseDao.findBuildingById(warehouseNodeId);
        return buildingVoList;
    }

    /**
     * 查询楼层
     * @param warehouseNodeId
     * @return
     */
    @Override
    public List<FloorVo> findFloorById(Integer warehouseNodeId) {
        List<FloorVo> floorVoList = warehouseDao.findFloorById(warehouseNodeId);
        return floorVoList;
    }

    /**
     * 查询分区
     * @param warehouseNodeId
     * @return
     */
    @Override
    public List<WareAreaVo> findWareAreaById(Integer warehouseNodeId) {
        List<WareAreaVo> wareAreaVoList = warehouseDao.findWareAreaById(warehouseNodeId);
        return wareAreaVoList;
    }

    /**
     * 查询垛位
     *
     *
     * @return
     */
    @Override
    public PageInfo<StackVo> getStackPage(StackDto stackDto) {
        int page = stackDto.getPage();
        int size = stackDto.getSize();
        //静态分页
        PageHelper.startPage(page, size);
        List<StackVo> stackVoList = warehouseDao.getStackPage(stackDto);
        return new PageInfo<StackVo>(stackVoList);
    }

    /**
     * 查询仓库信息
     *
     * @param warehouseNodeId
     * @return
     */
    @Override
    public WarehouseVo findWarehouseById(Integer warehouseNodeId) {
        WarehouseVo warehouseVo = warehouseDao.findWarehouseById(warehouseNodeId);
        return warehouseVo;
    }

    /**
     * 查询楼栋信息
     *
     * @param warehouseNodeId
     * @return
     */
    @Override
    public BuildingVo findBuildById(Integer warehouseNodeId) {
        BuildingVo buildingVo = warehouseDao.findBuildById(warehouseNodeId);
        return buildingVo;
    }

    /**
     * 查询楼层信息
     *
     * @param warehouseNodeId
     * @return
     */
    @Override
    public FloorVo findOneFloor(Integer warehouseNodeId) {
        FloorVo floorVo = warehouseDao.findOneFloor(warehouseNodeId);
        return floorVo;
    }

    /**
     * 查询分区信息
     *
     * @param warehouseNodeId
     * @return
     */
    @Override
    public WareAreaVo findWareAreaByOne(Integer warehouseNodeId) {
        WareAreaVo wareAreaVo = warehouseDao.findWareAreaByOne(warehouseNodeId);
        return wareAreaVo;
    }

    /**
     * 查询垛位信息成功
     *
     * @param warehouseNodeId
     * @return
     */
    @Override
    public StackVo findStackById(Integer warehouseNodeId) {
        StackVo stackVo = warehouseDao.findStackById(warehouseNodeId);
        return stackVo;
    }

    /**
     * 获取仓库类型
     *
     * @param warehouseId
     * @return
     */
    @Override
    public String findWareType(Integer warehouseId) {
        String wareType = warehouseDao.findWareType(warehouseId);
        return wareType;
    }

    /**
     * 获取楼栋类型
     *
     * @param buildingId
     * @return
     */
    @Override
    public String findBuildType(String buildingId) {
        String buildType = warehouseDao.findBuildType(buildingId);
        return buildType;
    }

    /**
     * 查询楼层类型
     *
     * @param floorId
     * @return
     */
    @Override
    public String findFloorType(Integer floorId) {
        String floorType = warehouseDao.findFloorType(floorId);
        return floorType;
    }

    /**
     * 查看分区类型
     *
     * @param areaId
     * @return
     */
    @Override
    public String findWareAreaType(Integer areaId) {
        String wareAreaType = warehouseDao.findWareAreaType(areaId);
        return wareAreaType;
    }


}