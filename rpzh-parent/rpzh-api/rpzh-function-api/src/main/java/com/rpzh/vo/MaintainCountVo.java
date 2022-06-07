package com.rpzh.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Date;
import java.util.List;

/**
 * @author wbl
 * @description [返回给前段的信息]
 */
@Data
@ApiModel(description = "养护计划返回数据")
@NoArgsConstructor
@AllArgsConstructor
public class MaintainCountVo {

    @ApiModelProperty(notes = "物料代码")
    private String materialCode;

    @ApiModelProperty(notes = "养护阶段")
    private String phases;

    @ApiModelProperty(notes = "养护方式")
    private String maintenceStyle;

    @ApiModelProperty(notes = "位置")
    private String stackName;

    @ApiModelProperty(notes = "制定时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdTime;

    @ApiModelProperty(notes = "养护数量")
    private Double maintainCount;

    private String pidRoute;

    @ApiModelProperty(notes = "养护数量集合")
    private List<MaintainCountVo> maintainCountVos;
}
