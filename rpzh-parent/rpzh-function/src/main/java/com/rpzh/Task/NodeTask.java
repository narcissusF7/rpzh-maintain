package com.rpzh.Task;

import com.rpzh.dao.NodeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

//@Component
public class NodeTask {

    @Autowired
    private NodeDao nodeDao;

    /**
     * 用来查询丢包是否
     * 默认两天没收到数据就判定位设备故障
     */
    @Scheduled(cron="${time.cron}")
    void isPacketLoss() {
        Date end = new Date();
        Calendar ca = Calendar.getInstance();
        ca.setTime(end);
        ca.add(Calendar.DATE, -2);
        Date begin = ca.getTime();
        //查询时间范围内的发送过数据的设备
        List<String> nodeIds = nodeDao.findAllNodeHistory(begin,end);

        List<String> enterStackNodeIds = nodeDao.findEnterStackNodeId();
        //遍历删除掉有接受过数据的入垛设备
        Iterator<String> iterator=enterStackNodeIds.iterator();
        while(iterator.hasNext()) {
            String s=iterator.next();
            if(nodeIds.contains(s)){
                iterator.remove();
            }
        }

        //如果存在未收到数据的入垛设备 则判定为设备故障
        if (enterStackNodeIds.size()>0){
            enterStackNodeIds.forEach(nodeId->{

            });
        }

    }

}
