package com.rpzh.pojo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

@Data
public class NodeExportData {
    @Excel(name = "采集位置", orderNum = "1")
    String gatherSite;
    @Excel(name = "平均氧气浓度", orderNum = "1")
    Double avgOxygen;
    @Excel(name = "最高氧气浓度", orderNum = "1")
    Double maxOxygen;
    @Excel(name = "最低氧气浓度", orderNum = "1")
    Double minOxygen;
    @Excel(name = "平均包芯温度", orderNum = "1")
    Double avgCoreTemp;
    @Excel(name = "最高包芯温度", orderNum = "1")
    Double maxCoreTemp;
    @Excel(name = "最低包芯温度", orderNum = "1")
    Double minCoreTemp;
    @Excel(name = "平均包芯与环境温度差值", orderNum = "1")
    Double tempReduce;
    @Excel(name = "平均温度", orderNum = "1")
    Double avgTemp;
    @Excel(name = "最高温度", orderNum = "1")
    Double maxTemp;
    @Excel(name = "最低温度", orderNum = "1")
    Double minTemp;
    @Excel(name = "平均湿度", orderNum = "1")
    Double avgHumiture;
    @Excel(name = "最高湿度", orderNum = "1")
    Double maxHumiture;
    @Excel(name = "最低湿度", orderNum = "1")
    Double minHumiture;
    @Excel(name = "日期", orderNum = "1")
    String dateTime;

}
