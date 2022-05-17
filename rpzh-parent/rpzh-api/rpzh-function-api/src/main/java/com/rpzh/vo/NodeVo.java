package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 节点管理数据返回
 * @Author liubc
 * @create 2022/3/7 10:19
 * @Description: good good study,day day up
 */
@Data
@ApiModel(description = "节点管理数据返回")
public class NodeVo {

    //主键id
    @ApiModelProperty(notes = "部门id主键")
    private Integer id;

    @ApiModelProperty(notes = "设备id集合")
    private List<Integer> ids;

    //设备id
    @ApiModelProperty(notes = "设备id")
    private String nodeId;

    //设备类型
    // 01：无线氧气传感器，02：无线二氧化碳传感器，
    // 03：无线温湿度传感器,04： 无线包芯温度传感器，
    // 05：无线包芯水分传感器，06：磷化氢传感器
    @ApiModelProperty(notes = "01：无线氧气传感器，02：无线二氧化碳传感器,03：无线温湿度传感器," +
            "04：无线包芯温度传感器,05：无线包芯水分传感器，06：磷化氢传感器")
    private String nodeType;

    //采集位置
    @ApiModelProperty(notes = "采集位置")
    private String gatherSite;

    //X坐标
    @ApiModelProperty(notes = "X坐标")
    private Integer siteX;

    //Y坐标
    @ApiModelProperty(notes = "Y坐标")
    private Integer siteY;

    //Z坐标
    @ApiModelProperty(notes = "Z坐标")
    private Integer siteZ;

    //位置路径
    @ApiModelProperty(notes = "位置路径")
    private String siteRoute;

    //状态,0:解垛,1:入垛
    @ApiModelProperty(notes = "状态,0:解垛,1:入垛")
    private String status;

    //创建时间
    @ApiModelProperty(notes = "创建时间")
    private String createTime;

    //入垛时间
    @ApiModelProperty(notes = "入垛时间")
    private String intoStackTime;

    //创建人
    @ApiModelProperty(notes = "创建人")
    private String createdUser;

    //最后更新人
    @ApiModelProperty(notes = "最后更新人")
    private String lastUpdatedUser;

    //最后更新时间
    @ApiModelProperty(notes = "最后更新时间")
    private String lastUpdatedTime;

    //传输数据
    @ApiModelProperty(notes = "传输数据")
    private String transferData;

    //垛位
    @ApiModelProperty(notes = "垛位")
    private String stack;

    //04:仓间,05非仓间
    @ApiModelProperty(notes = "04:仓间,05非仓间")
    private String siteType;

    //采集位置id
    @ApiModelProperty(notes = "采集位置id")
    private Integer siteId;

    //养护计划
    @ApiModelProperty(notes = "养护计划")
    private Integer maintainStageId;

    //替换之前设备id
    @ApiModelProperty(notes = "替换之前设备id")
    private String beforeNodeId;

    //替换之后设备id
    @ApiModelProperty(notes = "替换之后设备id")
    private String afterNodeId;

    //替换的位置
    @ApiModelProperty(notes = "替换的位置")
    private String replaceLocation;
}