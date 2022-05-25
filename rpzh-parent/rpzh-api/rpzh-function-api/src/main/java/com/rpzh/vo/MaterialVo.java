package com.rpzh.vo;

import lombok.Data;

/**
 * @Author: zyq
 * @Date: 2022/05/06/11:36
 * @Description: 功能
 */
@Data
public class MaterialVo {
    private String materialCode;
    private String materialBatchNum;
    private String tobaccoYear;
    private Double maintainCount;
    private String principalName;
    private Integer principalId;
    private Integer stackId;
}
