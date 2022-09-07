package com.rpzh.dto;

import lombok.Data;

/**
 * db2视图数据库对接取数据传参
 * @Author liubc
 * @create 2022-09-07 14:52
 * @Description: good good study,day day up
 */
@Data
public class ViewDataDto {



    //分页开始页面
    private int page;



    //分页每页显示
    private int size;



}