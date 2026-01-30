package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识需要自动填充的属性
 */
@Target(ElementType.METHOD)// 表示该注解只能用于方法上
@Retention(RetentionPolicy.RUNTIME)// 表示该注解在运行时保留.这个是必须写的，记住就行
public @interface AutoFile {
    //指定数据库操作类型，比如我想在插入数据时，自动填充创建时间和更新时间，就可以指定insert和update
    //查询操作就没有必要写了，因为查询操作不需要自动填充
    OperationType value();
}
