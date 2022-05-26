package com.rpzh.util;

import lombok.Data;

@Data
public class MonitorCondition {
    private Integer warehouseId;
    private Integer BulidingId;
    private Integer floodId;
    private Integer wareAreaId;
    private Integer StackId;
    private String siteRoute;
    private String siteType;
    private String maintainStage;
    private String materialCode;
    private String materialName;
    private Integer monitorStatus;
    private String[] nodeTypes;
    private Integer sortCondition = 0;
    private Integer page;
    private Integer size;
}
