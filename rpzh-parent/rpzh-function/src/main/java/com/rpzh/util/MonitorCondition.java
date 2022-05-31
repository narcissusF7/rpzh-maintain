package com.rpzh.util;

import lombok.Data;

@Data
public class MonitorCondition {
    private String pidRoute;
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
