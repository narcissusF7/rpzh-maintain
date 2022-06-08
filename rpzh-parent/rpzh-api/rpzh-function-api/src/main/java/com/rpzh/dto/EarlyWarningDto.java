package com.rpzh.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author liujj
 * @since 2022-04-29
 */
@Data
public class EarlyWarningDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer earlyWarningId;

    private String warningType;

    private Integer nodeId;

    private BigDecimal warningVal;

    private LocalDateTime warningTime;

    private String startTime;

    private String endTime;

    private String materialCode;

    private String materialName;

    private Integer principalId;

    private String principalName;

    private String warningGrade;

    private String isAffirm;

    private String dealSuggestion;

    private Integer stackId;

    private Integer maintainPlanId;

    //分页开始页面
    private int page=1;

    //分页每页显示
    private int size=10;


}
