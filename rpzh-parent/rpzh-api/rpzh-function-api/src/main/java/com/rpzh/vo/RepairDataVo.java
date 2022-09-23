package com.rpzh.vo;

import lombok.Data;

/**
 * @Author: zyq
 * @Date: 2022/07/25/18:51
 * @Description: 功能
 */
@Data
public class RepairDataVo {
    private String deviceId;
    private String deviceType;
    private String repairReason;
    private String siteName;
    private String nodeName;
    private String siteX;
    private String siteY;
    private String siteZ;
}
