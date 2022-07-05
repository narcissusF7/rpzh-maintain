package com.rpzh.pojo;

import lombok.Data;

/**
 * @Author: ljj
 * @Date: 2022/05/18/15:30
 * @Description: 通用数据表数据实体类
 */
@Data
public class GeneralSetting {
    //主键id
    private Integer id;
    private String settingKey;
    private String settingValue;
    private String settingName;
}
