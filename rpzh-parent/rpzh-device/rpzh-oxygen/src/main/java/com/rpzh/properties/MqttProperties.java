package com.rpzh.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author wbl
 * @version 0.1.0
 * @create 2021-06-28 14:40
 * @description mqtt属性配置
 */
@ConfigurationProperties(prefix = "mqtt")
@Data
public class MqttProperties {
    /**
     * 客户端Id
     */
    private String clientId;

    /**
     * ip
     */
    private String host="localhost";
    /**
     * 端口
     */
    private int port;

    public void setPort(int port) {
        this.port = port;
    }
    /**
     * 接收主题
     */
    private String rxTopic;
    /**
     * 发布主题
     */
    private String txTopic;
    /**
     * Qos
     */
    private byte qos;

    public void setQos(byte qos) {
        this.qos = qos;
    }

    /**
     * 用户名
     */
    private String username = "";
    /**
     * 密码
     */
    private String password = "";
    /**
     * 超时
     */
    private int timeout;
    /**
     * 心跳
     */
    private int keepalive;
    /**
     * 是否自动重联
     */
    private boolean automaticReconnect;
    /**
     *  是否保存Session
     */
    private boolean cleanSession;

}
