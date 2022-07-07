package com.rpzh.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author zyq
 * 预警信息设置
 */
@Data
public class WareAreaAlarmSettingVo {

    @ApiModelProperty(notes = "仓间氧气下限")
    private String warehouseOxygenLower;
    @ApiModelProperty(notes = "仓间氧气上限一级")
    public String warehouseOxygenUpperOne;
    @ApiModelProperty(notes = "仓间氧气上限二级")
    private String warehouseOxygenUpperTwo;
    @ApiModelProperty(notes = "仓间氧气上限三级")
    private String warehouseOxygenUpperThree;
    @ApiModelProperty(notes = "仓间氧气上限集合")
    private String oxygenList;
    /**
     * 仓间温度
     */
    @ApiModelProperty(notes = "仓间温度上限集合")
    private String tempList;
    @ApiModelProperty(notes = "仓间温度上限一级")
    private String warehouseTempUpperOne;
    @ApiModelProperty(notes = "仓间温度上限二级")
    private String warehouseTempUpperTwo;
    @ApiModelProperty(notes = "仓间温度上限三级")
    private String warehouseTempUpperThree;
    @ApiModelProperty(notes = "仓间温度下限")
    private String warehouseTempLower;

    @ApiModelProperty(notes = "仓间水分上限一级")
    private String warehouseMoistureUpperOne;
    @ApiModelProperty(notes = "仓间水分上限二级")
    private String warehouseMoistureUpperTwo;
    @ApiModelProperty(notes = "仓间水分上限三级")
    private String warehouseMoistureUpperThree;
    @ApiModelProperty(notes = "仓间水分下限")
    private String moistureList;
    @ApiModelProperty(notes = "仓间水分上限集合")
    private String warehouseMoistureLower;
    @ApiModelProperty(notes = "位置类型")
    private String siteType;
    @ApiModelProperty(notes = "位置id")
    private Integer stackId;
    @ApiModelProperty(notes = "位置路径,00:根节点,01:仓库, 02: 楼号, 03: 层, 04: 区, 05: 垛位")
    private String siteName;
    /**
     * 更新时间
     */
    @ApiModelProperty(notes = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedTime;
    @ApiModelProperty(notes = "创建人")
    private String createUser;
    @ApiModelProperty(notes = "仓间数值设置表id")
    private Integer id;

}
