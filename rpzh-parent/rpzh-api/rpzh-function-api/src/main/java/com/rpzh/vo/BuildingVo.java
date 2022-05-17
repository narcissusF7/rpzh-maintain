package com.rpzh.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 楼栋数据返回
 * @Author liubc
 * @create 2022/2/25 16:23
 * @Description: good good study,day day up
 */
@Data
public class BuildingVo implements Serializable {

    //主键ID
    private Integer id;

    //楼号
    private String buildingNo;

    //负责人
    private String principalName;

    //负责人id
    private Integer principalId;

    //楼号名称
    private String buildingName;

    //可堆货容积
    private BigDecimal useVolume;

    //可堆货容积单位
    private String useVolumeUnit;

    //可堆货面积
    private BigDecimal useArea;

    //可堆货面积单位
    private String useAreaUnit;

    //朝向
    private String buildingOrientation;

    //仓库ID
    private Integer warehouseId;

    //备注
    private String remark;

    //父节点路径
    private String pidRoute;

    //创建时间
    private String createdTime;

    //创建人
    private String createdUser;

    //最后更新时间
    private String lastUpdatedTime;

    //更新人
    private String lastUpdatedUser;

    //位置名称
    private String siteName;

    //楼栋类型
    private String buildingType;

    private List<FloorVo> floorVos;
}