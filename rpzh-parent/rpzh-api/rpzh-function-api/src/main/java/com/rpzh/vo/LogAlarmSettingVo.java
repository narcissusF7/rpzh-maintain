package com.rpzh.vo;

import lombok.Data;

import java.util.Date;

/**
 * @Author: zyq
 * @Date: 2022/06/21/10:07
 * @Description: 预警模板操作日志
 */
@Data
public class LogAlarmSettingVo {
    //预警模板操作日志id
    private Integer id;
    //操作人员
    private String logUserName;
    //操作类型:添加,修改
    private String logType;
    //操作主机ip
    private String logIp;
    //操作时间
    private Date logDate;
    //预警规则名称
    private String earlyWarningRuleName;
    //养护阶段
    private String phases;
    //仓间预警id
    private Integer alarmValueSettingId;
}
