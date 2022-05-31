package com.rpzh.constant;

public class WarningTypeConstant {
    //预警类型:  01:设备电量故障,02:设备信号强度故障,03:设备丢包故障,04:垛内虫数预警,05:仓间虫数预警,06:垛内氧气预警,
    // 07:仓间氧气预警,08:垛内温度预警,09:仓间温度预警,10:垛内湿度预警,11:仓间湿度预警,12:包芯温度预警,13:平均包芯高于环境预警
    public static String NODE_LOW_BATTERY_BREAKDOWN ="01";
    public static String NODE_SIGNAL_STRENGTH_BREAKDOWN ="02";
    public static String NODE_PACKET_LOSS_BREAKDOWN ="03";
    public static String BORER_POPULATION_OVERRUN_WARNING ="04";
    public static String CORE_TEMP_OVERRUN_WARNING ="05";
    public static String ATMOSPHERE_TEMP_OVERRUN_WARNING ="06";
    public static String ATMOSPHERE_HUMIDITY_OVERRUN_WARNING ="07";
    public static String OXYGEN_OVERRUN_WARNING ="08";
    public static String STACK_TEMP_OVERRUN_WARNING ="09";
}
