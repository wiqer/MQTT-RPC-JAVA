package com.wiqer.rpc.impl.core.improved;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 高性能JSON序列化器
 * 改进设计：使用缓存机制，优化性能
 */
public class OptimizedJsonSerializer implements Serializer {
    
    private static final Logger logger = LoggerFactory.getLogger(OptimizedJsonSerializer.class);
    
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Class<?>, JsonNode> schemaCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, byte[]> serializationCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Class<?>> deserializationCache = new ConcurrentHashMap<>();
    
    private boolean compressionEnabled = false;
    private boolean cacheEnabled = true;
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong evictionCount = new AtomicLong(0);
    
    public OptimizedJsonSerializer() {
        this.objectMapper = new ObjectMapper();
        configureObjectMapper();
    }
    
    public OptimizedJsonSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        configureObjectMapper();
    }
    
    /**
     * 配置ObjectMapper
     */
    private void configureObjectMapper() {
        // 性能优化配置
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        
        // 启用缓存
        objectMapper.configure(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID, true);
    }
    
    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            return new byte[0];
        }
        
        try {
            String cacheKey = generateCacheKey(obj);
            
            if (cacheEnabled) {
                byte[] cached = serializationCache.get(cacheKey);
                if (cached != null) {
                    hitCount.incrementAndGet();
                    return cached;
                }
                missCount.incrementAndGet();
            }
            
            byte[] result = objectMapper.writeValueAsBytes(obj);
            
            if (cacheEnabled && result.length < 1024) { // 只缓存小对象
                serializationCache.put(cacheKey, result);
            }
            
            return result;
            
        } catch (JsonProcessingException e) {
            throw RpcException.serializationError("Failed to serialize object: " + obj.getClass().getName(), e);
        }
    }
    
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        if (data == null || data.length == 0) {
            return null;
        }
        
        try {
            return objectMapper.readValue(data, clazz);
        } catch (Exception e) {
            throw RpcException.serializationError("Failed to deserialize to " + clazz.getName(), e);
        }
    }
    
    @Override
    public String serializeToString(Object obj) {
        if (obj == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw RpcException.serializationError("Failed to serialize object to string: " + obj.getClass().getName(), e);
        }
    }
    
    @Override
    public <T> T deserializeFromString(String data, Class<T> clazz) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.readValue(data, clazz);
        } catch (Exception e) {
            throw RpcException.serializationError("Failed to deserialize string to " + clazz.getName(), e);
        }
    }
    
    @Override
    public String getSerializerName() {
        return "OptimizedJsonSerializer";
    }
    
    @Override
    public String getSerializerVersion() {
        return "1.0.0";
    }
    
    @Override
    public boolean supportsType(Class<?> clazz) {
        // JSON序列化器支持所有类型
        return true;
    }
    
    @Override
    public int getSerializedSize(Object obj) {
        if (obj == null) {
            return 0;
        }
        
        try {
            return objectMapper.writeValueAsBytes(obj).length;
        } catch (JsonProcessingException e) {
            logger.warn("Failed to calculate serialized size for object: {}", obj.getClass().getName(), e);
            return -1;
        }
    }
    
    @Override
    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }
    
    @Override
    public void setCompressionEnabled(boolean enabled) {
        this.compressionEnabled = enabled;
        if (enabled) {
            logger.info("Compression enabled for JSON serializer");
        }
    }
    
    @Override
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }
    
    @Override
    public void setCacheEnabled(boolean enabled) {
        this.cacheEnabled = enabled;
        if (!enabled) {
            clearCache();
        }
        logger.info("Cache {} for JSON serializer", enabled ? "enabled" : "disabled");
    }
    
    @Override
    public void clearCache() {
        serializationCache.clear();
        deserializationCache.clear();
        schemaCache.clear();
        hitCount.set(0);
        missCount.set(0);
        evictionCount.set(0);
        logger.info("Cache cleared for JSON serializer");
    }
    
    @Override
    public CacheStats getCacheStats() {
        return new CacheStats(
            hitCount.get(),
            missCount.get(),
            evictionCount.get(),
            serializationCache.size()
        );
    }
    
    /**
     * 生成缓存键
     */
    private String generateCacheKey(Object obj) {
        return obj.getClass().getName() + ":" + obj.hashCode();
    }
    
    /**
     * 获取ObjectMapper实例
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
    
    /**
     * 预热缓存
     */
    public void warmupCache(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            try {
                // 创建示例对象并序列化以预热缓存
                Object sample = createSampleObject(clazz);
                if (sample != null) {
                    serialize(sample);
                    logger.debug("Warmed up cache for class: {}", clazz.getName());
                }
            } catch (Exception e) {
                logger.warn("Failed to warm up cache for class: {}", clazz.getName(), e);
            }
        }
    }
    
    /**
     * 创建示例对象（用于缓存预热）
     */
    private Object createSampleObject(Class<?> clazz) {
        try {
            // 尝试使用默认构造函数
            return clazz.newInstance();
        } catch (Exception e) {
            // 如果无法创建实例，返回null
            return null;
        }
    }
    
    /**
     * 获取序列化缓存大小
     */
    public int getSerializationCacheSize() {
        return serializationCache.size();
    }
    
    /**
     * 获取反序列化缓存大小
     */
    public int getDeserializationCacheSize() {
        return deserializationCache.size();
    }
    
    /**
     * 获取模式缓存大小
     */
    public int getSchemaCacheSize() {
        return schemaCache.size();
    }
} 