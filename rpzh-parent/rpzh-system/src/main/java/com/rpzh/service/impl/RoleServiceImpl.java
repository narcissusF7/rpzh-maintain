package com.rpzh.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.RoleDao;
import com.rpzh.dto.RoleDto;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.RoleService;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.RoleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author liubc
 * @create 2021/5/26 10:28
 */
@Service
public class RoleServiceImpl implements RoleService {


    @Autowired
    private RoleDao roleDao;

    /**
     * 查询角色是否存在
     *
     * @param roleKey
     * @return
     */
    @Override
    public Integer findRoleExist(String roleKey) {
        Integer roleCount = roleDao.findRoleExist(roleKey);
        return roleCount;
    }

    /**
     * 查询角色名称是否存在
     *
     * @param roleName
     * @return
     */
    @Override
    public Integer findRoleNameExist(String roleName) {
        Integer roleNameCount = roleDao.findRoleNameExist(roleName);
        return roleNameCount;
    }

    /**
     * 新增角色
     *
     * @param roleDto
     * @return
     */
    @Override
    @Transactional
    public Integer addRole(RoleDto roleDto) {
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户名
        String username = userInfo.get("username");
        roleDto.setCreateBy(username);
        //获取当前系统时间
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createtime = sdf.format(date);
        roleDto.setCreateTime(createtime);
        Integer flag = roleDao.addRole(roleDto);
        //获取角色分配的菜单权限id
        List<Integer> menuIds = roleDto.getMenuIds();
        //获取添加的角色id
        Integer roleId = roleDao.findRoleId(roleDto);
        //将角色id菜单权限id插入角色菜单关联表
        if (ToolUtil.isNotEmpty(menuIds)){
            Integer insertFlag = roleDao.insertRoleMenu(roleId,menuIds);
        }
        return flag;
    }

    /**
     * 查询所有的角色信息
     *int page, int size
     * @param
     * @param
     * @return
     */
    @Override
    public PageInfo<RoleVo> findAllRole(RoleDto roleDto) {
        //当前页
        int page = roleDto.getPage();
        //每页显示数
        int size = roleDto.getSize();
        //静态分页
        PageHelper.startPage(page,size);
        List<RoleVo> roleVoList = roleDao.findAllRole(roleDto);
        //分页查询
        return new PageInfo<RoleVo>(roleVoList);
    }

    /**
     * 批量删除角色
     * @param roleId
     * @return
     */
    @Override
    @Transactional
    public Integer delectRole(Long[] roleId) {

        for (Long aLong : roleId) {
            //判断是否是超级管理员角色
            checkRoleAllowed(aLong);
            Integer roleCount = countUserRoleByRoleId(aLong);
            String roleName = selectRoleById(aLong);
            if (roleCount > 0){
                throw new RuntimeException(roleName+"角色已经分配,不能删除");
            }
        }
        //删除角色与菜单关联
        Integer roleMenu = roleDao.deleteRoleMenu(roleId);
        //删除角色id
        Integer deleteRole = roleDao.deleteRoleByIds(roleId);
        return deleteRole;
    }


    /**
     * 判断是否允许操作角色
     * @param role
     */
    @Override
    public void checkRoleAllowed(Long role)
    {
        if (role == 1)
        {
            throw new RuntimeException("不允许操作超级管理员角色");
        }
    }

    /**
     * 修改角色
     *
     * @param roleDto
     * @return
     */
    @Override
    @Transactional
    public Integer editRole(RoleDto roleDto) {
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户名
        String username = userInfo.get("username");
        roleDto.setUpdateBy(username);
        //修改角色信息
        Integer updateFlag = roleDao.updateRole(roleDto);
        Long roleId = roleDto.getRoleId();
        //删除角色与菜单的关联
        Integer deleteFlag = roleDao.deleteMenuRole(roleId);
        //获取菜单id集合
        List<Integer> menuIds = roleDto.getMenuIds();
        Integer roleid = roleId.intValue();
        //将角色id菜单权限id插入角色菜单关联表
        if (ToolUtil.isNotEmpty(menuIds)){
            Integer insertFlag = roleDao.insertRoleMenu(roleid,menuIds);
        }
        return deleteFlag;
    }

