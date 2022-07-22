package com.rpzh.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 氧气数据接收
 * @Author liubc
 * @create 2021/12/7 8:54
 */
@Data
public class O2DataVo {

    //氧气设备id
    private String deviceId;

    //采集位置
    private String gatherLocation;

    //设备类型
    private String deviceTypeName;

    //氧气含量
    private BigDecimal oxygen;

    //收到数据时间
    private String createTime;

    //没天数据集合
    private String dayDataOxygen;

    //每月数据集合
    private String monthDataOxygen;

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