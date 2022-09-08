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

    private Integer areaId;

    private String nameCn;

    @ApiModelProperty(notes = "物料代码")
    private String materialCode;

    @ApiModelProperty(notes = "物料代码集合")
    private List<String> materialCodes;

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

    @ApiModelProperty(notes = "开垛时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedTime;

    @ApiModelProperty(notes = "养护天数")
    private Integer maintainDays;

    @ApiModelProperty(notes = "调养日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date aftercareDateStart;

    @ApiModelProperty(notes = "调养结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date aftercareDateEnd;

    @ApiModelProperty(notes = "降氧完成天数")
    private Integer oxygenReductionTime;

    @ApiModelProperty(notes = "负责人")
    private String principalName;
    @ApiModelProperty(notes = "负责人id")
    private String principalId;
    @ApiModelProperty(notes = "负责人ids集合")
    private List<Integer> principalIds;
    @ApiModelProperty(notes = "垛位负责人id")
    private Integer personId;

    @ApiModelProperty(notes = "到期提醒时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expireTime;

    @ApiModelProperty(notes = "养护数量")
    private Double maintainCount;

    @ApiModelProperty(notes = "封垛日期")
    private String sealStackTime;

    @ApiModelProperty(notes = "预警设置表id")
    private Integer alarmValueSettingId;

    @ApiModelProperty(notes = "变更状态  1:变更,0:未变更")
    private String changeStatus;
    @ApiModelProperty(notes = "是否是新建养护计划  1:是,0:否")
    private Integer version;
    @ApiModelProperty(notes = "预警设置信息")
    private AlarmValueSettingVo alarmValueSettingVo;

    @ApiModelProperty(notes = "垛内氧气当前数值")
    private Double stackOxygenNow=0d;

    @ApiModelProperty(notes = "仓间氧气当前数值")
    private Double warehouseOxygenNow=0d;

    @ApiModelProperty(notes = "包芯温度当前数值")
    private Double tempCoreChipNow=0d;

    @ApiModelProperty(notes = "垛内温度当前数值")
    private Double stackTempNow=0d;

    @ApiModelProperty(notes = "垛内水分当前数值")
    private Double stackMoistureNow=0d;

    @ApiModelProperty(notes = "仓间水分当前数值")
    private Double warehouseMoistureNow=0d;

    @ApiModelProperty(notes = "仓间温度当前数值")
    private Double warehouseTempNow=0d;

    @ApiModelProperty(notes = "平均包芯高于环境的当前数值")
    private Double coreTempHighEnvTempNow=0d;

    @ApiModelProperty(notes = "养护阶段集合")
    private List<MaintainPlanVo> maintainPlanVos;
    @ApiModelProperty(notes = "位置路径,00:根节点,01:仓库, 02: 楼号, 03: 层, 04: 区, 05: 垛位")
    public String pidRoute;
    @ApiModelProperty(notes = "评吸意见")
    private String content;
}
