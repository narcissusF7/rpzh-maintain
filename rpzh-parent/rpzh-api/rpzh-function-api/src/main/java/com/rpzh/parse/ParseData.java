package com.rpzh.parse;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public ParseData() {
    }
    public ParseData(Map<Integer,String> map) {

        this.nodeId = map.get(0);
        this.nodeType = "0"+(int)Double.parseDouble(map.get(1));
        String str = map.get(2);
        String[] split = str.split(",");
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            data.add(Double.parseDouble(split[i]));
        }
        this.Data = data;
        this.frequency = (int)Double.parseDouble(map.get(3));
        this.voltage = (int)Double.parseDouble(map.get(4));
        this.signalStrength = (int)Double.parseDouble(map.get(5));
        this.packetLossNum = (int)Double.parseDouble(map.get(6));

    }
}
