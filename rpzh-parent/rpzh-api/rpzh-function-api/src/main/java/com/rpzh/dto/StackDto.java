package com.rpzh.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 垛位管理传参
 * @Author liubc
 * @create 2022/2/28 9:31
 * @Description: good good study,day day up
 */
@Data
public class StackDto {

    @ApiModelProperty("垛位表主键")
    private Integer stackId;
    @ApiModelProperty("垛位编号")
    private String stackNo;
    @ApiModelProperty("中文名称")
    private String nameCn;
    @ApiModelProperty("创建时间")
    private LocalDateTime createdTime;
    @ApiModelProperty("创建人")
    private String createdUser;
    @ApiModelProperty("最后更新时间")
    private LocalDateTime lastUpdatedTime;
    @ApiModelProperty("最后更新人")
    private String lastUpdatedUser;
    @ApiModelProperty("创建人ID")
    private Integer createdUserId;
    @ApiModelProperty("最后更新人ID")
    private Integer lastUpdatedUserId;
    @ApiModelProperty("区域ID")
    private Integer areaId;
    @ApiModelProperty("位置路径,00:根节点,01:仓库, 02: 楼号, 03: 层, 04: 区, 05: 垛位")
    private String pidRoute;
    @ApiModelProperty("位置名称")
    private String siteName;
    @ApiModelProperty("垛位类型")
    private String stackType;
    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty("所在区域")
    private Integer wareAreaId;
    @ApiModelProperty("养护计划id")
    private Integer maintainPlanId;
    private int page;
    private int size;
}