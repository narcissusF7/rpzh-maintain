package com.rpzh.util;

/**
 * 当前路径拼接工具类
 * @Author liubc
 * @create 2022-07-04 17:09
 * @Description: good good study,day day up
 */
public class SiteRouteUtil {

    /**
     * 路径拼接
     * @param siteRoute 父路径
     * @param id  当前位置id
     * @return
     */
    public static String route(String siteRoute,String id){
        if (siteRoute.contains("04")){
            siteRoute = siteRoute + ",[05:"+id+"]";
        }else if (siteRoute.contains("03")){
            siteRoute = siteRoute + ",[04:"+id+"]";
        }else if (siteRoute.contains("02")){
            siteRoute = siteRoute + ",[03:"+id+"]";
        }else if (siteRoute.contains("01")){
            siteRoute = siteRoute + ",[02:"+id+"]";
        }else if (siteRoute.contains("00")){
            siteRoute = siteRoute + ",[01:"+id+"]";
        }
        return siteRoute;
    }

    /**
     * 从路径中获取垛位id/仓间id/层id/楼栋id/仓库id
     * @param siteRoute 路径路由
     * @param siteType 位置类型
     * @return
     */
    public static Integer getSiteId(String siteRoute,String siteType){
        if ("05".equals(siteType)){
            return Integer.parseInt(siteRoute.split("05:")[1].split("]")[0]);
        }else if ("04".equals(siteType)){
            return Integer.parseInt(siteRoute.split("04:")[1].split("],\\[05")[0]);
        }else if ("03".equals(siteType)){
            return Integer.parseInt(siteRoute.split("03:")[1].split("],\\[04")[0]);
        }else if ("02".equals(siteType)){
            return Integer.parseInt(siteRoute.split("02:")[1].split("],\\[03")[0]);
        }else if ("01".equals(siteType)){
            return Integer.parseInt(siteRoute.split("01:")[1].split("],\\[02")[0]);
        }
        return null;
    }

    /*
    * 获取预警名称
    * */
    public static String getWarningName(String warningType){
        switch (warningType){
            case "01":
                return "设备电量故障";
            case "02":
                return "设备信号强度故障";
            case "03":
                return "设备丢包故障";
            case "04":
                return "垛内甲虫预警";
            case "05":
                return "仓间甲虫预警";
            case "06":
                return "垛内氧气预警";
            case "07":
                return "仓间氧气预警";
            case "08":
                return "垛内温度预警";
            case "09":
                return "仓间温度预警";
            case "10":
                return "垛内湿度预警";
            case "11":
                return "仓间湿度预警";
            case "12":
                return "包芯温度预警";
            case "13":
                return "平均包芯高于环境预警";
            case "14":
                return "仓间粉螟预警";
            default:
                return "平均包芯低于环境预警";
        }
    }
}