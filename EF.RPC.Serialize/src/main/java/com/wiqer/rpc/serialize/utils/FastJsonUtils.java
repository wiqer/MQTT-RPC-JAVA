package com.wiqer.rpc.serialize.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.JSONLibDataFormatSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class FastJsonUtils {

    private static final SerializeConfig config;

    static {
        config = new SerializeConfig();
        config.put(java.util.Date.class, new JSONLibDataFormatSerializer()); // 使用和json-lib兼容的日期输出格式
        config.put(java.sql.Date.class, new JSONLibDataFormatSerializer()); // 使用和json-lib兼容的日期输出格式
    }

    private static final SerializerFeature[] features = {SerializerFeature.WriteMapNullValue, // 输出空置字段
            SerializerFeature.WriteNullListAsEmpty, // list字段如果为null，输出为[]，而不是null
            SerializerFeature.WriteNullNumberAsZero, // 数值字段如果为null，输出为0，而不是null
            SerializerFeature.WriteNullBooleanAsFalse, // Boolean字段如果为null，输出为false，而不是null
            SerializerFeature.WriteNullStringAsEmpty // 字符类型字段如果为null，输出为""，而不是null
    };


    public static String convertObjectToJSON(Object object) {
        return JSON.toJSONString(object, config, features);
    }

    public static String toJSONNoFeatures(Object object) {
        return JSON.toJSONString(object, config);
    }

    public static Object toBean(String text) {
        return JSON.parse(text);
    }
    public static byte[] SerializeBytes( Object t) {
        return JSON.toJSONBytes(t);
    }
    public static <T> T DeSerializeBytes(byte[] content, Class<T> tClass) {
        return JSON.parseObject(content,tClass);
    }
    public static <T> T toBean(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }
    // 转换为数组
    public static Object[] toArray(String text) {
        return toArray(text, null);
    }

    // 转换为数组
    public static <T> Object[] toArray(String text, Class<T> clazz) {
        return JSON.parseArray(text, clazz).toArray();
    }

    // 转换为List
    public static <T> List<T> toList(String text, Class<T> clazz) {
        return JSON.parseArray(text, clazz);
    }

    /**
     * 将string转化为序列化的json字符串
     */
    public static Object textToJson(String text) {
        return JSON.parse(text);
    }

    /**
     * json字符串转化为map
     */
    public static <K, V> Map<K, V> stringToCollect(String s) {
        return (Map<K, V>) JSONObject.parseObject(s);
    }

    /**
     * 转换JSON字符串为对象
     */
    public static Object convertJsonToObject(String jsonData, Class<?> clazz) {
        return JSONObject.parseObject(jsonData, clazz);
    }

    public static Object convertJSONToObject(String content, Class<?> clazz) {
        return JSONObject.parseObject(content, clazz);
    }
    public static <T> T convertJSONToObject(String content, Type type) {
        return JSONObject.parseObject(content, type);
    }
    /**
     * 将map转化为string
     */
    public static <K, V> String collectToString(Map<K, V> m) {
        return JSONObject.toJSONString(m);
    }

}
