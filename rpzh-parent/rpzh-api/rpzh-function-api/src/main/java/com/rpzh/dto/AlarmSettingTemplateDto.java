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
    private String earlyWarningRuleName;
    private List<String> materialCodes;
    private String alarmValueSettingDto;
    private List<String> alarmValueSettingDtos;
    private String maintenceStyle;
}
