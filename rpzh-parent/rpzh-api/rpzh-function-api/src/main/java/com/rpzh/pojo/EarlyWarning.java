package com.rpzh.pojo;

import lombok.Data;

@Data
public class EarlyWarning {
    private Integer id;
    private String warningType;
    private String nodeId;
    private String nodeType;
    private Double warningValOne;
    private String warningTimeOne;
    private Double warningValTwo;
    private String warningTimeTwo;
    private Double warningValThree;
    private String warningTimeThree;
    private Integer principalId;
    private String principalName;
    private Integer warningGrade;
    private Integer isAffirm;
    private String dealSuggestion;
    private String remark;
    private String dealTime;
    private Integer stackId;
    private Integer areaId;

    public EarlyWarning(){

    }

    public EarlyWarning(Integer id, String warningType, String nodeId, String nodeType, Double warningValOne, String warningTimeOne, Double warningValTwo, String warningTimeTwo, Double warningValThree, String warningTimeThree, Integer principalId, String principalName, Integer warningGrade, Integer isAffirm, String dealSuggestion, String remark, String dealTime, Integer stackId, Integer areaId) {
        this.id = id;
        this.warningType = warningType;
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.warningValOne = warningValOne;
        this.warningTimeOne = warningTimeOne;
        this.warningValTwo = warningValTwo;
        this.warningTimeTwo = warningTimeTwo;
        this.warningValThree = warningValThree;
        this.warningTimeThree = warningTimeThree;
        this.principalId = principalId;
        this.principalName = principalName;
        this.warningGrade = warningGrade;
        this.isAffirm = isAffirm;
        this.dealSuggestion = dealSuggestion;
        this.remark = remark;
        this.dealTime = dealTime;
        this.stackId = stackId;
        this.areaId = areaId;
    }

    public EarlyWarning(String warningType, String nodeId, String nodeType, Double warningValOne, Integer warningGrade) {
        this.warningType = warningType;
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.warningValOne = warningValOne;
        this.warningGrade = warningGrade;
    }
    public EarlyWarning(String warningType, String nodeId, String nodeType, Integer warningGrade) {
        this.warningType = warningType;
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.warningGrade = warningGrade;
    }
}
