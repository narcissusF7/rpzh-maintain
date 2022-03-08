package com.rpzh.dto;

import com.rpzh.pojo.BasePojo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;


/**
 * 仓库前端传参
 * @Author liubc
 * @create 2022/2/23 14:45
 * @Description: good good study,day day up
 */
@Data
public class WarehouseDto extends BasePojo {
    //主键ID
    @ApiModelProperty("仓库ID")
    private Integer id;

    //仓库名称
    @ApiModelProperty("仓库名称")
    private String name;

    //建造时间
    @ApiModelProperty("建造时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String buildTime;

    //库龄
    @ApiModelProperty("库龄")
    private Integer age;

    //仓库编码
    @ApiModelProperty("仓库编码")
    private String code;

    //仓库类型
    private String wareType;

    //可堆货容积
    @ApiModelProperty("可堆货容积")
    private BigDecimal useVolume;

    //可堆货容积单位
    @ApiModelProperty("可堆货容积单位")
    private String useVolumeUnit;

    //可堆货面积
    @ApiModelProperty("可堆货面积")
    private BigDecimal useArea;

    //可堆货面积单位
    @ApiModelProperty("可堆货面积单位")
    private String useAreaUnit;

    //是否自动化仓库,0:否,1:是
    @ApiModelProperty("是否自动化仓库")
    private String isAutomation;

    //是否统计数据,0:否,1:是
    @ApiModelProperty("是否统计数据")
    private String isStatistics;

    //是否启用,0:否,1:是
    @ApiModelProperty("是否启用")
    private String isUsing;

    //父节点路径
    private String pidRoute;

    //位置名称
    private String siteName;

    //用户账号
    private String username;

    //仓库创造时间
    private String createdTime;

    //备注
    @ApiModelProperty("备注")
    private String remark;
}