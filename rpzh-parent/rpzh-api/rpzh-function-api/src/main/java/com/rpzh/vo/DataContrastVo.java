package com.rpzh.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author: zyq
 * @Date: 2022/06/30/10:14
 * @Description: 功能
 */
@Data
public class DataContrastVo {
    //时间
    private String gatherTime;
    //对比的时间类型 1:天  2:周  3:月  4:年  默认天
    private Double node;
}
