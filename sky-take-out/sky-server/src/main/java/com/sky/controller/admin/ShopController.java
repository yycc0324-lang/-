package com.sky.controller.admin;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")//为了避免bin冲突，加个admin
@RequestMapping("/admin/shop")
@Slf4j


public class ShopController {
    public static final String KEY = "SHOP_STATUS";

    @Autowired //注入Redis的连接工厂对象
    private RedisTemplate redisTemplate;
    //设置营业状态
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){//这个注解的作用是把路径参数status的值赋给status变量
        log.info("设置营业状态：{}",status==1?"营业中":"打烊中");//三目运算符
        redisTemplate.opsForValue().set(KEY,status);//设置redis（String）的key为SHOP_STATUS，value为status
        return Result.success();
    }
    @GetMapping("/status")
    public Result<Integer> getstatus(){
        //从redis中获取营业状态
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("获取营业状态：{}", status != null && status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }

}
