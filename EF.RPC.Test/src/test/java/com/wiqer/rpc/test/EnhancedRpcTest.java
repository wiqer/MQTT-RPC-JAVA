package com.wiqer.rpc.test;

import com.wiqer.rpc.impl.bootstrap.EnhancedRpcBootstrap;
import com.wiqer.rpc.impl.ioc.EnhancedIOCContainer;
import com.wiqer.rpc.impl.proxy.EnhancedDynamicProxyFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * 增强RPC框架测试
 * 展示改进设计的使用方式
 */
@Slf4j
public class EnhancedRpcTest {
    
    @Test
    public void testEnhancedRpcBootstrap() {
        log.info("开始测试增强的RPC框架...");
        
        // 创建启动器
        EnhancedRpcBootstrap bootstrap = new EnhancedRpcBootstrap();
        
        // 配置RPC框架
        EnhancedRpcBootstrap.RpcConfiguration config = new EnhancedRpcBootstrap.RpcConfiguration();
        config.setVersion("v1");
        config.setRunMode("auto");
        config.setTimeout(5000);
        config.setScanPackage("com.wiqer.rpc");
        config.setEnableCache(true);
        config.setEnableLogging(true);
        
        bootstrap.configure(config);
        
        // 启动RPC框架
        bootstrap.start();
        
        // 获取缓存统计信息
        EnhancedIOCContainer iocContainer = bootstrap.getIocContainer();
        EnhancedDynamicProxyFactory proxyFactory = bootstrap.getProxyFactory();
        
        log.info("IOC容器缓存统计: {}", iocContainer.getCacheStats());
        log.info("代理工厂缓存统计: {}", proxyFactory.getCacheStats());
        
        // 停止RPC框架
        bootstrap.stop();
        
        log.info("增强的RPC框架测试完成");
    }
    
    @Test
    public void testEnhancedIOCContainer() {
        log.info("开始测试增强的IOC容器...");
        
        EnhancedIOCContainer container = EnhancedIOCContainer.getInstance();
        
        // 注册Bean定义
        EnhancedIOCContainer.BeanDefinition beanDefinition = 
            new EnhancedIOCContainer.BeanDefinition("testBean", String.class, "v1");
        container.registerBeanDefinition("testBean", beanDefinition);
        
        // 注册单例Bean
        container.registerSingleton("singletonBean", "Hello World");
        
        // 获取Bean
        Object bean = container.getBean("singletonBean");
        log.info("获取到的Bean: {}", bean);
        
        // 获取缓存统计
        log.info("IOC容器缓存统计: {}", container.getCacheStats());
        
        // 清除缓存
        container.clearCache();
        
        log.info("增强的IOC容器测试完成");
    }
    
    @Test
    public void testEnhancedProxyFactory() {
        log.info("开始测试增强的代理工厂...");
        
        EnhancedDynamicProxyFactory factory = new EnhancedDynamicProxyFactory();
        
        // 创建测试接口的代理
        TestInterface proxy = factory.createProxy(TestInterface.class, 
            (proxy1, method, args) -> {
                log.info("代理方法调用: {}", method.getName());
                return "Proxy Response";
            });
        
        // 调用代理方法
        String result = proxy.testMethod();
        log.info("代理调用结果: {}", result);
        
        // 获取缓存统计
        log.info("代理工厂缓存统计: {}", factory.getCacheStats());
        
        // 清除缓存
        factory.clearCache();
        
        log.info("增强的代理工厂测试完成");
    }
    
    /**
     * 测试接口
     */
    public interface TestInterface {
        String testMethod();
    }
} 