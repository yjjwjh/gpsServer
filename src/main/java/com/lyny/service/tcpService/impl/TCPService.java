package com.lyny.service.tcpService.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lyny.mapper.GPS_deviceMapper;
import com.lyny.mapper.GPS_lbsDataMapper;
import com.lyny.mapper.GPS_locationDataMapper;
import com.lyny.mapper.GPS_wifiDataMapper;
import com.lyny.pojo.*;
import com.lyny.service.mqttService.MqttGateway;

import com.lyny.service.tcpService.SendClientDataService;
import com.lyny.unit.GPSUtil;
import com.lyny.unit.Util;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by yjj on 2021-03-05
 */
@Slf4j
@Service
public class TCPService {
    @Resource
    private GPS_deviceMapper gps_deviceMapper;
    @Autowired
    private SendClientDataService sendClientDataService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    private GPS_locationDataMapper gps_locationDataMapper;
    @Resource
    private GPS_wifiDataMapper gps_wifiDataMapper;
    @Resource
    private GPS_lbsDataMapper gps_lbsDataMapper;
    @Autowired
    private MqttGateway mqttGateway;

    /**
     * 数据帧解析
     * 0x780x78+数据长度+协议号+数据内容+0x0d0x0a
     *
     * @param bytes
     */
    public void dataParsing(byte[] bytes, ChannelHandlerContext channelHandlerContext) {
        //获取sessionId
        String sessionId = channelHandlerContext.channel().id().asLongText();
        //根据协议号进行处理
        //1.获取协议号 第4个字节
        int agrNum = bytes[3] & 0xff;
        switch (agrNum) {
            case 0x01://登录
                //判断字节数是否正确
                if (bytes.length != 15) {
                    //非法数据帧，协议号为0x01登录
                    log.info("非法数据帧，协议号为0x01登录:" + Util.byteArrayToString(bytes));
                    return;
                } else {
                    log.info("设备--->服务器  设备登录  协议号:" + Util.toHexString(agrNum) + " 数据帧：" + Util.byteArrayToString(bytes));
                    //数据内容=8byte_IMEI+1byte_软件版本号
                    //获取IMEI号
                    byte[] imeiBytes = Util.arraySpit(bytes, 4, 11);
                    //软件版本号
                    int version = bytes[12];
                    //从数据库中查询imei号是否存在
                    List<GPS_device> gpsDevices = gps_deviceMapper.findByIMEI(Util.bcd2Str(imeiBytes));
                    if (gpsDevices.size() == 0 || gpsDevices.size() > 1) {
                        //不存在此设备  登录失败
                        sendClientDataService.sendLoginReply(channelHandlerContext, false);
                    } else {
                        //存在，登录成功
                        sendClientDataService.sendLoginReply(channelHandlerContext, true);
                        GPS_device gpsDevice = gpsDevices.get(0);
                        gpsDevice.setSessionId(sessionId);//会话id
                        gpsDevice.setVersionNum(String.valueOf(version));//设备软件版本
                        gpsDevice.setStatus(1);//登录上线
                        //绑定设备会话id,修改sessionId
                        gps_deviceMapper.update(gpsDevice);
                        //加入缓存
                        //会话缓存
                        redisTemplate.boundSetOps("sessionId").add(sessionId);
                        //设备信息缓存
                        redisTemplate.boundHashOps("device").put(sessionId, gpsDevice);
                        //链路缓存
                        redisTemplate.boundHashOps("channel").put(String.valueOf(gpsDevice.getId()), channelHandlerContext);
                    }
                }
                break;
            default://不是登录
                //先判断是否已登录，从缓存中查询
                BoundSetOperations setOps = redisTemplate.boundSetOps("sessionId");
                if (!setOps.members().contains(sessionId)) {//是否存在此会话
                    //不存在
                    //让其设备下线
                    log.info("登录异常:" + "该设备还未登录，设备为" + channelHandlerContext.channel().remoteAddress());
                    channelHandlerContext.close();//强行下线设备
                    return;
                }
                //从数据库查询设备信息
                //从缓存中获取设备对象
                GPS_device gpsDevice = (GPS_device) redisTemplate.boundHashOps("device").get(sessionId);
                log.info("设备--->服务器，设备名称:" + gpsDevice.getDeviceName() + "协议号:" + Util.toHexString(agrNum) + " 数据帧：" + Util.byteArrayToString(bytes));
                switch (agrNum) {
                    case 0x08://心跳包  设备到服务器
                        //判断字节数是否正确
                        if (bytes.length != 6) {
                            //非法数据帧，协议号为0x08登录
                            log.info("非法数据帧，协议号为0x08心跳包:" + Util.byteArrayToString(bytes));
                            return;
                        }
                        //目前不做处理
                        break;
                    case 0x10://GPS定位数据包
                        //解析定位数据
                        locationAnalyze(bytes, gpsDevice);
                        sendClientDataService.sendLocationReply(channelHandlerContext, 0x10, gpsDevice.getDeviceName());
                        break;
                    case 0x11://离线GPS定位数据包
                        //解析定位数据
                        locationAnalyze(bytes, gpsDevice);
                        sendClientDataService.sendLocationReply(channelHandlerContext, 0x11, gpsDevice.getDeviceName());
                        break;
                    case 0x13://状态包
                        //修改设备信息
                        gpsDevice.setBattery(bytes[4] & 0xff);//电池电量
                        gpsDevice.setSoftwareVersion(bytes[5] & 0xff);//软件版本号
                        gpsDevice.setTimeZone(bytes[6] & 0xff);//时区
                        gpsDevice.setTimeInterval(bytes[7] & 0xff);//状态包上传时间间隔，单位分
                        //版本不一样，后面还有信号强度
                        if (bytes.length == 11) {
                            gpsDevice.setSignalIntensity(bytes[8] & 0xff);//信号强度
                        }
                        //更新数据库和缓存
                        gps_deviceMapper.update(gpsDevice);//数据库
                        redisTemplate.boundHashOps("device").put(sessionId, gpsDevice);//更新缓存
                        //回复状态包
                        sendClientDataService.sendReplyStart(channelHandlerContext, bytes, gpsDevice.getDeviceName());//回复状态包，一样
                        break;
                    case 0x14://设备休眠
                        //表示设备要和服务器断开连接进入休眠状态了
                        //从缓存中删除
                        redisTemplate.boundSetOps("sessionId").remove(sessionId);//会话id
                        redisTemplate.boundHashOps("device").delete(sessionId);//删除缓存设备信息
                        redisTemplate.boundHashOps("channel").delete(String.valueOf(gpsDevice.getId()));//删除连接通道
                        break;
                    case 0x15://恢复出厂设置
                        //判断字节数
                        if (bytes.length != 6) {
                            //非法数据帧，协议号为0x01登录
                            log.info("非法数据帧，协议号为0x15恢复出厂设置:" + Util.byteArrayToString(bytes));
                            return;
                        }
                        //服务器---->设备  回复
                        sendClientDataService.sendFactoryDataReset(channelHandlerContext, gpsDevice.getDeviceName());
                        break;
                    case 0x17://离线wifi数据  1.2版本 加了报警信息
                        //解析wifi和lbs数据
                        wifiAndLbsAnalyze(bytes, gpsDevice);
                        //回复
                        sendClientDataService.sendWifiReply(channelHandlerContext, 0x17, gpsDevice.getDeviceName());
                        break;
                    case 0x30://更新数据
                        //返回当前系统时间
                        sendClientDataService.sendGMTTime(channelHandlerContext, gpsDevice.getDeviceName());
                        break;
                    case 0x41://拨打sos号码
                        //目前不做处理
                        break;
                    case 0x42://拨打爸爸电话
                        //目前不做处理
                        break;
                    case 0x43://拨打妈妈电话
                        //目前不做处理
                        break;
                    case 0x56://脱落报警  00脱落  01穿戴
                        //获取标志
                        int flag = bytes[4] & 0xff;
                        log.info("脱落报警：" + flag);
                        //待写逻辑
                        break;
                    case 0x57://同步设置数据
                        //服务器回复，设置数据，包括设备开关, 闹钟, 电话号码
                        //待写
                        break;
                    case 0x58://同步白名单
                        //服务器收到之后回复设备白名单，白名单最大个数是50条
                        //起始位2byte 包长度1byte 协议号1byte 白名单（长度不定） 结束位2byte
                        //eg.7878 01 58 646473 3A 3138383338303231303133 3B 63 3A 3138353338303035343032 3B 0D0A
                        //待写
                        break;
                    case 0x67://恢复密码指令
                        sendClientDataService.sendRestorePassword(channelHandlerContext, gpsDevice.getDeviceName());
                        break;
                    case 0x69://wifi数据 1.2版本 加了报警信息
                        //解析wifi和lbs数据
                        wifiAndLbsAnalyze(bytes, gpsDevice);
                        //回复
                        sendClientDataService.sendWifiReply(channelHandlerContext, 0x69, gpsDevice.getDeviceName());
                        break;
                    case 0x80://设备不上传定位数据原因
                        //数据内容 01：时间不正确   02：lbs数量少  03：wifi数量少  04：lbs查找次数超过3次   05：相同的lbs和wifi数据
                        //06：禁止lbs上传，同时没有wifi   07：gps间距小于50米
                        int cause = bytes[4] & 0xff;
                        //待写
                        log.info("设备不上传定位数据的原因：" + cause);

                        break;
                    case 0x81://充电完成
                        if ((bytes[2] & 0xff) == 0x01) {
                            log.info("充电完成!");
                        } else if ((bytes[2] & 0xff) == 0x02) {
                            log.info("地点报警!");
                        }
                        //待写逻辑
                        break;
                    case 0x82://充电连接
                        log.info("设备充电连接!");
                        //待写逻辑
                        break;
                    case 0x83://充电断开
                        log.info("设备充电断开!");
                        //待写逻辑
                        break;
                    case 0x86://超速报警
                        //获取超速速度  BCD值
                        int speed = Integer.valueOf(Integer.toHexString((bytes[4] & 0xff)));
                        log.info("超速报警!速度为:" + speed);
                        //待写逻辑
                        break;
                    case 0x94://震动报警
                        log.info("震动报警");
                        //业务逻辑待写
                        break;
                    case 0x98://接收有短信发送给设备的设置上传时间
                        //获取上传时间
                        int upTime = ((bytes[4] & 0xff) << 8) + (bytes[5] & 0xff);
                        sendClientDataService.sendSynchroSetUpTime(channelHandlerContext, upTime, gpsDevice.getDeviceName());
                        break;
                    case 0x99://设备SOS报警
                        log.info("设备SOS报警");
                        //业务逻辑待写
                        break;
                    case 0xb3://iccid 设备发送服务器的iccid
                        //获取设备iccid
                        byte[] iccidByte = Util.arraySpit(bytes, 4, 23);
                        String iccid = Util.bcd2Str(iccidByte);
                        gpsDevice = (GPS_device) redisTemplate.boundHashOps("device").get(sessionId);
                        gpsDevice.setIccid(iccid);
                        //更新数据库
                        gps_deviceMapper.update(gpsDevice);
                        //跟新缓存
                        redisTemplate.boundHashOps("device").put(sessionId, gpsDevice);
                        break;
                    default:
                        //无此协议号
                        log.info("设备到服务器无此以协议号:" + Util.toHexString(agrNum));
                }
        }

    }

