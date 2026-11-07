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
     * 用户登录
     * @param id
     * @return
     */
    User logFindById(String id);

    /**
     * 预热单个用户的 Redis 缓存（USER_账号 / MAINTIAN_USER_ID_USERNAME）
     * @param username 用户账号
     * @return 是否成功
     */
    boolean warmUpUserCache(String username);

    /**
     * 全量预热所有有效用户的 Redis 缓存
     * @return 成功预热的用户数
     */
    int warmUpAllUserCache();

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
     * 中台账号解析为本地账号（中台化改造）。
     * 已绑定则返回本地账号；未绑定则按 defaultRoleKey 自动开户并绑定后返回。
     *
     * @param platformUid      中台用户唯一标识（sub/openid）
     * @param platformUsername 中台登录账号/昵称
     * @param defaultRoleKey   自动开户时赋予的本地默认角色 role_key
     * @return 本地账号 username
     */
    String resolvePlatformUser(String platformUid, String platformUsername, String defaultRoleKey);

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

    /**
     * mq删除用户
     * @param userNames
     * @param userIds
     * @return
     */
    Integer mqDeleteUser(List<String> userNames, List<Integer> userIds);

    /**
     * Mq修改信息
     * @param userDto
     * @return
     */
    Integer updateUser(UserDto userDto);

    /**
     * 管理员通过Mq修改用户密码
     * @param usernameAdmin
     * @param newpassWordAdmin
     * @return
     */
    Integer adminMqUpdatePassWord(String usernameAdmin, String newpassWordAdmin);
}