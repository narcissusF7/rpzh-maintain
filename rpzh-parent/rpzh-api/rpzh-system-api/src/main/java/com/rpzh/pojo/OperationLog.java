package com.rpzh.pojo;

import lombok.Data;

import java.util.Date;

/**
 * @Author liubc
 * @create 2022/1/18 9:39
 * @Description: good good study,day day up
 */
@Data
public class OperationLog {
    /**
     * 主键ID
     */
    private String id;
    /**
     * 系统模块
     */
    private String systemModule;
    /**
     * 操作类型  0=其他 , 1=增加 , 2=删除 , 3=修改 , 4=查询 , 5=授权 ...
     */
    private Integer operationType;
    /**
     * 请求方式 GET POST ...
     */
    private String requestMethod;
    /**
     * 请求参数
     */
    private String operationParams;
    /**
     * 请求地址
     */
    private String operationUrl;
    /**
     * 操作人员
     */
    private String operationUserName;

    /**
     * 操作主机ip
     */
    private String operationIp;
    /**
     * 操作地点
     */
    private String operationAddress;
    /**
     * 操作状态
     */
    private Integer operationStatus;
    /**
     * 操作时间
     */
    private Date operationDate;
    /**
     * 调用的类和方法名称
     */
    private String classMethodName;
    /**
     * 返回参数
     */
    private String returnParams;

    /**
     * 错误信息
     */
    private String errorInfo;

    /**
     * 返回信息
     */
    private String returnInfo;
}