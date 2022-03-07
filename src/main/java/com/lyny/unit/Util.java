package com.lyny.unit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yjj on 2021-03-05
 */
public class Util {
    /**
     * 数组截取
     * @param sourceBytes 源数组
     * @param beginIndex 截取开始的索引
     * @param endIndex 截取结束的索引
     * @return
     */
    public static byte[] arraySpit(byte[] sourceBytes,int beginIndex,int endIndex)
    {
        byte[] bytes=new byte[endIndex-beginIndex+1];
        System.arraycopy(sourceBytes,beginIndex,bytes,0,bytes.length);
        return bytes;
    }
    /**
     * 字节数组转16进制string
     *
     * @param bytes
     * @return
     */
    public static String byteArrayToString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(toHexString(bytes[i] & 0xff));
        }
        return sb.toString();
    }

    /**
     * int转16字符串
     *
     * @param b
     * @return
     */
    public static String toHexString(int b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        return hex.toUpperCase();
    }

    /**
     * int转16字符串  2个字节
     *
     * @param b
     * @return
     */
    public static String toTwoHexString(int b) {
        String hex = Integer.toHexString(b & 0xFFFF);
        if (hex.length() == 1) {
            hex = "000" + hex;
        }else if(hex.length()==2){
            hex="00"+hex;
        }else if(hex.length()==3){
            hex="0"+hex;
        }
        return hex.toUpperCase();
    }

    /**
     * 字节转换为浮点
     *
     * @param b 字节（至少4个字节）
     * @param
     * @return
     */
    public static float byte2float(byte[] b) {
        int l;
        l = b[0];
        l &= 0xff;
        l |= ((long) b[1] << 8);
        l &= 0xffff;
        l |= ((long) b[2] << 16);
        l &= 0xffffff;
        l |= ((long) b[3] << 24);
        return Float.intBitsToFloat(l);
    }



    /**
     * @函数功能: BCD码转为10进制串(阿拉伯数据)
     * @输入参数: BCD码
     * @输出结果: 10进制串
     */
    public static String bcd2Str(byte[] bytes){
        StringBuffer temp=new StringBuffer(bytes.length*2);

        for(int i=0;i<bytes.length;i++){
            if(i==0){
                if(((bytes[i]& 0xf0)>>>4)!=0){
                    temp.append((byte)((bytes[i]& 0xf0)>>>4));
                }
            }else{
                temp.append((byte)((bytes[i]& 0xf0)>>>4));
            }
            temp.append((byte)(bytes[i]& 0x0f));
        }
        return temp.toString();
    }

    /**
     * 当前时间转6字节的BCD字符串
     * @return
     */
    public static String date2BCD(){
        StringBuffer sb=new StringBuffer();
        Calendar now = Calendar.getInstance();
        sb.append(int2BCD(now.get(Calendar.YEAR)-2000));//年
        sb.append(int2BCD(now.get(Calendar.MONTH) + 1));//月
        sb.append(int2BCD(now.get(Calendar.DAY_OF_MONTH)));//日
        sb.append(int2BCD(now.get(Calendar.HOUR_OF_DAY)));//时
        sb.append(int2BCD(now.get(Calendar.MINUTE)));//分
        sb.append(int2BCD(now.get(Calendar.SECOND)));//秒
        return sb.toString();
    }

    /**
     * int转bcd字符串  16进制 1个字节
     * @param a
     * @return
     */
    public static String int2BCD(int a){
        String aStr=String.valueOf(a);
        if(aStr.length()<2){
            return "0"+aStr;
        }
        return aStr;
    }

    /**
     * int转bcd字符串 16进制  2个字节
     * @param a
     * @return
     */
    public static String int2DoubleBCD(int a){
        String aStr=String.valueOf(a);
        if(aStr.length()==1){
            return "000"+aStr;
        }else if(aStr.length()==2){
            return "00"+aStr;
        }else if(aStr.length()==3){
            return "0"+aStr;
        }
        return aStr;
    }

    /**
     * 16进制字符串转字节数组
     * @param str
     * @return
     */
    public static byte[] toBytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }

    /**
     * 6字节转日期时间
     * 0A03170F3217，年月日时分秒，每个占1byte，转换过来10年3月23日15时50分23秒，年份再加2000就是2010年，时间为GMT0
     * @param bytes
     * @return
     */
    public static String byteArrayToDateStr(byte[] bytes){
        StringBuffer sb=new StringBuffer();
        sb.append(2000+bytes[0]);//年
        if(bytes[1]<10) {//月
            sb.append(0);
            sb.append(bytes[1]);
        }else{
            sb.append(bytes[1]);
        }
        if(bytes[2]<10) {//日
            sb.append(0);
            sb.append(bytes[2]);
            sb.append(" ");
        }else{
            sb.append(bytes[2]);
            sb.append(" ");
        }
        if(bytes[3]<10) {//时
            sb.append(0);
            sb.append(bytes[3]);
            sb.append(":");
        }else{
            sb.append(bytes[3]);
            sb.append(":");
        }
        if(bytes[4]<10) {//分
            sb.append(0);
            sb.append(bytes[4]);
            sb.append(":");
        }else{
            sb.append(bytes[4]);
            sb.append(":");
        }
        if(bytes[5]<10) {//秒
            sb.append(0);
            sb.append(bytes[5]);
        }else{
            sb.append(bytes[5]);
        }
        return sb.toString();
    }

    /**
     * 6字节BCD转日期
     * @param bytes
     * @return
     */
    public static String byteArrayBCDToDateStr(byte[] bytes){

        String timeStr = byteArrayToString(bytes);
        StringBuffer sb=new StringBuffer();
        sb.append(2000+Integer.parseInt(timeStr.substring(0,2)));//年
        sb.append("-");
        sb.append(timeStr.substring(2,4));//月
        sb.append("-");
        sb.append(timeStr.substring(4,6));//日
        sb.append(" ");
        sb.append(timeStr.substring(6,8));//小时
        sb.append(":");
        sb.append(timeStr.substring(8,10));//分钟
        sb.append(":");
        sb.append(timeStr.substring(10,12));//秒
        return sb.toString();
    }
    /**
     * 字节转经纬度小数
     * @param bytes
     * @return
     */
    public static double byteArrayToLonLat(byte[] bytes){
        int sum=0;
        sum+=(bytes[0]&0xff)<<24;
        sum+=(bytes[1]&0xff)<<16;
        sum+=(bytes[2]&0xff)<<8;
        sum+=bytes[3]&0xff;
        //以分为小数的经纬度值
        double lol=(double)sum/30000;
        //度
        int d= (int) (lol/60);
        //分
        double f=lol-(d*60);
        //转为小数经纬度返回
        return (d+f/60);
    }


    /**
     * 验证手机号码
     * @param mobiles
     * @return
     */
    public static boolean isMobileNO(String mobiles){
        boolean flag = false;
        try{
            Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
            Matcher m = p.matcher(mobiles);
            flag = m.matches();
        }catch(Exception e){
            flag = false;
        }
        return flag;
    }

}
