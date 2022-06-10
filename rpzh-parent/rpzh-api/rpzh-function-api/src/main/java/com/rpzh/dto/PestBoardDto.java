package com.rpzh.dto;

import lombok.Data;

@Data
public class PestBoardDto {

    //主键id
    private Integer id;
    //虫情板Id
    private String pestBoardId;
    //虫情板类型
    private String boardType;
    //负责人id
    private Integer principalId;
    //负责人
    private Integer principalName;
    //位置id
    private Integer siteId;
    //位置类型
    private String siteType;
    //位置名称
    private String siteName;
    //位置路由
    private String siteRoute;
    //坐标X
    private String siteX;
    //坐标Y
    private String siteY;
    //坐标Z
    private String siteZ;
    //使用天数
    private String useDays;
    //开始使用时间
    private String useTimeStart;
    //结束使用时间
    private String useTimeEnd;
    //添加时间
    private String createdTime;
    //最后修改时间
    private String lastUpdatedTime;
    //最后修改人
    private String lastUpdatedUser;
    //添加人
    private String createdUser;
    //最后修改人id
    private Integer lastUpdatedUserId;
    //备注
    private String remark;
}
