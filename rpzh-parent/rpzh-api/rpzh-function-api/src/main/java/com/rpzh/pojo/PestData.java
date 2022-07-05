
package com.rpzh.pojo;

import lombok.Data;
/**
 * @Author: ljj
 * @Date: 2022/05/18/15:30
 * @Description: 虫情数据实体类
 */
@Data
public class PestData {
    //主键id
    private  Integer id;
    //虫情点位id
    private  String pestBoardId;
    //采集位置
    private  String gatherSite;
    //采集时间
    private  String gatherTime;
    //甲虫数量
    private  Integer beetleCount;
    //粉螟数量
    private  Integer mealwormCount;
    //预警等级
    private  Integer alertLevel;
    //甲虫预警等级
    private  Integer beetleLevel;
    //粉螟预警等级
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
