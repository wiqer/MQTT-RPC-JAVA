package com.wiqer.rpc.impl.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public  @interface EFRpcService {
    /// <summary>
    /// 版本号
    /// </summary>
     String version() ;
    /// <summary>
    /// 类的类型
    /// </summary>
    Class<?> strategyType() ;
}
