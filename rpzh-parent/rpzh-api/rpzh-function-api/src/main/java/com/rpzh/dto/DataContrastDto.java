package com.rpzh.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rpzh.pojo.NodeData;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/06/30/10:14
 * @Description: 功能
 */
@Data
public class DataContrastDto {
    //private List<NodeData> nodeDatas = new ArrayList<>();
    private String nodeDatas;
    //采集的开始时间、结束时间
/*
    @DateTimeFormat来控制入参，@JsonFormat来控制出参
*/

    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    //对比的开始时间、结束时间
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;
    //采集的时间类型 1:天  2:周  3:月  4:年  默认月
    private String gatherType = "3";
    //对比的时间类型 1:天  2:周  3:月  4:年  默认天
    private String contrastType = "1";
}
