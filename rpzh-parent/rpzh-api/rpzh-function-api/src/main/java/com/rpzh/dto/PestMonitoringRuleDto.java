package com.rpzh.dto;

import lombok.Data;

@Data
public class PestMonitoringRuleDto {
    private Integer[] weekPatrol;
    private Integer valueOne;
    private Integer valueTwo;
    private Integer valueThree;
    private String colorOne;
    private String colorTwo;
    private String colorThree;
    private Integer replacePeriod;
    private String[] siteRoutes;
    private Integer[] siteIds;
    private String[] siteNames;
    private String[] stackWarningValues;
    private String[] delIds;
}
