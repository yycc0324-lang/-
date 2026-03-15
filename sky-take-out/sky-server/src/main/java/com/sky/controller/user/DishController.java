package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;//引入redis缓存，改善代码

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")

    //想改造一下代码，把菜品的搜索放到redis缓存中，磁盘读写速度太慢，所以需要定义好key和value（防止缓存穿透）
    public Result<List<DishVO>> list(Long categoryId) {
        String key = "dish_" + categoryId;//定义key就是菜的类的id
        //查redis中是否存在菜品数据
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if (list != null && list.size() > 0) {
            return Result.success(list);
        }

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品
        //查询数据库.把数据给redis
        list = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(key, list);
        return Result.success(list);
    }
    //把清理缓存写成方法
    private void clearCache(String pattern) {
        redisTemplate.delete(redisTemplate.keys(pattern));
    }

}
