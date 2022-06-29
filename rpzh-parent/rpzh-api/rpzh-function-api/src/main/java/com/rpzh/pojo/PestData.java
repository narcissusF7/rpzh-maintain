
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
    private  Integer mealwormLevel;

    public PestData() {
    }

    public PestData(String pestBoardId, String gatherSite, String gatherTime, Integer beetleCount, Integer mealwormCount,Integer alertLevel) {
        this.pestBoardId = pestBoardId;
        this.gatherSite = gatherSite;
        this.gatherTime = gatherTime;
        this.beetleCount = beetleCount;
        this.mealwormCount = mealwormCount;
        this.alertLevel = alertLevel;

    }

    public PestData(String pestBoardId, String gatherSite, String gatherTime, Integer beetleCount,Integer alertLevel) {
        this.pestBoardId = pestBoardId;
        this.gatherSite = gatherSite;
        this.gatherTime = gatherTime;
        this.beetleCount = beetleCount;
        this.alertLevel = alertLevel;
    }

    public PestData(Integer id, String pestBoardId, String gatherSite, String gatherTime, Integer beetleCount, Integer mealwormCount ,Integer alertLevel,Integer mealwormLevel) {
        this.id = id;
        this.pestBoardId = pestBoardId;
        this.gatherSite = gatherSite;
        this.gatherTime = gatherTime;
        this.beetleCount = beetleCount;
        this.mealwormCount = mealwormCount;
        this.alertLevel = alertLevel;
        this.mealwormLevel = mealwormLevel;
    }
}
