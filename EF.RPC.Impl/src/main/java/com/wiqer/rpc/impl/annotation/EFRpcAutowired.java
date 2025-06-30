package com.wiqer.rpc.impl.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC自动注入注解 - 标识需要注入的RPC服务
 * 改进设计：增加运行模式支持，遵循开闭原则
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface EFRpcAutowired {
    
    /**
     * 版本号
     */
    String version() default "v1";
    
    /**
     * 运行模式：auto(自动), syn(同步), asyn(异步)
     */
    String runMode() default "auto";
    
    /**
     * 超时时间（毫秒）
     */
    long timeout() default 5000;
    
    /**
     * 服务名称
     */
    String serviceName() default "";
}

