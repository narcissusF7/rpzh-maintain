package com.rpzh.dto;

import lombok.Data;

import java.util.Date;

/**
 * 操作日志前端传输类
 */
@Data
public class OperationLogDto {
    /**
     * 系统模块
     */
    private String sysModule;
    /**
     * 操作类型
     */
    private String operationType;
    /**
     * 操作人员
     */
    private String operationUser;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 第几页
     */
    private Integer page = 0;
    /**
     * 每页大小 默认10
     */
    private Integer size = 10;
}
