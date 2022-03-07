package com.lyny.service.tcpService;

import com.lyny.pojo.WhiteList;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * Created by yjj on 2021-03-15
 */
public interface SendClientDataService {

    void sendLoginReply(ChannelHandlerContext channelHandlerContext, boolean isLogin);

    void sendSuperviseNum(ChannelHandlerContext channelHandlerContext, int status, String deviceName);

    void sendLocationReply(ChannelHandlerContext channelHandlerContext, int agr, String deviceName);

    void sendReplyStart(ChannelHandlerContext channelHandlerContext, byte[] bytes, String deviceName);

    void sendSetTimeInterval(ChannelHandlerContext channelHandlerContext, int time, String deviceName);

    void sendSetHeartbeatTime(ChannelHandlerContext channelHandlerContext, int time, String deviceName);

    void sendFactoryDataReset(ChannelHandlerContext channelHandlerContext, String deviceName);

    void sendWhiteListLength(ChannelHandlerContext channelHandlerContext, int num, String deviceName);

    void sendWifiReply(ChannelHandlerContext channelHandlerContext, int agr, String deviceName);

    void sendGMTTime(ChannelHandlerContext channelHandlerContext, String deviceName);

    void sendLBSStart(ChannelHandlerContext channelHandlerContext, int start, String deviceName);

    void sendGPSAndLBSUp(ChannelHandlerContext channelHandlerContext, int gpsStart, String gpsStartTime, int lbsStart, String lbsOnTime, String lbsOffTime, String deviceName);

    void sendPhone(ChannelHandlerContext channelHandlerContext, int agx, String phoneNumStr, String deviceName);

    void sendStopDataUp(ChannelHandlerContext channelHandlerContext, String deviceName);

    void sendTimingTmeSet(ChannelHandlerContext channelHandlerContext, int on_off, String beginTime, String endTime, String deviceName);

    void sendNotDisturb(ChannelHandlerContext channelHandlerContext, int on_off, int week, String beginTime1, String endTime1, String beginTime2, String endTime2, String deviceName);

    void sendRestart(ChannelHandlerContext channelHandlerContext, int command, String deviceName);

    void sendFindEquipment(ChannelHandlerContext channelHandlerContext, int command, String deviceName);

    void sendAlarmClock(ChannelHandlerContext channelHandlerContext, String clockStr1, String clockStr2, String clockStr3, String deviceName);

    void sendSynchronizationSetData(ChannelHandlerContext channelHandlerContext, int upInterval, int on_off, String alarmClock
            , int notDisturb, String notDisturbStr, int gpsOn_off, String gpsTime, String sosNum, String fatherNum, String motherNum, String deviceName);

    void sendSynchronizationWhiteList(ChannelHandlerContext channelHandlerContext, List<WhiteList> wlList, String deviceName);

    void sendLightSense(ChannelHandlerContext channelHandlerContext, int flag, String deviceName);

    void sendRecordRequest(ChannelHandlerContext channelHandlerContext, int flag, String deviceName);

    void sendRecordHandler(ChannelHandlerContext channelHandlerContext, int fun, int control, int on_off, String deviceName);

    void sendUpdateIpAndPort(ChannelHandlerContext channelHandlerContext, String ip, int port, String deviceName);

    void sendRestorePassword(ChannelHandlerContext channelHandlerContext, String deviceName);

    void sendHMLocation(ChannelHandlerContext channelHandlerContext, int command, String deviceName);

    void sendSetSpeedingAlarm(ChannelHandlerContext channelHandlerContext, int speed, String deviceName);

    void sendVibrationAlarmOn(ChannelHandlerContext channelHandlerContext, int level, String deviceName);

    void sendVibrationAlarmOff(ChannelHandlerContext channelHandlerContext, String deviceName);

    void sendSetUpTime(ChannelHandlerContext channelHandlerContext, int upTime, String deviceName);

    void sendSynchroSetUpTime(ChannelHandlerContext channelHandlerContext, int upTime, String deviceName);







}
