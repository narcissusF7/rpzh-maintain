package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/06/30/10:14
 * @Description: 功能
 */
@Data
@ApiModel(description = "数据对比数据返回")
public class DataVo {
    @ApiModelProperty(notes = "数据对比中曲线名称")
    private String nodeName;
    @ApiModelProperty(notes = "一条曲线的数据")
    private List<DataContrastVo> dataContrastVos;

    public DataVo(String nodeName,List<DataContrastVo> dataContrastVos){
        this.nodeName = nodeName;
        this.dataContrastVos = dataContrastVos;
    }
}
