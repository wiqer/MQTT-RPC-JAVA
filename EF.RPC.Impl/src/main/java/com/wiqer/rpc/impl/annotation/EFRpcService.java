package com.wiqer.rpc.impl.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC服务注解 - 标识RPC服务实现类
 * 改进设计：增加配置支持，遵循开闭原则
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface EFRpcService {
    
    /**
     * 版本号
     */
    String version() default "v1";
    
    /**
     * 策略类型
     */
    Class<?> strategyType() default Object.class;
    
    /**
     * 服务名称
     */
    String serviceName() default "";
    
    /**
     * 是否启用
     */
    boolean enabled() default true;
}
