package com.lyny.nettyServer.handler;


import com.lyny.nettyServer.dataFormat.SmartCarProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * netty粘包问题解决
 */
public class DataEncoder extends MessageToByteEncoder<SmartCarProtocol> {


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, SmartCarProtocol smartCarProtocol, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(smartCarProtocol.getContent());
    }
}
