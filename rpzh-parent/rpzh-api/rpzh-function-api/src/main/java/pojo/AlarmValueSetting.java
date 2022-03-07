package pojo;

import cn.hutool.core.date.DateUtil;
import dto.MaintainPlanDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author wbl
 * 预警信息设置
 */
@Data
@NoArgsConstructor
public class AlarmValueSetting {

    public AlarmValueSetting(MaintainPlanDto maintainPlanDto) {
        this.earlyWarningRuleName = DateUtil.now() + "_某某垛";
        this.phases = "01";
        this.maintenceStyle = "01";
        this.oxygenUpperOne = maintainPlanDto.getOxygenUpperLimit()[0];
        this.oxygenUpperTwo = maintainPlanDto.getOxygenUpperLimit()[1];
        this.oxygenUpperThree = maintainPlanDto.getOxygenUpperLimit()[2];
        this.oxygenLower = maintainPlanDto.getOxygenLowerLimit();
        this.concentrationCo2UpperOne = maintainPlanDto.getCarbonDioxideUpperLimit()[0];
        this.concentrationCo2UpperTwo = maintainPlanDto.getCarbonDioxideUpperLimit()[1];
        this.concentrationCo2UpperThree = maintainPlanDto.getCarbonDioxideUpperLimit()[2];
        this.concentrationCo2Lower = maintainPlanDto.getCarbonDioxideLowerLimit();
        this.tempCoreChipUpperOne = maintainPlanDto.getTempCoreUpperLimit()[0];
        this.tempCoreChipUpperTwo = maintainPlanDto.getTempCoreUpperLimit()[1];
        this.tempCoreChipUpperThree = maintainPlanDto.getTempCoreUpperLimit()[2];
        this.tempCoreChipLower = maintainPlanDto.getTempCoreLowerLimit();
        this.wetCoreChipUpperOne = maintainPlanDto.getWetCoreUpperLimit()[0];
        this.wetCoreChipUpperTwo = maintainPlanDto.getWetCoreUpperLimit()[1];
        this.wetCoreChipUpperThree = maintainPlanDto.getWetCoreUpperLimit()[2];
        this.wetCoreChipLower = maintainPlanDto.getWetCoreLowerLimit();
        this.stackMoistureUpperOne = maintainPlanDto.getWetUpperLimit()[0];
        this.stackMoistureUpperTwo = maintainPlanDto.getWetUpperLimit()[1];
        this.stackMoistureUpperThree = maintainPlanDto.getWetUpperLimit()[2];
        this.stackMoistureLower = maintainPlanDto.getWetLowerLimit();
        this.stackTempUpperOne = maintainPlanDto.getTempUpperLimit()[0];
        this.stackTempUpperTwo = maintainPlanDto.getTempUpperLimit()[1];
        this.stackTempUpperThree = maintainPlanDto.getTempUpperLimit()[2];
        this.stackTempLowerOne = maintainPlanDto.getTempLowerLimit();
        this.createdTime = DateUtil.date();
        this.year = maintainPlanDto.getYearOfTobacco();
        this.materialCode = maintainPlanDto.getTobaccoCode();
        this.materialName = maintainPlanDto.getTobaccoName();
        this.createdUserId = maintainPlanDto.getPrincipalId();
    }

    private Integer id;
    private String earlyWarningRuleName;
    private String phases;
    private String maintenceStyle;
    private Double oxygenUpperOne;
    private Double oxygenUpperTwo;
    private Double oxygenUpperThree;
    private Double oxygenLower;
    private Integer concentrationCo2UpperOne;
    private Integer concentrationCo2UpperTwo;
    private Integer concentrationCo2UpperThree;
    private Integer concentrationCo2Lower;
    private Double tempCoreChipUpperOne;
    private Double tempCoreChipUpperTwo;
    private Double tempCoreChipUpperThree;
    private Double tempCoreChipLower;
    private Double wetCoreChipUpperOne;
    private Double wetCoreChipUpperTwo;
    private Double wetCoreChipUpperThree;
    private Double wetCoreChipLower;
    private Double stackTempUpperOne;
    private Double stackTempUpperTwo;
    private Double stackTempUpperThree;
    private Double stackTempLowerOne;
    private Integer siteId;
    private Double warehouseTempUpperOne;
    private Double warehouseTempUpperTwo;
    private Double warehouseTempUpperThree;
    private Double warehouseTempLower;
    private Date createdTime;
    private Date lastUpdatedTime;
    private Integer createdUserId;
    private Integer lastUpdatedUserId;
    private Double stackMoistureUpperOne;
    private Double stackMoistureUpperTwo;
    private Double stackMoistureUpperThree;
    private Double stackMoistureLower;
    private Double warehouseMoistureUpperOne;
    private Double warehouseMoistureUpperTwo;
    private Double warehouseMoistureUpperThree;
    private Double warehouseMoistureLower;
    private String year;
    private String materialCode;
    private String materialName;
    private String materialBatchNumber;
    private Integer insectUpperOne;
    private Integer insectUpperTwo;
    private Integer insectUpperThree;
    private Double phosphineUpperOne;
    private Double phosphineUpperTwo;
    private Double phosphineUpperThree;
    private Double phosphineLower;
}
