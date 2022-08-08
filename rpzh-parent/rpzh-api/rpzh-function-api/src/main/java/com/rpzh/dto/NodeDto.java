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
    //主键id
    private Integer id;
    //设备id
    private String nodeId;
    //设备id集
    private List<String> nodeIds;
    //垛位id
    private Integer stackId;
    //垛位名称
    private String stack;
    //养护计划id
    private Integer maintainPlanId;
    //养护计划阶段
    private String maintainStage;
    //rfid
    private String rfid;
    //设备类型
    private String nodeType;
    //状态
    private String status;
    //采集位置
    private String gatherSite;
    //位置类型 04 仓间 05 非仓间
    private String siteType;
    //位置id
    private Integer siteId;
    private String siteX;
    private String siteY;
    private String siteZ;
    //位置路由
    private String siteRoute;
    //入垛时间
    private String intoStackTime;
    //创建时间
    private String createdTime;
    //创建人
    private String createdUser;
    //最后修改时间
    private LocalDateTime lastUpdatedTime;
    //最后修改人
    private String lastUpdatedUser;
    //开始时间
    private String startTime;
    //结束时间
    private String endTime;
    //修改后的设备id
    private String afterNodeId;
    //箱号
    private String caseNo;
    //当前页数
    private Integer page;
    //每页数量
    private Integer size;
    //数据类型
    private Integer dataType;
    //主键id集
    private Integer[] ids;
    //物料代码
    private String materialCode;
    //对比的类型:1:设备   2:位置
    private String contrastType;
}