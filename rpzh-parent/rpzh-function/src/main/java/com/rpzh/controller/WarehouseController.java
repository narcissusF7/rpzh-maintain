package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.*;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.WarehouseService;
import com.rpzh.vo.*;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 仓库管理
 *
 * @Author liubc
 * @create 2022/2/23 11:31
 * @Description: good good study,day day up
 */
@RestController
@RequestMapping("/maintain/warehouse")
@CrossOrigin
public class WarehouseController {


    @Autowired
    private WarehouseService warehouseService;


    /**
     * 新增仓库
     *
     * @param warehouseDto
     * @return
     */
    @PostMapping("/addWarehouse")
    public Result addWarehouse(WarehouseDto warehouseDto) {
        //获取仓库名称
        String name = warehouseDto.getName();
        if (ToolUtil.isEmpty(name)) {
            return new Result(false, StatusCode.ERROR, "仓库名称不能为空");
        }
        //获取仓库建造时间
        String buildTime = warehouseDto.getBuildTime();
        if (ToolUtil.isEmpty(buildTime)) {
            return new Result(false, StatusCode.ERROR, "仓库建造时间不能为空");
        }

        if (ToolUtil.isEmpty(warehouseDto.getPrincipalName())) {
            return new Result(false, StatusCode.ERROR, "负责人账号不能为空");
        }
        //仓库代码唯一
        String code = warehouseDto.getCode();
        int codeCount = warehouseService.findCodeCount(code);
        if (codeCount > 0) {
            return new Result(false, StatusCode.ERROR, "仓库代码已经存在,禁止添加");
        }
        //判断仓库是否存在
        int nameCount = warehouseService.findNameCount(name);
        if (nameCount > 0) {
            return new Result(false, StatusCode.ERROR, "该仓库已经存在,禁止添加");
        }
        int addFlag = warehouseService.addWarehouse(warehouseDto);
        return new Result(true, StatusCode.OK, "添加成功");
    }


    /**
     * 修改仓库
     *
     * @param warehouseDto
     * @return
     */
    @PostMapping("/updateWarehouse")
    public Result updateWarehouse(WarehouseDto warehouseDto) {
        String name = warehouseDto.getName();
        if (ToolUtil.isEmpty(name)) {
            return new Result(false, StatusCode.ERROR, "仓库名称不能为空");
        }
        Integer id = warehouseDto.getId();
        //判断仓库名称是否存在
        String wareName = warehouseService.findWareName(name, id);
        if (name.equals(wareName)) {
            return new Result(false, StatusCode.ERROR, "该仓库名称已经存在,禁止修改");
        }
        //判断仓库代码是否存在
        String code = warehouseDto.getCode();
        if (ToolUtil.isEmpty(code)) {
            return new Result(false, StatusCode.ERROR, "仓库代码不能为空");
        }
        String careCode = warehouseService.findWareCode(code, id);
        if (code.equals(careCode)) {
            return new Result(false, StatusCode.ERROR, "该仓库代码已经存在,不允许修改");
        }
        int updateFlag = warehouseService.updateWarehouse(warehouseDto);
        return new Result(true, StatusCode.OK, "修改仓库信息成功");
    }


    /**
     * 新增楼号
     *
     * @param buildingDto
     * @return
     */
    @PostMapping("/addBuilding")
    public Result addBuilding(BuildingDto buildingDto) {
        //获取楼号
        String buildingNo = buildingDto.getBuildingNo();
        if (ToolUtil.isEmpty(buildingNo)) {
            return new Result(false, StatusCode.ERROR, "楼号不能为空");
        }
        //楼栋名称
        String buildingName = buildingDto.getBuildingName();
        if (ToolUtil.isEmpty(buildingName)) {
            return new Result(false, StatusCode.ERROR, "楼栋名称不能为空");
        }
        //仓库id
        Integer warehouseId = buildingDto.getWarehouseId();
        if (ToolUtil.isEmpty(warehouseId)) {
            return new Result(false, StatusCode.ERROR, "仓库id不能为空");
        }
        //获取仓库类型
        String wareType = warehouseService.findWareType(warehouseId);
        buildingDto.setBuildingType(wareType);
        int addFlag = warehouseService.addBuilding(buildingDto);
        return new Result(true, StatusCode.OK, "新增楼号成功");
    }

