package com.sky.controller.user;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userserShopController")
@RequestMapping("/user/shop")
@Slf4j


public class ShopController {
    public static final String KEY = "SHOP_STATUS";

    @Autowired //注入Redis的连接工厂对象
    private RedisTemplate redisTemplate;

    @GetMapping("/status")
    public Result<Integer> getstatus(){
        //从redis中获取营业状态
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("获取营业状态：{}", status != null && status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }

}
