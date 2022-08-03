package com.rpzh.pojo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

@Data
public class NodeExportData {
    @Excel(name = "采集位置", orderNum = "1")
    String gatherSite;
    @Excel(name = "平均氧气浓度", orderNum = "1")
    double avgOxygen;
    @Excel(name = "最高氧气浓度", orderNum = "1")
    double maxOxygen;
    @Excel(name = "最低氧气浓度", orderNum = "1")
    double minOxygen;
    @Excel(name = "平均包芯温度", orderNum = "1")
    double avgCoreTemp;
    @Excel(name = "最高包芯温度", orderNum = "1")
    double maxCoreTemp;
    @Excel(name = "最低包芯温度", orderNum = "1")
    double minCoreTemp;
    @Excel(name = "平均包芯与环境温度差值", orderNum = "1")
    double tempReduce;
    @Excel(name = "平均温度", orderNum = "1")
    double avgTemp;
    @Excel(name = "最高温度", orderNum = "1")
    double maxTemp;
    @Excel(name = "最低温度", orderNum = "1")
    double minTemp;
    @Excel(name = "平均湿度", orderNum = "1")
    double avgHumiture;
    @Excel(name = "最高湿度", orderNum = "1")
    double maxHumiture;
    @Excel(name = "最低湿度", orderNum = "1")
    double minHumiture;
    @Excel(name = "最低湿度", orderNum = "1")
    String dateTime;

}
