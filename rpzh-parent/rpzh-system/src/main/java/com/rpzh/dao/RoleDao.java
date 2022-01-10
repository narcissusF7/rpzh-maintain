package com.rpzh.dao;

import com.rpzh.dto.RoleDto;
import com.rpzh.vo.RoleVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author liubc
 * @create 2021/5/26 10:29
 */
@Mapper
public interface RoleDao {

    /**
     * 查询角色是否存在
     * @param roleKey
     * @return
     */
    Integer findRoleExist(@Param("roleKey") String roleKey);

    /**
     * 查询角色名称是否存在
     * @param roleName
     * @return
     */
    Integer findRoleNameExist(@Param("roleName") String roleName);


    /**
     * 新增角色
     * @param roleDto
     * @return
     */
    Integer addRole(@Param("roleDto") RoleDto roleDto);

    /**
     * 获取角色id
     * @param roleDto
     * @return
     */
    Integer findRoleId(@Param("roleDto") RoleDto roleDto);

    /**
     * 将角色id菜单id对应关系插入角色菜单关联表
     * @param roleId
     * @param menuIds
     * @return
     */
    Integer insertRoleMenu(@Param("roleId") Integer roleId, @Param("menuIds") List<Integer> menuIds);

    /**
     * 查询所有的角色
     * @return
     */
    List<RoleVo> findAllRole(@Param("roleDto") RoleDto roleDto);

    /**
     * 查看角色使用数量
     * @param roleId
     * @return
     */
    int countUserRoleByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询角色名称
     * @param roleId
     * @return
     */
    String selectRoleById(@Param("roleId") Long roleId);

    /**
     * 删除角色菜单关联
     * @param roleId
     * @return
     */
    Integer deleteRoleMenu(@Param("roleId") Long[] roleId);

    /**
     * 删除角色
     * @param roleId
     * @return
     */
    Integer deleteRoleByIds(@Param("roleId") Long[] roleId);

    /**
     * 判断角色是否存在
     * @param roleKey
     * @param roleId
     * @return
     */
    Integer findRoleKey(@Param("roleKey") String roleKey, @Param("roleId") Long roleId);

    /**
     * 判断角色的唯一
     * @param roleName
     * @param roleId
     * @return
     */
    Integer findRoleName(@Param("roleName") String roleName, @Param("roleId") Long roleId);

    /**
     * 修改角色信息
     * @param roleDto
     * @return
     */
    Integer updateRole(@Param("roleDto") RoleDto roleDto);

    /**
     * 删除角色与菜单的关联
     * @param roleId
     * @return
     */
    Integer deleteMenuRole(@Param("roleId") Long roleId);

    /**
     * 修改管理员角色信息
     * @param roleDto
     * @return
     */
    Integer updateAdminRole(@Param("roleDto") RoleDto roleDto);
}