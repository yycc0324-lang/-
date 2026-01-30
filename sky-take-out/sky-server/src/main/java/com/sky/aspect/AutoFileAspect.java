package com.sky.aspect;

import com.sky.annotation.AutoFile;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现自动填充功能
 */
@Aspect// 表示当前类是一个切面类
@Component// 表示当前类被Spring托管
public class AutoFileAspect {

    //指定切入点（对那些类的哪些方法进行拦截）
    @Pointcut("execution( * com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFile)")//* com.sky.mapper.*.*(..))指的是com.sky.mapper包及其子包下的所有方法
    //并不是所有的mapper方法都被拦截，所以就需要注释@Aspect注解，只对@AutoFill注解的方法进行拦截
    public void autoFillPointCut() {
        // 空方法体，仅作为切入点标识
    }
    //指定通知（即具体的拦截方法,因为要在执行SQL之前写时间所以使用前置通知）
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //获取当前被拦截的方法上的数据库操作类型
        //获取方法签名对象
        String methodName = joinPoint.getSignature().getName();

        // 从方法对象上获取 @AutoFile 注解
        Method method = null;
        for (Method m : joinPoint.getTarget().getClass().getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                method = m;
                break;
            }
        }

        if (method == null || !method.isAnnotationPresent(AutoFile.class)) {
            return;
        }

        AutoFile autoFile = method.getAnnotation(AutoFile.class);
        OperationType operationType = autoFile.value();

        //获取方法的实体对象
        Object[] args = joinPoint.getArgs();

        //准备赋值数据（目前在这里是时间和当前登陆用户的ID）
        if (args == null || args.length == 0) {
            return;
        }

        //根据操作类型来赋值，通过反射（比如当前是Insert就给四个值赋，Update就是俩）
        if (operationType == OperationType.INSERT) {
            //为4个属性赋值
            args[0].getClass().getMethod("setCreateTime", LocalDateTime.class).invoke(args[0], LocalDateTime.now());
            args[0].getClass().getMethod("setCreateUser", Long.class).invoke(args[0], BaseContext.getCurrentId());
            args[0].getClass().getMethod("setUpdateTime", LocalDateTime.class).invoke(args[0], LocalDateTime.now());
            args[0].getClass().getMethod("setUpdateUser", Long.class).invoke(args[0], BaseContext.getCurrentId());
        } else if (operationType == OperationType.UPDATE) {
            args[0].getClass().getMethod("setUpdateTime", LocalDateTime.class).invoke(args[0], LocalDateTime.now());
            args[0].getClass().getMethod("setUpdateUser", Long.class).invoke(args[0], BaseContext.getCurrentId());
        }
    }
}