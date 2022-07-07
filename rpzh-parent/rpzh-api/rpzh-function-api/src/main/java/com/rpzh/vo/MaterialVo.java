package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/05/06/11:36
 * @Description: 功能
 */
@Data
@ApiModel(description = "物料信息数据返回")
public class MaterialVo {
    @ApiModelProperty(notes = "物料代码")
    private String materialCode;
    @ApiModelProperty(notes = "物料代码集合")
    private List<String> materialCodes;
    @ApiModelProperty(notes = "养护数量")
    private Double maintainCount;
    @ApiModelProperty(notes = "负责人id")
    private Integer principalId;
    @ApiModelProperty(notes = "垛位id")
    private Integer stackId;
    @ApiModelProperty(notes = "位置路由")
    private String pidRoute;
}
