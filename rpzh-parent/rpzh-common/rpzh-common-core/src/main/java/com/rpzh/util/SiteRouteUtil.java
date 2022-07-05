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
}