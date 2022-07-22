package com.rpzh.config;

import com.rpzh.properties.MqttProperties;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.Resource;

/**
 * @author wbl
 * @version 0.1.0
 * @create 2021-06-28 14:36
 * @description mqtt配置
 */
@Configuration
@EnableConfigurationProperties(MqttProperties.class)
@Slf4j
@EnableAsync
public class MqttConfiguration {
    @Resource
    private MqttProperties properties;

    /**
     * MQTT连接配置
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        //连接参数
        MqttConnectOptions options = new MqttConnectOptions();
        //连接地址
        options.setServerURIs(new String[]{properties.getHost() + ":" + properties.getPort()});
        if (!"".equals(properties.getUsername())) {
            //用户名
            options.setUserName(properties.getUsername());
        }
        if (!"".equals(properties.getPassword())) {
            //密码
            options.setPassword(properties.getPassword().toCharArray());
        }
        //心跳时间
        options.setKeepAliveInterval(properties.getKeepalive());
        //断开是否自动重联
        options.setAutomaticReconnect(properties.isAutomaticReconnect());
        //保持session
        options.setCleanSession(properties.isCleanSession());
        factory.setConnectionOptions(options);
        return factory;
    }

    /**
     * 接收通道
     */
    @Bean
    public MessageChannel mqttInputChannelHumiture() {
        return new DirectChannel();
    }


    /**
     * 配置client,监听的topic  入站 接受数据
     */
    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(properties.getClientId() + "_inbound", mqttClientFactory(),
                        properties.getRxTopic());
        adapter.setCompletionTimeout(properties.getTimeout());
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(properties.getQos());
        adapter.setOutputChannel(mqttInputChannelHumiture());
        return adapter;
    }


}
