package com.rpzh.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author: zyq
 * @Date: 2022/06/30/10:14
 * @Description: 功能
 */
@Data
@ApiModel(description = "数据对比中一条曲线的一个数据返回")
public class DataContrastVo {
    @ApiModelProperty(notes = "时间")
    private String gatherTime;
    @ApiModelProperty(notes = "数值")
    private Double node=0d;

    public DataContrastVo(String gatherTime){
        this.gatherTime = gatherTime;
    }
}
