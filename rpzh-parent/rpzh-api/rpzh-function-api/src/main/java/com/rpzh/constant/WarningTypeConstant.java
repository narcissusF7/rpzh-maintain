package com.rpzh.constant;

import com.rpzh.entity.ToolUtil;

import javax.xml.soap.Node;

public class WarningTypeConstant {
    //预警类型:  01:设备电量故障,02:设备信号强度故障,03:设备丢包故障,04:垛内虫数预警,05:仓间虫数预警,06:垛内氧气预警,
    // 07:仓间氧气预警,08:垛内温度预警,09:仓间温度预警,10:垛内湿度预警,11:仓间湿度预警,12:包芯温度预警,13:平均包芯高于环境预警
    public static String NODE_LOW_BATTERY_BREAKDOWN ="01";
    public static String NODE_SIGNAL_STRENGTH_BREAKDOWN ="02";
    public static String NODE_PACKET_LOSS_BREAKDOWN ="03";
    public static String STACK_BORER_POPULATION_OVERRUN_WARNING ="04";
    public static String WARE_BORER_POPULATION_OVERRUN_WARNING ="05";
    public static String STACK_OXYGEN_OVERRUN_WARNING ="06";
    public static String WARE_OXYGEN_OVERRUN_WARNING ="07";
    public static String STACK_TEMP_OVERRUN_WARNING ="08";
    public static String WARE_TEMP_OVERRUN_WARNING ="09";
    public static String STACK_HUMIDITY_OVERRUN_WARNING ="10";
    public static String WARE_HUMIDITY_OVERRUN_WARNING ="11";
    public static String CORE_TEMP_OVERRUN_WARNING ="12";
    public static String CORE_TEMP_OVERRUN_STACK_WARNING ="13";

    public static String getWarningType(String siteType , String nodeType , String evnType){
        //非温湿度设备
        if (ToolUtil.isEmpty(evnType)){
            if (NodeConstant.WET_CORE_CHIP_NODE_TYPE_VALUE.equals(nodeType)){
                return CORE_TEMP_OVERRUN_WARNING;
            }
            //仓间设备
            if (NodeConstant.NODE_WARE_AREA.equals(siteType)){
                switch (nodeType){
                    //如果是仓间氧气设备
                    case NodeConstant.OXYGEN_NODE_TYPE_VALUE :
                        return WARE_OXYGEN_OVERRUN_WARNING;
                    default:
                        return WARE_OXYGEN_OVERRUN_WARNING;
                }
            }else {
                switch (nodeType){
                    //如果是垛内氧气设备
                    case NodeConstant.OXYGEN_NODE_TYPE_VALUE :
                        return STACK_OXYGEN_OVERRUN_WARNING;
                    default:
                        return STACK_OXYGEN_OVERRUN_WARNING;
                }
            }
        }else {//温湿度设备
            if (NodeConstant.NODE_WARE_AREA.equals(siteType)){//仓间温湿度
                switch (evnType){
                    //如果是仓间氧气设备
                    case "temp" :
                        return WARE_TEMP_OVERRUN_WARNING;
                    default:
                        return WARE_HUMIDITY_OVERRUN_WARNING;
                }
            }else {
                switch (evnType){
                    //如果是仓间氧气设备
                    case "temp" :
                        return STACK_TEMP_OVERRUN_WARNING;
                    default:
                        return STACK_HUMIDITY_OVERRUN_WARNING;
                }
            }
        }
    }
}
