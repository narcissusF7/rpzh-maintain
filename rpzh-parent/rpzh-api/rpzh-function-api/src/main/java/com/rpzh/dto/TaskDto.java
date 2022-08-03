package com.rpzh.dto;

import lombok.Data;

import java.util.List;

/**
 * 任务中心管理前端传参
 *
 * @Author liubc
 * @create 2022-05-30 14:49
 * @Description: good good study,day day up
 */
@Data
public class TaskDto {

    //主键id
    private String id;

    //任务编号
    private String taskNum;

    //箱号
    private String caseNo;

    //任务类型
    private String type;

    //统计类型,Y:年统计,M:月统计,D:日统计
    private String deteType;

    //任务地点
    private String taskAddress;

    //位置路径
    private String siteRoute;

    //移垛任务目标位置
    private String targetSiteRoute;

    //移垛任务目标位置名称
    private String targetSiteName;

    //移垛之前的路径
    private String siteRouteBefore;

    //移垛目标位置id
    private String targetSiteId;

    //位置名称
    private String siteName;

    //当前位置id
    private String siteId;

    //移垛数据
    private String siteDate;

    //任务位置路径
    private String taskSiteRoute;

    //推送方式
    private String pushWay;

    //处理措施
    private String dealMethod;

    //处理结果
    private String dealResult;

    //发起时间
    private String createdTime;

    //开始时间
    private String startTime;

    //结束时间
    private String endTime;

    //结束开始时间
    private String endStartTime;

    //过期时间
    private String expiresTime;

    //任务完成时间
    private String finishTime;

    //任务详情
    private String detail;

    //任务评价
    private String evaluate;

    //评价人
    private String evaluateUser;

    //任务评价时间
    private String evaluateTime;

    //负责人
    private String principal;

    //任务状态,0:未完成,1:完成,2:逾期(mq代码控制)
    private String status;

    //创建人
    private String createdUser;

    //分页开始页面
    private int page;

    //分页每页显示
    private int size;

    //日期数据
    List<String> days;

    public TaskDto (){};
    public TaskDto(String taskNum, String type, String taskAddress, String siteRoute, String principal,String endTime) {
        this.taskNum = taskNum;
        this.type = type;
        this.taskAddress = taskAddress;
        this.taskSiteRoute = siteRoute;
        this.principal = principal;
        this.endTime = endTime;
        switch (type){
            case "06":
                this.detail = "垛内氧气";
                break;
            case "07":
                this.detail = "仓间氧气";
                break;
            case "08":
                this.detail = "垛内温度";
                break;
            case "09":
                this.detail = "仓间温度";
                break;
            case "10":
                this.detail = "垛内湿度";
                break;
            case "11":
                this.detail = "仓间湿度";
                break;
            case "12":
                this.detail = "包芯温度";
                break;
            case "13":
                this.detail = "平均包芯高于环境";
                break;
            case "19":
                this.detail = "平均包芯低于环境";
                break;
        }
    }
}