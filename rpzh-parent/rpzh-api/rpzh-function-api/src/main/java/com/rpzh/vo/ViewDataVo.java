package com.rpzh.vo;

import lombok.Data;

/**
 * db2对接取数据返回
 * @Author liubc
 * @create 2022-09-07 14:55
 * @Description: good good study,day day up
 */
@Data
public class ViewDataVo {


    //垛位编码
    private String QUICKCODE;

    //垛位名称
    private String SHELFNAME;

    //物料编码
    private String MATERIALCODE;

    //物料名称
    private String MATERIALNAME;

    //条码
    private String MATERIALBATCH;

    //箱号
    private Integer BOXNUM;








}