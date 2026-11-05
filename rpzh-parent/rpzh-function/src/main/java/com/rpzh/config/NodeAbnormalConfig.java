package com.rpzh.config;

import com.rpzh.dao.EarlyWarningDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 设备异常状态判定阈值，支持两层配置：
 * 1) bootstrap-{profile}.yml 的 node.abnormal.*（改配置后需重启）
 * 2) tb_general_setting 中 setting_key='nodeAbnormal' 的同名行（优先级更高，改库即生效，无需重启）
 *
 * <p>可在库中执行以下语句覆盖默认值：
 * insert into tb_general_setting(setting_key, setting_value, setting_name)
 * values('nodeAbnormal','48','timeoutHours'),('nodeAbnormal','3','maxErrorCount');</p>
 */
@Component
@Slf4j
public class NodeAbnormalConfig {

    @Value("${node.abnormal.enabled:true}")
    private boolean enabled;

    /** 超过该小时数未收到已入垛设备的数据，判定为异常 */
    @Value("${node.abnormal.timeout-hours:48}")
    private int timeoutHours;

    /** 设备连续发出异常数据达到该次数，判定为异常 */
    @Value("${node.abnormal.max-error-count:3}")
    private int maxErrorCount;

    @Autowired(required = false)
    private EarlyWarningDao earlyWarningDao;

    private Integer overrideInt(String name, int fallback) {
        if (earlyWarningDao == null) {
            return fallback;
        }
        try {
            String value = earlyWarningDao.findSettingValue("nodeAbnormal", name);
            if (value != null && !value.trim().isEmpty()) {
                return Integer.parseInt(value.trim());
            }
        } catch (NumberFormatException e) {
            log.warn("nodeAbnormal.{} 配置值不是整数，回退到 {}", name, fallback);
        } catch (Exception e) {
            log.warn("读取 nodeAbnormal.{} 配置失败，回退到 {}：{}", name, fallback, e.getMessage());
        }
        return fallback;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getTimeoutHours() {
        return Math.max(1, overrideInt("timeoutHours", timeoutHours));
    }

    public int getMaxErrorCount() {
        return Math.max(1, overrideInt("maxErrorCount", maxErrorCount));
    }
}
