package com.rpzh.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

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
    //预警的节点设备id集合
    private String nodeId;
    //设备类型  01:无线氧气传感器,03:无线温湿度传感器,04: 无线包芯温度传感器
    private String nodeType;
    //负责人id
    private Integer principalId;
    //负责人
    private String principalName;
    //各个节点设备id的预警等级集合,1:一级预警,2:二级预警,3:三级预警
    private String warningGrade;
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
    //预警值
    private String warningVal;
    //一级预警值
    private Double warningValOne;
    //一级预警时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date warningTimeOne;
    //二级预警值
    private Double warningValTwo;
    //二级预警时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date warningTimeTwo;
    //三级预警值
    private Double warningValThree;
    //三级预警时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date warningTimeThree;
    //预警时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date warningTime;
    //位置路由
    private String siteRoute;
    //任务id
    private Integer taskId;



    public EarlyWarning(){

    }

    public EarlyWarning(Integer id, String warningType, String nodeId, String nodeType, String warningVal, Date warningTime, Integer principalId, String principalName, String warningGrade, Integer isAffirm, String dealSuggestion, String remark, String dealTime, Integer stackId, Integer areaId) {
        this.id = id;
        this.warningType = warningType;
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.principalId = principalId;
        this.principalName = principalName;
        this.warningGrade = warningGrade;
        this.isAffirm = isAffirm;
        this.dealSuggestion = dealSuggestion;
        this.remark = remark;
        this.dealTime = dealTime;
        this.stackId = stackId;
        this.areaId = areaId;
        this.warningVal = warningVal;
        this.warningTime = warningTime;
    }

    public EarlyWarning(String warningType, String nodeId, String nodeType, String warningVal, String warningGrade,String siteRoute,String siteType,Integer siteId) {
        this.warningType = warningType;
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.warningVal = warningVal;
        this.warningGrade = warningGrade;
        this.siteRoute = siteRoute;
        if("04".equals(siteRoute)){
            this.areaId = siteId;
        }else{
            this.stackId = siteId;
        }
    }
    public EarlyWarning(String warningType, String nodeId, String nodeType, String warningGrade) {
        this.warningType = warningType;
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.warningGrade = warningGrade;
    }
}
