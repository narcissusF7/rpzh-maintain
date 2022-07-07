package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author: zyq
 * @Date: 2022/06/21/10:07
 * @Description: 预警模板操作日志
 */
@Data
@ApiModel(description = "预警模板操作日志")
public class LogAlarmSettingVo {
    @ApiModelProperty(notes ="预警模板操作日志id")
    private Integer id;
    @ApiModelProperty(notes ="操作人员")
    private String logUserName;
    @ApiModelProperty(notes ="操作类型:添加,修改")
    private String logType;
    @ApiModelProperty(notes ="操作主机ip")
    private String logIp;
    @ApiModelProperty(notes ="操作时间")
    private Date logDate;
    @ApiModelProperty(notes ="预警规则名称")
    private String earlyWarningRuleName;
    @ApiModelProperty(notes ="养护阶段")
    private String phases;
    @ApiModelProperty(notes ="仓间预警id")
    private Integer alarmValueSettingId;
}
