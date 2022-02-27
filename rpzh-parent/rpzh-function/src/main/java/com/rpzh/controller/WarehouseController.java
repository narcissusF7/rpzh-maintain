package com.rpzh.controller;

import com.rpzh.dto.BuildingDto;
import com.rpzh.dto.FloorDto;
import com.rpzh.dto.WarehouseDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.WarehouseNodeType;
import com.rpzh.service.WarehouseService;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.WarehouseVo;
import org.apache.poi.ss.usermodel.Footer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

/**
 * 仓库管理
 * @Author liubc
 * @create 2022/2/23 11:31
 * @Description: good good study,day day up
 */
@RestController
@RequestMapping("/warehouse")
@CrossOrigin
public class WarehouseController {


    @Autowired
    private WarehouseService warehouseService;


    /**
     * 新增仓库
     * @param warehouseDto
     * @return
     */
    @PostMapping("/addWarehouse")
    public Result addWarehouse(WarehouseDto warehouseDto){
        //获取仓库名称
        String name = warehouseDto.getName();
        if (ToolUtil.isEmpty(name)){
            return new Result(false, StatusCode.ERROR, "仓库名称不能为空");
        }
        //获取仓库建造时间
        String buildTime = warehouseDto.getBuildTime();
        if (ToolUtil.isEmpty(buildTime)){
            return new Result(false, StatusCode.ERROR, "仓库建造时间不能为空");
        }
        //仓库代码唯一
        String code = warehouseDto.getCode();
        int codeCount = warehouseService.findCodeCount(code);
        if(codeCount > 0){
            return new Result(false, StatusCode.ERROR, "仓库代码已经存在,禁止添加");
        }
        //判断仓库是否存在
        int nameCount = warehouseService.findNameCount(name);
        if (nameCount > 0){
            return new Result(false, StatusCode.ERROR, "该仓库已经存在,禁止添加");
        }
        int addFlag = warehouseService.addWarehouse(warehouseDto);
        return new Result(true, StatusCode.OK, "添加成功");
    }


    /**
     * 修改仓库
     * @param warehouseDto
     * @return
     */
    @PostMapping("/updateWarehouse")
    public Result updateWarehouse(WarehouseDto warehouseDto){
        String name = warehouseDto.getName();
        if (ToolUtil.isEmpty(name)){
            return new Result(false, StatusCode.ERROR, "仓库名称不能为空");
        }
        Integer id = warehouseDto.getId();
        //判断仓库名称是否存在
        String wareName = warehouseService.findWareName(name,id);
        if (name.equals(wareName)){
            return new Result(false, StatusCode.ERROR, "该仓库名称已经存在,禁止修改");
        }
        //判断仓库代码是否存在
        String code = warehouseDto.getCode();
        if (ToolUtil.isEmpty(code)){
            return new Result(false, StatusCode.ERROR, "仓库代码不能为空");
        }
        String careCode = warehouseService.findWareCode(code,id);
        if (code.equals(careCode)){
            return new Result(false, StatusCode.ERROR, "该仓库代码已经存在,不允许修改");
        }
        int updateFlag = warehouseService.updateWarehouse(warehouseDto);
        return new Result(true, StatusCode.OK, "修改仓库信息成功");
    }


    /**
     *新增楼号
     * @param buildingDto
     * @return
     */
    @PostMapping("/addBuilding")
    public Result addBuilding(BuildingDto buildingDto){
        //获取楼号
        String buildingNo = buildingDto.getBuildingNo();
        if (ToolUtil.isEmpty(buildingNo)){
            return new Result(false, StatusCode.ERROR, "楼号不能为空");
        }
        //楼栋名称
        String buildingName = buildingDto.getBuildingName();
        if (ToolUtil.isEmpty(buildingName)){
            return new Result(false, StatusCode.ERROR, "楼栋名称不能为空");
        }
        //仓库id
        Integer warehouseId = buildingDto.getWarehouseId();
        if (ToolUtil.isEmpty(warehouseId)){
            return new Result(false, StatusCode.ERROR, "仓库id不能为空");
        }
        int addFlag = warehouseService.addBuilding(buildingDto);
        return new Result(true, StatusCode.OK, "新增楼号成功");
    }

    /**
     * 修改楼栋
     * @param buildingDto
     * @return
     */
    @PostMapping("/updateBuilding")
    public Result updateBuilding(BuildingDto buildingDto){
        if (ToolUtil.isEmpty(buildingDto.getId())){
            return new Result(false, StatusCode.ERROR, "请选择一个楼栋");
        }
        int updateFlag = warehouseService.updateBuilding(buildingDto);
        return new Result(true, StatusCode.OK, "修改楼栋信息成功");
    }

    /**
     *新增楼层
     * @param floorDto
     * @return
     */
    @PostMapping("/addFloor")
    public Result addFloor(FloorDto floorDto){
        //获取楼层编号
        String floorNo = floorDto.getFloorNo();
        if (ToolUtil.isEmpty(floorNo)){
            return new Result(false, StatusCode.ERROR, "楼层编号不能为空");
        }
        //是否分区
        Integer isPartition = floorDto.getIsPartition();
        if (ToolUtil.isEmpty(isPartition)){
            return new Result(false, StatusCode.ERROR, "分区不能为空");
        }
        String buildingId = floorDto.getBuildingId();
        if (ToolUtil.isEmpty(buildingId)){
            return new Result(false, StatusCode.ERROR, "楼号id不能为空");
        }
        int addFlag = warehouseService.addFloor(floorDto);
        return new Result(true, StatusCode.OK, "新增楼层成功");
    }

    /**
     * 修改楼层
     * @param floorDto
     * @return
     */
    @PostMapping("/updateFloor")
    public Result updateFloor(FloorDto floorDto){
        Integer id = floorDto.getId();
        if (ToolUtil.isEmpty(id)){
            return new Result(false, StatusCode.ERROR, "请选择一个楼层");
        }
        String buildingId = floorDto.getBuildingId();
        if (ToolUtil.isEmpty(buildingId)){
            return new Result(false, StatusCode.ERROR, "请选择一个楼栋");
        }
        int updateFlag = warehouseService.updateFloor(floorDto);
        return new Result(true, StatusCode.OK, "修改楼层成功");
    }
}