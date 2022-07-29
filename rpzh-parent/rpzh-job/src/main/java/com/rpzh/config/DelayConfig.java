package com.rpzh.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMq配置类
 * @Author liubc
 * @create 2022-06-06 11:29
 * @Description: good good study,day day up
 */
@Configuration
public class DelayConfig {

    /***
     * 创建Queue1,过期队列
     */
    @Bean
    public Queue queue1(){
        return QueueBuilder.durable("queue1")   //队列名字
                .withArgument("x-dead-letter-exchange","delayExchange")  //消息进入死信队列后，将消息发送给指定的交换机
                .withArgument("x-dead-letter-routing-key","queue2")
                .build();
    }

    /***
     * 创建Queue2,真正读取消息的队列
     */
    @Bean
    public Queue queue2(){
        return new Queue("queue2");
    }

    /***
     * 创建交换机
     */
    @Bean
    public DirectExchange delayExchange(){
        return new DirectExchange("delayExchange");
    }


    /**
     * 绑定Queue2
     */
    @Bean
    public Binding bindQueu2DelayExchange(Queue queue2, Exchange delayExchange){
        return BindingBuilder.bind(queue2).to(delayExchange).with("queue2").noargs();
    }
}