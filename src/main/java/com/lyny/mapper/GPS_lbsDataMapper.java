package com.lyny.mapper;

import com.lyny.pojo.GPS_lbsData;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by yjj on 2021-03-11
 */
@Mapper
public interface GPS_lbsDataMapper {
    /**
     * 新增lbs
     * @param gpsLbsData
     */
    void insert(GPS_lbsData gpsLbsData);



}
