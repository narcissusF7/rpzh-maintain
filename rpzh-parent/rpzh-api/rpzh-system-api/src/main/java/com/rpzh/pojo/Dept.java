package com.rpzh.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author liubc成
 * @create 2021/5/18 11:36
 * 部门操作类
 * tb_dept
 */
@Data
@ApiModel(description = "部门操作类")
public class Dept {

    //部门id
    @ApiModelProperty(notes = "部门id")
    private Integer deptId;

    //父部门名称
    @ApiModelProperty(notes = "父部门名称")
    private String parentName;

    //父部门id
    @ApiModelProperty(notes = "父部门id")
    private Integer parentId;

    //祖级列表
    @ApiModelProperty(notes = "祖级列表")
    private String ancestors;

    //部门名称
    @ApiModelProperty(notes = "部门名称")
    private String deptName;

    //显示顺序
    @ApiModelProperty(notes = "显示顺序")
    private Integer orderNum;

    //负责人
    @ApiModelProperty(notes = "负责人")
    private String leader;

    //联系电话
    @ApiModelProperty(notes = "联系电话")
    private String phone;

    //邮箱
    @ApiModelProperty(notes = "邮箱")
    private String email;

    //部门状态,0正常,1:停用
    @ApiModelProperty(notes = "部门状态,0正常,1:停用")
    private String status;

    //删除标志,0:存在,2:删除
    @ApiModelProperty(notes = "删除标志,0:存在,2:删除")
    private String delFlag;

    //创建者
    @ApiModelProperty(notes = "创建者")
    private String createBy;

    //创建时间
    @ApiModelProperty(notes = "创建时间")
    private String createTime;

    //更新者
    @ApiModelProperty(notes = "更新者")
    private String updateBy;

    //更新时间
    @ApiModelProperty(notes = "更新时间")
    private String updateTime;

    //子部门
    @ApiModelProperty(notes = "子部门")
    private List<Dept> children = new ArrayList<Dept>();

}