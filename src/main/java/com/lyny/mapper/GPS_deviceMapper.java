package com.lyny.mapper;

import com.lyny.pojo.GPS_device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by yjj on 2021-03-05
 */
@Mapper
public interface GPS_deviceMapper {
    /**
     * 根据ime查询设备信息
     * @param imei
     * @return
     */
    List<GPS_device> findByIMEI(String imei);

    /**
     * 根据id绑定设备会话号
     * @param sessionId
     * @param id
     */
    void bindingDevice(@Param("sessionId") String sessionId, @Param("id") int id);

    /**
     * 修改设备信息
     * @param gps_device
     */
    void update(GPS_device gps_device);

    /**
     * 插入设备信息
     * @param gps_device
     */
    void insert(GPS_device gps_device);

    /**
     * 根据设备id修改设备状态
     * @param status
     * @param id
     */
    void updateStatusById(@Param("status") int status,@Param("id") int id);
}
