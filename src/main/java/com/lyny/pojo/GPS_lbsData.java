package com.lyny.pojo;

import lombok.Data;

/**
 * Created by yjj on 2021-03-11
 */
@Data
public class GPS_lbsData {
    private long id;

    private int mcc;//移动国家码

    private int mnc;//移动网络码

    private int lac;//lac

    private int cellid;//cellid

    private int mciss;//mciss

    private String time;//时间

    private int deviceId;//设备id



}
