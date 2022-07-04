package com.rpzh.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.beans.Transient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 仓库交互类
 * @Author liubc
 * @create 2022/2/23 11:53
 * @Description: good good study,day day up
 */
@Data
@ApiModel(description = "仓库返回类")
public class WarehouseVo implements Serializable {

    //主键ID
    @ApiModelProperty(notes = "主键id")
    private Integer id;

    //仓库名称
    @ApiModelProperty(notes = "仓库名称")
    private String name;

    //建造时间
    //@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(notes = "建造时间")
    private String buildTime;

    //库龄
    @ApiModelProperty(notes = "库龄")
    private Integer age;

    //负责人
    @ApiModelProperty(notes = "负责人")
    private String principalName;

    //负责人
    @ApiModelProperty(notes = "负责人id")
    private Integer principalId;

    //仓库编码
    @ApiModelProperty(notes = "仓库编码")
    private String code;

    //可堆货容积
    @ApiModelProperty(notes = "可堆货容积")
    private BigDecimal useVolume;

    //仓库类型
    @ApiModelProperty(notes = "仓库类型")
    private String wareType;

    //可堆货容积单位
    @ApiModelProperty(notes = "可堆货容积单位")
    private String useVolumeUnit;

    //可堆货面积
    @ApiModelProperty(notes = "可堆货面积")
    private BigDecimal useArea;

    //可堆货面积单位
    @ApiModelProperty(notes = "可堆货面积单位")
    private String useAreaUnit;

    //是否自动化仓库
    @ApiModelProperty(notes = "是否自动化仓库")
    private Integer isAutomation;

    //是否统计数据
    @ApiModelProperty(notes = "是否统计数据")
    private Integer isStatistics;

    //是否启用
    @ApiModelProperty(notes = "是否启用")
    private Integer isUsing;

    //创建时间
    @ApiModelProperty(notes = "创建时间")
    private String createdTime;

    //创建人
    @ApiModelProperty(notes = "创建人")
    private String createdUser;

    //创建人ID
    @ApiModelProperty(notes = "创建人ID")
    private Integer createdUserId;

    //最后更新时间
    @ApiModelProperty(notes = "最后更新时间")
    private String lastUpdatedTime;

    //最后更新人
    @ApiModelProperty(notes = "最后更新人")
    private String lastUpdatedUser;

    //最后更新人ID
    @ApiModelProperty(notes = "最后更新人ID")
    private Integer lastUpdatedUserId;

    //备注
    @ApiModelProperty(notes = "备注")
    private String remark;

    //父节点路径
    @ApiModelProperty(notes = "父节点路径")
    private String pidRoute;

    //位置名称
    @ApiModelProperty(notes = "位置名称")
    private String siteName;

    @ApiModelProperty(notes = "子下楼栋")
    private List<BuildingVo> buildingVos;

    @ApiModelProperty(notes = "位置路由")
    private ArrayList<Integer> siteRoute;

}