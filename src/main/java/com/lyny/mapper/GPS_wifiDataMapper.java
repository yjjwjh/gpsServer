package com.lyny.mapper;

import com.lyny.pojo.GPS_wifiData;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by yjj on 2021-03-11
 */
@Mapper
public interface GPS_wifiDataMapper {
    /**
     * 新增wifi
     * @param gpsWifiData
     */
    void insert(GPS_wifiData gpsWifiData);




}
