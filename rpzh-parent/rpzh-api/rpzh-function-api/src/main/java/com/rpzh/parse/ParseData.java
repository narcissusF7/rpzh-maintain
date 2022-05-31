package com.rpzh.parse;

import lombok.Data;

import java.util.List;

@Data
public class ParseData{

    /**
     * 设备类型id
     */
    private String nodeType;
    /**
     * 数据包类型id
     */
    private byte dataPacketType;
    /**
     * 数据
     */
    private List<Double> Data;
    /**
     * 数据时间
     */
    private List<String> DateTime;
    /**
     * 频率
     */
    private int frequency;
    /**
     * 电量
     */
    private double voltage;

    /**
     * 信号强度
     */
    private Integer signalStrength;
    /**
     * 设备id
     */
    private String nodeId;
    /**
     * 网关地址
     */
    private String gatewayAddress;

    /**
     * 丢包个数
     */
    private Integer packetLossNum;
}
