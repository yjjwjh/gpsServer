package com.lyny.nettyServer.handler;



import com.lyny.nettyServer.dataFormat.SmartCarProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * netty粘包问题解决
 */
public class DataDecoder extends ByteToMessageDecoder {
    public final int BASE_LENGTH = 6;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf buffer, List<Object> out) throws Exception {
        if (buffer.readableBytes() == 0) {
            channelHandlerContext.close();//关闭连接
            return;//结束
        }
        // 读取data数据
        /*byte[] data1 = new byte[96];
        buffer.readBytes(data1);
        String ssss = Util.byteArrayToString(data1);*/
        //System.out.println(buffer.readableBytes());
        // 可读长度必须大于基本长度
        //  if (buffer.readableBytes() >= BASE_LENGTH) {
        // 防止socket字节流攻击
        // 防止，客户端传来的数据过大
        // 因为，太大的数据，是不合理的
        if (buffer.readableBytes() > 2048) {
            buffer.skipBytes(buffer.readableBytes());
        }
        // 记录包头开始的index
        int beginReader;
        //记录协议开始标记
        byte beginFlag;
        byte twoFlag;
        while (true) {
            // 获取包头开始的index
            beginReader = buffer.readerIndex();
            // 标记包头开始的index
            buffer.markReaderIndex();
            // 读到了协议的开始标志，结束while循环
            if ((beginFlag = buffer.readByte()) == ConstantValue.HEAD_DATA && (twoFlag=buffer.readByte()) == ConstantValue.HEAD_DATA1) {
                break;
            }
            // 未读到包头，略过一个字节
            // 每次略过，一个字节，去读取，包头信息的开始标记
            buffer.resetReaderIndex();
            buffer.readByte();

            // 当略过，一个字节之后，
            // 数据包的长度，又变得不满足
            // 此时，应该结束。等待后面的数据到达
            if (buffer.readableBytes() < BASE_LENGTH) {
                return;
            }
        }
        //字节转int
        byte lengthBuffer=buffer.readByte();//内容长度字节（有些协议无意义）
        byte agrNumByte=buffer.readByte();//协议号（根据协议号进行数据截取）
        int agrNum=agrNumByte & 0xff;//协议号
        int length;
        byte[] data;
        byte[] dataAdd;
        byte lbsByte;
        SmartCarProtocol protocol;
        switch (agrNum){
            case 0x01://登录
                length = 9+2;
                break;
            case 0x10://gps定位数据包
                length=18+3+2;
                break;
            case 0x11://离线gps定位数据包
                length=18+3+2;
                break;
            case 0x13://状态包
                length=5+2;
                break;
            case 0x17://离线wifi
                //wifi
                length=(lengthBuffer & 0xff)*7+6;
                // 判断请求数据包数据是否到齐
                if (buffer.readableBytes() < length) {
                    // 还原读指针
                    buffer.readerIndex(beginReader);
                    return;
                }
                // 读取data数据
                data = new byte[length];
                buffer.readBytes(data);

                lbsByte=buffer.readByte();
                length+=(lbsByte & 0xff)*5+1+3+1+2;
                // 还原读指针
                buffer.readerIndex(beginReader+4);
                break;
            case 0x69://离线wifi
                //wifi
                length=(lengthBuffer & 0xff)*7+6;
                // 判断请求数据包数据是否到齐
                if (buffer.readableBytes() < length) {
                    // 还原读指针
                    buffer.readerIndex(beginReader);
                    return;
                }
                // 读取data数据
                data = new byte[length];
                buffer.readBytes(data);
                lbsByte=buffer.readByte();
                length+=(lbsByte & 0xff)*5+1+3+1+2;
                // 还原读指针
                buffer.readerIndex(beginReader+4);
                break;
            case 0x81://充电完成 长度为01，充电报警 长度为02
                length=1;
                // 判断请求数据包数据是否到齐
                if (buffer.readableBytes() < length) {
                    // 还原读指针
                    buffer.readerIndex(beginReader);
                    return;
                }
            default:
                length = (lengthBuffer & 0xff)+1;
        }
        // 判断请求数据包数据是否到齐
        if (buffer.readableBytes() < length) {
            // 还原读指针
            buffer.readerIndex(beginReader);
            return;
        }
        // 读取data数据
        data = new byte[length];
        buffer.readBytes(data);
        dataAdd=new byte[length+3];
        System.arraycopy(data,0,dataAdd,3,length);
        dataAdd[0]=twoFlag;//第2个字节，头中第二个
        dataAdd[1]=lengthBuffer;//第三个字节，长度
        dataAdd[2]=agrNumByte;//第四个字节，协议号
        protocol = new SmartCarProtocol(beginFlag, dataAdd);
        out.add(protocol);
    }
}
