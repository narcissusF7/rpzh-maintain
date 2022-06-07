package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author liubc
 * @create 2022-05-30 16:08
 * @Description: good good study,day day up
 */
@Data
@ApiModel(description = "任务中心数据返回")
public class TaskVo {

    @ApiModelProperty(notes = "主键id")
    private Integer id;

    //任务编号
    @ApiModelProperty(notes = "任务编号")
    private String taskNum;

    //任务编号
    @ApiModelProperty(notes = "任务地址")
    private String taskAddress;

    //任务类型
    @ApiModelProperty(notes = "任务类型,01:虫情巡查,02:虫情杀虫,03:仓库检查,04:仓间温度预警,05:垛内温度预警,06:垛内湿度预警,07:翻垛任务,08:移垛任务")
    private String type;

    //发起时间
    @ApiModelProperty(notes = "发起时间,创建时间")
    private String createdTime;

    //截止时间
    @ApiModelProperty(notes = "截止时间")
    private String endTime;

    //任务详情
    @ApiModelProperty(notes = "任务详情")
    private String detail;

    //负责人
    @ApiModelProperty(notes = "负责人")
    private String principal;

    //任务状态
    @ApiModelProperty(notes = "任务状态,0:未完成,1:完成,2:逾期")
    private String status;

    @ApiModelProperty(notes = "处理时间")
    private String dealTime;

    @ApiModelProperty(notes = "处理方式")
    private String dealMethod;

    @ApiModelProperty(notes = "完成时间")
    private String finishTime;

    @ApiModelProperty(notes = "创建人")
    private String createdUser;

    @ApiModelProperty(notes = "评价")
    private String evaluate;

    @ApiModelProperty(notes = "推送方式,M:短信,A:App")
    private String pushWay;

    @ApiModelProperty(notes = "处理结果")
    private String dealResult;

    @ApiModelProperty(notes = "图片地址")
    private String pictureUrl;

    @ApiModelProperty(notes = "图片组名")
    private String pictureGroup;

    @ApiModelProperty(notes = "图片图片文件夹名")
    private String pictureFilename;
}