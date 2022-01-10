package com.rpzh.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author liubc成
 * @create 2021/5/20 11:30
 * 角色管理传参
 */
@Data
public class RoleDto {
    //角色id
    private Long roleId;

    //第几页
    private int page;

    //每页显示数
    private int size;

    //角色名称
    private String roleName;

    //角色权限字符串
    private String roleKey;

    //显示顺序
    private Integer roleSort;

    //数据范围 1=所有数据权限,2=自定义数据权限,3=仅本人数据权限
    private String dataScope;

    //角色状态（0停用,1正常）
    private String status;

    //删除标志（0代表存在 2代表删除）
    private String delFlag;

    //创建者
    private String createBy;

    //创建时间
    private String createTime;

    //更新者
    private String updateBy;

    //更新时间
    private String updateTime;

    //备注
    private String remark;

    //菜单id
    private List<Integer> menuIds;

}