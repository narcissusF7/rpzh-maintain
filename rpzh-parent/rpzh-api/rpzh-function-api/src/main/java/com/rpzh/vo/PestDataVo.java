package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 虫情板数据返回类
 *
 * @Author liujj
 * @create 2022/6/10 9:39
 * @Description: good good study,day day up
 */
@Data
@ApiModel(description = "虫情板数据返回类")
public class PestDataVo {
    @ApiModelProperty(notes = "主键id")
    private Integer id;
    @ApiModelProperty("虫情点位id")
    private String pestBoardId;
    @ApiModelProperty("采集位置")
    private String gatherSite;
    @ApiModelProperty("预警等级")
    private Integer alertLevel;
    @ApiModelProperty("甲虫预警等级")
    private Integer beetleLevel;
    @ApiModelProperty("粉螟预警等级")
    private Integer mealwormLevel;
    @ApiModelProperty("采集时间")
    private String gatherTime;
    @ApiModelProperty("甲虫数量")
    private Integer beetleCount;
    @ApiModelProperty("粉螟数量")
    private Integer mealwormCount;
    @ApiModelProperty("预警颜色")
    private String warningColor;
    @ApiModelProperty("甲虫预警颜色")
    private String beetleColor;
    @ApiModelProperty("粉螟预警颜色")
    private String mealwormColor;
}
