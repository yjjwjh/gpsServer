package com.lyny.nettyServer.handler;


import com.lyny.mapper.GPS_deviceMapper;
import com.lyny.nettyServer.dataFormat.SmartCarProtocol;
import com.lyny.pojo.GPS_device;
import com.lyny.service.tcpService.impl.TCPService;
import com.lyny.unit.Util;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by yjj on 2021-02-01
 */
@ChannelHandler.Sharable
@Component
@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<SmartCarProtocol> {
    @Autowired
    private TCPService tcpService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    private GPS_deviceMapper gps_deviceMapper;

    /**
     * 服务器接收到消息
     * @param channelHandlerContext
     * @param smartCarProtocol
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SmartCarProtocol smartCarProtocol) throws Exception {
        //接收到终端的消息
        //SmartCarProtocol protocol = (SmartCarProtocol) msg;
        //获取字节数组
        byte[] bytes = objectToArray(smartCarProtocol);
        //校验
        boolean isTrue = tcpService.isAnalyze(bytes);
        if (!isTrue) {
            log.info("校验失败，头尾不是约定的，非法数据帧：" + Util.byteArrayToString(bytes));
            return;
        }
        //数据解析
        tcpService.dataParsing(bytes, channelHandlerContext);

    }

    /**
     * 设备自主下线
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("设备链接断开：{}", ctx.channel().remoteAddress());
        redisTemplate.boundSetOps("sessionId").remove(ctx.channel().id().asLongText());//删除会话
        GPS_device gpsDevice = (GPS_device) redisTemplate.boundHashOps("device").get(ctx.channel().id().asLongText());
        if(gpsDevice!=null){
            //修改设备状态信息
            gps_deviceMapper.updateStatusById(0,gpsDevice.getId());
            redisTemplate.boundHashOps("channel").delete(String.valueOf(gpsDevice.getId()));//删除连接通道
        }

        redisTemplate.boundHashOps("device").delete(ctx.channel().id().asLongText());//删除对象

    }

    /**
     * 新连接
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("链接创建：{}", ctx.channel().remoteAddress());
    }

    /**
     * 当Netty由于IO错误或者处理器在处理事件时抛出异常时调用
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        redisTemplate.boundSetOps("sessionId").remove(ctx.channel().id().asLongText());//删除会话
        GPS_device gpsDevice = (GPS_device) redisTemplate.boundHashOps("device").get(ctx.channel().id().asLongText());
        //修改设备状态信息
        if(gpsDevice!=null){
            //修改设备状态信息
            gps_deviceMapper.updateStatusById(0,gpsDevice.getId());
            redisTemplate.boundHashOps("channel").delete(String.valueOf(gpsDevice.getId()));//删除连接通道
        }
        redisTemplate.boundHashOps("device").delete(ctx.channel().id().asLongText());//删除对象
        Channel channel = ctx.channel();
        if(!channel.isActive()){
            log.info("############### -- 客户端 -- "+ channel.remoteAddress()+ "  断开了连接！");
            cause.printStackTrace();
            ctx.close();
        }else{
            log.info("连接异常！");
        }
    }

    /**
     * 粘包对象转字节数组
     *
     * @param protocol
     * @return
     */
    private byte[] objectToArray(SmartCarProtocol protocol) {
        byte[] content = protocol.getContent();
        byte head_data = protocol.getHead_data();
        byte[] bytes = new byte[content.length + 1];
        bytes[0] = head_data;
        for (int i = 0; i < content.length; i++) {
            bytes[i + 1] = content[i];
        }
        return bytes;
    }
}