    /**
     * 修改楼栋
     *
     * @param buildingDto
     * @return
     */
    @PostMapping("/updateBuilding")
    public Result updateBuilding(BuildingDto buildingDto) {
        if (ToolUtil.isEmpty(buildingDto.getId())) {
            return new Result(false, StatusCode.ERROR, "请选择一个楼栋");
        }
        //获取仓库类型
        Integer warehouseId = buildingDto.getWarehouseId();
        if (ToolUtil.isEmpty(warehouseId)) {
            return new Result(false, StatusCode.ERROR, "仓库id不能为空");
        }
        //String wareType = warehouseService.findWareType(warehouseId);
        //buildingDto.setBuildingType(wareType);
        int updateFlag = warehouseService.updateBuilding(buildingDto);
        return new Result(true, StatusCode.OK, "修改楼栋信息成功");
    }

    /**
     * 新增楼层
     *
     * @param floorDto
     * @return
     */
    @PostMapping("/addFloor")
    public Result addFloor(FloorDto floorDto) {
        //获取楼层编号
        String floorNo = floorDto.getFloorNo();
        if (ToolUtil.isEmpty(floorNo)) {
            return new Result(false, StatusCode.ERROR, "楼层编号不能为空");
        }
        //是否分区
        Integer isPartition = floorDto.getIsPartition();
        if (ToolUtil.isEmpty(isPartition)) {
            return new Result(false, StatusCode.ERROR, "分区不能为空");
        }
        String buildingId = floorDto.getBuildingId();
        if (ToolUtil.isEmpty(buildingId)) {
            return new Result(false, StatusCode.ERROR, "楼号id不能为空");
        }
        //获取楼栋类型
        String buildType = warehouseService.findBuildType(buildingId);
        floorDto.setFloorType(buildType);
        int addFlag = warehouseService.addFloor(floorDto);
        return new Result(true, StatusCode.OK, "新增楼层成功");
    }

    /**
     * 修改楼层
     *
     * @param floorDto
     * @return
     */
    @PostMapping("/updateFloor")
    public Result updateFloor(FloorDto floorDto) {
        Integer id = floorDto.getId();
        if (ToolUtil.isEmpty(id)) {
            return new Result(false, StatusCode.ERROR, "请选择一个楼层");
        }
        String buildingId = floorDto.getBuildingId();
        if (ToolUtil.isEmpty(buildingId)) {
            return new Result(false, StatusCode.ERROR, "请选择一个楼栋");
        }
        //获取楼栋类型
        //String buildType = warehouseService.findBuildType(buildingId);
        //floorDto.setFloorType(buildType);
        int updateFlag = warehouseService.updateFloor(floorDto);
        return new Result(true, StatusCode.OK, "修改楼层成功");
    }

    /**
     * 新增分区
     *
     * @param wareAreaDto
     * @return
     */
    @PostMapping("/addWarearea")
    public Result addWarearea(WareAreaDto wareAreaDto) {
        String areaNo = wareAreaDto.getAreaNo();
        if (ToolUtil.isEmpty(areaNo)) {
            return new Result(false, StatusCode.ERROR, "分区标号不能为空");
        }
        Integer floorId = wareAreaDto.getFloorId();
        if (ToolUtil.isEmpty(floorId)) {
            return new Result(false, StatusCode.ERROR, "楼层id不能为空");
        }
        //查询楼层类型
        String floorType = warehouseService.findFloorType(floorId);
        wareAreaDto.setWareAreaType(floorType);
        int addFlag = warehouseService.addWarearea(wareAreaDto);
        return new Result(true, StatusCode.OK, "新增分区成功");
    }

    /**
     * 修改分区
     *
     * @param wareAreaDto
     * @return
     */
    @PostMapping("/updateWarearea")
    public Result updateWarearea(WareAreaDto wareAreaDto) {
        Integer id = wareAreaDto.getId();
        if (ToolUtil.isEmpty(id)) {
            return new Result(false, StatusCode.ERROR, "请选择一个分区");
        }
        Integer floorId = wareAreaDto.getFloorId();
        if (ToolUtil.isEmpty(floorId)) {
            return new Result(false, StatusCode.ERROR, "楼层id不能为空");
        }
        //查询楼层类型
        //String floorType = warehouseService.findFloorType(floorId);
        //wareAreaDto.setWareAreaType(floorType);
        int updateFlag = warehouseService.updateWarearea(wareAreaDto);
        return new Result(true, StatusCode.OK, "修改分区成功");
    }


