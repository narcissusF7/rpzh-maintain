package com.rpzh.dto;

import lombok.Data;

import java.util.List;

/**
 * 删除仓库传参
 *
 * @Author liubc
 * @create 2022/2/28 16:42
 * @Description: good good study,day day up
 */
@Data
public class DelDto {

    //仓库id
    private Integer warehouseNodeId;

    //仓库类型,仓库节点类型（01：仓库, 02: 楼号, 03:楼层，04：分区，05：垛位）
    private String warehouseNodeType;

    //删除垛位时使用
    private List<String> stackIds;
}