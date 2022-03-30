package com.rpzh.vo;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 分区管理数据返回
 * @Author liubc
 * @create 2022/2/28 10:19
 * @Description: good good study,day day up
 */
@Data
public class WareAreaVo {

    //主键ID
    private Integer id;

    //区域编号
    private String areaNo;

    //分区名称
    private String areaName;

    //层ID
    private Integer floorId;

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

    //分区类型
    private String  wareAreaType;

    private Map<String,String> data;
}