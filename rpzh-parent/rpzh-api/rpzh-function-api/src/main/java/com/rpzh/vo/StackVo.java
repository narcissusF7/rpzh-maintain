package com.rpzh.vo;

import lombok.Data;

import java.util.Date;

/**
 * 垛位数据返回
 * @Author liubc
 * @create 2022/3/2 10:59
 * @Description: good good study,day day up
 */
@Data
public class StackVo {

    //主键ID
    private Integer id;

    //垛位编号
    private String stackNo;

    //中文名称
    private String nameCn;

    //区域ID
    private Integer areaId;

    //备注
    private String remark;

    //父节点路径
    private String pidRoute;

    //位置名称
    private String siteName;
    //创建时间
    private String createdTime;

    //创建人
    private String createdUser;

    //创建人ID
    private Integer createdUserId;

    //最后更新时间
    private String lastUpdatedTime;

    //最后更新人
    private String lastUpdatedUser;

    //最后更新人ID
    private Integer lastUpdatedUserId;

    //垛位类型
    private String stackType;

    //物料代码
    private String materialCode;

    //烟叶年份
    private String year;

}