package com.rpzh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author liubc成
 * @create 2021/5/22 10:19
 * 菜单管理前端传参
 */
@Data
@ApiModel(description = "菜单管理传参")
public class MenuDto {

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

    //地址
    @ApiModelProperty(notes = "地址")
    private String path;

    //组件路径
    @ApiModelProperty(notes = "组件路径")
    private String component;

    //类型（M目录 C菜单 F按钮）
    @ApiModelProperty(notes = "类型（M目录 C菜单 F按钮）")
    private String menuType;

    //显示状态（0显示 1隐藏）
    @ApiModelProperty(notes = "显示状态（0显示 1隐藏）")
    private String visible;

    //创建时间
    @ApiModelProperty(notes = "创建时间")
    private String createtime;

    //菜单状态（0显示 1隐藏）
    @ApiModelProperty(notes = "菜单状态（0显示 1隐藏）")
    private String status;

    //权限字符串
    @ApiModelProperty(notes = "权限字符串")
    private String perms;

    //菜单图标
    @ApiModelProperty(notes = "菜单图标")
    private String icon;

    //创建人
    @ApiModelProperty(notes = "创建人")
    private String createBy;

    //更新人
    @ApiModelProperty(notes = "更新人")
    private String updateBy;

    //更新时间
    @ApiModelProperty(notes = "更新时间")
    private String updatetime;

    //备注
    @ApiModelProperty(notes = "备注")
    private String remark;

    //用户名
    @ApiModelProperty(notes = "用户名")
    private String username;


}