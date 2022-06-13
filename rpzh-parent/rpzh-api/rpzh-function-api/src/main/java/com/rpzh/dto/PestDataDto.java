package com.rpzh.dto;

import lombok.Data;

@Data
public class PestDataDto {
    private String pestBoardId;
    private Integer page =1;
    private Integer size =10;
    private String beginTime;
    private String endTime;
    private String siteType;
    private Integer beetleCount;
    private Integer mealwormCount;
    private String gatherSite;
    private String siteRoute;
    private Integer alertLevel;
    private String rfid;
}