    /**
     * 判断数据帧是否正确
     *
     * @param bytes
     * @return
     */
    public boolean isAnalyze(byte[] bytes) {
        if (bytes.length > 4 && bytes[0] == 120 && bytes[1] == 120 && bytes[bytes.length - 2] == 0x0D && bytes[bytes.length - 1] == 0x0A) {
            return true;
        } else {

            return false;
        }
    }

    /**
     * 解析gps定位数据
     *
     * @param bytes
     * @param gpsDevice
     */
    public void locationAnalyze(byte[] bytes, GPS_device gpsDevice) {

        //创建定位对象
        GPS_locationData gps_locationData = new GPS_locationData();
        gps_locationData.setDeviceId(gpsDevice.getId());//设备id
        //判断版本号
        if (gpsDevice.getVersionNum().equals("1")) {//V1.0
            //判断字节数是否正确
            if (bytes.length != 24) {
                //非法数据帧，协议号为0x10定位数
                log.info("非法数据帧，协议号为0x10定位数据:" + Util.byteArrayToString(bytes));
                return;
            }
            //长度正确，解析包
            //日期
            byte[] timeBytes = Util.arraySpit(bytes, 4, 9);
            String timeStr = Util.byteArrayToDateStr(timeBytes);
            gps_locationData.setTimeStr(timeStr);
            //GPS数据长度
            gps_locationData.setGpsDataLength((bytes[10] & 0xf0) >>> 4);
            //可见卫星个数
            gps_locationData.setSatellite(bytes[10] & 0x0f);
            //gps经纬度
            byte[] gps_latByte = Util.arraySpit(bytes, 11, 14);
            gps_locationData.setLatitude(Util.byteArrayToLonLat(gps_latByte));//纬度
            byte[] gps_lonByte = Util.arraySpit(bytes, 15, 18);
            gps_locationData.setLongitude(Util.byteArrayToLonLat(gps_lonByte));//经度
            //gps速度
            gps_locationData.setSpeed(bytes[19]);
            //GPS定位状态，东西经，南北纬，航向
            gps_locationData.setN_s((bytes[20] & 0x04) >> 2);//南北纬
            gps_locationData.setE_w((bytes[20] & 0x08) >> 3);//东西经
            gps_locationData.setIsLocation((bytes[20] & 0x16) >> 4);//gps定位状态
            gps_locationData.setAlarm((bytes[20] & 0x03) * 256 + bytes[21]);//航向
            //插入数据库
            gps_locationDataMapper.insert(gps_locationData);
        } else {//v1.2版本
            //判断字节数是否正确
            if (bytes.length != 27) {
                //非法数据帧，协议号为0x10定位数
                log.info("非法数据帧，协议号为0x10定位数据:" + Util.byteArrayToString(bytes));
                return;
            }
            //长度正确，解析包
            //日期
            byte[] timeBytes = Util.arraySpit(bytes, 4, 9);
            String timeStr = Util.byteArrayToDateStr(timeBytes);
            gps_locationData.setTimeStr(timeStr);
            //GPS数据长度
            gps_locationData.setGpsDataLength((bytes[10] & 0xf0) >>> 4);
            //可见卫星个数
            gps_locationData.setSatellite(bytes[10] & 0x0f);
            //gps经纬度
            byte[] gps_latByte = Util.arraySpit(bytes, 11, 14);
            gps_locationData.setLatitude(Util.byteArrayToLonLat(gps_latByte));//纬度
            byte[] gps_lonByte = Util.arraySpit(bytes, 15, 18);
            gps_locationData.setLongitude(Util.byteArrayToLonLat(gps_lonByte));//经度
            //gps速度
            gps_locationData.setSpeed(bytes[19]);
            //GPS定位状态，东西经，南北纬，航向
            gps_locationData.setN_s((bytes[20] & 0x04) >> 2);//南北纬
            gps_locationData.setE_w((bytes[20] & 0x08) >> 3);//东西经
            gps_locationData.setIsLocation((bytes[20] & 0x16) >> 4);//gps定位状态
            gps_locationData.setAlarm((bytes[20] & 0x03) * 256 + bytes[21]);//航向
            //海拔
            gps_locationData.setHeight(bytes[22] * 256 + bytes[23]);
            //警报
            gps_locationData.setAlarm(bytes[24]);
            //插入数据库
            gps_locationDataMapper.insert(gps_locationData);
        }
        //发送数据到thingboard上，使用mqtt
        GPS_Data_mqtt gps_data_mqtt = new GPS_Data_mqtt();
        gps_data_mqtt.setDeviceName(gpsDevice.getDeviceName());
        //坐标转换，腾讯地图使用的是GCJ-02坐标系（火星坐标系） 从GPS设备获取的是地理坐标系（WGS84坐标）
        double lon;
        double lat;
        if (gps_locationData.getN_s() == 0) {//南纬
            lat=-gps_locationData.getLatitude();
        } else {//北纬
            lat=gps_locationData.getLatitude();
        }
        if (gps_locationData.getE_w() == 0) {//东经
            lon=gps_locationData.getLongitude();
        } else {//西经
            lon=-gps_locationData.getLongitude();
        }
        double[] doubles = GPSUtil.transformWGS84ToGCJ02(lon, lat);
        gps_data_mqtt.setSerialNumber(gpsDevice.getDeviceName());
        gps_data_mqtt.setSensorType("Thermometer");
        gps_data_mqtt.setSensorModel("T1000");
        gps_data_mqtt.setLon(doubles[0]);
        gps_data_mqtt.setLat(doubles[1]);
        mqttGateway.sendToMqtt(JSON.toJSONString(gps_data_mqtt), "/sensor/data");
    }

