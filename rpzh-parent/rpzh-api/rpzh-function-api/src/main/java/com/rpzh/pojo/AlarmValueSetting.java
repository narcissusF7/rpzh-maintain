package com.rpzh.pojo;

import cn.hutool.core.date.DateUtil;
import com.rpzh.dto.MaintainPlanDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author wbl
 * 预警信息设置
 */
@Data
public class AlarmValueSetting {
    private Integer id;
    private String earlyWarningRuleName;
    private String phases;
    private String maintenceStyle;

    private Double stackOxygenUpperOne;
    private Double stackOxygenUpperTwo;
    private Double stackOxygenUpperThree;
    private Double stackOxygenLower;

    private Double warehouseOxygenUpperOne;
    private Double warehouseOxygenUpperTwo;
    private Double warehouseOxygenUpperThree;
    private Double warehouseOxygenLower;

    private Double wetCoreChipUpperOne;
    private Double wetCoreChipUpperTwo;
    private Double wetCoreChipUpperThree;
    private Double wetCoreChipLower;

    private Double tempCoreChipUpperOne;
    private Double tempCoreChipUpperTwo;
    private Double tempCoreChipUpperThree;
    private Double tempCoreChipLower;

    private Double stackTempUpperOne;
    private Double stackTempUpperTwo;
    private Double stackTempUpperThree;
    private Double stackTempLower;

    private Double stackMoistureUpperOne;
    private Double stackMoistureUpperTwo;
    private Double stackMoistureUpperThree;
    private Double stackMoistureLower;

    private Double warehouseTempUpper0ne;
    private Double warehouseTempUpperTwo;
    private Double warehouseTempUpperThree;
    private Double warehouseTempLower;

    private Double warehouseMoistureUpperOne;
    private Double warehouseMoistureUpperTwo;
    private Double warehouseMoistureUpperThree;
    private Double warehouseMoistureLower;

    private Date createdTime;
    private Date updatedTime;
    private String createdUser;
    private String updatedUser;
    private String year;
    private String materialName;
    private String materialCode;
    private String materialBatchNumber;

    private Integer status;
    private Integer alertUpgradeDay;
    private String earlyWarningColourOne;
    private String earlyWarningColourTwo;
    private String earlyWarningColourThree;
}
