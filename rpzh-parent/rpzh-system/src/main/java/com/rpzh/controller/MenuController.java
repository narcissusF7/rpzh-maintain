package com.rpzh.controller;

import com.rpzh.annotation.OperationLog;
import com.rpzh.dto.MenuDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.BusinessType;
import com.rpzh.pojo.Menu;
import com.rpzh.service.MenuService;
import com.rpzh.service.RoleService;
import com.rpzh.vo.MenuVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author liubc
 * @create 2021/5/20 14:30
 * 菜单管理
 */
@RestController
@RequestMapping("/menu")
@CrossOrigin
@Api(value = "菜单管理", tags = "菜单管理", description = "菜单管理")
public class MenuController {

    @Autowired
    private MenuService menuService;


    /**
     * 获取菜单列表
     * @return
     */
    @RequestMapping("/list")
    @ApiOperation(value = "获取菜单列表")
    public Result menuList(){
        List<Menu> menuList = menuService.SelectMenuList();
        //构建前端所需要的菜单
        return new Result(true, StatusCode.OK, "获取菜单列表成功",menuList);
    }

    /**
     *新增菜单
     * @param menuDto
     * @return
     */
    @PostMapping("/addMenu")
    @ApiOperation(value = "新增菜单")
    @OperationLog(mapper = MenuService.class, businessType = BusinessType.INSERT, title = "菜单管理")
    public Result addMenu(MenuDto menuDto){
        String path = menuDto.getPath();
        if (ToolUtil.isEmpty(path)){
            return new Result(false, StatusCode.ERROR, "地址不能为空");
        }
        //获取父菜单id
        Long parentId = menuDto.getParentId();
        if (ToolUtil.isEmpty(parentId)){
            return new Result(false, StatusCode.ERROR, "上级菜单不能为空");
        }
        //根据父菜单id查询父菜单名称
        String parentMenuNume = menuService.findParentMenuName(parentId);
        //获取菜单名称
        String menuName = menuDto.getMenuName();
        if (ToolUtil.isEmpty(menuName)){
            return new Result(false, StatusCode.ERROR, "菜单名称不能为空");
        }
        if (menuName.equals(parentMenuNume)){
            return new Result(false, StatusCode.ERROR, "子菜单名称不能与父菜单相同");
        }
        //根据父菜单id和菜单名称查询菜单名称是否唯一
        Integer menuCount = menuService.findMenuCount(menuName,parentId);
        if (menuCount > 0){
            return new Result(false, StatusCode.ERROR, "该菜单名称已经存在");
        }
        //新增菜单
        Integer insert = menuService.insertMenu(menuDto);
        return new Result(true, StatusCode.OK, "新增菜单成功");
    }


    /**
     * 删除菜单
     * @param menuId
     * @return
     */
    @GetMapping("/deleteMenu")
    @ApiOperation(value = "删除菜单")
    @OperationLog(mapper = MenuService.class, businessType = BusinessType.DELETE, title = "菜单管理")
    public Result deleteMenu(@RequestParam("menuId") Long menuId){
        //查询是否存在子菜单
        Integer menuCount = menuService.findSonMenu(menuId);
        if (menuCount > 0){
            return new Result(false, StatusCode.ERROR, "存在子菜单,不允许删除");
        }
        //查看菜单是否分配角色权限
        Integer menuRole = menuService.findMenuRole(menuId);
        if (menuRole > 0){
            return new Result(false, StatusCode.ERROR, "该菜单已经分配角色,不能删除");
        }
        Integer menuFlag = menuService.deleteMenu(menuId);
        return new Result(true, StatusCode.OK, "删除菜单成功");
    }


    /**
     * 修改菜单
     * @param menuDto
     * @return
     */
    //@PreAuthorize("hasAnyAuthority('system:menu:editMenu')")
    @PostMapping("/editMenu")
    @ApiOperation(value = "修改菜单")
    @OperationLog(mapper = MenuService.class, businessType = BusinessType.UPDATE, title = "菜单管理")
    public Result editMenu(MenuDto menuDto){
        //获取父菜单名称
        String parentName = menuDto.getParentName();
        //获取修改的菜单名称
        String menuName = menuDto.getMenuName();
        //获取修改的菜单id
        Long menuId = menuDto.getMenuId();
        //获取父菜单id
        Long parentId = menuDto.getParentId();
        if (ToolUtil.isEmpty(parentId) || ToolUtil.isEmpty(menuId) || ToolUtil.isEmpty(menuName) || ToolUtil.isEmpty(parentName)){
            return new Result(false, StatusCode.ERROR, "上级菜单,菜单名称不能为空");
        }
        if (menuId.equals(parentId)){
            return new Result(false, StatusCode.ERROR, "父部门不能是自己");
        }
        //判断菜单的唯一
        boolean bool = menuService.findMenuList(parentId,menuId,menuName);
        if (bool){
            return new Result(false, StatusCode.ERROR, "修改菜单失败,该菜单名称已经存在");
        }
        if (parentName.equals(menuName)){
            return new Result(false, StatusCode.ERROR, "菜单名称不能和父菜单相同");
        }
        //判断修改的菜单名称是否与子菜单相同
        boolean sonBool = menuService.findMenuSonList(menuId,menuName);
        if (sonBool){
            return new Result(false, StatusCode.ERROR, "菜单名称不能和子菜单相同");
        }
        //修改菜单
        Integer updateMenu = menuService.updateMenu(menuDto);
        return new Result(true, StatusCode.OK, "修改菜单成功");
    }

    /**
     * 查询所有的菜单
     * 新增菜单时返回
     * @param
     * @return
     */
    @PostMapping("/findAllMenu")
    @ApiOperation(value = "查询所有的菜单新增菜单时返回")
    public Result findAllMenu(){
        List<Menu> menuList = menuService.findAllMenus();
        return new Result(true, StatusCode.OK, "查询菜单成功",menuList);
    }


    /**
     * 获取路由菜单,加载首页菜单列表树
     * @return
     */
    @GetMapping("/getRouters")
    @ApiOperation(value = "获取路由菜单,加载首页菜单列表树")
    public Result getRouters(){
        List<Menu> menuList = menuService.getRouters();
        //构建前端所需要的菜单
        return new Result(true, StatusCode.OK, "获取菜单列表成功",menuList);
    }

    /**
     * 修改菜单时返回
     * @param roleId
     * @return
     */
    @RequestMapping("/getOneRouters")
    @ApiOperation(value = "修改菜单时返回")
    public Result getOneRouters(@RequestParam("roleId") String roleId){
        List<Integer> menuIdList = menuService.getOneRouters(roleId);
        return new Result(true, StatusCode.OK, "查询菜单列表成功",menuIdList);
    }

    /**
     * 搜索菜单
     * @param menuDto
     * @return
     */
    @RequestMapping("/searchMenuList")
    @ApiOperation(value = "搜索菜单")
    public Result searchMenuList(MenuDto menuDto){
        List<MenuVo> menuVoList = menuService.searchMenuList(menuDto);
        return new Result(true, StatusCode.OK, "搜索菜单成功",menuVoList);
    }

    /**
     * 修改菜单状态
     * @param menuDto
     * @return
     */
    @RequestMapping("/changeMenuStatus")
    @ApiOperation(value = "修改菜单状态")
    @OperationLog(mapper = MenuService.class, businessType = BusinessType.UPDATE, title = "菜单管理")
    public Result changeMenuStatus(MenuDto menuDto){
        Integer changeFlag = menuService.changeMenuStatus(menuDto);
        return new Result(true, StatusCode.OK, "修改菜单状态成功");
    }
}