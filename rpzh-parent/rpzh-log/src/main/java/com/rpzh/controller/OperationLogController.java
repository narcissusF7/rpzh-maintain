package com.rpzh.controller;

import com.alibaba.fastjson.JSONObject;
import com.rpzh.dto.OperationLogDto;
import com.rpzh.entity.LogOperation;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.OperationLogService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wbl
 * @version 0.1.0
 * @create 2021-06-10 09:03
 * @description 操作日志控制层
 */
@RestController
@RequestMapping("/operationlog")
public class OperationLogController {

    @Resource
    private OperationLogService operationLogService;


    @RequestMapping
    private Result insertOperationLog(@RequestParam("operationLogJson") String operationLogJson) {
        LogOperation operationLog = JSONObject.parseObject(operationLogJson, LogOperation.class);
        return new Result<>(true, StatusCode.OK, "操作日志", operationLogService.insertOperationLog(operationLog));
    }

    @RequestMapping("/findOperationLog")
    public Result findOperationLog(OperationLogDto operationLogDto) {
        return new Result(true, StatusCode.OK, "查询日志",
                operationLogService.findOperationLog(operationLogDto));
    }

    @RequestMapping("/findSystemModule")
    public Result findSystemModule(){
        
        return new Result(true,StatusCode.OK,"查询系统模块",operationLogService.findSystemModule());
    }
}
