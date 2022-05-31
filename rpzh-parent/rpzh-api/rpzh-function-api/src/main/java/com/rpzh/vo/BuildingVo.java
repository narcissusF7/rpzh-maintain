package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 楼栋数据返回
 * @Author liubc
 * @create 2022/2/25 16:23
 * @Description: good good study,day day up
 */
@Data
@ApiModel(description = "楼栋返回类")
public class BuildingVo implements Serializable {

    //主键ID
    @ApiModelProperty(notes = "主键ID")
    private Integer id;

    //楼号
    @ApiModelProperty(notes = "楼号")
    private String buildingNo;

    //负责人
    @ApiModelProperty(notes = "负责人")
    private String principalName;

    //负责人id
    @ApiModelProperty(notes = "负责人id")
    private Integer principalId;

    //楼号名称
    @ApiModelProperty(notes = "楼号名称")
    private String buildingName;

    //可堆货容积
    @ApiModelProperty(notes = "可堆货容积")
    private BigDecimal useVolume;

    //可堆货容积单位
    @ApiModelProperty(notes = "可堆货容积单位")
    private String useVolumeUnit;

    //可堆货面积
    @ApiModelProperty(notes = "可堆货面积")
    private BigDecimal useArea;

    //可堆货面积单位
    @ApiModelProperty(notes = "可堆货面积单位")
    private String useAreaUnit;

    //朝向
    @ApiModelProperty(notes = "朝向")
    private String buildingOrientation;

    //仓库ID
    @ApiModelProperty(notes = "仓库ID")
    private Integer warehouseId;

    //备注
    @ApiModelProperty(notes = "备注")
    private String remark;

    //父节点路径
    @ApiModelProperty(notes = "父节点路径")
    private String pidRoute;

    //创建时间
    @ApiModelProperty(notes = "创建时间")
    private String createdTime;

    //创建人
    @ApiModelProperty(notes = "创建人")
    private String createdUser;

    //最后更新时间
    @ApiModelProperty(notes = "最后更新时间")
    private String lastUpdatedTime;

    //更新人
    @ApiModelProperty(notes = "更新人")
    private String lastUpdatedUser;

    //位置名称
    @ApiModelProperty(notes = "位置名称")
    private String siteName;

    //楼栋类型
    @ApiModelProperty(notes = "楼栋类型")
    private String buildingType;

    @ApiModelProperty(notes = "子下楼层")
    private List<FloorVo> floorVos;

    @ApiModelProperty(notes = "位置路由")
    private ArrayList<Integer> siteRoute;
}