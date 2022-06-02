package com.rpzh.util;

import lombok.Data;

@Data
public class MonitorCondition {
    //采集位置
    private String gatherSite;
    //物料代码
    private String materialCode;
    //监控状态
    private Integer monitorStatus;
    //多个设备类型
    private String[] nodeTypes;
    //采集位置类型 04 仓间 05 非仓间
    private String siteType;

    //位置等级
    private Integer level;
    //位置id
    private Integer siteId;

    //位置路由
    private String pidRoute;

    private String startTime;
    private String endTime;
    //监控类型 0 温度 1湿度
    private Integer monitorType;
    //排序规则 0 按位置排序 1 按预警排序
    private Integer sortCondition = 0;
    private Integer page;
    private Integer size;
}
