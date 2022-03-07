package com.rpzh.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 分区管理
 * @Author liubc
 * @create 2022/2/27 9:04
 * @Description: good good study,day day up
 */
@Data
public class WareAreaDto {
    //主键ID
    @ApiModelProperty("分区ID")
    private Integer id;

    //区域编号
    @ApiModelProperty("分区编号")
    //@NotEmpty(message = "分区编号不允许为空")
    private String areaNo;

    //分区名称
    @ApiModelProperty("分区名称")
    private String areaName;

    //层ID
    @ApiModelProperty("楼层ID")
    //@NotNull(message = "楼层ID不允许为空")
    private Integer floorId;

    //创建人
    private String createdUser;

    //更新人
    private String updateUser;

    //父节点路径
    private String pidRoute;

    //位置名称
    private String siteName;

    //备注
    @ApiModelProperty("备注")
    private String remark;
}