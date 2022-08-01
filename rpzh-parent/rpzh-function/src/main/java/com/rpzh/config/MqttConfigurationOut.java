package com.rpzh.config;

import com.rpzh.properties.MqttProperties;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.Resource;

/**
 * @author wbl
 * @version 0.1.0
 * @create 2021-06-28 14:36
 * @description mqtt出站配置
 */
@Configuration
@EnableConfigurationProperties(MqttProperties.class)
@Slf4j
@EnableAsync
public class MqttConfigurationOut {
    @Resource
    private MqttProperties properties;

    /**
     * MQTT连接配置
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactoryOut() {
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
     * 输出通道
     */
    @Bean
    public MessageChannel mqttOutboundChannelMaintain() {
        return new DirectChannel();
    }


    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannelMaintain")
    public MessageHandler mqttOutAdapter(@Qualifier("mqttClientFactoryOut") MqttPahoClientFactory connectionFactory) {
        //创建一个新的出站管道，由于MQTT的发布与订阅是两个独立的连接，因此客户端的ID(即APPID）不能与订阅时所使用的ID一样，否则在服务端会认为是同一个客户端，而造成连接失败
        MqttPahoMessageHandler outGate = new MqttPahoMessageHandler(properties.getClientId() + "_put", connectionFactory);
        DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter();
        //bytes类型接收
        converter.setPayloadAsBytes(true);
        outGate.setAsync(true);
        //设置连接超时时时
        outGate.setCompletionTimeout(properties.getTimeout());
        //设置通信质量
        outGate.setDefaultQos(properties.getQos());
        outGate.setConverter(converter);
        return outGate;
    }
}
