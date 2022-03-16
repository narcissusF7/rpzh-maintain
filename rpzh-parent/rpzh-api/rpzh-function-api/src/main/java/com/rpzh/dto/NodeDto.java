package com.rpzh.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 节点管理前端传参
 * @Author liubc
 * @create 2022/3/3 9:42
 * @Description: good good study,day day up
 */
@Data
public class NodeDto {

    //主键id
    private Integer id;

    private List<Integer> ids;

    //设备id
    private String nodeId;

    //设备类型
    // 01：无线氧气传感器，02：无线二氧化碳传感器，
    // 03：无线温湿度传感器,04： 无线包芯温度传感器，
    // 05：无线包芯水分传感器，06：磷化氢传感器
    private String nodeType;

    //替换之前的id
    private String beforeNodeId;

    //替换之后的id
    private String afterNodeId;

    //采集位置
    private String gatherSite;

    //X坐标
    private Integer siteX;

    //Y坐标
    private Integer siteY;

    //Z坐标
    private Integer siteZ;

    //位置路径
    private String siteRoute;

    //状态,0:解垛,1:入垛,2:未使用
    private String status;

    //创建时间
    private String createTime;

    //入垛时间
    private String intoStackTime;

    //创建人
    private String createdUser;

    //最后更新人
    private String lastUpdatedUser;

    //最后更新时间
    private String lastUpdatedTime;

    //传输数据
    private String transferData;

    //开始时间
    private String startTime;

    //结束时间
    private String endTime;

    //垛位
    private String stack;

    //04:仓间,05非仓间
    private String siteType;

    //分页开始页面
    private int page;

    //分页每页显示
    private int size;

    //采集位置id
    private Integer siteId;
}