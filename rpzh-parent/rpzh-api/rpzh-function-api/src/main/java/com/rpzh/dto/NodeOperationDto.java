package com.rpzh.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 设备管理记录传参
 * @Author liujj
 * @create 2022/4/27 9:31
 * @Description: good good study,day day up
 */
@Data
@ApiModel(value = "设备操作记录交互类")
public class NodeOperationDto {

    @ApiModelProperty("设备操作记录id")
    private Integer operationId;

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("设备id")
    private String nodeId;

    @ApiModelProperty("采集位置")
    private String gatherSite;

    @ApiModelProperty("设备类型")
    private String nodeType;

    @ApiModelProperty("位置id")
    private Integer siteId;

    private String siteX;

    private String siteY;

    private String siteZ;

    @ApiModelProperty("操作类型")
    private Integer operationType;

    @ApiModelProperty("操作详情")
    private String operationDetails;

    @ApiModelProperty("创建时间")
    private String createdTime;

    @ApiModelProperty("操作员")
    private String createdUser;

    private int page;

    private int size;


    public NodeOperationDto() {
        this.nodeId = "";
        this.siteId = 0;
        this.siteX = "-";
        this.siteY = "-";
        this.siteZ = "-";
        this.operationType = 0;
        this.operationDetails = "";
        this.createdUser = "";
    }
}
