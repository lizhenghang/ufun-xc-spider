package com.ufun.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/***
 * @author lizhenghang
 * @mail 1475546247@qq.com
 * @TIME 2019/4/11 15:11
 */
public class CalendarUtils {

    private static Calendar calendar=Calendar.getInstance();

    private static void reset(){
        calendar.setTime(new Date());
    }

    /**
     * 获取明天的日期字符串
     * @param pattern
     * @return
     */
    public static String getTomorrowDateString(String pattern) {
        reset();
        SimpleDateFormat sdf = getDateFormat(pattern);
        Date date = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        date = calendar.getTime();
        return sdf.format(date);
    }

    /**
     *
     * @param pattern
     * @return
     */
    public static String getDateString(String pattern){
        reset();
        Date date=calendar.getTime();
        SimpleDateFormat sdf=getDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * 获取一个指定模式的时间格式化工具
     * @param pattern
     * @return
     */
    public static SimpleDateFormat getDateFormat(String pattern){
        SimpleDateFormat dateFormat=null;
        if(pattern==null||"".equals(pattern)){
            dateFormat=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        }else{
            dateFormat=new SimpleDateFormat(pattern);
        }
        return dateFormat;
    }

    /**
     * 获取指定追加天数的数组
     * @param addNum 增加多少天
     * @param pattern
     * @return
     */
    public static String[] getCallendarString(int addNum,String pattern){
        reset();
        String[] arr=new String[addNum+1];
        SimpleDateFormat sdf=getDateFormat(pattern);
        Date date=calendar.getTime();
        arr[0]=sdf.format(date);
        for (int i = 1; i < arr.length; i++) {
            calendar.add(Calendar.DAY_OF_MONTH,1);
            date=calendar.getTime();
            arr[i]=sdf.format(date);
        }
        return arr;
    }

    public static void main(String[] args) {
        String[] arr=CalendarUtils.getCallendarString(10,"yyyy-MM-dd");
        for (String s : arr) {
            System.out.println(s);
        }
    }
}