    /**
     * 新增垛位
     *
     * @param stackDto
     * @return
     */
    @PostMapping("/addStack")
    public Result addStack(StackDto stackDto) {
        String stackNo = stackDto.getStackNo();
        if (ToolUtil.isEmpty(stackNo)) {
            return new Result(false, StatusCode.ERROR, "垛位编号不能为空");
        }
        String nameCn = stackDto.getNameCn();
        if (ToolUtil.isEmpty(nameCn)) {
            return new Result(false, StatusCode.ERROR, "中文名称不能为空");
        }
        Integer areaId = stackDto.getAreaId();
        if (ToolUtil.isEmpty(areaId)) {
            return new Result(false, StatusCode.ERROR, "分区id不能为空");
        }
        //查看分区类型
        String wareAreaType = warehouseService.findWareAreaType(areaId);
        stackDto.setStackType(wareAreaType);
        int addFlag = warehouseService.addStack(stackDto);
        return new Result(true, StatusCode.OK, "新增垛位成功");
    }


    /**
     * 更新垛位
     *
     * @param stackDto
     * @return
     */
    @PostMapping("/updateStack")
    public Result updateStack(StackDto stackDto) {
        Integer id = stackDto.getStackId();
        if (ToolUtil.isEmpty(id)) {
            return new Result(false, StatusCode.ERROR, "请选择一个垛位");
        }
        Integer areaId = stackDto.getAreaId();
        if (ToolUtil.isEmpty(areaId)) {
            return new Result(false, StatusCode.ERROR, "分区id不能为空");
        }
        //查看分区类型
        //String wareAreaType = warehouseService.findWareAreaType(areaId);
        //stackDto.setStackType(wareAreaType);
        int updateFlag = warehouseService.updateStack(stackDto);
        return new Result(true, StatusCode.OK, "修改垛位成功");
    }


    /**
     * 删除仓库
     *
     * @param delDto
     * @return
     */
    @PostMapping("/del")
    public Result del(DelDto delDto) {
        Integer warehouseNodeId = delDto.getWarehouseNodeId();
        String warehouseNodeType = delDto.getWarehouseNodeType();
        if (ToolUtil.isEmpty(warehouseNodeId) && !"05".equals(warehouseNodeType)) {
            return new Result(false, StatusCode.ERROR, "请选择一个仓库");
        }
        if (ToolUtil.isEmpty(warehouseNodeType)) {
            return new Result(false, StatusCode.ERROR, "请选择仓库类型");
        }
        //01：仓库, 02: 楼号, 03:楼层，04：分区，05：垛位）
        switch (warehouseNodeType) {
            case "01"://删除仓库
                //查看仓库下是否存在楼栋
                int count1 = warehouseService.findBuildingCount(warehouseNodeId);
                if (count1 > 0) {
                    return new Result(false, StatusCode.ERROR, "该仓库下存在楼号，不允许删除");
                }
                int delWarehose = warehouseService.delWarehose(warehouseNodeId);
                return new Result(true, StatusCode.ERROR, "删除仓库成功");
            case "02"://删除楼号
                //查看楼号下是否存在楼层
                int count2 = warehouseService.findFloorCount(warehouseNodeId);
                if (count2 > 0) {
                    return new Result(false, StatusCode.ERROR, "该楼号下存在楼层，不允许删除");
                }
                int delBuild = warehouseService.delBuilding(warehouseNodeId);
                return new Result(true, StatusCode.OK, "删除楼号成功");
            case "03"://删除楼层
                //查看楼层下是否存在分区
                int count3 = warehouseService.findWareAreaCount(warehouseNodeId);
                if (count3 > 0) {
                    return new Result(false, StatusCode.ERROR, "该楼层下存在分区，不允许删除");
                }
                int delFloor = warehouseService.delFloor(warehouseNodeId);
                return new Result(true, StatusCode.OK, "删除楼层成功");
            case "04"://删除分区
                //查看分区下是否存在垛位
                int count4 = warehouseService.findStackCount(warehouseNodeId);
                if (count4 > 0) {
                    return new Result(false, StatusCode.ERROR, "该分区下存在垛位，不允许删除");
                }
                int delWareArea = warehouseService.delWareArea(warehouseNodeId);
                return new Result(true, StatusCode.OK, "删除分区成功");
            case "05"://删除垛位
                List<String> stackIds = delDto.getStackIds();
                if (ToolUtil.isEmpty(stackIds)){
                    return new Result(false, StatusCode.ERROR, "请选择一个垛位");
                }
                int delStack = warehouseService.delStack(stackIds);
                return new Result(true, StatusCode.OK, "删除垛位成功");
            default:
                break;
        }
        return new Result(true, StatusCode.OK, "删除成功");
    }


