package com.rpzh.constant;

public class WarningTypeConstant {
    //预警类型,01:设备电量故障,02:设备信号强度故障,03:设备丢包故障,04:虫数超限预警,05:包芯温度超限预警,06:环境温度超限预警,07:环境湿度超限预警,08:氧气超限预警,09:垛位温度预警
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
