package com.rpzh.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/09/24/10:02
 * @Description: 功能
 */
@Data
@ApiModel(description = "App消息数据返回")
public class AppMessageVo {
    @ApiModelProperty(notes = "发消息的用户名")
    private String sendName;
    @ApiModelProperty(notes = "收消息的用户名")
    private String recipientName;
    //发消息的手机号
    private String sendPhone;
    //收消息的手机号
    private String recipientPhone;
    //类型:1:出库报警消息,2:普通类型消息
    private Integer type;
    @ApiModelProperty(notes = "消息内容")
    private String content;
    //出库报警中的ip地址
    private String ip;
    //出库报警中的位置
    private String siteName;
    //声光报警器是否开启:1:是,2:否
    private Integer isClose;
    //消息类型:1:草稿(点取消),2:消息(点确认)
    private Integer messageType;
    @ApiModelProperty(notes = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sendTime;
    @ApiModelProperty(notes = "消息是否已读:1:是,2:否")
    private Integer isRead;
    @ApiModelProperty(notes = "给前端的时间")
    private String strTime;
    private String id;
    private List<String> ids;
    private List<AppMessageVo> appMessageVos = new ArrayList<>();
    private List<String> recipientNames = new ArrayList<>();
}
