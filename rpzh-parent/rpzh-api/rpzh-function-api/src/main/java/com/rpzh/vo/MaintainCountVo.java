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
 * @author zyq
 * @description
 */
@Data
@ApiModel(description = "养护数量数据返回")
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
    @ApiModelProperty(notes = "位置路由")
    private String pidRoute;
    @ApiModelProperty(notes = "垛位的中文名称")
    private String nameCn;
    @ApiModelProperty(notes = "养护数量集合")
    private List<MaintainCountVo> children;
    @ApiModelProperty(notes = "养护数量表id")
    private String id;
    @ApiModelProperty(notes = "仓间id")
    private Integer areaId;
    @ApiModelProperty(notes = "楼层id")
    private Integer floorId;
    @ApiModelProperty(notes = "楼栋id")
    private Integer buildingId;
    @ApiModelProperty(notes = "仓库id")
    private Integer warehouseId;
    @ApiModelProperty(notes = "垛位id")
    private Integer stackId;
}
