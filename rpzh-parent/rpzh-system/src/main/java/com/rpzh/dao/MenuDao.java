package com.rpzh.dao;

import com.rpzh.dto.MenuDto;
import com.rpzh.pojo.Menu;
import com.rpzh.vo.MenuVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author liubc
 * @create 2021/5/24 9:53
 */
@Mapper
public interface MenuDao {
    /**
     * 根据菜单名称和菜单id判断菜单名称是否唯一
     * @param menuName
     * @param parentId
     * @return
     */
    Integer findMenuCount(@Param("menuName") String menuName, @Param("parentId") Long parentId);

    /**
     * 新增菜单
     * @param menuDto
     * @return
     */
    Integer insertMenu(@Param("menuDto") MenuDto menuDto);


    /**
     * 获取父菜单名称
     * @param parentId
     * @return
     */
    String findParentMenuName(@Param("parentId") Long parentId);

    /**
     * 查询菜单是否存在子菜单
     * @param menuId
     * @return
     */
    Integer findSonMenu(@Param("menuId") Long menuId);

    /**
     * 查看菜单是否分配角色权限
     * @param menuId
     * @return
     */
    Integer findMenuRole(@Param("menuId") Long menuId);

    /**
     * 删除菜单
     * @param menuId
     * @return
     */
    Integer deleteMenu(@Param("menuId") Long menuId);

    /**
     * 查询所有得菜单
     * @param parentId
     * @param menuId
     * @return
     */
    List<String> findMenuList(@Param("parentId") Long parentId, @Param("menuId") Long menuId);

    /**
     * 获取所有的子菜单
     * @param menuId
     * @return
     */
    List<String> findMenuSonList(@Param("menuId") Long menuId);

    /**
     * 修改菜单
     * @param menuDto
     * @return
     */
    Integer updateMenu(@Param("menuDto") MenuDto menuDto);

    /**
     * 获取用户的角色信息
     * @param username
     * @return
     */
    //List<String> findRole(@Param("username") String username);

    /**
     * 管理员查询所有的菜单
     * @param menuDto
     * @return
     */
    List<Menu> findAllMenu(@Param("menuDto") MenuDto menuDto);

    /**
     * 查询用户的特有菜单列表
     * @param menuDto
     * @return
     */
    List<Menu> findUserSpecificMenu(@Param("menuDto") MenuDto menuDto);

    /**
     * 管理员查询所有的菜单
     * @return
     */
    List<Menu> findAllMenuRouters();

    /**
     * 普通用户查询菜单
     * @param username
     * @return
     */
    List<Menu> findUserRoutersMenu(@Param("username") String username);

    /**
     * 查询所有的菜单
     * @return
     */
    List<Menu> findAllMenus();

    /**
     * 查询特有菜单
     * @param username
     * @return
     */
    List<Menu> findUserMenu(@Param("username") String username);

    /**
     * 根据菜单id,查询菜单集合
     * @param
     * @return
     */
    List<Integer> getOneRouters(@Param("roleId") String roleId);

    /**
     * 搜索菜单
     * @param menuDto
     * @return
     */
    List<MenuVo> searchMenuList(@Param("menuDto") MenuDto menuDto);

    /**
     * 普通用户搜索菜单
     * @param menuDto
     * @return
     */
    List<MenuVo> searchUserMenuList(@Param("menuDto") MenuDto menuDto);

    /**
     * 修改菜单状态成功
     * @param menuDto
     * @return
     */
    Integer changeMenuStatus(@Param("menuDto") MenuDto menuDto);

    /**
     * 查询正常的菜单
     * @return
     */
    List<Menu> findAllMenuStatuTrue();

    /**
     * 特有用户的正常菜单
     * @param username
     * @return
     */
    List<Menu> findUserRoutersMenuStatuTrue(@Param("username") String username);

    /**
     * 查询父部门下所有的子部门id
     * @param menuId1
     * @return
     */
    List<Integer> findParentSon(@Param("menuId1") Long menuId1);

    /**
     * 查询用户的角色
     * @param username
     * @return
     */
    List<Integer> findAdminRole(@Param("username") String username);
}
