package com.rpzh.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author liujj
 * @since 2022-04-29
 */
@Data
@ApiModel(value="EarlyWarning对象", description="")
public class EarlyWarningDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预警表id")
    private Integer earlyWarningId;

    @ApiModelProperty(value = "预警类型,01:检测设备故障,02:虫数超限预警,03:温度超限预警,04:湿度超限预警,05:氧气超限预警,06:二氧化碳超限预警,磷化氢超限预警")
    private String warningType;

    @ApiModelProperty(value = "节点设备id")
    private Integer nodeId;

    @ApiModelProperty(value = "预警值")
    private BigDecimal warningVal;

    @ApiModelProperty(value = "预警时间")
    private LocalDateTime warningTime;

    @ApiModelProperty(value = "烟叶代码")
    private String materialCode;

    @ApiModelProperty(value = "烟叶名称")
    private String materialName;

    @ApiModelProperty(value = "负责人id")
    private Integer principalId;

    @ApiModelProperty(value = "预警等级,1:一级预警,2:二级预警,3:三级预警")
    private String warningGrade;

    @ApiModelProperty(value = "是否确认,0:未确认,1:确认预警,2,误报")
    private String isAffirm;

    @ApiModelProperty(value = "处理意见")
    private String dealSuggestion;

    @ApiModelProperty(value = "垛位主键")
    private Integer stackId;

    @ApiModelProperty(value = "养护计划主键")
    private Integer maintainPlanId;

    //分页开始页面
    private int page;

    //分页每页显示
    private int size;


}
