package com.rpzh.parse;

import lombok.Data;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    public ParseData(Map<String,Object> map,String nodeType) throws ParseException {

        this.nodeId = map.get("deviceAddress").toString();
        List<Double> listData = new ArrayList<>();
        List<String> listDate = new ArrayList<>();
        SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        switch (nodeType) {
            // 温度
            case "temp":
                this.nodeType = "04";
                for (int i = 2; i >=0; --i) {
                    Double envData = ((BigDecimal)((List) map.get("tempData")).get(i)).doubleValue();
                    listData.add(envData);
                    String tempDate = ((List<String>) map.get("tempDateTime")).get(i);

                    listDate.add(sm.format(simpleDateFormat.parse(tempDate)));
                }
                break;
            // 氧气
            case "oxygen":
                for (int i = 2; i >=0; --i) {
                    Double envData = ((BigDecimal)((List) map.get("o2Data")).get(i)).doubleValue();
                    listData.add(envData);
                    String o2Date = ((List<String>) map.get("o2DateTime")).get(i);
                    listDate.add(sm.format(simpleDateFormat.parse(o2Date)));
                }
                this.nodeType = "01";
                break;
            // 温湿度
            case "humiture":
                for (int i = 2; i >=0; --i) {
                    List<BigDecimal> envData = (List<BigDecimal>)((List) map.get("envData")).get(i);
                    listData.add(envData.get(0).doubleValue());
                    String envDate = ((List<String>) map.get("envDateTime")).get(i);
                    listDate.add(sm.format(simpleDateFormat.parse(envDate)));
                }
                for (int i = 2; i >=0; --i) {
                    List<BigDecimal> envData = (List<BigDecimal>)((List) map.get("envData")).get(i);
                    listData.add(envData.get(1).doubleValue());
                    String envDate = ((List<String>) map.get("envDateTime")).get(i);
                    listDate.add(sm.format(simpleDateFormat.parse(envDate)));
                }
                this.nodeType = "03";
                break;
            default:
                break;
        }

        this.Data = listData;
        this.DateTime = listDate;
        this.frequency = (int)Double.parseDouble(map.get("frequency").toString());
        this.voltage = Double.parseDouble(map.get("voltage").toString());
        this.signalStrength = (int)Double.parseDouble(map.get("snr").toString());

    }

}
