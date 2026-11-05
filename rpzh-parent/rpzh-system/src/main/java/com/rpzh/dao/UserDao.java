package com.rpzh.dao;

import cn.hutool.core.date.DateTime;
import com.rpzh.dto.UserDto;
import com.rpzh.pojo.User;
import com.rpzh.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author liubc
 * @create 2021/3/30 16:33
 */
@Mapper
public interface UserDao {


    /**
     * 登录查询用户
     * @param id
     * @return
     */
    User findById(String id);
    /**
     * 登录查询用户
     * @param id
     * @return
     */
    User logFindById(String id);

    /**
     * 查询用户是否存在
     * @param userName
     * @return
     */
    Integer findUserNameCount(@Param("userName") String userName);

    /**
     * 添加用户
     * @param userDto
     * @return
     */
    Integer addUser(@Param("userDto") UserDto userDto);

    /**
     * 查看手机号是否唯一
     * @param phonenumber
     * @return
     */
    Integer findCountPhoneNumber(@Param("phonenumber") String phonenumber);

    /**
     * 根据用户账号获取用户id
     * @param userName
     * @return
     */
    Integer findUserByUserName(@Param("userName") String userName);

    /**
     * 将用户id 部门id插入到用户部门关联表
     * @param userId
     * @param deptId
     * @return
     */
    Integer insertUserDept(@Param("userId") Integer userId, @Param("deptId") Long deptId);


    /**
     * 将用户id,角色id插入到用户角色关联表
     * @param userId
     * @param roleIds
     * @return
     */
    Integer insertUserRole(@Param("userId") Integer userId, @Param("roleIds") Long[] roleIds);

    /**
     * 分页查询所有用户的信息
     * @param userDto
     * @return
     */
    List<UserVo> findAllUserList(@Param("userDto") UserDto userDto);


    /**
     * 删除用户
     * @param userId
     * @return
     */
    Integer updateUser(@Param("userId") Long[] userId);

    /**
     * 修改管理员信息
     * 只允许修改用户昵称,部门,手机号,邮箱
     * @param userDto
     * @return
     */
    Integer updateAdmin(@Param("userDto") UserDto userDto);

    /**
     * 删除用户部门关联
     * @param userId
     * @return
     */
    Integer deleteUserDept(@Param("userId") Long userId);

    /**
     * 添加用户部门关联
     * @param userDto
     * @return
     */
    Integer addUserDept(@Param("userDto") UserDto userDto);


    /**
     * 修改普通用户信息
     * @param userDto
     * @return
     */
    Integer updateCommonUser(@Param("userDto") UserDto userDto);


    /**
     * 删除用户角色关联
     * @param userId
     * @return
     */
    Integer deleteUserRole(@Param("userId") Long userId);


    /**
     * 添加用户角色关联
     * @param userId
     * @param roleIds
     * @return
     */
    Integer addUserRole(@Param("userId") Long userId, @Param("roleIds") Long[] roleIds);

    /**
     * 修改密码
     * @param username
     * @param passWord
     * @return
     */
    Integer editPassWord(@Param("username") String username, @Param("passWord") String passWord);

    /**
     * 查看用户角色
     * @param username
     * @return
     */
    List<String> findUserRole(@Param("username") String username);

    /**
     * 查询用户菜单权限
     * @param role
     * @return
     */
    List<String> findRole(@Param("role") String role);

    /**
     * 超级管理员mq发送消息修改密码
     * @param username
     * @param passWord
     * @return
     */
    Integer adminUpdatePassWord(@Param("username") String username, @Param("passWord") String passWord);

    /**
     * 修改用户的状态
     * @param userDto
     * @return
     */
    Integer changeUserStatus(@Param("userDto") UserDto userDto);


    /**
     * 修改用户时返回
     * @param userId
     * @return
     */
    List<Integer> findUserByUpdate(@Param("userId") Long userId);

    /**
     * 查询用户的信息
     * @param username
     * @return
     */
    UserVo findOneUserMessage(@Param("username") String username);

    /**
     * 查询角色名称
     * @param username
     * @return
     */
    List<String> findRoleNameList(@Param("username") String username);


    /**
     *个人中心修改用户信息
     * @param userDto
     * @return
     */
    Integer changeUserMessage(@Param("userDto") UserDto userDto);

    /**
     * 根据用户的账号查询用户的id
     * @param username
     * @return
     */
    Long findUserId(@Param("username") String username);

    /**
     * 批量删除用户与角色的关联
     * @param userId
     * @return
     */
    Integer deleteUserConnectRole(@Param("userId") Long[] userId);

    /**
     * 删除用户部门的关联
     * @param userId
     * @return
     */
    Integer deleteUserConnectDept(@Param("userId") Long[] userId);

    /**
     * 管理员修改密码的同时当前用户的错误次数改为0
     * @param username
     * @return
     */
    int updateErrorTimes(@Param("username") String username);

    /**
     * 修改密码时修改更新时间
     * @param username
     * @param dateTime
     * @return
     */
    int updateTime(@Param("username") String username, @Param("dateTime") DateTime dateTime);

    /**
     * 查询是否是管理员角色
     * @param adminUsername
     * @return
     */
    List<Integer> findIsAdminRole(@Param("adminUsername") String adminUsername);

    /**
     * mq修改
     * @param userDto
     * @return
     */
    Integer mqChangeUserStatus(@Param("userDto") UserDto userDto);

    /**
     * mq删除用户
     * @param userNames
     * @return
     */
    Integer mqDeleteUser(@Param("userNames") List<String> userNames);

    /**
     * mq删除关联表
     * @param userIds
     * @return
     */
    Integer deleteMqUserConnectRole(@Param("userIds") List<Integer> userIds);

    /**
     * mq删除关联表
     * @param userIds
     * @return
     */
    Integer deleteMqUserConnectDept(@Param("userIds") List<Integer> userIds);

    /**
     * Mq修改用户信息
     * @param userDto
     * @return
     */
    Integer mqUpdateUser(@Param("userDto") UserDto userDto);

    /**
     * 查询缓存预热所需的用户信息（账号、昵称、手机号、部门）
     * @param username 用户账号
     * @return
     */
    UserVo findUserCache(@Param("username") String username);

    /**
     * 查询全部有效用户账号，用于缓存全量预热
     * @return
     */
    List<String> findAllValidUsername();

    /**
     * 按用户id查询账号，用于用户信息变更后刷新缓存
     * @param userId
     * @return
     */
    String findUsernameById(@Param("userId") Long userId);
}