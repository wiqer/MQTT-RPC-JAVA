package com.wiqer.rpc.serialize;

import java.lang.reflect.Type;

public interface SerializerInterface {
    byte[] SerializeBytes(Class<?>  type, Object t);
    byte[] SerializeBytes(Object t);
    String SerializeString(Object t);
    /// <summary>
    /// 反序列化
    /// </summary>
    /// <typeparam name="T"></typeparam>
    /// <param name="content"></param>
    /// <returns></returns>
    <T> T  DeSerializeBytes(byte[] content,Class<T> tClass);

    /// <summary>
    /// 反序列化
    /// </summary>

    /// <param name="content"></param>
    /// <returns></returns>
    Object DeSerializeBytes(Class<?>  type, byte[] content);
    <T> T DeSerializeString(String content,Class<T> tClass);
    <T> T DeSerializeString(String content,Type type);
    Object DeSerializeString(Class<?>  type, String content);
}
