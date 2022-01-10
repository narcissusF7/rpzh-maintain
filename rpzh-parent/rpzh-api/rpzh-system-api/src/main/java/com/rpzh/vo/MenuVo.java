package com.rpzh.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author liubc成
 * @create 2021/5/25 19:51
 * 菜单管理返回数据
 */
@Data
public class MenuVo {

    //菜单ID
    private Long menuId;

    //菜单名称
    private String menuName;

    //父菜单名称
    private String parentName;

    //父菜单ID
    private Long parentId;

    //显示顺序
    private String orderNum;

    //路由地址
    private String path;

    //组件路径
    private String component;

    //是否为外链（0是 1否）
    private String isFrame;

    //是否缓存（0缓存 1不缓存）
    private String isCache;

    //类型（M目录 C菜单 F按钮）
    private String menuType;

    //显示状态（0显示 1隐藏）
    private String visible;

    //菜单状态（0失效 1正常）
    private String status;

    //权限字符串
    private String perms;

    //菜单图标
    private String icon;

    //创建时间
    private String createtime;
    /**
     * 路由名字
     */
    private String name;

    /**
     * 是否隐藏路由，当设置 true 的时候该路由不会再侧边栏出现
     */
    private boolean hidden;

    /**
     * 重定向地址，当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
     */
    private String redirect;


    /**
     * 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
     */
    private Boolean alwaysShow;

    /**
     * 其他元素
     */
    private MetaVo meta;

    /**
     * 子路由
     */
    private List<RouterVo> children;
}