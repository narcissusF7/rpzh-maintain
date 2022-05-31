package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 楼层数据返回
 * @Author liubc
 * @create 2022/2/27 9:33
 * @Description: good good study,day day up
 */
@Data
@ApiModel(description = "楼层返回类")
public class FloorVo implements Serializable {
    //主键ID
    @ApiModelProperty(notes = "主键ID")
    private Integer id;

    //楼层编号
    @ApiModelProperty(notes = "楼层编号")
    private String floorNo;

    //负责人
    @ApiModelProperty(notes = "负责人")
    private String principalName;

    //负责人id
    @ApiModelProperty(notes = "负责人id")
    private Integer principalId;

    //楼层名称
    @ApiModelProperty(notes = "楼层名称")
    private String floorName;

    //是否分区
    @ApiModelProperty(notes = "是否分区")
    private String isPartition;

    //楼ID
    @ApiModelProperty(notes = "楼ID")
    private Integer buildingId;

    //备注
    @ApiModelProperty(notes = "备注")
    private String remark;

    //父节点路径
    @ApiModelProperty(notes = "父节点路径")
    private String pidRoute;

    //位置名称
    @ApiModelProperty(notes = "位置名称")
    private String siteName;

    //创建时间
    @ApiModelProperty(notes = "创建时间")
    private String createdTime;

    //创建人
    @ApiModelProperty(notes = "创建人")
    private String createdUser;

    //创建人ID
    @ApiModelProperty(notes = "创建人ID")
    private Integer createdUserId;

    //楼层类型
    @ApiModelProperty(notes = "楼层类型")
    private String floorType;

    //最后更新时间
    @ApiModelProperty(notes = "最后更新时间")
    private String lastUpdatedTime;

    //最后更新人
    @ApiModelProperty(notes = "最后更新人")
    private String lastUpdatedUser;

    //最后更新人ID
    @ApiModelProperty(notes = "最后更新人ID")
    private Integer lastUpdatedUserId;

    @ApiModelProperty(notes = "子下分区")
    private List<WareAreaVo> wareAreaVos;

    @ApiModelProperty(notes = "位置路由")
    private ArrayList<Integer> siteRoute;
}