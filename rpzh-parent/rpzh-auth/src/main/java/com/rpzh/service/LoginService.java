package com.rpzh.service;

/**
 * @Author liubc
 * @create 2021/8/2 16:33
 */
public interface LoginService {

    /**
     * 每次登录失败时失败次数加1
     * @param username
     * @return
     */
    int addErrorTimes(String username);

    /**
     * 登录成功后错误次数清零
     * @param username
     * @return
     */
    int clearError(String username);

    /**
     * 查询错误次数
     * @param username
     * @return
     */
    Integer checkErrorTimes(String username);

    /**
     * 查询用户密码更新时间
     * @param username
     * @return
     */
    String checkUpdateTime(String username);

    /**
     * 生成图片验证码
     * @param uuidCode
     * @param code
     */
    void saveCode(String uuidCode, String code);

    /**
     * 获取redis图片验证码
     * @param uuidCode
     * @return
     */
    String findRedisCode(String uuidCode);

    /**
     * 校验通过后删除验证码，防止重放
     * @param uuidCode
     */
    void deleteCode(String uuidCode);
}
