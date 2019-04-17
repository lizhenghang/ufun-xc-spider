package com.ufun.work;

import com.ufun.bean.Hotel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/***
 * @author lizhneghang
 * @mail 1475546247@qq.com
 * @TIME 2019/3/25 11:12
 */
public class GenForQueueWork implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(GenForQueueWork.class);

    private List<Hotel> hotelList;      //酒店数据
    private BlockingQueue<Hotel> queue; //阻塞队列,供多线程使用

    /**
     * 单线程任务操作
     */
    @Override
    public void run() {
        int i=0;
        Iterator<Hotel> it=hotelList.iterator();

        /**
         * 将所有的hotel加入阻塞队列，以供多线程消耗
         */
        while(it.hasNext()){
            try {
                Hotel hotel=it.next();
                queue.put(hotel);//向队尾存入元素，如果队列满，则等待；
                log.info(hotel.getSno()+"酒店入queue");
            } catch (Exception e){
                log.warn("操作"+e.toString());
                e.printStackTrace();
                continue;
            }
        }
        end();//加入一批无效的数据，当多线程消耗到无效数据表名有效的数据已经消费完
        log.warn("!=========数据添加完成，无效元素入队=========!");
    }

    /**
     * 加入一批无效的hotel，当多线程读到这里的hotel证明有效的hotel已经消费完，那么线程就应该终止了。
     * 避免下次定时任务的时候这些线程还存活着,浪费内存性能.
     * 当然熟悉API的话有很多更科学的方式可以达到这个目的，此处时间关系用这种粗狂的方式
     */
    public void end(){
        for(int i=0;i<10000;i++){
            Hotel hotel=new Hotel();
            hotel.setFlag(false);//设置无效标记
            try {
                queue.put(hotel);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Hotel> getHotelList() {
        return hotelList;
    }

    public void setHotelList(List<Hotel> hotelList) {
        this.hotelList = hotelList;
    }

    public BlockingQueue<Hotel> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<Hotel> queue) {
        this.queue = queue;
    }
}
