package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/05/19/14:03
 * @Description: 报警信息数据返回
 */
@Data
@ApiModel(description = "预警返回数据")
public class WarningVo {
    @ApiModelProperty(notes = "预警统计集合")
    private List<WarningCountVo> warningCount;
    @ApiModelProperty(notes = "EarlyWarningVo")
    private List<EarlyWarningVo> earlyWarning;
}
