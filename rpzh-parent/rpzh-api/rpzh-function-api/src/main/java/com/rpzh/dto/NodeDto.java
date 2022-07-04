package com.rpzh.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 节点管理前端传参
 * @Author liubc
 * @create 2022/3/3 9:42
 * @Description: good good study,day day up
 */
@Data
public class NodeDto {

    private static final long serialVersionUID = 1L;
    private Integer id;
    private String nodeId;
    private Integer flag=0;
    private String materialCode;
    //对比的类型:1:设备   2:位置
    private String contrastType;
    private List<String> nodeIds;
    private Integer stackId;
    private String stack;
    private Integer maintainPlanId;
    private String maintainStage;
    private String rfid;
    private String nodeType;
    private String status;
    private String gatherSite;
    private String siteType;
    private Integer siteId;
    private String siteX;
    private String siteY;
    private String siteZ;
    private String siteRoute;
    private String intoStackTime;
    private String createdTime;
    private String createdUser;
    private LocalDateTime lastUpdatedTime;
    private String lastUpdatedUser;
    private String startTime;
    private String endTime;
    private String afterNodeId;
    private String caseNo;
    private Integer page;
    private Integer size;
    private Integer dataType;
    private Integer[] ids;

}