    /**
     * 获取垛位列表
     *
     * @param stackDto
     * @return
     */
    @PostMapping("/getStackPage")
    public Result<PageInfo> getStackPage(StackDto stackDto) {
        if (ToolUtil.isEmpty(stackDto.getWareAreaId())) {
            return new Result<>(false, StatusCode.ERROR, "区域id不能为空");
        }
        PageInfo<StackVo> pageInfo = warehouseService.getStackPage(stackDto);
        return new Result<PageInfo>(true, StatusCode.OK, "查询成功", pageInfo);
    }

    /**
     * 加载仓库页面列表树
     *
     * @param warehouseNodeId   仓库节点id
     * @param warehouseNodeType 仓库类型
     * @return
     */
    @PostMapping("/warehouseTree")
    public Result warehouseTree(Integer warehouseNodeId, String warehouseNodeType) {
        if (ToolUtil.isEmpty(warehouseNodeId)) {
            return new Result(false, StatusCode.ERROR, "仓库节点id不能为空");
        }
        if (ToolUtil.isEmpty(warehouseNodeType)) {
            return new Result(false, StatusCode.ERROR, "仓库类型不能为空");
        }
        //01：仓库, 02: 楼号, 03:楼层，04：分区，05：垛位）
        switch (warehouseNodeType) {
            case "01"://查询仓库
                List<WarehouseVo> warehouseVoList = warehouseService.findAllWarehouse(warehouseNodeId);
                return new Result(true, StatusCode.OK, "查询仓库信息成功", warehouseVoList);
            case "02"://查询楼号
                List<BuildingVo> buildingVoList = warehouseService.findBuildingById(warehouseNodeId);
                return new Result(true, StatusCode.OK, "查询楼号信息成功", buildingVoList);
            case "03"://查询楼层
                List<FloorVo> floorVoList = warehouseService.findFloorById(warehouseNodeId);
                return new Result(true, StatusCode.OK, "查询楼层成功", floorVoList);
            case "04"://查询分区
                List<WareAreaVo> wareAreaVoList = warehouseService.findWareAreaById(warehouseNodeId);
                return new Result(true, StatusCode.OK, "查询分区成功", wareAreaVoList);
            default:
                break;
        }
        return new Result(true, StatusCode.OK, "查询成功");
    }

    /**
     * 获取仓库信息
     *
     * @param warehouseNodeId   仓库节点ID
     * @param warehouseNodeType 仓库节点类型（01：仓库, 02: 楼号, 03:楼层，04：分区，05：垛位）
     * @return
     */
    @PostMapping("/warehouseInfo")
    public Result warehouseInfo(Integer warehouseNodeId, String warehouseNodeType) {
        if (ToolUtil.isEmpty(warehouseNodeId)) {
            return new Result(false, StatusCode.ERROR, "仓库id不能为空");
        }
        if (ToolUtil.isEmpty(warehouseNodeType)) {
            return new Result(false, StatusCode.ERROR, "仓库类型不能为空");
        }
        switch (warehouseNodeType) {
            case "01"://查询仓库信息
                WarehouseVo warehouseVo = warehouseService.findWarehouseById(warehouseNodeId);
                return new Result(true, StatusCode.OK, "查询仓库信息成功", warehouseVo);
            case "02"://查询楼栋信息
                BuildingVo buildingVo = warehouseService.findBuildById(warehouseNodeId);
                return new Result(true, StatusCode.OK, "查询楼栋信息成功", buildingVo);
            case "03"://查询楼层信息
                FloorVo floorVo = warehouseService.findOneFloor(warehouseNodeId);
                return new Result(true, StatusCode.OK, "查询楼层信息成功",floorVo);
            case "04"://查询分区信息
                WareAreaVo wareAreaVo = warehouseService.findWareAreaByOne(warehouseNodeId);
                return new Result(true, StatusCode.OK, "查询分区信息成功", wareAreaVo);
            case "05"://查询垛位信息
                StackVo stackVo = warehouseService.findStackById(warehouseNodeId);
                return new Result(true, StatusCode.OK, "查询垛位信息成功", stackVo);
        }
        return null;
    }

    /**
     * 多级目录  树状结构
     * @return success or failure
     */
    @GetMapping("/locating")
    public Result<T> locating(){
        return new Result<>(true, StatusCode.OK, "查询目录成功", warehouseService.locating());
    }

    /**
     * 没有养护计划 多级目录  树状结构
     * @return success or failure
     */
    @PostMapping("/locatingMaintain")
    public Result<T> locatingMaintain(){
        return warehouseService.locatingMaintain();
    }
}