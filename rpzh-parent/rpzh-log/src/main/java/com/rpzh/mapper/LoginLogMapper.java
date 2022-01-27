package com.rpzh.mapper;

import com.rpzh.entity.LogLogin;
import org.apache.ibatis.annotations.Mapper;


/**
 * @author wbl
 * @version 0.1.0
 * @create 2021-06-08 19:15
 * @description 登录日志mapper层
 */
@Mapper
public interface LoginLogMapper {

    /**
     * @param loginLog 日志记录类
     * @return int
     * @version 0.1.0
     * @description 添加一条登录日志记录
     * @author wbl
     * @date 2021/6/8 19:12
     */
    int insertLoginLog(LogLogin loginLog);

}
