package com.lyny.nettyServer.config;


import com.lyny.nettyServer.handler.DataDecoder;
import com.lyny.nettyServer.handler.DataEncoder;
import com.lyny.nettyServer.handler.MessageHandler;
import com.lyny.nettyServer.handler.ServerIdleStateHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    @Autowired
    private MessageHandler messageHandler;

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new ServerIdleStateHandler());//空闲监测
        pipeline.addLast(new DataDecoder());
        pipeline.addLast(new DataEncoder());
        pipeline.addLast(messageHandler);
    }
}
