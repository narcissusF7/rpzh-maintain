package com.rpzh.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户操作类
 * @Author liubc
 * @create 2022/1/7 9:21
 * @Description: good good study,day day up
 */
@Data
public class User implements Serializable {

    //用户id
    private Integer userId;

    //部门id
    private Integer deptId;

    //用户账号
    private String username;

    //用户密码
    private String password;

    //用户昵称
    private String nickname;

    //用户类型
    private String userType;

    //用户邮箱
    private String email;

    //手机号码
    private String phonenumber;

    //用户性别,0:女,1:男 2:未知
    private String sex;

    //头像地址
    private String avatar;

    //账号状态0:停用,1:正常
    private String status;

    //删除标志:0:删除,1:存在
    private String delFlag;

    //最后登录ip
    private String loginIp;

    //最后登录时间
    private String loginDate;

    //创建者
    private String createBy;

    //创建时间
    private String createtime;

    //更新者
    private String updateBy;

    //更新时间
    private String updateTime;

    //备注
    private String remark;

}