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
    @ApiModelProperty("主键id")
    private String id;
    @ApiModelProperty("虫情点位id")
    private String pestBoardId;
    @ApiModelProperty("位置路由")
    private String siteRoute;
    @ApiModelProperty("虫情板类型")
    private String boardType;
    @ApiModelProperty("负责人id")
    private String principalId;
    @ApiModelProperty("负责人名称")
    private String principalName;
    @ApiModelProperty("位置id")
    private String siteId;
    @ApiModelProperty("位置类型")
    private String siteType;
    @ApiModelProperty("位置名称")
    private String siteName;
    @ApiModelProperty("坐标X")
    private String siteX;
    @ApiModelProperty("坐标Y")
    private String siteY;
    @ApiModelProperty("坐标Z")
    private String siteZ;
    @ApiModelProperty("创建人")
    private String createdUser;
    @ApiModelProperty("创建时间")
    private String createdTime;
    @ApiModelProperty("最后更新时间")
    private String lastUpdatedTime;
    @ApiModelProperty("最后更新人")
    private String lastUpdatedUser;
    @ApiModelProperty("最后更新人id")
    private String lastUpdatedUserId;
    @ApiModelProperty("备注")
    private String remark;
}
