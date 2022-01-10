package com.rpzh.service;

import com.rpzh.dto.MenuDto;
import com.rpzh.pojo.Menu;
import com.rpzh.vo.MenuVo;

import java.util.List;

/**
 * @Author liubc
 * @create 2021/5/24 9:51
 */
public interface MenuService {


    /**
     * 根据菜单名称和父菜单id判断菜单名称是否唯一
     * @param menuName
     * @param parentId
     * @return
     */
    Integer findMenuCount(String menuName, Long parentId);

    /**
     * 新增菜单
     * @param menuDto
     * @return
     */
    Integer insertMenu(MenuDto menuDto);

    /**
     * 获取父菜单名称
     * @param parentId
     * @return
     */
    String findParentMenuName(Long parentId);

    /**
     * 查询菜单是否存在子菜单
     * @param menuId
     * @return
     */
    Integer findSonMenu(Long menuId);

    /**
     * 查看菜单是否分配角色权限
     * @param menuId
     * @return
     */
    Integer findMenuRole(Long menuId);

    /**
     * 删除菜单
     * @param menuId
     * @return
     */
    Integer deleteMenu(Long menuId);

    /**
     * 判断菜单的唯一
     * @param parentId
     * @param menuId
     * @param menuName
     * @return
     */
    boolean findMenuList(Long parentId, Long menuId, String menuName);


    /**
     * 判断修改的菜单名称是否和子菜单相同
     * @param menuId
     * @param menuName
     * @return
     */
    boolean findMenuSonList(Long menuId, String menuName);

    /**
     * 修改菜单
     * @param menuDto
     * @return
     */
    Integer updateMenu(MenuDto menuDto);

    /**
     * 获取菜单列表树
     * @return
     */
    List<Menu> getRouters();

    /**
     * 获取所有的菜单
     * @return
     */
    List<Menu> findAllMenus();

    /**
     * 根据菜单id查询菜单集合
     * @param
     * @return
     */
    List<Integer> getOneRouters(String roleId);

    /**
     * 搜索菜单
     * @param menuDto
     * @return
     */
    List<MenuVo> searchMenuList(MenuDto menuDto);

    /**
     * 修改菜单状态成功
     * @param menuDto
     * @return
     */
    Integer changeMenuStatus(MenuDto menuDto);

    /**
     * 查询所有的菜单,所有的状态的
     * @return
     */
    List<Menu> SelectMenuList();
}