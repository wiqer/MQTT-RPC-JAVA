package com.wiqer.rpc.impl.ioc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;

/**
 * 增强的IOC容器
 * 改进设计：借鉴.NET版本的三级缓存设计，提供更好的Bean管理
 */
@Slf4j
@Component
public class EnhancedIOCContainer {
    
    private static volatile EnhancedIOCContainer instance;
    
    // 一级缓存：Bean实例缓存（单例Bean）
    private final Map<String, Object> singletonCache = new ConcurrentHashMap<>();
    
    // 二级缓存：Bean定义缓存
    private final Map<String, BeanDefinition> beanDefinitionCache = new ConcurrentHashMap<>();
    
    // 三级缓存：方法信息缓存
    private final Map<String, Object> methodInfoCache = new ConcurrentHashMap<>();
    
    // 依赖注入缓存
    private final Map<String, Set<String>> dependencyCache = new ConcurrentHashMap<>();
    
    private EnhancedIOCContainer() {}
    
    public static EnhancedIOCContainer getInstance() {
        if (instance == null) {
            synchronized (EnhancedIOCContainer.class) {
                if (instance == null) {
                    instance = new EnhancedIOCContainer();
                }
            }
        }
        return instance;
    }
    
    /**
     * 注册单例Bean
     */
    public void registerSingleton(String beanName, Object bean) {
        singletonCache.put(beanName, bean);
        log.info("注册单例Bean: {}", beanName);
    }
    
    /**
     * 注册Bean定义
     */
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitionCache.put(beanName, beanDefinition);
        log.info("注册Bean定义: {}", beanName);
    }
    
    /**
     * 获取Bean（支持懒加载）
     */
    public Object getBean(String beanName) {
        // 检查一级缓存
        Object bean = singletonCache.get(beanName);
        if (bean != null) {
            return bean;
        }
        
        // 检查二级缓存
        BeanDefinition beanDefinition = beanDefinitionCache.get(beanName);
        if (beanDefinition != null) {
            try {
                // 创建Bean实例
                bean = createBean(beanName, beanDefinition);
                
                // 如果是单例，放入一级缓存
                if (beanDefinition.isSingleton()) {
                    singletonCache.put(beanName, bean);
                }
                
                log.info("创建Bean: {}", beanName);
                return bean;
            } catch (Exception e) {
                log.error("创建Bean失败: {}", beanName, e);
                throw new RuntimeException("创建Bean失败: " + beanName, e);
            }
        }
        
        return null;
    }
    
    /**
     * 根据类型获取Bean
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> beanClass) {
        for (Object bean : singletonCache.values()) {
            if (beanClass.isAssignableFrom(bean.getClass())) {
                return (T) bean;
            }
        }
        
        // 从Bean定义中查找
        for (BeanDefinition beanDefinition : beanDefinitionCache.values()) {
            if (beanClass.isAssignableFrom(beanDefinition.getBeanClass())) {
                return (T) getBean(beanDefinition.getBeanName());
            }
        }
        
        return null;
    }
    
    /**
     * 创建Bean实例
     */
    private Object createBean(String beanName, BeanDefinition beanDefinition) throws Exception {
        // 创建实例
        Object bean = beanDefinition.getBeanClass().newInstance();
        
        // 执行依赖注入
        performDependencyInjection(beanName, bean);
        
        return bean;
    }
    
    /**
     * 执行依赖注入
     */
    private void performDependencyInjection(String beanName, Object bean) {
        // TODO: 实现字段注入、方法注入等
        log.debug("执行依赖注入: {}", beanName);
    }
    
    /**
     * 检查Bean是否存在
     */
    public boolean containsBean(String beanName) {
        return singletonCache.containsKey(beanName) || beanDefinitionCache.containsKey(beanName);
    }
    
    /**
     * 获取所有Bean名称
     */
    public Set<String> getBeanNames() {
        Set<String> beanNames = new HashSet<>();
        beanNames.addAll(singletonCache.keySet());
        beanNames.addAll(beanDefinitionCache.keySet());
        return beanNames;
    }
    
    /**
     * 获取Bean定义
     */
    public BeanDefinition getBeanDefinition(String beanName) {
        return beanDefinitionCache.get(beanName);
    }
    
    /**
     * 缓存方法信息
     */
    public void cacheMethodInfo(String methodKey, Object methodInfo) {
        methodInfoCache.put(methodKey, methodInfo);
        log.debug("缓存方法信息: {}", methodKey);
    }
    
    /**
     * 获取方法信息
     */
    public Object getMethodInfo(String methodKey) {
        return methodInfoCache.get(methodKey);
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        singletonCache.clear();
        beanDefinitionCache.clear();
        methodInfoCache.clear();
        dependencyCache.clear();
        log.info("清除所有缓存");
    }
    
    /**
     * 获取缓存统计信息
     */
    public CacheStats getCacheStats() {
        return new CacheStats(
            singletonCache.size(),
            beanDefinitionCache.size(),
            methodInfoCache.size(),
            dependencyCache.size()
        );
    }
    
    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private final int singletonCount;
        private final int beanDefinitionCount;
        private final int methodInfoCount;
        private final int dependencyCount;
        
        public CacheStats(int singletonCount, int beanDefinitionCount, 
                         int methodInfoCount, int dependencyCount) {
            this.singletonCount = singletonCount;
            this.beanDefinitionCount = beanDefinitionCount;
            this.methodInfoCount = methodInfoCount;
            this.dependencyCount = dependencyCount;
        }
        
        public int getSingletonCount() { return singletonCount; }
        public int getBeanDefinitionCount() { return beanDefinitionCount; }
        public int getMethodInfoCount() { return methodInfoCount; }
        public int getDependencyCount() { return dependencyCount; }
        
        @Override
        public String toString() {
            return String.format("CacheStats{singleton=%d, beanDefinition=%d, methodInfo=%d, dependency=%d}", 
                singletonCount, beanDefinitionCount, methodInfoCount, dependencyCount);
        }
    }
    
    /**
     * Bean定义类
     */
    public static class BeanDefinition {
        private final String beanName;
        private final Class<?> beanClass;
        private final String version;
        private final boolean singleton;
        
        public BeanDefinition(String beanName, Class<?> beanClass, String version, boolean singleton) {
            this.beanName = beanName;
            this.beanClass = beanClass;
            this.version = version;
            this.singleton = singleton;
        }
        
        public BeanDefinition(String beanName, Class<?> beanClass, String version) {
            this(beanName, beanClass, version, true);
        }
        
        public String getBeanName() { return beanName; }
        public Class<?> getBeanClass() { return beanClass; }
        public String getVersion() { return version; }
        public boolean isSingleton() { return singleton; }
    }
} 