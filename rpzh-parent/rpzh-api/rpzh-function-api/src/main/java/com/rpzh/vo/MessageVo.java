package com.rpzh.vo;

import lombok.Data;

/**
 * @Author: liubc
 * @Date: 2022/06/14/9:59
 * @Description: 宁静致远
 */
@Data
public class MessageVo {

    //结果代码
    private String result;

    //网关任务流水号
    private String serialnumber;

    //描述
    private String description;

    //失败号码列表
    private String faillist;



}
