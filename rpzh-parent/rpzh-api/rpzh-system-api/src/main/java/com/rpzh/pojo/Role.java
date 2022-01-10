package com.rpzh.pojo;

import lombok.Data;

/**
 * @Author liubc成
 * @create 2021/5/20 11:31
 * 角色管理
 */
@Data
public class Role {

    //角色id
    private Integer roleId;

    //角色名称
    private String roleName;

    //角色权限字符串
    private String roleKey;

    //显示顺序
    private Integer roleSort;

    //数据范围
    private String dataScope;

    //角色状态
    private String status;

    //删除标志
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

}