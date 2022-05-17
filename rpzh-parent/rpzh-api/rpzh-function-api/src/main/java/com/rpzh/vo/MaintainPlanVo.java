package com.rpzh.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author wbl
 * @description [返回给前段的信息]
 */
@Data
@ApiModel(description = "养护计划返回数据")
public class MaintainPlanVo {

    @ApiModelProperty(notes = "养护计划id")
    private Integer id;

    @ApiModelProperty(notes = "养护阶段id")
    private Integer stageId;

    @ApiModelProperty(notes = "物料代码")
    private String materialCode;

    @ApiModelProperty(notes = "烟叶年份")
    private String tobaccoYear;

    @ApiModelProperty(notes = "养护阶段")
    private String phases;

    @ApiModelProperty(notes = "养护方式")
    private String maintenceStyle;

    @ApiModelProperty(notes = "位置")
    private String stackName;

    @ApiModelProperty(notes = "位置id")
    private Integer stackId;

    @ApiModelProperty(notes = "制定时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdTime;

    @ApiModelProperty(notes = "养护天数")
    private Integer maintainDays;

    @ApiModelProperty(notes = "调养日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date aftercareDateStart;

    @ApiModelProperty(notes = "降氧完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date oxygenReductionTime;

    @ApiModelProperty(notes = "负责人")
    private String principalName;

    @ApiModelProperty(notes = "到期提醒时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    @ApiModelProperty(notes = "养护数量")
    private Double maintainCount;

    private String sealStackTime;

    @ApiModelProperty(notes = "预警设置表id")
    private Integer alarmValueSettingId;

    @ApiModelProperty(notes = "变更状态")
    private String changeStatus;
    private Integer version;

    @ApiModelProperty(notes = "预警设置信息")
    private AlarmValueSettingVo alarmValueSettingVo;

    @ApiModelProperty(notes = "养护阶段集合")
    private List<MaintainPlanVo> maintainPlanVos;

    private String materialCodeYear;
}
