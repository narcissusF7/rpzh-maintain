package com.rpzh.vo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "入库质检返回")
public class EnterHouseExamineVo {
    @ApiModelProperty(notes ="入库单号")
    private String iostorinvLid;
    @ApiModelProperty(notes ="箱号")
    private int boxNum;
    @ApiModelProperty(notes ="外观描述编码")
    private String appearance;
    @ApiModelProperty(notes ="外观描述")
    private String appearanceName;
    @ApiModelProperty(notes = "内衬袋描述编码")
    private String liningBag;
    @ApiModelProperty(notes = "内衬袋描述")
    private String liningBagName;
    @ApiModelProperty(notes = "标示描述编码")
    private String mark;
    @ApiModelProperty(notes = "标示描述")
    private String markName;
    @ApiModelProperty(notes = "颜色描述编码")
    private String color;
    @ApiModelProperty(notes = "颜色描述")
    private String colorName;
    @ApiModelProperty(notes = "异味描述编码")
    private String peculiarSmell;
    @ApiModelProperty(notes = "异味描述")
    private String peculiarSmellName;
    @ApiModelProperty(notes = "水分描述编码")
    private String moisture;
    @ApiModelProperty(notes = "水分描述")
    private String moistureName;
    @ApiModelProperty(notes = "霉变描述")
    private String mildewName;
    @ApiModelProperty(notes = "霉变描述编码")
    private String mildew;
    @ApiModelProperty(notes = "包温描述")
    private String packetTempName;
    @ApiModelProperty(notes = "包温描述编码")
    private String packetTemp;
    @ApiModelProperty(notes = "虫情描述")
    private String hazardName;
    @ApiModelProperty(notes = "虫情描述编码")
    private String hazard;
    @ApiModelProperty(notes = "杂物描述编码")
    private String sundries;
    @ApiModelProperty(notes = "杂物描述")
    private String sundriesName;
    @ApiModelProperty(notes = "是否符合技术要求")
    private String isSizeUp;
    @ApiModelProperty(notes = "重量")
    private Integer weight;
}
