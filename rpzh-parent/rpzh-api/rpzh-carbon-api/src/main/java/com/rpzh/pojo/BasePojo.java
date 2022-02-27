package com.rpzh.pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 基础类
 * @Author liubc
 * @create 2022/2/23 14:47
 * @Description: good good study,day day up
 */
@Data
public class BasePojo {

    //分页开始页面
    @ApiModelProperty(notes = "分页开始页")
    private int page;

    //分页每页显示
    @ApiModelProperty(notes = "分页每页显示")
    private int size;

}