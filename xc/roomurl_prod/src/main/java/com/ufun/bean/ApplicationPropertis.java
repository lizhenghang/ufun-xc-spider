package com.ufun.bean;

/***
 * @author mayue
 * @mail dashingmy@163.com
 * @TIME 2019/3/23 16:26
 */
public class ApplicationPropertis {

    private int threadCount;

    private String table;

    private String dataBase;

    private int spiderDay;

    public int getSpiderDay() {
        return spiderDay;
    }

    public void setSpiderDay(int spiderDay) {
        this.spiderDay = spiderDay;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getDataBase() {
        return dataBase;
    }

    public void setDataBase(String dataBase) {
        this.dataBase = dataBase;
    }

    @Override
    public String toString() {
        return "ApplicationPropertis{" +
                "threadCount=" + threadCount +
                ", table='" + table + '\'' +
                ", dataBase='" + dataBase + '\'' +
                ", spiderDay=" + spiderDay +
                '}';
    }
}
