package com.rpzh.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 当前路径拼接工具类
 * @Author liubc
 * @create 2022-07-04 17:09
 * @Description: good good study,day day up
 */
public class SiteRouteUtil {

    /**
     * 路径拼接
     *
     * pid_route 存在两种历史格式：
     *   1) 初始化数据：0,1,11,101,202          —— 纯 id，按逗号分隔的下标即层级
     *   2) 运行期追加：0,1,11,101,202,[04:206] —— 末段带 [类型:id] 标记
     * 旧实现用 contains("04") 之类的字符串匹配判断层级，遇到纯数字路径会误判
     * （例如 "101" 含 "01" 会把垛位错标成 [02:xxx]），这里改为解析末段真实层级。
     *
     * @param siteRoute 父路径
     * @param id  当前位置id
     * @return
     */
    public static String route(String siteRoute,String id){
        if (siteRoute == null || siteRoute.trim().isEmpty()) {
            return siteRoute;
        }
        String route = siteRoute.trim();
        String[] tokens = route.split(",");
        String last = tokens[tokens.length - 1].trim();
        int nextLevel;
        if (last.startsWith("[") && last.contains(":")) {
            // [04:206] -> 下一层 05
            nextLevel = Integer.parseInt(last.substring(1, 3)) + 1;
        } else {
            // 纯 id：token 个数即下一层的层级下标（0,1,11 -> 下一层是 03）
            nextLevel = tokens.length;
        }
        return route + ",[" + String.format("%02d", nextLevel) + ":" + id + "]";
    }

    /**
     * 从路径中获取垛位id/仓间id/层id/楼栋id/仓库id
     * @param siteRoute 路径路由
     * @param siteType 位置类型
     * @return
     */
    public static Integer getSiteId(String siteRoute, String siteType) {
        if (siteRoute == null || siteType == null || siteRoute.trim().isEmpty()) {
            return null;
        }
        // pid_route 有三种历史格式，旧实现只认完整的 [00:x],[01:x],...,[05:x]：
        //   1) 纯数字       0,1,11,101,202,303
        //   2) 混合         0,1,11,[03:101],[04:206]
        //   3) 完整标记     [00:0],[01:1],[02:11],[03:101],[04:206],[05:312]
        // 统一按「纯数字取下标当层级 / [NN:id] 取 NN 当层级」解析，避免
        // split("04:")[1] 在格式 2 上得到 "206]" 而抛 NumberFormatException。
        Map<String, String> ids = new HashMap<>();
        String[] tokens = siteRoute.trim().split(",");
        Pattern marked = Pattern.compile("^\\[(\\d{2}):([^]]+)]$");
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();
            if (token.isEmpty()) continue;
            Matcher m = marked.matcher(token);
            if (m.matches()) {
                ids.put(m.group(1), m.group(2).trim());
            } else if (i < 100) {
                ids.put(String.format("%02d", i), token);
            }
        }
        String id = ids.get(siteType);
        if (id == null || id.isEmpty()) return null;
        try {
            return Integer.valueOf(id.trim());
        } catch (NumberFormatException e) {
            return null;
        }
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