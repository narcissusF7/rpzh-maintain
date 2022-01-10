package com.rpzh.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author liubc
 * @create 2021/8/2 15:51
 */
@Mapper
public interface LoginDao {

    /**
     * 查询当前用户账号的错误次数
     * @param username
     * @return
     */
    Integer findErrorTimes(@Param("username") String username);

    /**
     * 添加错误次数
     * @param username
     * @param errorTimes
     * @return
     */
    int addErrorTimes(@Param("username") String username, @Param("errorTimes") int errorTimes);


    /**
     * 登录成功后错误次数清零
     * @param username
     * @return
     */
    int clearError(@Param("username") String username);

    /**
     * 查询用户更新时间
     * @param username
     * @return
     */
    String checkUpdateTime(@Param("username") String username);
}
