package com.rpzh.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 楼层数据返回
 * @Author liubc
 * @create 2022/2/27 9:33
 * @Description: good good study,day day up
 */
@Data
public class FloorVo implements Serializable {
    //主键ID
    private Integer id;

    //楼层编号
    private String floorNo;

    //负责人
    private String principalName;

    //负责人id
    private Integer principalId;

    //楼层名称
    private String floorName;

    //是否分区
    private String isPartition;

    //楼ID
    private Integer buildingId;

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

    //楼层类型
    private String floorType;

    //最后更新时间
    private String lastUpdatedTime;

    //最后更新人
    private String lastUpdatedUser;

    //最后更新人ID
    private Integer lastUpdatedUserId;

    private List<WareAreaVo> wareAreaVos;
}