package com.rpzh.vo;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author wbl
 * 温湿度vo
 */
@Data
@NoArgsConstructor
public class EnvVo implements Comparable<EnvVo> {

    public EnvVo(Map envMap, int dataIndex) {
        this.deviceId = envMap.get("deviceAddress").toString();
        this.gatherLocation = envMap.get("gatherLocation").toString();
        double[][] envData = JSONObject.parseObject(envMap.get("envData").toString().getBytes(StandardCharsets.UTF_8),
                double[][].class);
        this.temp = BigDecimal.valueOf(envData[dataIndex][0]);
        this.moisture = BigDecimal.valueOf(envData[dataIndex][1]);
        this.createTime = DateUtil.now();
        this.gatewayId = envMap.get("gatewayAddress").toString();
        this.remark = envMap.get("remark").toString();
    }

    private Integer id;
    /**
     * 节点id
     */
    private String deviceId;
    /**
     * 采集位置
     */
    private String gatherLocation;
    /**
     * 设备类型
     */
    private String deviceTypeName;
    /**
     * 温度
     */
    private BigDecimal temp;
    /**
     * 湿度
     */
    private BigDecimal moisture;
    /**
     * 采集时间
     */
    private String createTime;
    /**
     * 网关id
     */
    private String gatewayId;
    /**
     * 备注
     */
    private String remark;


    /**
     * 温度最大值
     */
    private Double tempMaxValue;
    /**
     * 温度最小值
     */
    private Double tempMinValue;
    /**
     * 温度平均值
     */
    private Double tempAvgValue;
    /**
     * 湿度最大值
     */
    private Double moistureMaxValue;
    /**
     * 湿度最小值
     */
    private Double moistureMinValue;
    /**
     * 湿度平均值
     */
    private Double moistureAvgValue;

    /**
     * 排序
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(EnvVo o) {
        long thisTime = DateUtil.parse(this.createTime).getTime();
        long oTime = DateUtil.parse(o.createTime).getTime();
        if (thisTime > oTime) {
            return -1;
        } else if (thisTime < oTime) {
            return 1;
        }
        return 0;
    }
}
