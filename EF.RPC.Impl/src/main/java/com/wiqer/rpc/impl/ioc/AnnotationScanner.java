package com.wiqer.rpc.impl.ioc;

import com.wiqer.rpc.impl.annotation.EFRpcAutowired;
import com.wiqer.rpc.impl.annotation.EFRpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * 注解扫描器 - 自动扫描和注册RPC服务
 * 参考.NET版本的扫描注入机制
 */
@Slf4j
@Component
public class AnnotationScanner {
    
    private final IOCContainer iocContainer = IOCContainer.getInstance();
    
    /**
     * 扫描指定包下的所有类
     */
    public void scanPackage(String basePackage) {
        log.info("开始扫描包: {}", basePackage);
        
        try {
            // 获取包下所有类
            Set<Class<?>> classes = ClassScanner.scanClasses(basePackage);
            
            // 注册服务
            registerServices(classes);
            
            // 注入依赖
            injectDependencies(classes);
            
            log.info("包扫描完成: {}", basePackage);
        } catch (Exception e) {
            log.error("扫描包失败: {}", basePackage, e);
            throw new RuntimeException("扫描包失败: " + basePackage, e);
        }
    }
    
    /**
     * 注册所有带@EFRpcService注解的服务
     */
    private void registerServices(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            EFRpcService serviceAnnotation = clazz.getAnnotation(EFRpcService.class);
            if (serviceAnnotation != null) {
                registerService(clazz, serviceAnnotation);
            }
        }
    }
    
    /**
     * 注册单个服务
     */
    private void registerService(Class<?> serviceClass, EFRpcService annotation) {
        try {
            String version = annotation.version();
            Class<?> strategyType = annotation.strategyType();
            
            // 创建Bean定义
            String beanName = generateBeanName(serviceClass, version);
            IOCContainer.BeanDefinition beanDefinition = 
                new IOCContainer.BeanDefinition(beanName, serviceClass, version);
            
            // 注册Bean定义
            iocContainer.registerBeanDefinition(beanName, beanDefinition);
            
            // 创建并注册Bean实例
            Object serviceInstance = serviceClass.newInstance();
            iocContainer.registerBean(beanName, serviceInstance);
            
            log.info("注册RPC服务: {} (版本: {})", serviceClass.getSimpleName(), version);
            
        } catch (Exception e) {
            log.error("注册服务失败: {}", serviceClass.getName(), e);
            throw new RuntimeException("注册服务失败: " + serviceClass.getName(), e);
        }
    }
    
    /**
     * 注入所有带@EFRpcAutowired注解的依赖
     */
    private void injectDependencies(Set<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            injectDependenciesForClass(clazz);
        }
    }
    
    /**
     * 为单个类注入依赖
     */
    private void injectDependenciesForClass(Class<?> clazz) {
        try {
            // 获取已注册的Bean实例
            Object beanInstance = iocContainer.getBean(clazz);
            if (beanInstance == null) {
                return; // 不是已注册的Bean，跳过
            }
            
            // 扫描字段上的@EFRpcAutowired注解
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                EFRpcAutowired autowiredAnnotation = field.getAnnotation(EFRpcAutowired.class);
                if (autowiredAnnotation != null) {
                    injectField(beanInstance, field, autowiredAnnotation);
                }
            }
            
        } catch (Exception e) {
            log.error("注入依赖失败: {}", clazz.getName(), e);
            throw new RuntimeException("注入依赖失败: " + clazz.getName(), e);
        }
    }
    
    /**
     * 注入单个字段
     */
    private void injectField(Object beanInstance, Field field, EFRpcAutowired annotation) {
        try {
            field.setAccessible(true);
            
            String version = annotation.version();
            Class<?> fieldType = field.getType();
            
            // 根据类型和版本查找对应的Bean
            Object dependency = findDependencyByTypeAndVersion(fieldType, version);
            
            if (dependency != null) {
                field.set(beanInstance, dependency);
                log.info("注入依赖: {} -> {}", field.getName(), dependency.getClass().getSimpleName());
            } else {
                log.warn("未找到匹配的依赖: {} (类型: {}, 版本: {})", 
                    field.getName(), fieldType.getSimpleName(), version);
            }
            
        } catch (Exception e) {
            log.error("注入字段失败: {}", field.getName(), e);
            throw new RuntimeException("注入字段失败: " + field.getName(), e);
        }
    }
    
    /**
     * 根据类型和版本查找依赖
     */
    private Object findDependencyByTypeAndVersion(Class<?> type, String version) {
        // 遍历所有Bean，查找匹配的类型和版本
        for (String beanName : iocContainer.getBeanNames()) {
            Object bean = iocContainer.getBean(beanName);
            if (bean != null && type.isAssignableFrom(bean.getClass())) {
                // 检查版本是否匹配（这里简化处理，实际可能需要更复杂的版本匹配逻辑）
                IOCContainer.BeanDefinition definition = iocContainer.getBeanDefinition(beanName);
                if (definition != null && version.equals(definition.getVersion())) {
                    return bean;
                }
            }
        }
        return null;
    }
    
    /**
     * 生成Bean名称
     */
    private String generateBeanName(Class<?> clazz, String version) {
        return version + clazz.getSimpleName();
    }
} 