package com.rpzh.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rpzh.vo.AlarmValueSettingVo;
import lombok.Data;

import java.util.Date;

/**
 * @author wbl
 * @description [养护计划前端传入类]
 */
@Data
public class MaintainPlanDto {
    /**
     * 物料代码
     */
    private String materialCode;
    /**
     * 物料代码与烟叶年份
     */
    private String materialCodeYear;
    private Integer stageId;
    /**
     * 垛位id
     */
    private Integer stackId;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 养护方式 (1:常规养护、2:机械调控)
     */
    private String maintenceStyle;
    /**
     * 阶段 （1:养护阶段 2:充氮杀虫阶段 3:磷化铝杀虫阶段 4:充氮保质阶段）
     */
    private String phases;

    /**
     * 垛位名称
     */
    private String stackName;

    /**
     * 养护计划id
     */
    private Integer id;
    private Integer flag;

    /**
     * 物料批次号
     */
    private String materialBatchNum;

    /**
     * 烟叶年份
     */
    private String tobaccoYear;

    /**
     * 制定时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdTime;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedTime;
    private String createUser;
    private String updateUser;
    /**
     * 养护天数
     */
    private Integer maintainDays;
    private Integer version = 1;
    /**
     * 调养日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date aftercareDateStart;
    @JsonFormat(pattern = "yyyy-MM-dd 00:00:00", timezone = "GMT+8")
    private Date aftercareDateEnd;
    /**
     * 降氧完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date oxygenReductionTime;
    /**
     * 负责人
     */
    private String principalName;
    /**
     * 到期提醒时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;
    /**
     * 养护数量
     */
    private Double maintainCount;

    /**
     * 预警设置表id
     */
    private Integer alarmValueSettingId;
    /**
     * 放在养护阶段表中的养护计划id
     */
    private Integer maintainPlanId;
    /**
     * 库龄
     */
    private Integer storageAge;
    /**
     * 预警数值
     */
    private AlarmValueSettingDto alarmValueSettingDto;

    /**
     * 第几页
     */
    private Integer page = 0;
    /**
     * 有几条数据
     */
    private Integer size = 10;

    private String siteName;

    private String changeStatus;
}
