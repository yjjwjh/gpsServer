package com.lyny.service.tcpService.impl;

import com.lyny.pojo.SendData;
import com.lyny.pojo.WhiteList;
import com.lyny.service.tcpService.SendClientDataService;
import com.lyny.unit.Util;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

/**
 * Created by yjj on 2021-03-05
 */
@Slf4j
@Service
public class SendClientDataServiceImpl implements SendClientDataService {

    /**
     * 登录回复 协议号x01成功 0x44失败  内容 无
     *
     * @param channelHandlerContext
     */
    @Override
    public void sendLoginReply(ChannelHandlerContext channelHandlerContext, boolean isLogin) {
        SendData sendData = new SendData();
        sendData.setLength(1);
        sendData.setDataContent("");
        if (isLogin) {
            //登录成功
            sendData.setAgrNum(0x01);
        } else {
            //登录失败
            sendData.setAgrNum(0x44);
        }
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, "");
    }


    /**
     * 协议号：05  服务器向终端发送监管号状态
     * 内容：状态号status
     * 01：监管号拨打设备号码，设备自动接听 拾音效果
     * 02：监管号拨打设备号码，自动接听双向通话
     * 03：监管号拨打设备号码，响铃手动接听双向通话
     *
     * @param channelHandlerContext
     * @param status
     */
    @Override
    public void sendSuperviseNum(ChannelHandlerContext channelHandlerContext, int status, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(2);
        sendData.setAgrNum(5);
        sendData.setDataContent(Util.toHexString(status));
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 定位信息回复  协议号：0x10在线定位数据 ，0x11离线定位数据
     * 数据长度为0
     * 日期时间：0A03170F3217，年月日时分秒，每个占1byte，转换过来10年3月23日15时50分23秒，年份再加2000就是2010年，时间为GMT0。
     *
     * @param channelHandlerContext
     * @param agr
     */
    @Override
    public void sendLocationReply(ChannelHandlerContext channelHandlerContext, int agr, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(0);
        sendData.setAgrNum(agr);
        StringBuffer sb = new StringBuffer();
        Calendar now = Calendar.getInstance();
        sb.append(Util.toHexString(now.get(Calendar.YEAR) - 2000));//年
        sb.append(Util.toHexString(now.get(Calendar.MONTH) + 1));//月
        sb.append(Util.toHexString(now.get(Calendar.DAY_OF_MONTH)));//日
        sb.append(Util.toHexString(now.get(Calendar.HOUR_OF_DAY)));//时
        sb.append(Util.toHexString(now.get(Calendar.MINUTE)));//分
        sb.append(Util.toHexString(now.get(Calendar.SECOND)));//秒
        sendData.setDataContent(sb.toString());
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 服务器收到状态包回复信息，内容一样
     *
     * @param channelHandlerContext
     * @param bytes
     */
    @Override
    public void sendReplyStart(ChannelHandlerContext channelHandlerContext, byte[] bytes, String deviceName) {
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(bytes);
        channelHandlerContext.channel().writeAndFlush(buf);
        log.info("服务器--->设备 设备名称：" + deviceName + " 数据帧:" + Util.byteArrayToString(bytes));
    }

    /**
     * 服务器---->设备
     * 设置状态包上传间隔  协议号：0x13
     * 起始位 2byte 包长度 1byte 协议号 1byte 状态包上传间隔 1byte 结束位 2byte
     *
     * @param channelHandlerContext
     * @param time
     */
    @Override
    public void sendSetTimeInterval(ChannelHandlerContext channelHandlerContext, int time, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(2);
        sendData.setAgrNum(0x13);
        StringBuffer sb = new StringBuffer();
        sb.append(Util.toHexString(time));
        sendData.setDataContent(sb.toString());
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 服务器--->设备  协议号：0x13
     * 设置心跳包上传间隔  单位：秒  设置范围：20-600秒
     *
     * @param channelHandlerContext
     * @param time
     */
    @Override
    public void sendSetHeartbeatTime(ChannelHandlerContext channelHandlerContext, int time, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(3);
        sendData.setAgrNum(0x13);
        StringBuffer sb = new StringBuffer();
        sb.append(Util.toTwoHexString(time));
        sendData.setDataContent(sb.toString());
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 服务器--->设备  协议号：0x15
     * 恢复出厂设置
     *
     * @param channelHandlerContext
     */
    @Override
    public void sendFactoryDataReset(ChannelHandlerContext channelHandlerContext, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(1);
        sendData.setAgrNum(0x15);
        sendData.setDataContent("");
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 服务器--->设备  协议号：0x16
     * 白名单总数
     *
     * @param channelHandlerContext
     */
    @Override
    public void sendWhiteListLength(ChannelHandlerContext channelHandlerContext, int num, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(2);
        sendData.setAgrNum(0x16);
        sendData.setDataContent(Util.toHexString(num));
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 离线wifi和在线wifi数据回复
     * 协议号：0x17离线   0x69
     * 起始位2byte保留字节1byte 协议号1byte 时间 6byte（BCD编码）  结束位2byte
     *
     * @param channelHandlerContext
     */
    @Override
    public void sendWifiReply(ChannelHandlerContext channelHandlerContext, int agr, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(0);
        sendData.setAgrNum(agr);
        String dateStr = Util.date2BCD();
        sendData.setDataContent(dateStr);
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 系统返回当前GMT时间
     * 协议号：0x30
     *
     * @param channelHandlerContext
     */
    @Override
    public void sendGMTTime(ChannelHandlerContext channelHandlerContext, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(8);
        sendData.setAgrNum(0x30);
        StringBuffer sb = new StringBuffer();
        Calendar now = Calendar.getInstance();
        sb.append(Util.toTwoHexString(now.get(Calendar.YEAR)));//年
        sb.append(Util.toHexString(now.get(Calendar.MONTH) + 1));//月
        sb.append(Util.toHexString(now.get(Calendar.DAY_OF_MONTH)));//日
        sb.append(Util.toHexString(now.get(Calendar.HOUR_OF_DAY)));//时
        sb.append(Util.toHexString(now.get(Calendar.MINUTE)));//分
        sb.append(Util.toHexString(now.get(Calendar.SECOND)));//秒
        sendData.setDataContent(sb.toString());
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);

    }

    /**
     * 禁止LBS定位  协议号0x33
     * on：01  off：00
     *
     * @param channelHandlerContext
     * @param start
     */
    @Override
    public void sendLBSStart(ChannelHandlerContext channelHandlerContext, int start, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(2);
        sendData.setAgrNum(0x33);
        sendData.setDataContent(Util.toHexString(start));
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 服务器向终端发送GPS LBS开关机，上传数据开关时间
     * 协议号  0x34
     * 起始 2Byte , 包长度 1Byte , 协议号 1Byte  , gps 1Byte + data 5Byte + lbs 1Byte + on3Byte +　off 3Byte  , 停止位 2Byte
     *
     * @param channelHandlerContext
     * @param gpsStart              gps状态开关 1个字节  0n = 0x01
     * @param gpsStartTime          5字节的16进制字符串，第一个字节on=0x01、off=0x00 ，后面四个字节是2个时间  08001200==08:00 12:00   不是转为16进制  ，再例如：07002200==07:00 22:00
     * @param lbsStart              lbs 状态开关 1个字节 01=on
     * @param lbsOnTime             3个字节 第一个字节on=0x01（设置开机） off=0x00
     * @param lbsOffTime            3个字节 第一个字节off=0x01（设置关机） on=0x00 后面2个字节是时间  0700=07:00  2200=22:00
     */
    @Override
    public void sendGPSAndLBSUp(ChannelHandlerContext channelHandlerContext, int gpsStart, String gpsStartTime, int lbsStart, String lbsOnTime, String lbsOffTime, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(0x0e);
        sendData.setAgrNum(0x34);
        StringBuffer sb = new StringBuffer();
        sb.append(Util.toHexString(gpsStart));
        sb.append(gpsStartTime);
        sb.append(Util.toHexString(lbsStart));
        sb.append(lbsOnTime);
        sb.append(lbsOffTime);
        sendData.setDataContent(sb.toString());
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 远程监听号码 协议号：0x40   SOS号码 协议号：0x41   爸爸电话  协议号：0x42   妈妈号码 协议号：0x43
     *
     * @param channelHandlerContext
     * @param phoneNumStr           手机号 11个数 例如：13399996666
     */
    @Override
    public void sendPhone(ChannelHandlerContext channelHandlerContext, int agx, String phoneNumStr, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(1 + phoneNumStr.length());
        sendData.setAgrNum(agx);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < phoneNumStr.length(); i++) {
            sb.append(Util.toHexString(phoneNumStr.charAt(i)));
        }
        sendData.setDataContent(sb.toString());
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 停止数据上传  协议号 0x44
     * 禁止设备上传任何数据
     *
     * @param channelHandlerContext
     */
    @Override
    public void sendStopDataUp(ChannelHandlerContext channelHandlerContext, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(1);
        sendData.setAgrNum(0x44);
        sendData.setDataContent("");
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * GPS定时时间段设置 协议号0x46
     * 起始位2byte 包长度1byte 协议号1byte 开关 1byte 起始时间 结束时间 结束位2byte
     * eg.7878 05 46 01 1130 1230 0D0A
     * 01开，00为关，时间为BCD编码，1130就是11:30，从这个时间开始关闭GPS到12:30打开
     * 可以设置一个时间段关闭GPS
     *
     * @param channelHandlerContext
     * @param on_off                开关状态  0x01：开，0x00：关
     * @param beginTime             时间是BCD编码   1130 = 11:30 1230 =12:30  16进制字符串
     * @param endTime               16进制字符串
     */
    @Override
    public void sendTimingTmeSet(ChannelHandlerContext channelHandlerContext, int on_off, String beginTime, String endTime, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(1 + beginTime.length() / 2 + endTime.length() / 2);
        sendData.setAgrNum(0x46);
        StringBuffer sb = new StringBuffer();
        sb.append(Util.toHexString(on_off));
        sb.append(beginTime);
        sb.append(endTime);
        sendData.setDataContent(sb.toString());
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 勿扰时间段设置 协议号：0x47
     *
     * @param channelHandlerContext
     * @param on_off                开关
     * @param week                  星期  如果这位为1，每周这一天勿扰时间短开启
     * @param beginTime1            开始时间1 BCD编码  0130 = 01:30  2个字节
     * @param endTime1              结束时间1 16进制字符串 2个字节
     * @param beginTime2            开始时间2 16进制字符串   2个字节
     * @param endTime2              结束时间2 16进制字符串   2个字节
     */
    @Override
    public void sendNotDisturb(ChannelHandlerContext channelHandlerContext, int on_off, int week, String beginTime1, String endTime1, String beginTime2, String endTime2, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(1 + beginTime1.length() / 2 + endTime1.length() / 2 + beginTime2.length() / 2 + endTime2.length() / 2);
        sendData.setAgrNum(0x47);
        StringBuffer sb = new StringBuffer();
        sb.append(Util.toHexString(on_off));
        sb.append(Util.toHexString(week));
        sb.append(beginTime1);
        sb.append(endTime1);
        sb.append(beginTime2);
        sb.append(endTime2);
        sendData.setDataContent(sb.toString());
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }


    /**
     * 重启设备 协议号：0x48
     *
     * @param channelHandlerContext
     * @param command               重启操作  0x01 ：设备收到这个指令之后 重启    0x02：设备收到这个指令之后 关机
     */
    @Override
    public void sendRestart(ChannelHandlerContext channelHandlerContext, int command, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(2);
        sendData.setAgrNum(0x48);
        sendData.setDataContent(Util.toHexString(command));
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 找设备 协议号：0x49
     *
     * @param channelHandlerContext
     * @param command               状态  0x00 ：停止找设备    0x01：开始找设备
     */
    @Override
    public void sendFindEquipment(ChannelHandlerContext channelHandlerContext, int command, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(2);
        sendData.setAgrNum(0x49);
        sendData.setDataContent(Util.toHexString(command));
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 闹钟  协议号：0x50  3个闹钟  起始位2byte 包长度1byte 协议号1byte 闹钟3个 9byte 结束位2byte
     * 如果取消设置，全部发送0，7878 0a 50 000000 000000 000000 0d0a
     *
     * @param channelHandlerContext
     * @param clockStr1             3个字节  BCD编码  第一位为每周哪一天闹铃，第二位和第三位是小时分钟
     * @param clockStr2             3个字节
     * @param clockStr3             3个字节
     */
    @Override
    public void sendAlarmClock(ChannelHandlerContext channelHandlerContext, String clockStr1, String clockStr2, String clockStr3, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(0x0a);
        sendData.setAgrNum(0x50);
        StringBuffer sb = new StringBuffer();
        sb.append(clockStr1);
        sb.append(clockStr2);
        sb.append(clockStr3);
        sendData.setDataContent(sb.toString());
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 同步设置数据 协议号：0x57
     * 起始位2byte 包长度1byte 协议号1byte  上传间隔2byte  开关1byte 闹钟9byte 勿扰时间开关1byte 勿扰时间9byte GPS定时开关1byte
     * GPS定时时间4byte SOS爸爸妈妈3个号码（长度不定，3B（";"）做分割符  结束位2byte
     *
     * @param channelHandlerContext
     * @param upInterval            上传间隔 BCD编码 2个字节 0x0060=60秒
     * @param on_off                开关 1byte
     * @param alarmClock            闹钟9byte
     * @param notDisturb            勿扰开关1byte
     * @param notDisturbStr         勿扰时间9byte
     * @param gpsOn_off             gps定时开关 1byte
     * @param gpsTime               gps定时时间 4byte
     * @param sosNum                sos号码 ascii编码
     * @param fatherNum             爸爸号码 ascii编码
     * @param motherNum             妈妈号码 ascii编码
     */
    @Override
    public void sendSynchronizationSetData(ChannelHandlerContext channelHandlerContext, int upInterval, int on_off, String alarmClock
            , int notDisturb, String notDisturbStr, int gpsOn_off, String gpsTime, String sosNum, String fatherNum, String motherNum, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(1 + 2 + 1 + 9 + 1 + 9 + 1 + 4 + sosNum.length() + 1 + fatherNum.length() + 1 + motherNum.length() + 1);
        sendData.setAgrNum(0x50);
        StringBuffer sb = new StringBuffer();
        sb.append(Util.int2DoubleBCD(upInterval));
        sb.append(Util.toHexString(on_off));
        sb.append(alarmClock);
        sb.append(Util.toHexString(notDisturb));
        sb.append(notDisturbStr);
        sb.append(Util.toHexString(gpsOn_off));
        sb.append(gpsTime);
        sb.append(sosNum + "3b");
        sb.append(fatherNum + "3b");
        sb.append(motherNum + "3b");
        sendData.setDataContent(sb.toString());
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 同步白名单 协议号：0x58
     *
     * @param channelHandlerContext
     * @param wlList                白名单集合
     */
    @Override
    public void sendSynchronizationWhiteList(ChannelHandlerContext channelHandlerContext, List<WhiteList> wlList, String deviceName) {
        SendData sendData = new SendData();
        int sum = 0;
        for (int i = 0; i < wlList.size(); i++) {
            sum += wlList.get(i).getName().length() + 1 + wlList.get(i).getNum().length() + 1;
        }
        sendData.setLength(1 + sum);
        sendData.setAgrNum(0x50);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < wlList.size(); i++) {
            for (int j = 0; j < wlList.get(i).getName().length(); j++) {
                sb.append(Util.toHexString(wlList.get(i).getName().charAt(j)));
            }
            sb.append("3A");
            for (int j = 0; j < wlList.get(i).getNum().length(); j++) {
                sb.append(Util.toHexString(wlList.get(i).getNum().charAt(j)));
            }
            sb.append("3B");
        }
        sendData.setDataContent(sb.toString());
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 光感开关 协议号  0x61
     *
     * @param channelHandlerContext
     * @param flag                  0x01=打开光感  0x00=关闭光感
     */
    @Override
    public void sendLightSense(ChannelHandlerContext channelHandlerContext, int flag, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(2);
        sendData.setAgrNum(0x61);
        sendData.setDataContent(Util.toHexString(flag));
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 录音请求  协议号：0x64
     *
     * @param channelHandlerContext
     * @param flag                  1个字节  01拍照  02录影   03录音
     */
    @Override
    public void sendRecordRequest(ChannelHandlerContext channelHandlerContext, int flag, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(2);
        sendData.setAgrNum(0x64);
        sendData.setDataContent(Util.toHexString(flag));
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 录音请求控制设备  协议号0x64
     *
     * @param channelHandlerContext
     * @param fun                   功能码 04 05 06 ff
     * @param control               控制开关 00关  01开
     */
    @Override
    public void sendRecordHandler(ChannelHandlerContext channelHandlerContext, int fun, int control, int on_off, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(3);
        sendData.setAgrNum(0x64);
        StringBuffer sb = new StringBuffer();
        sb.append(Util.toHexString(control));
        sb.append(Util.toHexString(on_off));
        sendData.setDataContent(sb.toString());
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 修改设备连接服务器的ip和端口  协议号：0x66
     *
     * @param channelHandlerContext
     * @param ip                    服务器ip 字符串形式  192.168.2.1
     * @param port                  端口号  int类型  8080  2个字节
     */
    @Override
    public void sendUpdateIpAndPort(ChannelHandlerContext channelHandlerContext, String ip, int port, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(0x07);
        sendData.setAgrNum(0x66);
        StringBuffer sb = new StringBuffer();
        //解析ip，转为4个字节数组
        String[] ipArray = ip.split("\\.");
        for (int i = 0; i < ipArray.length; i++) {
            sb.append(Util.toHexString(Integer.valueOf(ipArray[i])));
        }
        sb.append(Util.toTwoHexString(port));
        sendData.setDataContent(sb.toString());
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }


    /**
     * 恢复密码  协议号：0x67
     * 在忘记设备密码的时候发短信给设备，设备发出这个恢复密码的指令，服务器收到之后，必须回复7878 02 67 01 0D0A，复位该设备的登录密码。
     *
     * @param channelHandlerContext
     */
    @Override
    public void sendRestorePassword(ChannelHandlerContext channelHandlerContext, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(0x02);
        sendData.setAgrNum(0x67);
        sendData.setDataContent("01");
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 手动定位  协议号：0x80
     * 起始位2byte 数据包长度1byte 协议号 1byte 停止位 2byte
     * 数据包长度  01  wifi/GPS/LBS 及时定位  787801800d0a
     * 02   wifi/LBS 787802800d0a
     *
     * @param channelHandlerContext
     * @param command
     */
    @Override
    public void sendHMLocation(ChannelHandlerContext channelHandlerContext, int command, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(command);
        sendData.setAgrNum(0x80);
        sendData.setDataContent("");
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 设置超速报警值  协议号：0x86
     * 起始位2byte 包长度1byte 协议号1byte超速速度 1byte结束位2byte
     *
     * @param channelHandlerContext
     * @param speed                 1个字节  BCD值 速度50 写入为0x50
     */
    @Override
    public void sendSetSpeedingAlarm(ChannelHandlerContext channelHandlerContext, int speed, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(0x02);
        sendData.setAgrNum(0x86);
        sendData.setDataContent(Util.int2BCD(speed));
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 震动报警开 协议号：0x92
     * 起始位2byte 包长度1byte 协议号1byte 震动等级1byte 结束位2byte
     *
     * @param channelHandlerContext
     * @param level                 震动等级  1个字节 01等级的震动 就发送震动报警0x94
     */
    @Override
    public void sendVibrationAlarmOn(ChannelHandlerContext channelHandlerContext, int level, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(0x02);
        sendData.setAgrNum(0x92);
        sendData.setDataContent(Util.toHexString(level));
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 震动报警关  协议号：0x93
     * 起始位2byte 包长度1byte 协议号1byte 结束位2byte
     *
     * @param channelHandlerContext
     */
    @Override
    public void sendVibrationAlarmOff(ChannelHandlerContext channelHandlerContext, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(0x01);
        sendData.setAgrNum(0x93);
        sendData.setDataContent("");
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 服务器设置上传间隔 协议号：0x97
     * 起始位2byte 包长度1byte 协议号1byte 上传间隔2byte 结束位2byte
     *
     * @param channelHandlerContext
     * @param upTime                上传时间 2个字节  上传间隔10--7200秒
     */
    @Override
    public void sendSetUpTime(ChannelHandlerContext channelHandlerContext, int upTime, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(0x03);
        sendData.setAgrNum(0x97);
        sendData.setDataContent(Util.toTwoHexString(upTime));
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }

    /**
     * 设备通过短信设置上传间隔，与服务器同步设置
     * 起始位2byte 包长度1byte 协议号1byte 上传间隔2byte 结束位2byte
     *
     * @param channelHandlerContext
     * @param upTime                传时间 2个字节  上传间隔10--7200秒
     */
    @Override
    public void sendSynchroSetUpTime(ChannelHandlerContext channelHandlerContext, int upTime, String deviceName) {
        SendData sendData = new SendData();
        sendData.setLength(0x03);
        sendData.setAgrNum(0x98);
        sendData.setDataContent(Util.toTwoHexString(upTime));
        //发送数据
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        buf.writeBytes(Util.toBytes(sendData.toString()));
        channelHandlerContext.channel().writeAndFlush(buf);
        debugLog(sendData, deviceName);
    }


    /**
     * 日志信息
     *
     * @param sendData
     */
    private void debugLog(SendData sendData, String deviceName) {
        log.info("服务器--->设备,设备名称:" + deviceName + "协议号：" + Util.toHexString(sendData.getAgrNum()) + " 数据帧:" + sendData.toString());
    }


}
