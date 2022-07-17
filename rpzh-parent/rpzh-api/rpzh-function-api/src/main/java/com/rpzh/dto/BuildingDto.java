package com.rpzh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 楼栋传参
 * @Author liubc
 * @create 2022/2/24 16:14
 * @Description: good good study,day day up
 */
@Data
@ApiModel(value = "楼号交互类")
public class BuildingDto {
    //主键ID
    @ApiModelProperty("楼号ID")
    private Integer id;

    //楼号
    @ApiModelProperty("楼号")
    //@NotEmpty(message = "楼号不允许为空")
    private String buildingNo;

    //楼号名称
    @ApiModelProperty("楼号名称")
    private String buildingName;

    //可堆货容积
    @ApiModelProperty("可堆货容积")
    //@NotNull(message = "可堆货容积不允许为空")
    private BigDecimal useVolume;

    //可堆货容积单位
    @ApiModelProperty("可堆货容积单位")
    //@NotEmpty(message = "可堆货容积单位不允许为空")
    private String useVolumeUnit;

    //可堆货面积
    @ApiModelProperty("可堆货面积")
    //@NotNull(message = "可堆货面积不允许为空")
    private BigDecimal useArea;

    //可堆货面积单位
    @ApiModelProperty("可堆货面积单位")
    //@NotEmpty(message = "可堆货面积单位不允许为空")
    private String useAreaUnit;

    //创建人
    private String createdUser;

    //更新人
    private String updateUser;

    //创建时间
    private String createdTime;

    //朝向
    @ApiModelProperty("朝向")
    private String buildingOrientation;

    //仓库ID
    @ApiModelProperty("仓库ID")
    //@NotNull(message = "仓库ID不允许为空")
    private Integer warehouseId;

    //父节点路径
    private String pidRoute;

    //位置名称
    private String siteName;

    //楼栋类型
    private String buildingType;

    //备注
    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty("负责人id")
    private Integer principalId;
    @ApiModelProperty("负责人账号")
    private String principalName;

}