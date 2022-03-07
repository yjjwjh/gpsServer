package com.lyny.pojo;

import com.lyny.unit.Util;
import lombok.Data;

/**
 * Created by yjj on 2021-03-06
 */
@Data
public class SendData {

    private final String headData="7878";//头信息 16进制字符串

    private int length;//数据长度

    private int AgrNum;//协议号

    private String dataContent;//数据内容  16进制字符串

    private final String tailData="0D0A";//尾信息 16进制字符串

    @Override
    public String toString() {
        return headData+ Util.toHexString(length)+Util.toHexString(AgrNum)+dataContent+tailData;
    }
}
