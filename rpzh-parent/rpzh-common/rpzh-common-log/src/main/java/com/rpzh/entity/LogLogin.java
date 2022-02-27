package com.rpzh.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wbl
 * @version 0.1.0
 * @create 2021-05-26 09:09
 * @description 登录日志映射类
 */
@Data
@ToString
@AllArgsConstructor
public class LogLogin implements Serializable{

    public LogLogin(){}
    /**
     * 主键id
     */
    private String id;
    /**
     * 用户名称
     */
    private String username;
    /**
     * 登录ip
     */

    private String loginIp;
    /**
     * 登录地址
     */

    private String loginAddress;
    /**
     * 浏览器类型
     */

    private String browserType;
    /**
     * 操作系统
     */

    private String os;
    /**
     * 登录状态
     */

    private Integer loginStatus;
    /**
     * 操作信息
     */

    private String operatingInformation;
    /**
     * 登录时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date loginTime;
    /**
     * 错误信息
     */
    private String errorInfo;
    /**
     * 调用的类和方法名称
     */
    private String classMethodName;
    /**
     * 请求方式 GET POST ...
     */
    private String requestMethod;
    /**
     * 返回信息
     */
    private String returnInfo;
}
