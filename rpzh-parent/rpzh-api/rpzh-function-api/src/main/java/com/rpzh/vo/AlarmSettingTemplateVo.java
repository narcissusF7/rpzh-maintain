package com.rpzh.vo;

import com.rpzh.dto.AlarmValueSettingDto;
import lombok.Data;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/06/07/11:30
 * @Description: 功能
 */
@Data
public class AlarmSettingTemplateVo {
    private String earlyWarningRuleName;
    private List<String> materialCodes;
    private List<AlarmValueSettingVo> alarmValueSettingDtos;
}
