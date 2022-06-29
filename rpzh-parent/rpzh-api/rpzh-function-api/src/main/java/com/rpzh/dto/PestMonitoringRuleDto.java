package com.rpzh.dto;

import lombok.Data;

import java.util.List;

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
    private String specialStack;
}
