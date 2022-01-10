package com.rpzh.dto;

import lombok.Data;

/**
 * @Author liubc成
 * @create 2021/5/18 15:39
 * 部门管理前端传参
 */
@Data
public class DeptDto {

    //部门id
    private Integer deptId;

    //父部门id
    private Integer parentId;

    //祖级列表
    private String ancestors;

    //部门名称
    private String deptName;

    //显示顺序
    private Integer orderNum;

    //负责人
    private String leader;

    //联系电话
    private String phone;

    //邮箱
    private String email;

    //部门状态,0正常,1:停用
    private String status;

    //删除标志,0:存在,2:删除
    private String delFlag;

    //创建者
    private String createBy;

    //创建时间
    private String createTime;

    //更新者
    private String updateBy;

    //更新时间
    private String updateTime;

}