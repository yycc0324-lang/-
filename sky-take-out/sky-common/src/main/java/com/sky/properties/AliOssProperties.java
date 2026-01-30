package com.sky.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component// 表示当前类被Spring托管，项目启动时自动加载
@ConfigurationProperties(prefix = "sky.alioss")// 表示将application.yml中的sky.alioss.*属性值，映射到当前这个组件中，就是把配置文件中的属性值注入到当前这个组件中
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AliOssProperties {

    private String endpoint;//自动赋值为application.yml中的sky.alioss.endpoint
    private String accessKeyId;//自动赋值为application.yml中的sky.alioss.accessKeyId
    private String accessKeySecret;//同上
    private String bucketName;

}
