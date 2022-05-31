package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: zyq
 * @Date: 2022/05/18/15:30
 * @Description: 预警升级与预警颜色返回数据
 */
@Data
@ApiModel(description = "预警升级与预警颜色返回数据")
public class SettingColorAndDayVo {
    @ApiModelProperty(notes = "预警颜色一级")
    private String colorOne;
    @ApiModelProperty(notes = "预警颜色二级")
    private String colorTwo;
    @ApiModelProperty(notes = "预警颜色三级")
    private String colorThree;

    @ApiModelProperty(notes = "预警一级升二级的天数")
    private String oneToTwoDay;
    @ApiModelProperty(notes = "预警二级升三级的天数")
    private String twoToThreeDay;
}
