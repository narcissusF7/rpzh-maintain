package com.rpzh.service.impl;

import com.rpzh.dao.MenuDao;
import com.rpzh.dto.MenuDto;
import com.rpzh.pojo.Menu;
import com.rpzh.service.MenuService;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.MenuVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author liubc
 * @create 2021/5/24 9:51
 */
@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuDao menuDao;


    /**
     * 根据菜单名称和父菜单id判断菜单名称是否唯一
     *
     * @param menuName
     * @param parentId
     * @return
     */
    @Override
    public Integer findMenuCount(String menuName, Long parentId) {
        Integer menuCount = menuDao.findMenuCount(menuName, parentId);
        return menuCount;
    }

    /**
     * 新增菜单
     *
     * @param menuDto
     * @return
     */
    @Override
    public Integer insertMenu(MenuDto menuDto) {
        //获取当前时间
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createTime = sdf.format(date);
        menuDto.setCreatetime(createTime);
        //获取当前用户
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户名
        String username = userInfo.get("username");
        menuDto.setCreateBy(username);
        Integer intsert = menuDao.insertMenu(menuDto);
        return intsert;
    }

    /**
     * 获取父菜单名称
     *
     * @param parentId
     * @return
     */
    @Override
    public String findParentMenuName(Long parentId) {
        String parentMenuName = menuDao.findParentMenuName(parentId);
        return parentMenuName;
    }

    /**
     * 查询菜单是否存在子菜单
     *
     * @param menuId
     * @return
     */
    @Override
    public Integer findSonMenu(Long menuId) {
        Integer menuCount = menuDao.findSonMenu(menuId);
        return menuCount;
    }

    /**
     * 查看菜单是否分配角色权限
     *
     * @param menuId
     * @return
     */
    @Override
    public Integer findMenuRole(Long menuId) {
        Integer menuRole = menuDao.findMenuRole(menuId);
        return menuRole;
    }

    /**
     * 删除菜单
     *
     * @param menuId
     * @return
     */
    @Override
    public Integer deleteMenu(Long menuId) {
        Integer menuFlag = menuDao.deleteMenu(menuId);
        return menuFlag;
    }

    @Override
    public boolean findMenuList(Long parentId, Long menuId, String menuName) {
        //查询所有的菜单
        List<String> menuList = menuDao.findMenuList(parentId, menuId);
        for (int i = 0; i < menuList.size(); i++) {
            String menu = menuList.get(i);
            if (menu.equals(menuName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断修改的菜单名称是否和子菜单相同
     *
     * @param menuId
     * @param menuName
     * @return
     */
    @Override
    public boolean findMenuSonList(Long menuId, String menuName) {
        List<String> menuSonMenu = menuDao.findMenuSonList(menuId);
        for (int i = 0; i < menuSonMenu.size(); i++) {
            String menu = menuSonMenu.get(i);
            if (menu.equals(menuName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 修改菜单
     *
     * @param menuDto
     * @return
     */
    @Override
    public Integer updateMenu(MenuDto menuDto) {
        //获取父菜单id
        Long parentId = menuDto.getParentId();
        int parentid = parentId.intValue();
        //获取修改菜单id
        Long menuId1 = menuDto.getMenuId();
        List<Integer> menuList = menuDao.findParentSon(menuId1);
        for (int i = 0; i < menuList.size(); i++) {
            int menuId = menuList.get(i);
            if (parentid == menuId){
                throw new RuntimeException("父部门的父部门不能是子部门");
            }
        }
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户名
        String username = userInfo.get("username");
        menuDto.setUpdateBy(username);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String updatetime = sdf.format(date);
        menuDto.setUpdatetime(updatetime);
        Integer updateMenu = menuDao.updateMenu(menuDto);
        return updateMenu;
    }

    /**
     * 获取菜单列表树
     *
     * @return
     */
    @Override
    public List<Menu> getRouters() {
        //获取用户信息
        //标记:前端测试用例,后期注释放开
        //Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户名
        //String username = userInfo.get("username");
        String username = "rpzh";
        //根据用户名账号查询用户的角色
        List<Integer> adminRoleList = menuDao.findAdminRole(username);
        boolean flag = false;
        for (Integer roleId : adminRoleList) {
            //判断是否包含管理员的角色
            if (roleId == 1){
                flag = true;
                break;
            }
        }
        /*List<String> roleList = menuDao.findRole(username);
        boolean flag = false;
        for (int i = 0; i < roleList.size(); i++) {
            String roleKey = roleList.get(i);
            //判断是否包含管理员的角色
            if ("admin".equals(roleKey)) {
                flag = true;
                break;
            }
        }*/
        List<Menu> menuList = null;
        if (flag) {
            //管理员查询所有的菜单
            menuList = menuDao.findAllMenuStatuTrue();
        } else {
            //根据用户的账号查询用户的特有菜单
            menuList = menuDao.findUserRoutersMenuStatuTrue(username);
        }
        return getChildPerms(menuList, 0);
    }

    /**
     * 获取所有的菜单
     *
     * @return
     */
    @Override
    public List<Menu> findAllMenus() {
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户名
        String username = userInfo.get("username");
        //根据用户名账号查询用户的角色
        List<Integer> adminRoleList = menuDao.findAdminRole(username);
        boolean flag = false;
        for (Integer roleId : adminRoleList) {
            //判断是否包含管理员的角色
            if (roleId == 1){
                flag = true;
                break;
            }
        }
        /*List<String> roleList = menuDao.findRole(username);
        boolean flag = false;
        for (int i = 0; i < roleList.size(); i++) {
            String roleKey = roleList.get(i);
            //判断是否包含管理员的角色
            if ("admin".equals(roleKey)) {
                flag = true;
                break;
            }
        }*/
        List<Menu> menuList = null;
        if (flag) {
            //管理员查询所有的菜单
            menuList = menuDao.findAllMenuRouters();
        } else {
            //根据用户的账号查询用户的特有菜单
            menuList = menuDao.findUserRoutersMenu(username);
        }
        return getChildPerms(menuList, 0);
    }

    /**
     * 根据菜单id查询菜单集合
     *
     * @param
     * @return
     */
    @Override
    public List<Integer> getOneRouters(String roleId) {
        List<Integer> menuIdList = menuDao.getOneRouters(roleId);
        return menuIdList;
    }

    /**
     * 搜索菜单
     * @param menuDto
     * @return
     */
    @Override
    public List<MenuVo> searchMenuList(MenuDto menuDto) {
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户名
        String username = userInfo.get("username");
        menuDto.setUsername(username);
        //根据用户名账号查询用户的角色
        List<Integer> adminRoleList = menuDao.findAdminRole(username);
        boolean flag = false;
        for (Integer roleId : adminRoleList) {
            //判断是否包含管理员的角色
            if (roleId == 1){
                flag = true;
                break;
            }
        }
        /*List<String> roleList = menuDao.findRole(username);
        boolean flag = false;
        for (int i = 0; i < roleList.size(); i++) {
            String roleKey = roleList.get(i);
            //判断是否包含管理员的角色
            if ("admin".equals(roleKey)) {
                flag = true;
                break;
            }
        }*/
        List<MenuVo> menuList = null;
        if (flag) {
            //管理员查询所有的菜单
            menuList = menuDao.searchMenuList(menuDto);
        } else {
            //根据用户的账号查询用户的特有菜单
            menuList = menuDao.searchUserMenuList(menuDto);
        }
        return menuList;
    }

    /**
     * 修改菜单状态成功
     *
     * @param menuDto
     * @return
     */
    @Override
    public Integer changeMenuStatus(MenuDto menuDto) {
        Integer changeFlag = menuDao.changeMenuStatus(menuDto);
        return changeFlag;
    }

    /**
     * 查询所有的菜单,所有的状态的
     *
     * @return
     */
    @Override
    public List<Menu> SelectMenuList() {
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户名
        String username = userInfo.get("username");
        //根据用户名账号查询用户的角色
        List<Integer> adminRoleList = menuDao.findAdminRole(username);
        boolean flag = false;
        for (Integer roleId : adminRoleList) {
            //判断是否包含管理员的角色
            if (roleId == 1){
                flag = true;
                break;
            }
        }
       /* List<String> roleList = menuDao.findRole(username);
        boolean flag = false;
        for (int i = 0; i < roleList.size(); i++) {
            String roleKey = roleList.get(i);
            //判断是否包含管理员的角色
            if ("admin".equals(roleKey)) {
                flag = true;
                break;
            }
        }*/
        List<Menu> menuList = null;
        if (flag) {
            //管理员查询所有的菜单
            menuList = menuDao.findAllMenuRouters();
        } else {
            //根据用户的账号查询用户的特有菜单
            menuList = menuDao.findUserRoutersMenu(username);
        }
        return getChildPerms(menuList, 0);
    }

    /**
     * 根据父节点的ID获取所有子节点
     *
     * @param list     分类表
     * @param parentId 传入的父节点ID
     * @return String
     */
    public List<Menu> getChildPerms(List<Menu> list, int parentId) {
        List<Menu> returnList = new ArrayList<Menu>();
        for (Iterator<Menu> iterator = list.iterator(); iterator.hasNext(); ) {
            Menu t = (Menu) iterator.next();
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() == parentId) {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 递归列表
     *
     * @param list
     * @param t
     */
    private void recursionFn(List<Menu> list, Menu t) {
        // 得到子节点列表
        List<Menu> childList = getChildList(list, t);
        t.setChildren(childList);
        for (Menu tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     * @param list
     * @param t
     * @return
     */
    private List<Menu> getChildList(List<Menu> list, Menu t) {
        List<Menu> tlist = new ArrayList<Menu>();
        Iterator<Menu> it = list.iterator();
        while (it.hasNext()) {
            Menu n = (Menu) it.next();
            if (n.getParentId().longValue() == t.getMenuId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }


    /**
     * 判断是否有子节点
     * @param list
     * @param t
     * @return
     */
    private boolean hasChild(List<Menu> list, Menu t) {
        return getChildList(list, t).size() > 0 ? true : false;
    }
}