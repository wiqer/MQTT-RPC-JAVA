package com.wiqer.rpc.serialize;

import com.wiqer.rpc.serialize.utils.FastJsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;

/**
 * JSON序列化器 - 增强版
 * 支持泛型反序列化和字节数组与字符串双格式
 */
@Slf4j
public class JsonSerializer implements SerializerInterface {
    
    static {
        ins = new JsonSerializer();
    }
    
    public static JsonSerializer ins;

    public static JsonSerializer getSerializer() {
        return ins;
    }
    
    @Override
    public byte[] SerializeBytes(Class<?> type, Object t) {
        try {
            return FastJsonUtils.SerializeBytes(t);
        } catch (Exception e) {
            log.error("序列化对象失败: {}", t, e);
            throw new RuntimeException("序列化对象失败", e);
        }
    }

    @Override
    public byte[] SerializeBytes(Object t) {
        try {
            return FastJsonUtils.SerializeBytes(t);
        } catch (Exception e) {
            log.error("序列化对象失败: {}", t, e);
            throw new RuntimeException("序列化对象失败", e);
        }
    }

    @Override
    public String SerializeString(Object t) {
        try {
            return FastJsonUtils.toJSONNoFeatures(t);
        } catch (Exception e) {
            log.error("序列化对象为字符串失败: {}", t, e);
            throw new RuntimeException("序列化对象为字符串失败", e);
        }
    }

    @Override
    public <T> T DeSerializeBytes(byte[] content, Class<T> tClass) {
        try {
            return FastJsonUtils.DeSerializeBytes(content, tClass);
        } catch (Exception e) {
            log.error("反序列化字节数组失败: {}", tClass.getName(), e);
            throw new RuntimeException("反序列化字节数组失败", e);
        }
    }

    @Override
    public Object DeSerializeBytes(Class<?> tClass, byte[] content) {
        try {
            return FastJsonUtils.DeSerializeBytes(content, tClass);
        } catch (Exception e) {
            log.error("反序列化字节数组失败: {}", tClass.getName(), e);
            throw new RuntimeException("反序列化字节数组失败", e);
        }
    }

    @Override
    public <T> T DeSerializeString(String content, Class<T> tClass) {
        try {
            return FastJsonUtils.toBean(content, tClass);
        } catch (Exception e) {
            log.error("反序列化字符串失败: {}", tClass.getName(), e);
            throw new RuntimeException("反序列化字符串失败", e);
        }
    }
    
    /**
     * 泛型反序列化 - 支持复杂泛型类型
     */
    public <T> T DeSerializeString(String content, Type type) {
        try {
            return FastJsonUtils.convertJSONToObject(content, type);
        } catch (Exception e) {
            log.error("泛型反序列化字符串失败: {}", type.getTypeName(), e);
            throw new RuntimeException("泛型反序列化字符串失败", e);
        }
    }
    
    /**
     * 泛型反序列化字节数组 - 支持复杂泛型类型
     */
    public <T> T DeSerializeBytes(byte[] content, Type type) {
        try {
            String jsonString = new String(content, "UTF-8");
            return DeSerializeString(jsonString, type);
        } catch (Exception e) {
            log.error("泛型反序列化字节数组失败: {}", type.getTypeName(), e);
            throw new RuntimeException("泛型反序列化字节数组失败", e);
        }
    }
    
    @Override
    public Object DeSerializeString(Class<?> type, String content) {
        try {
            return FastJsonUtils.convertJSONToObject(content, type);
        } catch (Exception e) {
            log.error("反序列化字符串失败: {}", type.getName(), e);
            throw new RuntimeException("反序列化字符串失败", e);
        }
    }
    
    /**
     * 检查序列化是否支持指定类型
     */
    public boolean isSerializable(Class<?> clazz) {
        try {
            // 尝试序列化一个空对象来检查类型是否支持
            Object testObj = clazz.newInstance();
            SerializeBytes(testObj);
            return true;
        } catch (Exception e) {
            log.debug("类型不支持序列化: {}", clazz.getName());
            return false;
        }
    }
    
    /**
     * 获取序列化后的字节大小
     */
    public int getSerializedSize(Object obj) {
        try {
            byte[] bytes = SerializeBytes(obj);
            return bytes != null ? bytes.length : 0;
        } catch (Exception e) {
            log.warn("获取序列化大小失败: {}", obj, e);
            return 0;
        }
    }
}
