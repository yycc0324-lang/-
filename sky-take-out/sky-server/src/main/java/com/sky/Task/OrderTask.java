package com.sky.Task;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类,处理超时任务
 */
@Slf4j
@Component

public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 0/1 * * * ?") //每分钟触发一次
    public void processTimeoutOrder(){
        log.info("处理超时订单监测", LocalDateTime.now());
        LocalDateTime DeletTime = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLt(Orders.PENDING_PAYMENT,DeletTime);//判断超时然后获取订单列表的逻辑
        if(ordersList != null && ordersList.size() > 0){
            for(Orders orders:ordersList){
                orders.setStatus(Orders.CANCELLED);//遍历筛选的订单列表然后把所有设置订单状态为取消
                orders.setCancelReason("订单超时，取消订单");//设置取消原因，给用户看，给商家交代，给后台记录
                //设置订单取消时间
                orders.setCancelTime(LocalDateTime.now());
            }

        }


    }

    /**
     * 处理派送中订单
     */
    //每日01:00执行
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("处理派送中订单", LocalDateTime.now());
        LocalDateTime DeletTime = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLt(Orders.DELIVERY_IN_PROGRESS,DeletTime);//判断超时然后获取订单列表的逻辑
        if(ordersList != null && ordersList.size() > 0){
            for(Orders orders:ordersList){
                orders.setStatus(Orders.COMPLETED);//遍历
            }
        }
    }

}
