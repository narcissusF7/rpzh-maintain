
package com.rpzh.pojo;

import lombok.Data;

@Data
public class PestData {
    private  Integer id;
    private  String pestBoardId;
    private  String gatherSite;
    private  String gatherTime;
    private  Integer beetleCount;
    private  Integer mealwormCount;
    private  Integer alertLevel;
    private  Integer beetleLevel;
    private  Integer mealwormLevel;

    public PestData() {
    }

    public PestData(String pestBoardId, String gatherSite, String gatherTime ,Integer alertLevel, Integer beetleCount,Integer beetleLevel, Integer mealwormCount,Integer mealwormLevel) {
        this.pestBoardId = pestBoardId;
        this.gatherSite = gatherSite;
        this.gatherTime = gatherTime;
        this.beetleCount = beetleCount;
        this.mealwormCount = mealwormCount;
        this.alertLevel = alertLevel;
        this.beetleLevel = beetleLevel;
        this.mealwormLevel = mealwormLevel;

    }

    public PestData(String pestBoardId, String gatherSite, String gatherTime, Integer beetleCount,Integer alertLevel) {
        this.pestBoardId = pestBoardId;
        this.gatherSite = gatherSite;
        this.gatherTime = gatherTime;
        this.beetleCount = beetleCount;
        this.alertLevel = alertLevel;
        this.beetleLevel = alertLevel;
    }
}
