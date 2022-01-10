package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.RoleDto;
import com.rpzh.vo.RoleVo;

/**
 * @Author liubc
 * @create 2021/5/26 10:28
 */
public interface RoleService {
    /**
     * 查询角色是否存在
     * @param roleKey
     * @return
     */
    Integer findRoleExist(String roleKey);

    /**
     * 查询角色名称是否存在
     * @param roleName
     * @return
     */
    Integer findRoleNameExist(String roleName);

    /**
     * 新增角色
     * @param roleDto
     * @return
     */
    Integer addRole(RoleDto roleDto);

    /**
     * 查询所有的角色信息
     * @param
     * @param
     * @return
     */
    PageInfo<RoleVo> findAllRole(RoleDto roleDto);


    /**
     * 批量删除角色
     * @param roleId
     * @return
     */
    Integer delectRole(Long[] roleId);

    /**
     * 判断角色是否可以被操作
     * @param roleId
     */
    void checkRoleAllowed(Long roleId);

    /**
     * 修改角色
     * @param roleDto
     * @return
     */
    Integer editRole(RoleDto roleDto);

    /**
     * 判断角色是否存在
     * @param roleKey
     * @param roleId
     * @return
     */
    Integer findRoleKey(String roleKey, Long roleId);

    /**
     * 判断角色的唯一
     * @param roleName
     * @param roleId
     * @return
     */
    Integer findRoleName(String roleName, Long roleId);

    /**
     * 修改角色的数据权限
     * @param roleDto
     * @return
     */
    Integer updateDataScope(RoleDto roleDto);

    /**
     * 修改超级管理员菜单权限
     * @param roleDto
     * @return
     */
    Integer editAdmingMenu(RoleDto roleDto);

    /**
     * 修改超级管理员角色
     * @param roleDto
     * @return
     */
    Integer editAdminRole(RoleDto roleDto);
}
