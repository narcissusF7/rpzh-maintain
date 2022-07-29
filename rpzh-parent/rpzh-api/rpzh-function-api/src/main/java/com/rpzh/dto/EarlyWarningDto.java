package com.rpzh.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * @author liujj
 * @since 2022-04-29
 */
@Data
public class EarlyWarningDto implements Serializable {

    private static final long serialVersionUID = 1L;

    //日期数据
    List<String> days;

    private Integer earlyWarningId;

    private String warningType;

    private String nodeType;

    private String nodeId;

    private String pidRoute;

    private BigDecimal warningVal;

    private LocalDateTime warningTime;

    private String siteName;

    private Date startTime;

    private Date endTime;

    private String materialCode;

    private Integer principalId;

    private String beginTime;
    private String finishTime;

    private String principalName;

    private String warningGrade;

    private String isAffirm;

    private String dealSuggestion;

    private Integer stackId;

    private Integer areaId;

    private List<Integer> stackIds;

    private Integer siteId;

    private Integer maintainPlanId;

    private Integer count;

    // 时间类型,1:日,2:周,3:月,4:年
    private Integer dayType;

    //分页开始页面
    private int page=1;

    //分页每页显示
    private int size=10;


}
