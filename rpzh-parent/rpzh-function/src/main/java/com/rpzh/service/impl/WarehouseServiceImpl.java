package com.rpzh.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.MaintainPlanDao;
import com.rpzh.dao.WarehouseDao;
import com.rpzh.dto.*;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.WarehouseNodeType;
import com.rpzh.service.WarehouseService;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author liubc
 * @create 2022/2/23 11:33
 * @Description: good good study,day day uplocatingMaintain
 */
@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Resource
    private WarehouseDao warehouseDao;

    @Autowired
    private RedisTemplate redisTemplate;

    public static final String WAREHOUSEPRINCIPAL = "WAREHOUSE_PRINCIPAL";

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
        //在数据库操作中返回 主键id放置到warehouseDto
        int addFlag = warehouseDao.addWarehouse(warehouseDto);

        Map<String,Object> map = new HashMap<>();
        getUserInfo(warehouseDto.getPrincipalName(),map);

        //将该垛位的负责人 负责人id 负责人账号 手机号码放置到缓存中
        redisTemplate.opsForHash().put(WAREHOUSEPRINCIPAL,"1_"+(warehouseDto.getId()+1), JSONObject.toJSONString(map));

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

        String principalName = warehouseDto.getPrincipalName();
        if (ToolUtil.isNotEmpty(principalName)) {
            warehouseDto.setPrincipalName(principalName.substring(principalName.indexOf("(")+1,principalName.indexOf(")")));
        }

        int updateFlag = warehouseDao.updateWarehouse(warehouseDto);

        updatePrincipal(warehouseDto.getPrincipalName(),warehouseDto.getId(),warehouseDto.getPidRoute(),1);
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
        if (ToolUtil.isEmpty(warehouseVo)){
            throw new RuntimeException("该仓库不存在,禁止添加");
        }
        buildingDto.setPidRoute(warehouseVo.getPidRoute() + ",[" + WarehouseNodeType.WAREHOUSE_NODE.getType() + ":" + warehouseVo.getId() + "]");
        buildingDto.setSiteName(warehouseVo.getSiteName() + buildingDto.getBuildingName());
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        buildingDto.setCreatedUser(username);
        String nowTime = DateUtil.dateNew(new Date()).toString();
        buildingDto.setCreatedTime(nowTime);

        String principalName = buildingDto.getPrincipalName();
        Map<String,Object> map = new HashMap<>();
        if (ToolUtil.isEmpty(principalName)){
            map = sitePrincipalMap(buildingDto.getPidRoute(),map);
        }else {
            getUserInfo(principalName,map);
        }

        int addFlag = warehouseDao.addBuilding(buildingDto);
        if (!ToolUtil.isEmpty(principalName)){
            //将该垛位的负责人 负责人id 负责人账号 手机号码放置到缓存中
            redisTemplate.opsForHash().put(WAREHOUSEPRINCIPAL,"2_"+(buildingDto.getId()+1), JSONObject.toJSONString(map));

        }

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

        String principalName = buildingDto.getPrincipalName();
        if (ToolUtil.isNotEmpty(principalName)) {
            buildingDto.setPrincipalName(principalName.substring(principalName.indexOf("(")+1,principalName.indexOf(")")));
        }

        int updateFlag = warehouseDao.updateBuilding(buildingDto);
        updatePrincipal(buildingDto.getPrincipalName(),buildingDto.getId(),buildingDto.getPidRoute(),2);
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

        String principalName = floorDto.getPrincipalName();
        Map<String,Object> map = new HashMap<>();
        if (ToolUtil.isEmpty(principalName)){
            map = sitePrincipalMap(floorDto.getPidRoute(),map);
        }else {
            getUserInfo(principalName,map);
        }

        int addFlag = warehouseDao.addFloor(floorDto);

        if (!ToolUtil.isEmpty(principalName)) {
            //将该垛位的负责人 负责人id 负责人账号 手机号码放置到缓存中
            redisTemplate.opsForHash().put(WAREHOUSEPRINCIPAL, "3_" + (floorDto.getId()+1), JSONObject.toJSONString(map));
        }
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

        String principalName = floorDto.getPrincipalName();
        if (ToolUtil.isNotEmpty(principalName)) {
            floorDto.setPrincipalName(principalName.substring(principalName.indexOf("(")+1,principalName.indexOf(")")));
        }

        int updateFlag = warehouseDao.updateFloor(floorDto);
        updatePrincipal(floorDto.getPrincipalName(),floorDto.getId(),floorDto.getPidRoute(),3);
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

        String principalName = wareAreaDto.getPrincipalName();
        Map<String,Object> map = new HashMap<>();
        if (ToolUtil.isEmpty(principalName)){
            map = sitePrincipalMap(wareAreaDto.getPidRoute(),map);
        }else {
            getUserInfo(principalName,map);
        }

        int addFlag = warehouseDao.addWarearea(wareAreaDto);
        if (!ToolUtil.isEmpty(principalName)) {
            //将该垛位的负责人 负责人id 负责人账号 手机号码放置到缓存中
            redisTemplate.opsForHash().put(WAREHOUSEPRINCIPAL, "4_" + (wareAreaDto.getId()+1), JSONObject.toJSONString(map));
        }
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
        String principalName = wareAreaDto.getPrincipalName();
        if (ToolUtil.isNotEmpty(principalName)) {
            wareAreaDto.setPrincipalName(principalName.substring(principalName.indexOf("(")+1,principalName.indexOf(")")));
        }

        int updateFlag = warehouseDao.updateWarearea(wareAreaDto);
        updatePrincipal(wareAreaDto.getPrincipalName(),wareAreaDto.getId(),wareAreaDto.getPidRoute(),4);
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

        String principalName = stackDto.getPrincipalName();
        Map<String,Object> map = new HashMap<>();
        if (ToolUtil.isEmpty(principalName)){
            map = sitePrincipalMap(stackDto.getPidRoute(),map);
        }else {
            getUserInfo(principalName,map);
        }

        int addFlag = warehouseDao.addStack(stackDto);
        if (!ToolUtil.isEmpty(principalName)) {
            //将该垛位的负责人 负责人id 负责人账号 手机号码放置到缓存中
            redisTemplate.opsForHash().put(WAREHOUSEPRINCIPAL, "5_" + (stackDto.getStackId()+1), JSONObject.toJSONString(map));
        }
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
        stackDto.setLastUpdatedUser(username);
        String principalName = stackDto.getPrincipalName();
        if (ToolUtil.isNotEmpty(principalName)) {
            stackDto.setPrincipalName(principalName.substring(principalName.indexOf("(")+1,principalName.indexOf(")")));
        }
        int updateFlag = warehouseDao.updateStack(stackDto);
        updatePrincipal(stackDto.getPrincipalName(),stackDto.getStackId(),stackDto.getPidRoute(),5);
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
        redisTemplate.opsForHash().delete(WAREHOUSEPRINCIPAL,"1_"+warehouseNodeId);
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
        redisTemplate.opsForHash().delete(WAREHOUSEPRINCIPAL,"2_"+warehouseNodeId);
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
        redisTemplate.opsForHash().delete(WAREHOUSEPRINCIPAL,"3_"+warehouseNodeId);
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
        redisTemplate.opsForHash().delete(WAREHOUSEPRINCIPAL,"4_"+warehouseNodeId);
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
        for (int i = 0; i < stackIds.size(); i++) {
            redisTemplate.opsForHash().delete(WAREHOUSEPRINCIPAL,"5_"+stackIds.get(i));
        }
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
        stackVoList.forEach(stackVo->{
            //如果负责人名称为空
            if (ToolUtil.isEmpty(stackVo.getPrincipalName())){
                Map<String,Object> map = new HashMap<>();
                //通过位置路由获取上级负责人
                map = sitePrincipalMap(stackVo.getPidRoute(), map);
                //如果负责人不为空
                if (ToolUtil.isNotEmpty(map)){
                    stackVo.setPrincipalName(map.get("nickname")+"("+map.get("username")+")");
                }
            }else {
                Object nickname = redisTemplate.opsForHash().get("USER_" + stackVo.getPrincipalName(), "nickname");
                stackVo.setPrincipalName(nickname+"("+stackVo.getPrincipalName()+")");
            }
        });
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

    @Resource
    private MaintainPlanDao maintainPlanDao;
    @Override
    public Result locatingMaintain() {
        //查询没有养护计划的垛位
        List<StackVo> stackVos = maintainPlanDao.findStackNotInMaintainPlan();
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

    @Override
    public Integer findStackBySiteNo(String siteNo,Integer level) {
        return warehouseDao.findStackBySiteNo(siteNo,level);
    }

    /**
     * 判断楼栋名称是否存在
     *
     * @param buildingName
     * @return
     */
    @Override
    public int findBuildingName(String buildingName,Integer warehouseId) {
        int count = warehouseDao.findBuildingName(buildingName,warehouseId);
        return count;
    }

    /**
     * 判断楼栋编号是否存在
     *
     * @param buildingNo
     * @return
     */
    @Override
    public int findBuildingCode(String buildingNo) {
        int count = warehouseDao.findBuildingCode(buildingNo);
        return count;
    }

    /**
     * 判断楼栋编号是否可以修改
     *
     * @param buildingNo
     * @param id
     * @return
     */
    @Override
    public int findBuildingNo(String buildingNo, Integer id) {
        int count = warehouseDao.findBuildingNo(buildingNo,id);
        return count;
    }

    /**
     * 判断楼栋名称是否存在
     *
     * @param buildingName
     * @param id
     * @return
     */
    @Override
    public int findBuildingNameCount(String buildingName, Integer id,Integer warehouseId) {
        int count = warehouseDao.findBuildingNameCount(buildingName,id,warehouseId);
        return count;
    }

    /**
     * 判断楼层是否存在
     *
     * @param floorName
     * @param buildingId
     * @return
     */
    @Override
    public int findFloorName(String floorName, String buildingId) {
        int countFloorName = warehouseDao.findFloorName(floorName,buildingId);
        return countFloorName;
    }

    /**
     * 判断楼层是否已经存在
     *
     * @param floorName
     * @param buildingId
     * @return
     */
    @Override
    public int findFloorNameCount(String floorName, String buildingId,Integer id) {
        int countFloorName = warehouseDao.findFloorNameCount(floorName, buildingId,id);
        return countFloorName;
    }

    /**
     * 判断分区名称是否存在
     *
     * @param areaName
     * @param floorId
     * @return
     */
    @Override
    public int findAreaName(String areaName, Integer floorId) {
        int countAreaName = warehouseDao.findAreaName(areaName,floorId);
        return countAreaName;
    }

    /**
     * 判断分区名称是否存在
     *
     * @param areaName
     * @param floorId
     * @param id
     * @return
     */
    @Override
    public int findAreaNameCount(String areaName, Integer floorId, Integer id) {
        int count = warehouseDao.findAreaNameCount(areaName,floorId,id);
        return count;
    }

    /**
     * 添加时判断垛位名称是否存在
     *
     * @param nameCn
     * @param areaId
     * @return
     */
    @Override
    public int findStackName(String nameCn, Integer areaId) {
        int count = warehouseDao.findStackName(nameCn,areaId);
        return count;
    }

    /**
     * 修改时判断垛位是否存在
     *
     * @param nameCn
     * @param areaId
     * @param id
     * @return
     */
    @Override
    public int findStackNameCount(String nameCn, Integer areaId, Integer id) {
        int countStackName = warehouseDao.findStackNameCount(nameCn,areaId,id);
        return countStackName;
    }

    /**
     * 判断楼层编号是否存在
     *
     * @param floorNo
     * @param id
     * @return
     */
    @Override
    public int findFloorNo(String floorNo, Integer id) {
        int count = warehouseDao.findFloorNo(floorNo,id);
        return count;
    }

    /**
     * 判断分区编号是否存在
     *
     * @param areaNo
     * @param id
     * @return
     */
    @Override
    public int findWareAreaNo(String areaNo, Integer id) {
        int count = warehouseDao.findWareAreaNo(areaNo,id);
        return count;
    }

    /**
     * 判断垛位编号是否存在
     *
     * @param stackNo
     * @param id
     * @return
     */
    @Override
    public int findStackNo(String stackNo, Integer id) {
        int count = warehouseDao.findStackNo(stackNo,id);
        return count;
    }

    /**
     * 将负责人放入缓存中
     * @param pidRoute
     * @param map
     * @return
     */
    public Map<String,Object> sitePrincipalMap(String pidRoute,Map<String,Object> map){
        String[] strs =  pidRoute.split(",");
        //如果负责人账号为空 向上查找
        for (int i = 0; i < strs.length; i++) {
            Integer level = strs.length-i-1;
            String str = strs[level];
            Integer id = Integer.parseInt(str.substring(4,str.length()-1));
            //获取负责人信息
            Object warehouseprincipal = redisTemplate.opsForHash().get(WAREHOUSEPRINCIPAL, level + "_" + id);
            if (!ToolUtil.isEmpty(warehouseprincipal)){
                map = JSONObject.parseObject(warehouseprincipal.toString(),Map.class);
                break;
            }
        }
        return map;
    }

    public boolean getUserInfo (String userName,Map map){

        if (redisTemplate.opsForHash().hasKey("USER_"+userName, "userId")) {
            Object nickname = redisTemplate.opsForHash().get("USER_" + userName, "nickname");
            Object phonenumber = redisTemplate.opsForHash().get("USER_" + userName, "phonenumber");
            Object userId = redisTemplate.opsForHash().get("USER_" + userName, "userId");
            map.put("nickname", nickname);
            map.put("phonenumber", phonenumber);
            map.put("userId", userId);
            map.put("userName", userName);
            return true;
        }
        return false;

    }

    /**
     * 修改负责人
     * @param principalName 负责人账号
     * @param siteId 位置id
     * @param pidRoute 位置路由
     */
    public void updatePrincipal(String principalName,Integer siteId,String pidRoute,Integer level){
        if (ToolUtil.isEmpty(pidRoute)){
            switch (level){
                case 1:
                    WarehouseVo warehouse = findWarehouseById(siteId);
                    pidRoute =warehouse.getPidRoute();
                    break;
                case 2:
                    BuildingVo buildingVo = findBuildById(siteId);
                    pidRoute =buildingVo.getPidRoute();
                    break;
                case 3:
                    FloorVo floorVos = findOneFloor(siteId);
                    pidRoute =floorVos.getPidRoute();
                    break;
                case 4:
                    WareAreaVo wareAreaByOne = findWareAreaByOne(siteId);
                    pidRoute =wareAreaByOne.getPidRoute();
                    break;
                case 5:
                    StackVo stackVo = findStackById(siteId);
                    pidRoute =stackVo.getPidRoute();
                    break;
                default:
            }
        }
        if (!ToolUtil.isEmpty(principalName)){
            String[] strs =  pidRoute.split(",");
            Object o = redisTemplate.opsForHash().get(WAREHOUSEPRINCIPAL, strs.length+"_" + siteId);
            Map<String,Object> map = new HashMap<>();
            if (ToolUtil.isEmpty(o)){
                map = sitePrincipalMap(pidRoute, map);

            }else {
//                map = JSONObject.parseObject(o+"",HashMap.class);
                getUserInfo(principalName,map);
            }
            redisTemplate.opsForHash().put(WAREHOUSEPRINCIPAL,strs.length+"_" + siteId,JSONObject.toJSONString(map));
        }
    }

    /**
     * 获取负责人
     * @param level  位置等级
     * @param siteId 位置id
     * @return
     */
    public Map<String,Object> getSitePrincipal(Integer level , Integer siteId){
        Object o = redisTemplate.opsForHash().get(WAREHOUSEPRINCIPAL, level + "_" + siteId);
        if (!ToolUtil.isEmpty(o)){
            return JSONObject.parseObject(o+"",HashMap.class);
        }else {
            Map<String,Object> map = new HashMap<>();
            switch (level){
                case 1:
                    WarehouseVo warehouse = findWarehouseById(siteId);
                    return sitePrincipalMap(warehouse.getPidRoute(),map);
                case 2:
                    BuildingVo buildingVo = findBuildById(siteId);
                    return sitePrincipalMap(buildingVo.getPidRoute(),map);
                case 3:
                    FloorVo floorVos = findOneFloor(siteId);
                    return sitePrincipalMap(floorVos.getPidRoute(),map);
                case 4:
                    WareAreaVo wareAreaByOne = findWareAreaByOne(siteId);
                    return sitePrincipalMap(wareAreaByOne.getPidRoute(),map);
                case 5:
                    StackVo stackVo = findStackById(siteId);
                    return sitePrincipalMap(stackVo.getPidRoute(),map);
                default:
                    return null;
            }
        }
    }

}