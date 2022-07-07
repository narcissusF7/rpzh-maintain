package com.rpzh.vo;

import com.rpzh.dto.AlarmValueSettingDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/06/07/11:30
 * @Description: 预警模板数据返回
 */
@Data
@ApiModel(description = "预警模板数据返回")
public class AlarmSettingTemplateVo {
    @ApiModelProperty(notes = "预警规则名称")
    private String earlyWarningRuleName;
    @ApiModelProperty(notes = "物料代码集合")
    private List<String> materialCodes;
    @ApiModelProperty(notes = "预警规则所有阶段集合")
    private List<AlarmValueSettingVo> alarmValueSettingDtos;
}
