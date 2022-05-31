package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 分区管理数据返回
 * @Author liubc
 * @create 2022/2/28 10:19
 * @Description: good good study,day day up
 */
@Data
@ApiModel(description = "分区返回类")
public class WareAreaVo implements Serializable {

    //主键ID
    @ApiModelProperty(notes = "主键id")
    private Integer id;

    //区域编号
    @ApiModelProperty(notes = "区域编号")
    private String areaNo;

    //负责人
    @ApiModelProperty(notes = "负责人")
    private String principalName;

    //负责人id
    @ApiModelProperty(notes = "负责人id")
    private Integer principalId;

    //分区名称
    @ApiModelProperty(notes = "分区名称")
    private String areaName;

    //层ID
    @ApiModelProperty(notes = "层Id")
    private Integer floorId;

    //备注
    @ApiModelProperty(notes = "备注")
    private String remark;

    //父节点路径
    @ApiModelProperty(notes = "父节点")
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
    @ApiModelProperty(notes = "创建人id")
    private Integer createdUserId;

    //最后更新时间
    @ApiModelProperty(notes = "最后更新时间")
    private String lastUpdatedTime;

    //最后更新人
    @ApiModelProperty(notes = "最后更新人")
    private String lastUpdatedUser;

    //最后更新人ID
    @ApiModelProperty(notes = "最后更新人id")
    private Integer lastUpdatedUserId;

    //分区类型
    @ApiModelProperty(notes = "分区类型")
    private String  wareAreaType;

//    private Map<String,String> data;

    @ApiModelProperty(notes = "子下垛位")
    private List<StackVo> stackVos;

    @ApiModelProperty(notes = "位置路由")
    private ArrayList<Integer> siteRoute;
}