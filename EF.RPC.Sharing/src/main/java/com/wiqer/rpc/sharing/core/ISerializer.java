package com.wiqer.rpc.sharing.core;

import java.lang.reflect.Type;

/**
 * 序列化接口
 * 定义统一的序列化和反序列化功能，支持多种序列化格式
 * 
 * @author EFRPC Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface ISerializer {
    
    /**
     * 序列化对象
     * 
     * @param obj 要序列化的对象
     * @return 序列化后的字节数组
     * @throws RpcException 序列化失败时抛出异常
     */
    byte[] serialize(Object obj) throws RpcException;
    
    /**
     * 反序列化对象
     * 
     * @param data 序列化的字节数组
     * @param clazz 目标类型
     * @param <T> 目标类型参数
     * @return 反序列化后的对象
     * @throws RpcException 反序列化失败时抛出异常
     */
    <T> T deserialize(byte[] data, Class<T> clazz) throws RpcException;
    
    /**
     * 反序列化对象（泛型类型）
     * 
     * @param data 序列化的字节数组
     * @param type 目标类型
     * @param <T> 目标类型参数
     * @return 反序列化后的对象
     * @throws RpcException 反序列化失败时抛出异常
     */
    <T> T deserialize(byte[] data, Type type) throws RpcException;
    
    /**
     * 获取序列化器名称
     * 
     * @return 序列化器名称
     */
    String getName();
    
    /**
     * 获取支持的内容类型
     * 
     * @return 内容类型（如：application/json, application/xml等）
     */
    String getContentType();
    
    /**
     * 获取序列化器版本
     * 
     * @return 序列化器版本
     */
    String getVersion();
    
    /**
     * 检查是否支持指定类型
     * 
     * @param clazz 要检查的类型
     * @return true表示支持，false表示不支持
     */
    boolean isSupported(Class<?> clazz);
    
    /**
     * 获取序列化器描述
     * 
     * @return 序列化器描述
     */
    String getDescription();
    
    /**
     * 设置序列化器配置
     * 
     * @param config 配置对象
     */
    void setConfig(IRpcConfig config);
    
    /**
     * 获取序列化器配置
     * 
     * @return 配置对象
     */
    IRpcConfig getConfig();
} 