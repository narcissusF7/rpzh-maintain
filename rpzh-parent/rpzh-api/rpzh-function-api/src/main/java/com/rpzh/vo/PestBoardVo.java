package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
/**
 * 虫情点位返回类
 * @Author liujj
 * @create 2022-06-08 11:31
 * @Description: good good study,day day up
 */
@Data
@ApiModel(description = "虫情点位返回类")
public class PestBoardVo {
    @ApiModelProperty(notes = "主键id")
    private String id;
    @ApiModelProperty(notes = "虫情点位id")
    private String pestBoardId;
    @ApiModelProperty("位置路由")
    private String siteRoute;
    @ApiModelProperty(notes = "虫情板类型")
    private String boardType;
    @ApiModelProperty(notes = "负责人id")
    private String principalUser;
    @ApiModelProperty(notes = "负责人名称")
    private String principalName;
    @ApiModelProperty(notes = "位置id")
    private String siteId;
    @ApiModelProperty(notes = "位置类型")
    private String siteType;
    @ApiModelProperty(notes = "位置名称")
    private String siteName;
    @ApiModelProperty(notes = "坐标X")
    private String siteX;
    @ApiModelProperty(notes = "坐标Y")
    private String siteY;
    @ApiModelProperty(notes = "坐标Z")
    private String siteZ;
    @ApiModelProperty(notes = "创建人")
    private String createdUser;
    @ApiModelProperty(notes = "创建时间")
    private String createdTime;
    @ApiModelProperty(notes = "最后更新时间")
    private String lastUpdatedTime;
    @ApiModelProperty(notes = "最后更新人")
    private String lastUpdatedUser;
    @ApiModelProperty(notes = "最后更新人id")
    private String lastUpdatedUserId;
    @ApiModelProperty(notes = "备注")
    private String remark;
    @ApiModelProperty(notes = "虫情预警颜色")
    private String pestWarningColor;
}
