package com.rpzh.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 温度数据返回
 *
 * @Author liubc
 * @create 2021/12/21 10:19
 * @Description: good good study,day day up
 */
@Data
public class TempDataVo {
    //温度设备id
    private String deviceId;

    //采集位置
    private String gatherLocation;

    //温度值
    private BigDecimal temp;

    //收到数据时间
    private String createTime;

    //设备类型
    private String deviceTypeName;

    //每天数据集合
    private String dayDataTemp;

    //每月数据集合
    private String monthDataTemp;

    //网关id,发送数据的网关
    private String gatewayId;

    //最大值
    private BigDecimal max;

    //平均值
    private BigDecimal avg;

    //最小值
    private BigDecimal min;

    //备注
    private String remark;

}