package com.rpzh.pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 基础类（分页基类）
 *
 * 统一自 carbon/humiture/oxygen/temp/function-api 中逐字节相同（carbon 仅差空行）的副本，
 * 收敛到 rpzh-common-api，避免各 api 模块重复定义。
 *
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
