package com.rpzh.service;

import com.rpzh.entity.Result;
import com.rpzh.util.AuthToken;

/**
 * @Author liubc
 * @create 2021/4/15 9:02
 */
public interface OauthService {

    /**
     * 授权认证的方法
     * @param username 用户名
     * @param password 密码
     * @param cilentId 客户端id
     * @param clientSecret  客户端秘钥
     * @return
     */
    AuthToken login(String username, String password, String cilentId, String clientSecret);

    /**
     * 修改密码
     * @param username
     * @param newpassWord
     * @return
     */
    Result editPassWord(String username, String newpassWord, String notarizePassword);


}