package com.lyny.pojo;

import lombok.Data;

/**
 * Created by yjj on 2021-03-05
 */
@Data
public class GPS_device {

    private int id;

    private String deviceName;

    private String versionNum;

    private String imei;

    private String accessToken;

    private String sessionId;

    private int battery;//电池电量半分比

    private int softwareVersion;//软件版本

    private int timeZone;//时区，整数

    private int timeInterval;//状态包上传时间间隔，单位分钟

    private int signalIntensity;//信号强度

    private String iccid;//iccid

    private int status;//设备状态，01在线，00不在线

}
