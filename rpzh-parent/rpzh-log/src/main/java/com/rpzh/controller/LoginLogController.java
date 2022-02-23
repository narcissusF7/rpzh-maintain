package com.rpzh.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.rpzh.dto.LoginLogDto;
import com.rpzh.entity.LogLogin;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.LoginLogService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wbl
 * @version 0.1.0
 * @create 2021-01-27 18:52
 * @description 登录日志控制层
 */
@RestController
@RequestMapping("/loginlog")
public class LoginLogController {


    @Resource
    private LoginLogService loginLogService;

    /**
     * @param loginLogStr 登录日志类JSON
     * @return com.rpzh.entity.Result<com.rpzh.com.rpzh.pojo.LoginLog>
     * @version 0.1.0
     * @description 记录登录日志
     * @author wbl
     * @date 2021/6/8 19:18
     */
    @PostMapping(consumes = "application/json")
    public Result<LogLogin> insertLoginLog(@RequestParam("loginLogStr") String loginLogStr) {
        LogLogin loginLog = JSONObject.parseObject(loginLogStr, LogLogin.class);
        return new Result<>(true, StatusCode.OK, "登录日志", loginLogService.insertLoginLog(loginLog));
    }
    /**
     * 日志查询
     * @param loginLogDto
     * @return
     */
    @RequestMapping("/findAllLoginLog")
    public Result<PageInfo> findAllLoginLog(LoginLogDto loginLogDto){
        PageInfo<LogLogin> pageInfo = loginLogService.findAllLoginLog(loginLogDto);
        return new Result<PageInfo>(true,StatusCode.OK,"查询成功",pageInfo);
    }
}
