package com.lyny.gps_device_server;



import com.alibaba.fastjson.JSONObject;
import com.lyny.service.tcpService.SendClientDataService;
import com.lyny.unit.GPSUtil;
import com.lyny.unit.Util;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@SpringBootTest
class GpsDeviceServerApplicationTests {
    @Autowired
    private SendClientDataService sendClientDataService;

    @Test
    void contextLoads() {

    }


    /**
     * GPS坐标转换为百度地图坐标
     * <p>
     * 需要引入javabase64.jar 和json的一些包
     */
    @Test
    public void test() {
        double lon=114.347312;
        double lat=30.513789;
        double[] doubles = GPSUtil.transformGCJ02ToBD09(lon, lat);
        System.out.println(doubles[0]+","+doubles[1]);

    }


}
