package com.rpzh.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author liubc成
 * @create 2021/5/25 19:55
 * 路由显示信息
 */
@Data
@ApiModel(description = "路由显示信息")
public class MetaVo {

    /**
     * 设置该路由在侧边栏和面包屑中展示的名字
     */
    @ApiModelProperty(notes = "设置该路由在侧边栏和面包屑中展示的名字")
    private String title;

    /**
     * 设置该路由的图标，对应路径src/assets/icons/svg
     */
    @ApiModelProperty(notes = "设置该路由的图标，对应路径src/assets/icons/svg")
    private String icon;

    /**
     * 设置为true，则不会被 <keep-alive>缓存
     */
    @ApiModelProperty(notes = "设置为true，则不会被 <keep-alive>缓存")
    private boolean noCache;
}