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
    private  Integer alartLevel;

    public PestData() {
    }

    public PestData(String pestBoardId, String gatherSite, String gatherTime, Integer beetleCount, Integer mealwormCount,Integer alartLevel) {
        this.pestBoardId = pestBoardId;
        this.gatherSite = gatherSite;
        this.gatherTime = gatherTime;
        this.beetleCount = beetleCount;
        this.mealwormCount = mealwormCount;
        this.alartLevel = alartLevel;
    }

    public PestData(String pestBoardId, String gatherSite, String gatherTime, Integer beetleCount,Integer alartLevel) {
        this.pestBoardId = pestBoardId;
        this.gatherSite = gatherSite;
        this.gatherTime = gatherTime;
        this.beetleCount = beetleCount;
        this.alartLevel = alartLevel;
    }

    public PestData(Integer id, String pestBoardId, String gatherSite, String gatherTime, Integer beetleCount, Integer mealwormCount ,Integer alartLevel) {
        this.id = id;
        this.pestBoardId = pestBoardId;
        this.gatherSite = gatherSite;
        this.gatherTime = gatherTime;
        this.beetleCount = beetleCount;
        this.mealwormCount = mealwormCount;
        this.alartLevel = alartLevel;
    }
}
