package com.rpzh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author liubc成
 * @create 2021/5/19 18:46
 * 用户管理前端传参
 */
@Data
@ApiModel(description = "用户管理传参")
public class UserDto implements Serializable {

    public Long getId() {
        return userId;
    }

    public String findOperationContent() {
        return userName;
    }


    //用户id
    @ApiModelProperty(notes = "用户id")
    private Long userId;

    //用户id
    @ApiModelProperty(notes = "用户id集合")
    private Long[] userIds;

    //用户id
    @ApiModelProperty(notes = "用户id集合")
    private List<String> userNames;

    //部门id
    @ApiModelProperty(notes = "部门id")
    private Long deptId;

    //分页开始页面
    @ApiModelProperty(notes = "分页开始页")
    private int page;

    //分页每页显示
    @ApiModelProperty(notes = "分页每页显示")
    private int size;

    //用户账号
    @ApiModelProperty(notes = "用户账号")
    private String userName;

    //用户密码
    @ApiModelProperty(notes = "用户密码")
    private String password;

    //用户密码,RocketMq同步用户使用
    @ApiModelProperty(notes = "未加密用户密码")
    private String noEncryPassWord;

    //用户名称
    @ApiModelProperty(notes = "用户名称")
    private String nickName;

    //用户邮箱
    @ApiModelProperty(notes = "用户邮箱")
    private String email;

    //手机号码
    @ApiModelProperty(notes = "手机号码")
    private String phonenumber;

    //用户性别用户性别（0女 1男 2未知）
    @ApiModelProperty(notes = "用户性别用户性别（0女 1男 2未知）")
    private String sex;

    //用户头像
    @ApiModelProperty(notes = "用户头像")
    private String avatar;

    //盐
    @ApiModelProperty(notes = "盐")
    private String salt;

    //帐号状态（0正常 1停用）
    @ApiModelProperty(notes = "帐号状态（0正常 1停用）")
    private String status;

    //删除标志（0代表存在 2代表删除）
    @ApiModelProperty(notes = "删除标志（0代表存在 2代表删除）")
    private String delFlag;

    //最后登录IP
    @ApiModelProperty(notes = "最后登录IP")
    private String loginIp;

    //最后登录时间
    @ApiModelProperty(notes = "最后登录时间")
    private Date loginDate;

    //创建人
    @ApiModelProperty(notes = "创建人")
    private String createBy;

    //备注
    @ApiModelProperty(notes = "备注")
    private String remark;

    //更新人
    @ApiModelProperty(notes = "更新人")
    private String updateBy;

    //角色组
    @ApiModelProperty(notes = "角色组")
    private Long[] roleIds;
}