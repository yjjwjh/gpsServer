package com.lyny.mapper;

import com.lyny.pojo.GPS_locationData;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * Created by yjj on 2021-03-08
 */
@Mapper
public interface GPS_locationDataMapper {
    /**
     * 添加
     * @param gps_locationData
     */
    void insert(GPS_locationData gps_locationData);




}
