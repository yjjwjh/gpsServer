package com.lyny.service.mqttService;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Created by yjj on 2021-02-02
 */
@Component
@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface MqttGateway {
    /**
     * 发送信息
     * @param data
     */
    void sendToMqtt(String data, @Header(MqttHeaders.RETAINED) boolean retain);

    /**
     * 指定主题发送信息
     * @param topic
     * @param payload
     */
    void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, String payload, @Header(MqttHeaders.RETAINED) boolean retain);

    void sendToMqtt(String data, @Header(MqttHeaders.TOPIC) String topic);

    void sendMessage(Message<?> message);
    /**
     * 指定主题和qos发送信息
     * @param topic
     * @param qos
     * @param payload
     */
    void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, @Header(MqttHeaders.QOS) int qos, String payload, @Header(MqttHeaders.RETAINED) boolean retain);
}
