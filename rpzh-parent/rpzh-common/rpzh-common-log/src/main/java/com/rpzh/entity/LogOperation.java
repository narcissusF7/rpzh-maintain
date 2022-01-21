package com.rpzh.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * @author wbl
 * 操作日志实体类
 */
@Data
@NoArgsConstructor
@ToString
public class LogOperation {
    private Long id;
    /**
     * 操作人
     */
    private String logRecorder;
    /**
     * 日志类型
     */
    private String logType;
    /**
     * 操作时间
     */
    private Date logTime;
    /**
     * 请求地址
     */
    private String logRequestUrl;
    /**
     * 请求参数
     */
    private String logRequestParams;
    /**
     * 操作ip
     */
    private String logIp;
    /**
     * 操作地址
     */
    private String logAddress;
    /**
     * 操作状态
     */
    private Boolean logStatus;
    /**
     * 操作的类与方法
     */
    private String logClassMethodName;
    /**
     * 返回参数
     */
    private String logReturnParams;
    /**
     * 错误信息
     */
    private String logErrorInfo;
    /**
     * 返回信息
     */
    private String logReturnInfo;

}
