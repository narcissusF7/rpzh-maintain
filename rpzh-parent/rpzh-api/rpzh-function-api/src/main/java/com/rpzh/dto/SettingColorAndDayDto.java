package com.rpzh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: zyq
 * @Date: 2022/05/18/15:30
 * @Description: 预警升级与预警颜色传参
 */
@Data
public class SettingColorAndDayDto {
    private String colorOne;

    private String colorTwo;

    private String colorThree;

    private String oneToTwoDay;

    private String twoToThreeDay;
}
