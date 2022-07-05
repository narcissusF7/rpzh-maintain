package com.rpzh.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/06/07/11:30
 * @Description: 功能
 */
@Data
public class AlarmSettingTemplateDto {
    //预警规则名称
    private String earlyWarningRuleName;
    //物料代码集合
    private List<String> materialCodes;
    //预警规则所有阶段的预警设置值---json字符串
    private String alarmValueSettingDto;
    //养护方式
    private String maintenceStyle;
}
