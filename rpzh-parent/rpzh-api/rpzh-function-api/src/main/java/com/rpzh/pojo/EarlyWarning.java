package com.rpzh.pojo;

import lombok.Data;
/**
 * @Author: ljj
 * @Date: 2022/05/26/12:37
 * @Description: 预警表
 */
@Data
public class EarlyWarning {
    //预警表id
    private Integer id;
    //预警类型:01:设备电量故障,02:设备信号强度故障,03:设备丢包故障,04:垛内甲虫预警,05:仓间甲虫预警,06:垛内氧气预警,07:仓间氧气预警,08:垛内温度预警,09:仓间温度预警,10:垛内湿度预警,11:仓间湿度预警,12:包芯温度预警,13:平均包芯高于环境预警,14:仓间粉螟预警
    private String warningType;
    //设备id
    private String nodeId;
    //设备类型  01:无线氧气传感器,03:无线温湿度传感器,04: 无线包芯温度传感器
    private String nodeType;
    //一级预警值
    private Double warningValOne;
    //一级预警时间
    private String warningTimeOne;
    //二级预警值
    private Double warningValTwo;
    //二级预警时间
    private String warningTimeTwo;
    //三级预警值
    private Double warningValThree;
    //三级预警时间
    private String warningTimeThree;
    //负责人id
    private Integer principalId;
    //负责人
    private String principalName;
    //预警等级,1:一级预警,2:二级预警,3:三级预警
    private Integer warningGrade;
    //是否处理,0:已处理,1:正在处理,2:未处理,
    private Integer isAffirm;
    //处理意见
    private String dealSuggestion;
    //备注
    private String remark;
    //处理时间
    private String dealTime;
    //垛位id
    private Integer stackId;
    //仓间id
    private Integer areaId;
    private String siteRoute;

    public EarlyWarning(){

    }

    public EarlyWarning(Integer id, String warningType, String nodeId, String nodeType, Double warningValOne, String warningTimeOne, Double warningValTwo, String warningTimeTwo, Double warningValThree, String warningTimeThree, Integer principalId, String principalName, Integer warningGrade, Integer isAffirm, String dealSuggestion, String remark, String dealTime, Integer stackId, Integer areaId) {
        this.id = id;
        this.warningType = warningType;
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.warningValOne = warningValOne;
        this.warningTimeOne = warningTimeOne;
        this.warningValTwo = warningValTwo;
        this.warningTimeTwo = warningTimeTwo;
        this.warningValThree = warningValThree;
        this.warningTimeThree = warningTimeThree;
        this.principalId = principalId;
        this.principalName = principalName;
        this.warningGrade = warningGrade;
        this.isAffirm = isAffirm;
        this.dealSuggestion = dealSuggestion;
        this.remark = remark;
        this.dealTime = dealTime;
        this.stackId = stackId;
        this.areaId = areaId;
    }

    public EarlyWarning(String warningType, String nodeId, String nodeType, Double warningValOne, Integer warningGrade,String siteRoute,String principalName) {
        this.warningType = warningType;
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.warningValOne = warningValOne;
        this.warningGrade = warningGrade;
        this.siteRoute = siteRoute;
        this.principalName = principalName;
    }
    public EarlyWarning(String warningType, String nodeId, String nodeType, Integer warningGrade) {
        this.warningType = warningType;
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.warningGrade = warningGrade;
    }
}
