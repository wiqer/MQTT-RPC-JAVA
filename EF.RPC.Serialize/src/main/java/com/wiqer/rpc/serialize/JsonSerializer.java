package com.wiqer.rpc.serialize;


import com.wiqer.rpc.serialize.utils.FastJsonUtils;

import java.lang.reflect.Type;

public class JsonSerializer implements SerializerInterface {
    static {
        ins =new JsonSerializer();
    }
    public static JsonSerializer ins ;

    public static JsonSerializer getSerializer() {

        return ins;
    }
    @Override
    public byte[] SerializeBytes(Class<?> type, Object t) {
        return FastJsonUtils.SerializeBytes(t);
    }

    @Override
    public byte[] SerializeBytes(Object t) {
        return FastJsonUtils.SerializeBytes(t);
    }

    @Override
    public String SerializeString(Object t) {
        return FastJsonUtils.toJSONNoFeatures(t);
    }

    @Override
    public <T> T DeSerializeBytes(byte[] content, Class<T> tClass) {
        return FastJsonUtils.DeSerializeBytes(content,tClass);
    }

    @Override
    public Object DeSerializeBytes(Class<?>  tClass, byte[] content) {
        return FastJsonUtils.DeSerializeBytes(content,tClass);
    }

    @Override
    public <T> T DeSerializeString(String content, Class<T> tClass) {
        return FastJsonUtils.toBean(content,tClass);
    }
    public <T> T DeSerializeString(String content, Type type){
        return FastJsonUtils.convertJSONToObject(content,type);
    }
    @Override
    public Object DeSerializeString(Class<?>  type, String content) {
        return FastJsonUtils.convertJSONToObject(content,type);
    }
}
