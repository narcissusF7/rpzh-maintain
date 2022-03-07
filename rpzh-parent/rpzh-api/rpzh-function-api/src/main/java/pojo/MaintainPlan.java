package pojo;

import cn.hutool.core.date.DateUtil;
import dto.MaintainPlanDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author wbl
 * @createTime 2022/2/25 11:24
 * @description [养护计划实体类]
 */
@Data
@NoArgsConstructor
public class MaintainPlan {

    public MaintainPlan(MaintainPlanDto maintainPlanDto) {
        this.materialCode = maintainPlanDto.getTobaccoCode();
        this.materialName = maintainPlanDto.getTobaccoName();
        this.siteId = maintainPlanDto.getStorageWarehouseId();
        this.siteName = maintainPlanDto.getStorageWarehouse();
        this.aftercareDateStart = maintainPlanDto.getMaintainDates();
        this.year = maintainPlanDto.getYearOfTobacco();
        this.maintainStage = maintainPlanDto.getMaintainStage();
        this.maintainDays = maintainPlanDto.getMaintainNumberOfDays();
        this.maintainCount = maintainPlanDto.getMaintainsAmount();
        this.oxygenReductionDays = maintainPlanDto.getOxygenFallCompleteTime();
        this.applyTime = maintainPlanDto.getDateOfApplyFor();
        this.principalName = maintainPlanDto.getPrincipal();
        this.principalId = maintainPlanDto.getPrincipalId();
        this.expireTime = maintainPlanDto.getExpirationReminder();
        this.createTime = DateUtil.date();
    }

    /**
     * 养护计划id
     */
    private Integer id;
    /**
     * 物料代码
     */
    private String materialCode;
    /**
     * 物料名称
     */
    private String materialName;
    /**
     * 物料批次号
     */
    private String materialBatchNum;
    /**
     * 仓库建造时间
     */
    private Date warehouseBuildTime;
    /**
     * 储存仓库ID
     */
    private Integer siteId;
    /**
     * 储存仓库位置名称
     */
    private String siteName;
    /**
     * 烟叶年份
     */
    private String year;
    /**
     * 储存仓库类型
     */
    private String siteType;
    /**
     * 负责人ID
     */
    private Integer principalId;
    /**
     * 负责人名称
     */
    private String principalName;
    /**
     * 申请时间
     */
    private Date applyTime;
    /**
     * 申请编号
     */
    private String applyNum;
    /**
     * 养护阶段
     */
    private String maintainStage;
    /**
     * 养护天数
     */
    private Integer maintainDays;
    /**
     * 开始调养日期
     */
    private Date aftercareDateStart;
    /**
     * 结束调养日期
     */
    private Date aftercareDateEnd;
    /**
     * 降氧完成时间(天)
     */
    private Integer oxygenReductionDays;
    /**
     * 养护数量（担）
     */
    private Double maintainCount;
    /**
     * 养护阶段固化信息
     */
    private String maintainStageData;
    /**
     * 创建人
     */
    private String createdUser;
    /**
     * 最后更新时间
     */
    private Date lastUpdatedTime;
    /**
     * 最后更新人
     */
    private String lastUpdatedUser;
    /**
     * 创建人ID
     */
    private Integer createdUserId;
    /**
     * 最后更新人ID
     */
    private Integer lastUpdatedUserId;
    /**
     * 审核状态
     */
    private Integer approveStatus;
    /**
     * 审批人ID
     */
    private Integer approverId;
    /**
     * 审批人名称
     */
    private String approverName;
    /**
     * 审批日期
     */
    private Date approverTime;
    /**
     * 审批人意见
     */
    private String approverOpinion;
    /**
     * 是否阶段变更：1-是；0-否
     */
    private Integer isChange;
    /**
     * 是否结束养护：1-是；0-否
     */
    private Integer isEnd;
    /**
     * 到期时间
     */
    private Date expireTime;
    /**
     * 评吸意见
     */
    private String content;
    /**
     * 评吸评分
     */
    private String grade;
    /**
     * 备注
     */
    private String remark;
    /**
     * 养护状态(0 = 开垛暂停)
     */
    private Integer status;
    /**
     * 报警值设置的Id
     */
    private Integer alarmValueSettingId;
    /**
     * 创建时间
     */
    private Date createTime;
}
