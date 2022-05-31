package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "设备操作记录返回")
public class NodeOperationVo {

    @ApiModelProperty(notes = "操作记录id")
    private Integer id;
    @ApiModelProperty(notes = "设备id")
    private String nodeId;

    @ApiModelProperty(notes = "位置id")
    private Integer siteId;

    @ApiModelProperty(notes = "坐标X")
    private String siteX;

    @ApiModelProperty(notes = "坐标Y")
    private String siteY;

    @ApiModelProperty(notes = "坐标Z")
    private String siteZ;

    @ApiModelProperty(notes = "操作类型")
    private Integer operationType;

    @ApiModelProperty(notes = "操作详情")
    private String operationDetails;

    @ApiModelProperty(notes = "创建时间")
    private String createdTime;

    @ApiModelProperty(notes = "操作员")
    private String createdUser;

}
