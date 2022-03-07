package com.lyny.pojo;

import lombok.Data;

/**
 * Created by yjj on 2021-03-11
 */
@Data
public class GPS_wifiData {
    private long id;//主键

    private String bssid;//bssid

    private String rssi;//rssi

    private String time;//时间

    private int deviceId;//设备表id



}
