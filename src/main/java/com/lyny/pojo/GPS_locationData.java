package com.lyny.pojo;

import lombok.Data;

/**
 * Created by yjj on 2021-03-08
 */
@Data
public class GPS_locationData {

    private long id;//key

    private int deviceId;//设备表id

    private String timeStr;//日期时间

    private int gpsDataLength;//gps数据长度

    private int satellite;//可见卫星个数

    private double longitude;//gps经度

    private double latitude;//gps纬度

    private int speed;//速度

    private int n_s;//南北纬，0南纬，1北纬

    private int e_w;//东西经，0东经，1西经

    private int isLocation;//GPS是否定位，0不定位，1定位

    private int azimuth;//航向

    private int height;//海拔

    private int alarm;//警报，0x01振动，0x02超速，0x04wifi考勤，0x08离开wifi考勤，0x16地电报警


}
