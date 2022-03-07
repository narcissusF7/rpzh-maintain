package dto;

import lombok.Data;

import java.util.Date;

/**
 * @author wbl
 * @description [养护计划前端传入类]
 */
@Data
public class MaintainPlanDto {
    /**
     * 物料批次号
     */
    private String materialBatchNum;
    /**
     * 烟叶代码
     */
    private String tobaccoCode;
    /**
     * 仓库名称(位置)
     */
    private String siteName;
    /**
     * 烟叶名称
     */
    private String tobaccoName;
    /**
     * 烟叶的年份
     */
    private String yearOfTobacco;
    /**
     * 存储的仓库位置名称
     */
    private String storageWarehouse;
    /**
     * 存储的仓库位置id
     */
    private Integer storageWarehouseId;
    /**
     * 养护阶段
     */
    private String maintainStage;
    /**
     * 养护天数
     */
    private Integer maintainNumberOfDays;
    /**
     * 调养日期
     */
    private Date maintainDates;
    /**
     * 养护数量
     */
    private Double maintainsAmount;
    /**
     * 降氧完成时间(天)
     */
    private Integer oxygenFallCompleteTime;
    /**
     * 申请的日期
     */
    private Date dateOfApplyFor;
    /**
     * 负责人
     */
    private String principal;
    /**
     * 负责人id
     */
    private Integer principalId;
    /**
     * 到期提醒
     */
    private Date expirationReminder;

    /**
     * 氧气上限1 , 2 , 3 级预警
     */
    private Double[] oxygenUpperLimit;
    /**
     * 氧气下限
     */
    private Double oxygenLowerLimit;

    /**
     * 二氧化碳上限1 , 2 , 3 级预警
     */
    private Integer[] carbonDioxideUpperLimit;
    /**
     * 二氧化碳下限
     */
    private Integer carbonDioxideLowerLimit;

    /**
     * 包芯水分上限1 , 2 , 3 级预警
     */
    private Double[] wetCoreUpperLimit;
    /**
     * 包芯水分下限
     */
    private Double wetCoreLowerLimit;

    /**
     * 包芯温度上限1 , 2 , 3 级预警
     */
    private Double[] tempCoreUpperLimit;
    /**
     * 包芯温度下限
     */
    private Double tempCoreLowerLimit;

    /**
     * 温度上限1 , 2 , 3 级预警
     */
    private Double[] tempUpperLimit;
    /**
     * 温度下限
     */
    private Double tempLowerLimit;

    /**
     * 湿度上限 1 , 2 , 3 级预警
     */
    private Double[] wetUpperLimit;
    /**
     * 湿度下限
     */
    private Double wetLowerLimit;
    /**
     * 第几页
     */
    private Integer page = 0;
    /**
     * 有几条数据
     */
    private Integer size = 10;
}
