package com.wiqer.rpc.serialize;

import java.lang.reflect.Type;

/**
 * 序列化接口 - 统一Java和.NET版本的序列化机制
 * 参考.NET版本的序列化接口设计
 */
public interface SerializerInterface {
    
    /**
     * 序列化为字节数组
     */
    byte[] SerializeBytes(Class<?> type, Object t);
    
    /**
     * 序列化为字节数组（泛型）
     */
    byte[] SerializeBytes(Object t);
    
    /**
     * 序列化为字符串
     */
    String SerializeString(Object t);
    
    /**
     * 从字节数组反序列化
     */
    <T> T DeSerializeBytes(byte[] content, Class<T> tClass);
    
    /**
     * 从字符串反序列化
     */
    <T> T DeSerializeString(String content, Class<T> tClass);
    
    /**
     * 从字符串反序列化（支持泛型）
     */
    <T> T DeSerializeString(String content, Type type);
    
    /**
     * 从字节数组反序列化（支持泛型）
     */
    <T> T DeSerializeBytes(byte[] content, Type type);
    
    /**
     * 检查序列化器是否支持指定类型
     */
    boolean isSupported(Class<?> type);
    
    /**
     * 获取序列化器名称
     */
    String getSerializerName();
}
