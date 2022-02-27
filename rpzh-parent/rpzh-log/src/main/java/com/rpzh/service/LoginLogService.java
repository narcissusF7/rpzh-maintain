package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.LoginLogDto;
import com.rpzh.entity.LogLogin;

/**
 * @author wbl
 * @version 0.1.0
 * @create 2021-01-27 19:11
 * @description 日志记录服务层
 */
public interface LoginLogService {

    /**
     * @param loginLog 日志记录类
     * @return int
     * @version 0.1.0
     * @description 添加一条登录日志记录
     * @author wbl
     * @date 2021/6/8 19:12
     */
    int insertLoginLog(LogLogin loginLog);

    /**
     * 查询登录日志
     * @param loginLogDto
     * @return
     */
    PageInfo<LogLogin> findAllLoginLog(LoginLogDto loginLogDto);
}
