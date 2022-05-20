package com.rpzh.dto;

import lombok.Data;

/**
 * @Author: zyq
 * @Date: 2022/05/19/14:04
 * @Description: 预警传参
 */
@Data
public class EarlyWarningDto {
    private String warningType;
    private String principalName;
    private String startTime;
    private String endTime;
}
