package com.rpzh.service.impl;

import com.rpzh.dao.LoginDao;
import com.rpzh.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * @Author liubc
 * @create 2021/8/2 16:34
 */
@Service
public class LoginServiceImpl implements LoginService {

    private static final String CACHE_KEY_RPZH_CODE = "RPZH_CAP_";

    @Autowired
    private LoginDao loginDao;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 每次登录失败时失败次数加1
     * @param username 用户的账号
     * @return
     */
    @Override
    public int addErrorTimes(String username) {
        //查询当前错误的次数
        int errorTimes = loginDao.findErrorTimes(username);
        errorTimes = errorTimes + 1;
        //错误次数加1
        int addErrorTimes = loginDao.addErrorTimes(username,errorTimes);
        return addErrorTimes;
    }

    /**
     * 登录成功后错误次数清零
     *
     * @param username
     * @return
     */
    @Override
    public int clearError(String username) {
        int clearError = loginDao.clearError(username);
        return clearError;
    }

    /**
     * 查询错误次数
     *
     * @param username
     * @return
     */
    @Override
    public Integer checkErrorTimes(String username) {
        //查询当前错误的次数
        Integer errorTimes = loginDao.findErrorTimes(username);
        return errorTimes;
    }

    /**
     * 查询用户密码更新时间
     *
     * @param username
     * @return
     */
    @Override
    public String checkUpdateTime(String username) {
        String updateTime = loginDao.checkUpdateTime(username);
        return updateTime;
    }

    /**
     * 生成图片验证码
     * @param uuidCode
     * @param code
     */
    @Override
    public void saveCode(String uuidCode, String code) {
        String key = CACHE_KEY_RPZH_CODE + uuidCode;
        // 缓存验证码，2分钟后失效
        redisTemplate.opsForValue().set(key,code, Duration.ofMinutes(2));
    }

    /**
     * 获取redis图片验证码
     * @param uuidCode
     * @return
     */
    @Override
    public String findRedisCode(String uuidCode) {
        String key = CACHE_KEY_RPZH_CODE + uuidCode;
        String redisCode = (String)redisTemplate.opsForValue().get(key);
        return redisCode;
    }
}