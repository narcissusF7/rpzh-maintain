package com.rpzh.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 垛位管理传参
 * @Author liubc
 * @create 2022/2/28 9:31
 * @Description: good good study,day day up
 */
@Data
public class StackDto {

    //主键ID
    //主键ID
    @ApiModelProperty("垛位ID")
    private Integer id;

    //垛位编号
    @ApiModelProperty("垛位编号")
    //@NotEmpty(message = "垛位编号不允许为空")
    private String stackNo;

    //中文名称
    @ApiModelProperty("中文名称")
    //@NotEmpty(message = "中文名称不允许为空")
    private String nameCn;

    //区域ID
    @ApiModelProperty("分区ID")
    //@NotNull(message = "分区ID不允许为空")
    private Integer areaId;

    //父节点路径
    private String pidRoute;

    //位置名称
    private String siteName;

    //创建人
    private String createdUser;

    //更新人
    private String updateUser;

    //区域id
    private Integer wareareaId;

    //分页开始页面
    @ApiModelProperty(notes = "分页开始页")
    private int page;

    //分页每页显示
    @ApiModelProperty(notes = "分页每页显示")
    private int size;

    //备注
    @ApiModelProperty("备注")
    private String remark;
}