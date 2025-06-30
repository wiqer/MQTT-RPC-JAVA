package com.wiqer.rpc.impl.core;

import lombok.Data;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 方法信息类 - 封装方法调用信息
 * 改进设计：遵循单一职责原则，只负责方法信息管理
 */
@Data
public class MethodInfo {
    
    /**
     * 方法对象
     */
    private Method method;
    
    /**
     * 包名
     */
    private String packageName;
    
    /**
     * 全限定名
     */
    private String fullName;
    
    /**
     * 请求全限定名
     */
    private String reqFullName;
    
    /**
     * 类名
     */
    private String className;
    
    /**
     * 方法名
     */
    private String name;
    
    /**
     * 返回类型
     */
    private Type responseType;
    
    /**
     * 参数类型数组
     */
    private Type[] reqTypes;
    
    /**
     * 版本号
     */
    private String version;
    
    /**
     * 方法键（用于缓存）
     */
    private String methodKey;
    
    public MethodInfo() {}
    
    public MethodInfo(Method method, String version) {
        this.method = method;
        this.version = version;
        this.name = method.getName();
        this.responseType = method.getGenericReturnType();
        this.reqTypes = method.getGenericParameterTypes();
        this.className = method.getDeclaringClass().getSimpleName();
        this.fullName = method.getDeclaringClass().getName();
        this.packageName = method.getDeclaringClass().getPackage().getName();
        this.methodKey = generateMethodKey();
    }
    
    /**
     * 生成方法键
     */
    private String generateMethodKey() {
        return version + fullName + "." + name;
    }
    
    /**
     * 获取方法键
     */
    public String getMethodKey() {
        if (methodKey == null) {
            methodKey = generateMethodKey();
        }
        return methodKey;
    }
} 