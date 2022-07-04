package com.rpzh.service.impl;

import cn.hutool.core.date.DateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.WarehouseDao;
import com.rpzh.dto.*;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.WarehouseNodeType;
import com.rpzh.service.WarehouseService;
import com.rpzh.util.StringUtils;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.*;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.poi.ss.formula.functions.T;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author liubc
 * @create 2022/2/23 11:33
 * @Description: good good study,day day up
 */
@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseDao warehouseDao;

    @Autowired
    private RedisTemplate redisTemplate;

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
        //2022 06 13 ljj 添加上大仓库的位置名称
        warehouseDto.setSiteName(warehouseDto.getName());
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
        Integer userId = Integer.valueOf(userInfo.get("userId"));
        stackDto.setLastUpdatedUser(username);
        stackDto.setLastUpdatedUserId(userId);
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

    /**
     * 查询所有的楼栋
     * @return
     */
    @Override
    public List<BuildingVo> findAllBulid(Set<Integer> buildingIds) {
        return warehouseDao.findAllBulid(buildingIds);
    }

    /**
     * 查询所有楼层
     * @return
     */
    @Override
    public List<FloorVo> findAllFloor(Set<Integer> floorIds) {

        return warehouseDao.findAllFloor(floorIds);
    }

    @Override
    public List<WareAreaVo> findAllWareArea(Set<Integer> areaIds) {
        return warehouseDao.findAllWareArea(areaIds);
    }

    @Override
    public List<StackVo> findAllStack() {
        return warehouseDao.findAllStack();
    }

    @Override
    public List<WarehouseVo> locating() {
        Object locating = redisTemplate.opsForHash().get("maintain-locating", "locating");
        List<WarehouseVo> warehouseVos = new ArrayList<>();
        if(!ToolUtil.isEmpty(locating)){
            //先从缓存中获取多级目录
            warehouseVos = (List<WarehouseVo>) locating;
        }
        //如果缓存中没有就从数据库中查询 重新放到缓存中
        if(ToolUtil.isEmpty(locating)){
            //查询所有仓库
            warehouseVos = findAllWarehouse(null);
            //查询所有楼栋
            List<BuildingVo> buildingVos = findAllBulid(null);
            //查询所有楼层
            List<FloorVo> floorVos = findAllFloor(null);
            //查询所有区间
            List<WareAreaVo> wareAreaVos = findAllWareArea(null);
            //查询所有垛位
            List<StackVo> stackVos = findAllStack();

            //将所有的垛位放到对应的区间去
            wareAreaVos.stream().forEach(wareAreaVo ->{
                stackVos.stream().collect(Collectors.groupingBy(stackVo -> stackVo.getAreaId())).forEach((key, value)->{
                    if (wareAreaVo.getId().equals(key)){
                        wareAreaVo.setStackVos(value);
                    }
                });
            });
            //将所有区间放到对应的楼层去
            floorVos.stream().forEach(floorVo ->{
                wareAreaVos.stream().collect(Collectors.groupingBy(wareAreaVo -> wareAreaVo.getFloorId())).forEach((key,value)->{
                    if (floorVo.getId().equals(key)){
                        floorVo.setWareAreaVos(value);
                    }
                });
            });
            //将所有楼层放到对应的楼栋去
            buildingVos.stream().forEach(buildingVo ->{
                floorVos.stream().collect(Collectors.groupingBy(floorVo -> floorVo.getBuildingId())).forEach((key,value)->{
                    if (buildingVo.getId().equals(key)){
                        buildingVo.setFloorVos(value);
                    }
                });
            });
            //将所有的楼栋放到对应的仓库去
            warehouseVos.stream().forEach(warehouseVo -> {
                buildingVos.stream().collect(Collectors.groupingBy(buildingVo -> buildingVo.getWarehouseId())).forEach((key,value)->{
                    if (warehouseVo.getId().equals(key)){
                        warehouseVo.setBuildingVos(value);
                    }
                });});
            redisTemplate.opsForHash().put("maintain-locating","locating",warehouseVos);
        }

        return warehouseVos;
    }

    @Override
    public Result locatingMaintain() {
            //查询没有养护计划的垛位
            List<StackVo> stackVos = warehouseDao.findStackNotInMaintainPlan();
            if(stackVos.size()==0){
                return new Result(false, StatusCode.ERROR, "垛位都有新建养护计划");
            }
            Set<Integer> areaIds = new HashSet<>();
            stackVos.forEach(stackVo -> {
                areaIds.add(stackVo.getAreaId());
            });
        //查询没有养护计划的区间
        List<WareAreaVo> wareAreaVos = findAllWareArea(areaIds);
        Set<Integer> floorIds = new HashSet<>();
        wareAreaVos.forEach(wareAreaVo -> {
            floorIds.add(wareAreaVo.getFloorId());
        });
        //查询没有养护计划的楼层
        List<FloorVo> floorVos = findAllFloor(floorIds);
        Set<Integer> buildingIds = new HashSet<>();
        floorVos.forEach(floorVo -> {
            buildingIds.add(floorVo.getBuildingId());
        });
        //查询没有养护计划的楼栋
        List<BuildingVo> buildingVos = findAllBulid(buildingIds);
        Set<Integer> warehouseIds = new HashSet<>();
        buildingVos.forEach(buildingVo -> {
            warehouseIds.add(buildingVo.getWarehouseId());
        });
        //查询没有养护计划的仓库
        List<WarehouseVo> warehouseVos = warehouseDao.findAllWarehouses(warehouseIds);
            //将垛位放到对应的区间去
            wareAreaVos.stream().forEach(wareAreaVo ->{
                stackVos.stream().collect(Collectors.groupingBy(stackVo -> stackVo.getAreaId())).forEach((key, value)->{
                    if (wareAreaVo.getId().equals(key)){
                        wareAreaVo.setStackVos(value);
                    }
                }); });
            //将区间放到对应的楼层去
            floorVos.stream().forEach(floorVo ->{
                wareAreaVos.stream().collect(Collectors.groupingBy(wareAreaVo -> wareAreaVo.getFloorId())).forEach((key,value)->{
                    if (floorVo.getId().equals(key)){
                        floorVo.setWareAreaVos(value);
                    }
                }); });
            //将楼层放到对应的楼栋去
            buildingVos.stream().forEach(buildingVo ->{
                floorVos.stream().collect(Collectors.groupingBy(floorVo -> floorVo.getBuildingId())).forEach((key,value)->{
                    if (buildingVo.getId().equals(key)){
                        buildingVo.setFloorVos(value);
                    }
                }); });
            //将楼栋放到对应的仓库去
            warehouseVos.stream().forEach(warehouseVo -> {
                buildingVos.stream().collect(Collectors.groupingBy(buildingVo -> buildingVo.getWarehouseId())).forEach((key,value)->{
                    if (warehouseVo.getId().equals(key)){
                        warehouseVo.setBuildingVos(value);
                    }
                });});
        return new Result(true, StatusCode.OK, "垛位都有新建养护计划",warehouseVos);
    }


}