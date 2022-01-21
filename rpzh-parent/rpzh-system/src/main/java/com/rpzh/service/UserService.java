package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.UserDto;
import com.rpzh.pojo.User;
import com.rpzh.vo.UserVo;

import java.util.List;

/**
 * @Author liubc
 * @create 2021/3/30 16:32
 */
public interface UserService {

    /**
     * 用户登录
     * @param id
     * @return
     */
    User findById(String id);

    /**
     * 添加用户
     * @param userDto
     * @return
     */
    Integer addUser(UserDto userDto);

    /**
     * 分页查询所有用户信息
     * @param userDto
     * @return
     */
    PageInfo<UserVo> findAllUserList(UserDto userDto);


    /**
     * 删除用户
     * @param userId
     * @return
     */
    Integer deleteUser(Long[] userId);

    /**
     * 修改用户
     * @param userDto
     * @return
     */
    Integer editUser(UserDto userDto);

    /**
     * 用户修改密码
     * @param username
     * @param newpassWord
     * @return
     */
    Integer editPassWord(String username, String newpassWord);

    /**
     * 获取用户权限
     * @return
     */
    String getUserRole(String username);

    /**
     * 超级管理员可以修改所有人密码
     * 仅仅admin用户可以操作
     * @param username
     * @param newpassWord
     * @return
     */
    Integer adminUpdatePassWord(String username, String newpassWord);

    /**
     * 修改用户状态
     * @param userDto
     * @return
     */
    Integer changeUserStatus(UserDto userDto);

    /**
     * 修改用户时返回
     * @param userId
     * @return
     */
    List<Integer> findUserByUpdate(Long userId);

    /**
     * 查询个人信息
     * @return
     */
    UserVo findOneUserMessage();

    /**
     * 修改个人信息
     * @param userDto
     * @return
     */
    Integer changeUserMessage(UserDto userDto);


    /**
     * mq修改用户状态
     * @param userDto
     * @return
     */
    Integer mqChangeUserStatus(UserDto userDto);
}