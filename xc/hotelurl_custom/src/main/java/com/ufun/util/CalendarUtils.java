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
        String[] arr=new String[addNum];
        SimpleDateFormat sdf=getDateFormat(pattern);
        for (int i = 0; i < addNum; i++) {
            calendar.add(Calendar.DAY_OF_MONTH,1);
            Date date=calendar.getTime();
            arr[i]=sdf.format(date);
        }
        return arr;
    }

    public static void main(String[] args) {
        System.out.println(getDateString("yyyy-MM-d"));
    }
}
