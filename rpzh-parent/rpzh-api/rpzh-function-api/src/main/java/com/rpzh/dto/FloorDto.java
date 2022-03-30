package com.rpzh.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 楼层前端传参
 * @Author liubc
 * @create 2022/2/25 15:31
 * @Description: good good study,day day up
 */
@Data
public class FloorDto {
    //主键ID
    @ApiModelProperty("楼层ID")
    private Integer id;

    //楼层编号
    @ApiModelProperty("楼层编号")
    //@NotEmpty(message = "楼层编号不允许为空")
    private String floorNo;

    //楼层名称
    @ApiModelProperty("楼层名称")
    private String floorName;

    //是否分区
    @ApiModelProperty("是否分区")
    //@NotNull(message = "是否分区不允许为空")
    private Integer isPartition;

    //创建人
    private String createdUser;

    //更新人
    private String updateUser;

    //父节点路径
    private String pidRoute;

    //位置名称
    private String siteName;

    //楼ID
    @ApiModelProperty("楼号ID")
    //@NotNull(message = "楼号ID不允许为空")
    private String buildingId;

    //楼层类型
    private String floorType;

    //备注
    @ApiModelProperty("备注")
    private String remark;
}