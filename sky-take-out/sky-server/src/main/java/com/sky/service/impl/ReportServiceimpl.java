package com.sky.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceimpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;//提前注入订单表给表数据

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 统计指定时间区间内的营业额数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {// .dateList(dateListString)这个return是build以后得到TurnoverReportVO的第一个数据然后还回去的
        log.info("营业额统计：{} - {}", begin, end);
////////////////////////////////////////////////////////////////////////////////////////
        //第一个数据
        //dateList用来存放日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);//日期加n的函数plusDays（n）
            dateList.add(begin);
        }
        //写遍历达到StringUtils.stringToInteger(dateList, ",");的效果
        String dateListString = "";
        for (LocalDate date : dateList) {
            dateListString += date + ",";
        }
/////////////////////////////////////////////////////////////////////////////////////////
        //第二个数据
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //查询date日期对应的营业额，即状态已完成的订单金额

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByStatusAndOrderTime(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        //将turnoverList转换为字符串
        String turnoverListString = "";
        for (Double turnover : turnoverList) {
            turnoverListString += turnover + ",";
        }
        //移除最后一个逗号
        if (turnoverListString.length() > 0) {
            turnoverListString = turnoverListString.substring(0, turnoverListString.length() - 1);
        }
        if (dateListString.length() > 0) {
            dateListString = dateListString.substring(0, dateListString.length() - 1);
        }

/////////////////////////////////////////////////////////////////////////////////////////
        return TurnoverReportVO
                .builder()
                .dateList(dateListString)
                .turnoverList(turnoverListString)
                .build();
    }

    /**
     * 统计指定时间区间内的用户数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        log.info("用户统计：{} - {}", begin, end);

        // 构建日期列表
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 转换为字符串
        String dateListString = dateList.stream()
                .map(LocalDate::toString)
                .collect(Collectors.joining(","));

        // 统计每日新增用户和总用户数
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 查询当天新增用户数
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            Integer newUser = userMapper.countByMap(map);
            newUserList.add(newUser);

            // 查询截止到当天的总用户数
            map = new HashMap();
            map.put("end", endTime);
            Integer totalUser = userMapper.countByMap(map);
            totalUserList.add(totalUser);
        }

        String newUserListString = newUserList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String totalUserListString = totalUserList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return UserReportVO.builder()
                .dateList(dateListString)
                .newUserList(newUserListString)
                .totalUserList(totalUserListString)
                .build();
    }

    /**
     * 统计指定时间区间内的订单数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        log.info("订单统计：{} - {}", begin, end);

        // 构建日期列表
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        String dateListString = dateList.stream()
                .map(LocalDate::toString)
                .collect(Collectors.joining(","));

        // 统计每日订单数和有效订单数
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 查询当天订单总数
            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            Integer orderCount = orderMapper.countByMap(map);
            orderCountList.add(orderCount);

            // 查询当天有效订单数（已完成）
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByMap(map);
            validOrderCountList.add(validOrderCount);
        }

        // 计算时间区间内的订单总数和有效订单数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).orElse(0);
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).orElse(0);

        // 计算订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        String orderCountListString = orderCountList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String validOrderCountListString = validOrderCountList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return OrderReportVO.builder()
                .dateList(dateListString)
                .orderCountList(orderCountListString)
                .validOrderCountList(validOrderCountListString)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 统计指定时间区间内的销量Top10
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        log.info("销量Top10统计：{} - {}", begin, end);

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderDetailMapper.getSalesTop10(beginTime, endTime, Orders.COMPLETED);

        String nameListString = salesTop10.stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.joining(","));

        String numberListString = salesTop10.stream()
                .map(item -> String.valueOf(item.getNumber()))
                .collect(Collectors.joining(","));

        return SalesTop10ReportVO.builder()
                .nameList(nameListString)
                .numberList(numberListString)
                .build();
    }
}