    /**
     * 解析wifi数据
     *
     * @param bytes
     * @param gpsDevice
     */
    public void wifiAndLbsAnalyze(byte[] bytes, GPS_device gpsDevice) {
        //数据解析
        //获取wifi数量

        int wifiNum = bytes[2] & 0xff;
        //获取时间
        byte[] timeBytes = Util.arraySpit(bytes, 4, 9);
        String timeStr = Util.byteArrayBCDToDateStr(timeBytes);
        //解析wifi
        for (int i = 0; i < wifiNum; i++) {
            GPS_wifiData gpsWifiData = new GPS_wifiData();
            byte[] wifiByte = Util.arraySpit(bytes, 10 + i * 7, 10 + i * 7 + 6);
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < 6; j++) {
                sb.append(Util.toHexString(wifiByte[j] & 0xff));
                if (j != 5) {
                    sb.append(":");
                }
            }
            gpsWifiData.setBssid(sb.toString());
            gpsWifiData.setRssi(Util.toHexString(wifiByte[6] & 0xff));
            gpsWifiData.setTime(timeStr);
            gpsWifiData.setDeviceId(gpsDevice.getId());
            gps_wifiDataMapper.insert(gpsWifiData);
        }
        //解析lbs
        int lbsNum = bytes[10 + 7 * wifiNum];
        //获取mcc mnc
        int mcc = ((bytes[10 + 7 * wifiNum + 1]) & 0xff) * 256 + ((bytes[10 + 7 * wifiNum + 2]) & 0xff);
        int mnc = (bytes[10 + 7 * wifiNum + 3]) & 0xff;
        for (int i = 0; i < lbsNum; i++) {
            byte[] lbsByte = Util.arraySpit(bytes, 10 + 7 * wifiNum + 3 + i * 5 + 1, 10 + 7 * wifiNum + 3 + i * 5 + 5);
            GPS_lbsData gps_lbsData = new GPS_lbsData();
            gps_lbsData.setLac(((lbsByte[0] & 0xff) << 8) + (lbsByte[1] & 0xff));
            gps_lbsData.setCellid(((lbsByte[2] & 0xff) << 8) + (lbsByte[3] & 0xff));
            gps_lbsData.setMciss(lbsByte[4] & 0xff);
            gps_lbsData.setMcc(mcc);
            gps_lbsData.setMnc(mnc);
            gps_lbsData.setTime(timeStr);
            gps_lbsData.setDeviceId(gpsDevice.getId());
            gps_lbsDataMapper.insert(gps_lbsData);
        }
    }


}
