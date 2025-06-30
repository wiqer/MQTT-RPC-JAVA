package com.wiqer.rpc.impl.core;

import lombok.extern.slf4j.Slf4j;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 方法注册表 - 管理RPC方法信息
 * 改进设计：替代MsgControllersMap，遵循单一职责原则
 */
@Slf4j
public class MethodRegistry {
    
    /**
     * 方法信息映射表
     */
    private final ConcurrentHashMap<String, MethodInfo> methodMap;
    
    /**
     * 版本号
     */
    private final String version;
    
    /**
     * 接口全限定名
     */
    private final String interfaceFullName;
    
    public MethodRegistry(String version, String interfaceFullName) {
        this.methodMap = new ConcurrentHashMap<>();
        this.version = version;
        this.interfaceFullName = interfaceFullName;
    }
    
    /**
     * 注册方法
     */
    public void registerMethod(String methodName, MethodInfo methodInfo) {
        String key = generateKey(methodName);
        methodMap.put(key, methodInfo);
        log.info("注册方法: {} -> {}", key, methodInfo.getName());
    }
    
    /**
     * 获取方法信息
     */
    public MethodInfo getMethod(String methodName) {
        String key = generateKey(methodName);
        return methodMap.get(key);
    }
    
    /**
     * 检查方法是否存在
     */
    public boolean containsMethod(String methodName) {
        String key = generateKey(methodName);
        return methodMap.containsKey(key);
    }
    
    /**
     * 获取所有方法名
     */
    public Collection<String> getMethodNames() {
        return methodMap.keySet();
    }
    
    /**
     * 获取所有方法信息
     */
    public Collection<MethodInfo> getMethods() {
        return methodMap.values();
    }
    
    /**
     * 获取方法数量
     */
    public int size() {
        return methodMap.size();
    }
    
    /**
     * 检查是否为空
     */
    public boolean isEmpty() {
        return methodMap.isEmpty();
    }
    
    /**
     * 清除所有方法
     */
    public void clear() {
        methodMap.clear();
        log.info("清除方法注册表");
    }
    
    /**
     * 生成方法键
     */
    private String generateKey(String methodName) {
        return version + interfaceFullName + "." + methodName;
    }
    
    /**
     * 获取版本号
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * 获取接口全限定名
     */
    public String getInterfaceFullName() {
        return interfaceFullName;
    }
} 