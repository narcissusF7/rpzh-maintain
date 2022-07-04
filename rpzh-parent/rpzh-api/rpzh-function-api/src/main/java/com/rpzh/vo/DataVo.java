package com.rpzh.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/06/30/10:14
 * @Description: 功能
 */
@Data
public class DataVo {

    private String nodeName;

    private List<DataContrastVo> dataContrastVos;

    public DataVo(String nodeName,List<DataContrastVo> dataContrastVos){
        this.nodeName = nodeName;
        this.dataContrastVos = dataContrastVos;
    }
}
