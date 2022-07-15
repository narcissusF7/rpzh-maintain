package com.rpzh.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;

/**
 * @Author: zyq
 * @Date: 2022/06/30/10:14
 * @Description: 数据对比前端传参
 */
@Data
public class DataContrastDto {
    //位置名称
    private String siteName;
    //位置路由
    private String siteRoute;
    //设备类型 01:氧气 02:包芯温度 03:环境温度 04:环境湿度 05:垛内平均包芯温度 06:仓间平均氧气 07:仓间平均温度 08:仓间平均湿度"
    private String nodeType;
    //设备id
    private String nodeId="";
    //采集的开始时间
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    //采集的结束时间
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    //对比的开始时间
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;
    //对比的结束时间
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;
    //采集的时间类型 1:天  2:周  3:月  4:年  默认月
    private String gatherType = "3";
}
