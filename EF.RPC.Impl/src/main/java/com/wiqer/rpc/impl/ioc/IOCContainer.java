package com.wiqer.rpc.impl.ioc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * IOC容器 - 管理Bean的注册、获取和缓存
 * 参考.NET版本的三级缓存设计
 */
@Slf4j
@Component
public class IOCContainer {
    
    private static volatile IOCContainer instance;
    
    // 一级缓存：Bean实例缓存
    private final Map<String, Object> beanCache = new ConcurrentHashMap<>();
    
    // 二级缓存：Bean定义缓存
    private final Map<String, BeanDefinition> beanDefinitionCache = new ConcurrentHashMap<>();
    
    // 三级缓存：方法信息缓存（预留）
    private final Map<String, Object> methodInfoCache = new ConcurrentHashMap<>();
    
    private IOCContainer() {}
    
    public static IOCContainer getInstance() {
        if (instance == null) {
            synchronized (IOCContainer.class) {
                if (instance == null) {
                    instance = new IOCContainer();
                }
            }
        }
        return instance;
    }
    
    /**
     * 注册Bean
     */
    public void registerBean(String beanName, Object bean) {
        beanCache.put(beanName, bean);
        log.info("注册Bean: {}", beanName);
    }
    
    /**
     * 注册Bean定义
     */
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitionCache.put(beanName, beanDefinition);
        log.info("注册Bean定义: {}", beanName);
    }
    
    /**
     * 获取Bean
     */
    public Object getBean(String beanName) {
        Object bean = beanCache.get(beanName);
        if (bean != null) {
            return bean;
        }
        
        // 如果缓存中没有，尝试从Bean定义创建
        BeanDefinition beanDefinition = beanDefinitionCache.get(beanName);
        if (beanDefinition != null) {
            try {
                bean = beanDefinition.getBeanClass().newInstance();
                beanCache.put(beanName, bean);
                log.info("创建并缓存Bean: {}", beanName);
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
        for (Object bean : beanCache.values()) {
            if (beanClass.isAssignableFrom(bean.getClass())) {
                return (T) bean;
            }
        }
        return null;
    }
    
    /**
     * 检查Bean是否存在
     */
    public boolean containsBean(String beanName) {
        return beanCache.containsKey(beanName) || beanDefinitionCache.containsKey(beanName);
    }
    
    /**
     * 获取所有Bean名称
     */
    public Set<String> getBeanNames() {
        Set<String> beanNames = new HashSet<>();
        beanNames.addAll(beanCache.keySet());
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
     * 清除缓存
     */
    public void clearCache() {
        beanCache.clear();
        beanDefinitionCache.clear();
        methodInfoCache.clear();
        log.info("清除所有缓存");
    }
    
    /**
     * Bean定义类
     */
    public static class BeanDefinition {
        private final String beanName;
        private final Class<?> beanClass;
        private final String version;
        
        public BeanDefinition(String beanName, Class<?> beanClass, String version) {
            this.beanName = beanName;
            this.beanClass = beanClass;
            this.version = version;
        }
        
        public String getBeanName() {
            return beanName;
        }
        
        public Class<?> getBeanClass() {
            return beanClass;
        }
        
        public String getVersion() {
            return version;
        }
    }
} 