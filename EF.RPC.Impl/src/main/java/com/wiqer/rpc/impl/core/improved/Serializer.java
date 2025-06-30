package com.wiqer.rpc.impl.core.improved;

/**
 * 序列化接口
 * 改进设计：支持多种序列化格式，提供缓存机制
 */
public interface Serializer {
    
    /**
     * 序列化对象为字节数组
     */
    byte[] serialize(Object obj);
    
    /**
     * 反序列化字节数组为对象
     */
    <T> T deserialize(byte[] data, Class<T> clazz);
    
    /**
     * 序列化对象为字符串
     */
    String serializeToString(Object obj);
    
    /**
     * 反序列化字符串为对象
     */
    <T> T deserializeFromString(String data, Class<T> clazz);
    
    /**
     * 获取序列化器名称
     */
    String getSerializerName();
    
    /**
     * 获取序列化器版本
     */
    String getSerializerVersion();
    
    /**
     * 检查是否支持指定的类型
     */
    boolean supportsType(Class<?> clazz);
    
    /**
     * 获取序列化后的数据大小
     */
    int getSerializedSize(Object obj);
    
    /**
     * 是否启用压缩
     */
    boolean isCompressionEnabled();
    
    /**
     * 设置压缩开关
     */
    void setCompressionEnabled(boolean enabled);
    
    /**
     * 是否启用缓存
     */
    boolean isCacheEnabled();
    
    /**
     * 设置缓存开关
     */
    void setCacheEnabled(boolean enabled);
    
    /**
     * 清空缓存
     */
    void clearCache();
    
    /**
     * 获取缓存统计信息
     */
    CacheStats getCacheStats();
    
    /**
     * 缓存统计信息
     */
    class CacheStats {
        private final long hitCount;
        private final long missCount;
        private final long evictionCount;
        private final long totalSize;
        
        public CacheStats(long hitCount, long missCount, long evictionCount, long totalSize) {
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.evictionCount = evictionCount;
            this.totalSize = totalSize;
        }
        
        public long getHitCount() {
            return hitCount;
        }
        
        public long getMissCount() {
            return missCount;
        }
        
        public long getEvictionCount() {
            return evictionCount;
        }
        
        public long getTotalSize() {
            return totalSize;
        }
        
        public double getHitRate() {
            long total = hitCount + missCount;
            return total > 0 ? (double) hitCount / total : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format("CacheStats{hitRate=%.2f%%, hits=%d, misses=%d, evictions=%d, size=%d}",
                    getHitRate() * 100, hitCount, missCount, evictionCount, totalSize);
        }
    }
} 