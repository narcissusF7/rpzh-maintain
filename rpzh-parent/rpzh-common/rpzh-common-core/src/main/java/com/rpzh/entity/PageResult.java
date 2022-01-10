package com.rpzh.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果工具类
 * @Author liubc成
 * @create 2021/3/29 15:27
 */
@ApiModel(description = "分页对象")
public class PageResult<T> implements Serializable {
    @ApiModelProperty(notes = "总记录数")
    private Long total;//总记录数
    @ApiModelProperty(notes = "记录")
    private List<T> rows;//记录

    public PageResult(Long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }

    public PageResult() {
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}
