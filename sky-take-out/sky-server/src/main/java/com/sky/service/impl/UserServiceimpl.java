package com.sky.service.impl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import io.netty.channel.unix.Errors;
import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceimpl implements UserService {

    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;
    @Override
    public User wxlogin(UserLoginDTO userLoginDTO) {

        String openid = user(userLoginDTO.getCode());  //这里的微信登录方法原来是写在这里的，现在移动到下面封装成函数调用了，优雅

        //判断openid是否为空，为空登录失败，抛出异常
        if (openid == null){
           log.warn("微信接口返回openid为空，使用测试openid");
           // 临时使用已知的openid进行测试
        }
        User user = userMapper.getByOpenid(openid);

        //判断当前用户是否为新用户，是新用户自动完成注册
        if (user == null){
                user = User.builder()
                        .openid(openid)
                        .createTime(LocalDateTime.now())
                        .build();
        userMapper.insert(user);//未来不报错需要写xml
        }
        return user;
    }
    private String user(String code){   //如果要写成上面的方法就需要自己去写程序来获得code，  map.put("js_code", userLoginDTO.getCode());但是使用方法传参数就把userLoginDTO.getCode()当成参数写（），优雅
        Map<String, String> map =new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, map);//把获取的数据都给json
        log.info("微信接口返回的数据：{}", json);
        JSONObject jsonObject = JSONObject.parseObject(json);//把数据给jsonObject
        String openid = jsonObject.getString("openid");//使用微信接口用刚刚所有的数据获取openid，此处的openid是微信接口获取的
        //叫做openid的属性数据，就是openid
        // 就是JSONObject.parseObject(json).getString("openid")
        log.info("解析出的openid：{}", openid);
        return openid;
    }
}
