package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 垛位数据返回
 * @Author liubc
 * @create 2022/3/2 10:59
 * @Description: good good study,day day up
 */
@Data
@ApiModel(description = "垛位管理数据返回")
public class StackVo implements Serializable {

    @ApiModelProperty(notes = "垛位id")
    private Integer id;

    //垛位编号
    @ApiModelProperty(notes = "垛位编号")
    private String stackNo;

    //中文名称
    @ApiModelProperty(notes = "中文名称")
    private String nameCn;

    //负责人
    @ApiModelProperty(notes = "负责人")
    private String principalName;

    //负责人id
    @ApiModelProperty(notes = "负责人id")
    private Integer principalId;

    //区域ID
    @ApiModelProperty(notes = "区域id")
    private Integer areaId;

    //备注
    @ApiModelProperty(notes = "备注")
    private String remark;

    //父节点路径
    @ApiModelProperty(notes = "父节点")
    private String pidRoute;

    //位置名称
    @ApiModelProperty(notes = "位置名称")
    private String siteName;
    //创建时间
    @ApiModelProperty(notes = "创建时间")
    private String createdTime;

    //创建人
    @ApiModelProperty(notes = "创建人")
    private String createdUser;

    //创建人ID
    @ApiModelProperty(notes = "创建人id")
    private Integer createdUserId;

    //最后更新时间
    @ApiModelProperty(notes = "最后更新时间")
    private String lastUpdatedTime;

    //最后更新人
    @ApiModelProperty(notes = "最后更新人")
    private String lastUpdatedUser;

    //最后更新人ID
    @ApiModelProperty(notes = "最后更新人Id")
    private Integer lastUpdatedUserId;

    //垛位类型
    @ApiModelProperty(notes = "垛位类型")
    private String stackType;

    @ApiModelProperty(notes = "位置路由")
    private ArrayList<Integer> siteRoute;

}