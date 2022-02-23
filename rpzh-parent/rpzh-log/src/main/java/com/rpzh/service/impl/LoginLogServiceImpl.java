package com.rpzh.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dto.LoginLogDto;
import com.rpzh.entity.LogLogin;
import com.rpzh.mapper.LoginLogMapper;
import com.rpzh.service.LoginLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wbl
 * @version 0.1.0
 * @create 2021-01-27 19:14
 * @description 登录日志服务层实现类
 */
@Service
public class LoginLogServiceImpl implements LoginLogService {


    @Resource
    private LoginLogMapper loginLogMapper;

    /**
     * @param loginLog 日志记录类
     * @return int
     * @version 0.1.0
     * @description 添加一条登录日志记录
     * @author wbl
     * @date 2021/6/8 19:12
     */
    @Override
    public int insertLoginLog(LogLogin loginLog) {
        return loginLogMapper.insertLoginLog(loginLog);
    }

    /**
     * 查询登录日志
     *
     * @param loginLogDto
     * @return
     */
    @Override
    public PageInfo<LogLogin> findAllLoginLog(LoginLogDto loginLogDto) {
        int page = loginLogDto.getPage();
        int size = loginLogDto.getSize();
        //静态分页
        PageHelper.startPage(page, size);
        List<LogLogin> loginLogVoList = loginLogMapper.findAllLoginLog(loginLogDto);
        return new PageInfo<>(loginLogVoList);
    }
}
