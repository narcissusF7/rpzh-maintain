package com.rpzh.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author liubc成
 * @create 2021/5/22 10:20
 * 菜单管理数据库映射类
 */
@Data
@ApiModel(description = "菜单管理数据库映射类")
public class Menu {

    //菜单ID
    @ApiModelProperty(notes = "菜单ID")
    private Long menuId;

    //菜单名称
    @ApiModelProperty(notes = "菜单名称")
    private String menuName;

    //父菜单名称
    @ApiModelProperty(notes = "父菜单名称")
    private String parentName;

    //父菜单ID
    @ApiModelProperty(notes = "父菜单ID")
    private Long parentId;

    //显示顺序
    @ApiModelProperty(notes = "显示顺序")
    private String orderNum;

    //路由地址
    @ApiModelProperty(notes = "路由地址")
    private String path;

    //组件路径
    @ApiModelProperty(notes = "组件路径")
    private String component;

    //是否为外链（0是 1否）
    @ApiModelProperty(notes = "是否为外链（0是 1否）")
    private String isFrame;

    //是否缓存（0缓存 1不缓存）
    @ApiModelProperty(notes = "是否缓存（0缓存 1不缓存）")
    private String isCache;

    //类型（M目录 C菜单 F按钮）
    @ApiModelProperty(notes = "类型（M目录 C菜单 F按钮）")
    private String menuType;

    //显示状态（0显示 1隐藏）
    @ApiModelProperty(notes = "显示状态（0显示 1隐藏）")
    private String visible;

    //菜单状态（0显示 1隐藏）
    @ApiModelProperty(notes = "菜单状态（0显示 1隐藏）")
    private String status;

    //权限字符串
    @ApiModelProperty(notes = "权限字符串")
    private String perms;

    //菜单图标
    @ApiModelProperty(notes = "菜单图标")
    private String icon;

    //创建时间
    @ApiModelProperty(notes = "创建时间")
    private String createtime;

    //子菜单
    @ApiModelProperty(notes = "子菜单")
    private List<Menu> children = new ArrayList<Menu>();
}