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
    //预警一级颜色设置
    private String colorOne;
    //预警二级颜色设置
    private String colorTwo;
    //预警三级颜色设置
    private String colorThree;
    //一级升二级所需的时间
    private String oneToTwoDay;
    //二级升三级所需的时间
    private String twoToThreeDay;
}
