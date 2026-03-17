package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);
    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);


    /**
     * 根据状态查询订单超时
     * @param status
     * @param OrderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{OrderTime}")
    List<Orders> getByStatusAndOrderTimeLt(Integer status, LocalDateTime OrderTime);

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 分页查询订单
     * @param orders
     * @return
     */
    Page<Orders> pageQuery(Orders orders);

    /**
     * 根据动态条件统计营业额数据
     *
     * @return
     */
    Double sumByStatusAndOrderTime(Map  map);

    /**
     * 根据动态条件统计订单数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 管理端订单分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery4Admin(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据动态条件统计营业额数据
     * @param map
     * @return
     */
    Double sumByMap(Map map);
}
