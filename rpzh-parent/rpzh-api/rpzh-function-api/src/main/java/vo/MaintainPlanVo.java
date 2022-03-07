package vo;

import lombok.Data;

/**
 * @author wbl
 * @description [返回给前段的信息]
 */
@Data
public class MaintainPlanVo {
    /**
     * 批次号
     */
    private String materialBatchNum;
    /**
     * 烟叶名称
     */
    private String materialName;

    /**
     * 养护阶段
     */
    private String maintainStage;
    /**
     * 位置
     */
    private String siteName;
    /**
     * 申请时间
     */
    private String applyTime;
}
