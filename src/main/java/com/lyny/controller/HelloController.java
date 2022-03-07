package com.lyny.controller;

import com.alibaba.fastjson.JSON;
import com.lyny.pojo.TestPojo;
import com.lyny.service.mqttService.MqttGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by yjj on 2021-03-05
 */
@RestController
public class HelloController {



    @Autowired
    private MqttGateway mqttGateway;
    @RequestMapping("/hello")
    public String hello()
    {

        return "你好！";
    }

    @RequestMapping("/sendMqtt")
    public String sendMqtt(String topic,String  sendData){
        mqttGateway.sendToMqtt(sendData,topic);
        return "OK";
    }

    @RequestMapping("/sendMqtt3")
    public String sendMqtt3(String topic,String key1,String key2){
        TestPojo testPojo=new TestPojo();
        testPojo.setKey1(key1);
        testPojo.setKey2(key2);
        String sendValue = JSON.toJSONString(testPojo);
        mqttGateway.sendToMqtt(sendValue,topic);
        return "OK";
    }
    @RequestMapping("/sendMqtt1")
    public String sendMqtt1(String topic,String  sendData){

        //发送的消息
        Message message = MessageBuilder.withPayload(topic+sendData)
                //发送的主题
                .setHeader(MqttHeaders.TOPIC, "topic1").build();
        mqttGateway.sendMessage(message);
        return "OK";
    }
    @RequestMapping("/sendMqtt2")
    public String sendMqtt2(String topic,String  sendData){


        mqttGateway.sendToMqtt(topic+sendData,MqttHeaders.TOPIC);
        return "OK";
    }

}
