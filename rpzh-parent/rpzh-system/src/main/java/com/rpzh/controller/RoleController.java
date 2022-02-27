package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.annotation.OperationLog;
import com.rpzh.dto.RoleDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.BusinessType;
import com.rpzh.service.RoleService;
import com.rpzh.vo.RoleVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author liubc
 * @create 2021/5/20 10:30
 * 角色管理
 */
@RestController
@RequestMapping("/role")
@CrossOrigin
@Api(value = "角色管理", tags = "角色管理", description = "角色管理")
public class RoleController {


    @Autowired
    private RoleService roleService;


    /**
     * 增加角色
     *
     * @param roleDto
     * @return
     */
    @PostMapping("/addRole")
    @ApiOperation(value = "增加角色")
    @OperationLog(mapper = RoleService.class, businessType = BusinessType.INSERT, title = "角色管理")
    public Result addRole(RoleDto roleDto) {
        //获取角色标识
        String roleKey = roleDto.getRoleKey();
        //获取角色名称
        String roleName = roleDto.getRoleName();
        //获取角色顺序
        Integer roleSort = roleDto.getRoleSort();
        //获取菜单
        List<Integer> menuIds = roleDto.getMenuIds();
        if (ToolUtil.isEmpty(roleKey) || ToolUtil.isEmpty(roleName) || ToolUtil.isEmpty(roleSort) || ToolUtil.isEmpty(menuIds)) {
            return new Result(false, StatusCode.ERROR, "角色标识,角色名称,菜单,角色顺序不能为空");
        }
        //根据角色标识查看该角色标识是否存在
        Integer roleCount = roleService.findRoleExist(roleKey);
        if (roleCount > 0) {
            return new Result(false, StatusCode.ERROR, "新增角色失败,该角色权限已经存在");
        }
        Integer roleNameCount = roleService.findRoleNameExist(roleName);
        if (roleNameCount > 0) {
            return new Result(false, StatusCode.ERROR, "新增角色失败,该角色名称已经存在");
        }
        Integer flag = roleService.addRole(roleDto);
        return new Result(true, StatusCode.OK, "新增角色成功");
    }

    /**
     * 分页查询所有的角色信息
     *
     * @param
     * @param
     * @return
     */
    @PostMapping("/findAllRole")
    @ApiOperation(value = "分页查询所有的角色信息")
    public Result<PageInfo> findAllRole(RoleDto roleDto) {
        PageInfo<RoleVo> pageInfo = roleService.findAllRole(roleDto);
        return new Result<PageInfo>(true, StatusCode.OK, "查询成功", pageInfo);
    }

    /**
     * 批量删除角色
     * @param roleId
     * @return
     */
    @GetMapping("/delectRole")
    @ApiOperation(value = "批量删除角色")
    //@PreAuthorize("hasAnyAuthority('system:role:delectRole')")
    @OperationLog(mapper = RoleService.class, businessType = BusinessType.DELETE, title = "角色管理")
    public Result delectRole(Long[] roleId){
        Integer delFlag = roleService.delectRole(roleId);
        return new Result(true, StatusCode.OK, "删除角色成功");
    }

    /**
     * 修改角色
     * @param roleDto
     * @return
     */
    @PostMapping("/editRole")
    @ApiOperation(value = "修改角色")
    @OperationLog(mapper = RoleService.class, businessType = BusinessType.UPDATE, title = "角色管理")
    public Result editRole(RoleDto roleDto){
        //获取角色id
        Long roleId = roleDto.getRoleId();
        //获取角色权限字符串
        String roleKey = roleDto.getRoleKey();
        //获取角色名称
        String roleName = roleDto.getRoleName();
        //获取角色顺序
        Integer roleSort = roleDto.getRoleSort();
        if (ToolUtil.isEmpty(roleId) || ToolUtil.isEmpty(roleKey) || ToolUtil.isEmpty(roleName) || ToolUtil.isEmpty(roleSort)){
            return new Result(false, StatusCode.ERROR, "角色名称,权限字符,角色顺序不能为空");
        }
        //根据id判断是否是管理员,管理员仅允许修改菜单权限
        //roleService.checkRoleAllowed(roleId);
        if (roleId == 1){
            Integer editUpdate = roleService.editAdminRole(roleDto);
            Integer editFlag = roleService.editAdmingMenu(roleDto);
            return new Result(true, StatusCode.OK, "修改超级管理员角色成功");
        }
        //判断角色权限字符串是否存在
        Integer roleKeyCount = roleService.findRoleKey(roleKey,roleId);
        if (roleKeyCount > 0){
            return new Result(false, StatusCode.ERROR, "修改失败,该权限字符串已经存在");
        }
        //判断角色名称是否存在
        Integer roleNameCount = roleService.findRoleName(roleName,roleId);
        if (roleNameCount > 0){
            return new Result(false, StatusCode.ERROR, "修改失败,该角色名称已经存在");
        }
        //修改角色
        Integer editUpdate = roleService.editRole(roleDto);
        return new Result(true, StatusCode.OK, "修改角色成功");
    }


    /**
     * 修改角色的状态
     * @param roleDto
     * @return
     */
    @PostMapping("/changeStatus")
    @ApiOperation(value = "修改角色的状态")
    @OperationLog(mapper = RoleService.class, businessType = BusinessType.UPDATE, title = "角色管理")
    public Result changeStatus(RoleDto roleDto){
        //获取角色id
        Long roleId = roleDto.getRoleId();
        //根据id判断是否是管理员,管理员不允许操作
        roleService.checkRoleAllowed(roleId);
        //修改角色状态
        Integer editUpdate = roleService.editRole(roleDto);
        return new Result(true, StatusCode.OK, "修改角色状态成功");
    }


    /**
     * 修改角色权限范围
     * @param roleDto
     * @return
     */
    @PostMapping("/dataScope")
    @ApiOperation(value = "修改角色权限范围")
    @OperationLog(mapper = RoleService.class, businessType = BusinessType.UPDATE, title = "角色管理")
    public Result dataScope(RoleDto roleDto){
        //获取角色id
        Long roleId = roleDto.getRoleId();
        if (ToolUtil.isEmpty(roleId)){
            return new Result(false, StatusCode.ERROR, "角色id不能为空");
        }
        //判断是否允许修改数据范围,超级管理员不可更改
        roleService.checkRoleAllowed(roleId);
        Integer updateFlag = roleService.updateDataScope(roleDto);
        return new Result(true, StatusCode.OK, "修改数据权限范围成功");
    }
}