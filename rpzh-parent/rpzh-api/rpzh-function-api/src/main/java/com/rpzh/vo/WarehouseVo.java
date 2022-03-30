package com.rpzh.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.beans.Transient;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 仓库交互类
 * @Author liubc
 * @create 2022/2/23 11:53
 * @Description: good good study,day day up
 */
@Data
public class WarehouseVo {

    //主键ID
    private Integer id;

    //仓库名称
    private String name;

    //建造时间
    //@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String buildTime;

    //库龄
    private Integer age;

    //仓库编码
    private String code;

    //可堆货容积
    private BigDecimal useVolume;

    //仓库类型
    private String wareType;

    //可堆货容积单位
    private String useVolumeUnit;

    //可堆货面积
    private BigDecimal useArea;

    //可堆货面积单位
    private String useAreaUnit;

    //是否自动化仓库
    private Integer isAutomation;

    //是否统计数据
    private Integer isStatistics;

    //是否启用
    private Integer isUsing;

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

    //备注
    private String remark;

    //父节点路径
    private String pidRoute;

    //位置名称
    private String siteName;
}