    /**
     * 判断角色是否存在
     *
     * @param roleKey
     * @param roleId
     * @return
     */
    @Override
    public Integer findRoleKey(String roleKey, Long roleId) {
        Integer roleKeyCount = roleDao.findRoleKey(roleKey,roleId);
        return roleKeyCount;
    }

    /**
     * 判断角色的唯一
     *
     * @param roleName
     * @param roleId
     * @return
     */
    @Override
    public Integer findRoleName(String roleName, Long roleId) {
        Integer roleNameCount = roleDao.findRoleName(roleName,roleId);
        return roleNameCount;
    }

    /**
     * 修改角色的数据权限
     * @param roleDto
     * @return
     */
    @Override
    @Transactional
    public Integer updateDataScope(RoleDto roleDto) {
        //获取修改的角色id
        Long roleId = roleDto.getRoleId();
        //删除角色与菜单的关联
        Integer deleteFlag = roleDao.deleteMenuRole(roleId);
        //获取菜单id集合
        List<Integer> menuIds = roleDto.getMenuIds();
        //类型转换
        Integer roleid = roleId.intValue();
        if (ToolUtil.isNotEmpty(menuIds)){
            Integer insertFlag = roleDao.insertRoleMenu(roleid, menuIds);
        }
        return deleteFlag;
    }

    /**
     * 修改超级管理员菜单权限
     *
     * @param roleDto
     * @return
     */
    @Override
    @Transactional
    public Integer editAdmingMenu(RoleDto roleDto) {
        //获取用户信息
        //Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户名
      /*  String username = userInfo.get("username");
        if (!("admin".equals(username))){
            throw new RuntimeException("你没有权限修改超级管理员角色");
        }*/
        Long roleId = roleDto.getRoleId();
        //删除角色与菜单的关联
        Integer deleteFlag = roleDao.deleteMenuRole(roleId);
        //获取菜单id集合
        List<Integer> menuIds = roleDto.getMenuIds();
        Integer roleid = roleId.intValue();
        //将角色id菜单权限id插入角色菜单关联表
        if (ToolUtil.isNotEmpty(menuIds)){
            Integer insertFlag = roleDao.insertRoleMenu(roleid,menuIds);
        }
        return deleteFlag;
    }

    /**
     * 修改超级管理员角色
     *
     * @param roleDto
     * @return
     */
    @Override
    public Integer editAdminRole(RoleDto roleDto) {
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户名
        String username = userInfo.get("username");
        roleDto.setUpdateBy(username);
        //修改角色信息
        Integer updateFlag = roleDao.updateAdminRole(roleDto);
        Long roleId = roleDto.getRoleId();
        //删除角色与菜单的关联
        Integer deleteFlag = roleDao.deleteMenuRole(roleId);
        //获取菜单id集合
        List<Integer> menuIds = roleDto.getMenuIds();
        Integer roleid = roleId.intValue();
        //将角色id菜单权限id插入角色菜单关联表
        if (ToolUtil.isNotEmpty(menuIds)){
            Integer insertFlag = roleDao.insertRoleMenu(roleid,menuIds);
        }
        return deleteFlag;
    }

    /**
     * 查看角色使用数量
     * @param roleId
     * @return
     */
    public int countUserRoleByRoleId(Long roleId)
    {
        return roleDao.countUserRoleByRoleId(roleId);
    }

    /**
     * 通过角色ID查询角色
     *
     * @param roleId 角色ID
     * @return 角色对象信息
     */
    public String selectRoleById(Long roleId)
    {
        return roleDao.selectRoleById(roleId);
    }
}