package com.rpzh.controller;

import com.rpzh.dto.DeptDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.pojo.Dept;
import com.rpzh.service.DeptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author liubc
 * @create 2021/5/18 11:32
 * 部门管理
 */
@RestController
@RequestMapping("/dept")
@CrossOrigin
@Api(value = "部门管理", tags = "部门管理", description = "部门管理")
public class DeptController {

    @Autowired
    private DeptService deptService;

    /**
     * 查询所有的部门
     * @return
     */
    @PostMapping("/findAllDept")
    @ApiOperation(value = "查询所有的部门", tags = "部门管理")
    public Result<Dept> findAllDept(DeptDto deptDto) {
        List<Dept> deptList = deptService.findAllDept(deptDto);
        return new Result<Dept>(true, StatusCode.OK, "查询成功", deptList);
    }

    /**
     * 增加部门
     *
     * @param deptDto
     * @return
     */
    @PostMapping("/addDept")
    public Result<String> addDept(DeptDto deptDto) {
        //获取上级部门id
        Integer parentId = deptDto.getParentId();
        //获取部门名称
        String deptName = deptDto.getDeptName();
        //显示排序
        Integer orderNum = deptDto.getOrderNum();
        if (ToolUtil.isEmpty(parentId) || ToolUtil.isEmpty(deptName) || ToolUtil.isEmpty(orderNum)) {
            return new Result<String>(false, StatusCode.ERROR, "上级部门,部门名称,显示排序不能为空");
        }
        //根据上级部门id和部门名称查询部门是否存在
        Integer sum = deptService.findOneDept(parentId, deptName);
        if (sum > 0) {
            return new Result<String>(false, StatusCode.ERROR, "该部门已经存在");
        } else {
            deptService.addDept(deptDto);
            return new Result<String>(true, StatusCode.OK, "添加部门成功");
        }
    }

    /**
     * 删除部门
     *
     * @return
     */
    @GetMapping("/deleteDept")
    @PreAuthorize("hasAnyAuthority('system:dept:deleteDept')")
    public Result deleteDept(Integer deptId) {
        //根据部门id判断部门是否存在下级部门
        Integer sum = deptService.findDepts(deptId);
        if (sum > 0) {
            return new Result(false, StatusCode.ERROR, "存在下级部门,不能删除");
        }
        //判断部门下是否存在用户
        Integer userSum = deptService.findUser(deptId);
        if (userSum > 0) {
            return new Result(false, StatusCode.ERROR, "部门存在用户,不能删除");
        }
        //删除部门
        Integer delDept = deptService.deleteDeptByDeptId(deptId);
        return new Result(true, StatusCode.OK, "删除部门成功");
    }

    /**
     * 修改部门
     * @return
     */
    @PreAuthorize("hasAnyAuthority('system:dept:editDept')")
    @PostMapping("/editDept")
    public Result editDept(DeptDto deptDto) {
        //获取部门id
        Integer deptId = deptDto.getDeptId();
        //获取上级部门id
        Integer parentId = deptDto.getParentId();
        //获取部门名称
        String deptName = deptDto.getDeptName();
        //显示排序
        Integer orderNum = deptDto.getOrderNum();
        if (ToolUtil.isEmpty(parentId) || ToolUtil.isEmpty(deptName) || ToolUtil.isEmpty(orderNum)) {
            return new Result(false, StatusCode.ERROR, "上级部门,部门名称,显示排序不能为空");
        }
        if (deptId.equals(parentId)){
            return new Result(false, StatusCode.ERROR, "父部门不能是自己");
        }
        //根据父部门id获取父部门名称
        String deptParentName = deptService.findDeptNameByParentId(parentId);
        if (deptName.equals(deptParentName)){
            return new Result(false, StatusCode.ERROR, "修改的部门名称不能与父部门相同");
        }
        //获取修改的部门下所有的部门名称
        List<String> deptList = deptService.findAllDeptByDeptId(deptId);
        //修改部门名称不能与子部门名称相同
        if (ToolUtil.isNotEmpty(deptList) && deptList.size() != 0){
            for (int i = 0; i < deptList.size(); i++) {
                String deptLowName = deptList.get(i);
                if (deptLowName.equals(deptName)){
                    return new Result(false, StatusCode.ERROR, "修改的部门名称不能与子部门相同");
                }
            }
        }
        //获取父部门下所有的同级部门,不包含本部门
        List<String> deptListNoContainThis = deptService.findDeptNameNoContainThis(parentId,deptId);
        if (ToolUtil.isNotEmpty(deptListNoContainThis) && deptListNoContainThis.size() != 0){
            for (int i = 0; i < deptListNoContainThis.size(); i++) {
                String deptNameNoThis = deptListNoContainThis.get(i);
                if (deptNameNoThis.equals(deptName)){
                    return new Result(false, StatusCode.ERROR, "该部门已经存在");
                }
            }
        }
        Integer deptResult = deptService.updateDept(deptDto);
        return new Result(true, StatusCode.OK, "修改部门成功");
    }

    /**
     * 修改部门的状态
     * @param deptDto
     * @return
     */
    @RequestMapping("/changeDeptStutas")
    public Result changeDeptStutas(DeptDto deptDto){
        Integer changeFlag = deptService.changeDeptStutas(deptDto);
        return new Result(true, StatusCode.OK, "修改部门的状态成功");
    }

    /**
     *搜索部门
     * @param deptDto
     * @return
     */
    @RequestMapping("/searchDept")
    public Result searchDept(DeptDto deptDto){
        List<Dept> deptList = deptService.searchDept(deptDto);
        return new Result(true, StatusCode.OK, "搜索部门成功", deptList);
    }

}