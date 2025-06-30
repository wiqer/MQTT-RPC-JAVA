package com.wiqer.rpc.impl.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 增强的动态代理工厂
 * 改进设计：借鉴.NET版本的优秀设计，提供更好的缓存和扩展性
 */
@Slf4j
@Component
public class EnhancedDynamicProxyFactory {
    
    // 一级缓存：代理对象缓存
    private final Map<Class<?>, Object> proxyCache = new ConcurrentHashMap<>();
    
    // 二级缓存：方法信息缓存
    private final Map<String, MethodInfo> methodInfoCache = new ConcurrentHashMap<>();
    
    // 三级缓存：调用处理器缓存
    private final Map<Class<?>, InvocationHandler> handlerCache = new ConcurrentHashMap<>();
    
    /**
     * 创建代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> interfaceClass, InvocationHandler handler) {
        // 检查一级缓存
        Object cachedProxy = proxyCache.get(interfaceClass);
        if (cachedProxy != null) {
            return (T) cachedProxy;
        }
        
        // 创建新代理
        Object proxy = Proxy.newProxyInstance(
            interfaceClass.getClassLoader(),
            new Class<?>[]{interfaceClass},
            handler
        );
        
        // 缓存代理和处理器
        proxyCache.put(interfaceClass, proxy);
        handlerCache.put(interfaceClass, handler);
        
        log.info("创建并缓存代理: {}", interfaceClass.getSimpleName());
        
        return (T) proxy;
    }
    
    /**
     * 创建RPC代理
     */
    public <T> T createRpcProxy(Class<T> interfaceClass, String version, RpcInvocationHandler rpcHandler) {
        return createProxy(interfaceClass, rpcHandler);
    }
    
    /**
     * 获取方法信息
     */
    public MethodInfo getMethodInfo(String methodKey) {
        return methodInfoCache.get(methodKey);
    }
    
    /**
     * 缓存方法信息
     */
    public void cacheMethodInfo(String methodKey, MethodInfo methodInfo) {
        methodInfoCache.put(methodKey, methodInfo);
        log.debug("缓存方法信息: {}", methodKey);
    }
    
    /**
     * 获取调用处理器
     */
    public InvocationHandler getHandler(Class<?> interfaceClass) {
        return handlerCache.get(interfaceClass);
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        proxyCache.clear();
        methodInfoCache.clear();
        handlerCache.clear();
        log.info("清除所有代理缓存");
    }
    
    /**
     * 清除指定接口的缓存
     */
    public void clearCache(Class<?> interfaceClass) {
        proxyCache.remove(interfaceClass);
        handlerCache.remove(interfaceClass);
        log.info("清除代理缓存: {}", interfaceClass.getSimpleName());
    }
    
    /**
     * 获取缓存统计信息
     */
    public CacheStats getCacheStats() {
        return new CacheStats(
            proxyCache.size(),
            methodInfoCache.size(),
            handlerCache.size()
        );
    }
    
    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private final int proxyCount;
        private final int methodInfoCount;
        private final int handlerCount;
        
        public CacheStats(int proxyCount, int methodInfoCount, int handlerCount) {
            this.proxyCount = proxyCount;
            this.methodInfoCount = methodInfoCount;
            this.handlerCount = handlerCount;
        }
        
        public int getProxyCount() { return proxyCount; }
        public int getMethodInfoCount() { return methodInfoCount; }
        public int getHandlerCount() { return handlerCount; }
        
        @Override
        public String toString() {
            return String.format("CacheStats{proxy=%d, methodInfo=%d, handler=%d}", 
                proxyCount, methodInfoCount, handlerCount);
        }
    }
    
    /**
     * 方法信息类
     */
    public static class MethodInfo {
        private final Class<?> interfaceClass;
        private final Method method;
        private final String version;
        private final Object[] arguments;
        
        public MethodInfo(Class<?> interfaceClass, Method method, String version, Object[] arguments) {
            this.interfaceClass = interfaceClass;
            this.method = method;
            this.version = version;
            this.arguments = arguments;
        }
        
        public Class<?> getInterfaceClass() { return interfaceClass; }
        public Method getMethod() { return method; }
        public String getVersion() { return version; }
        public Object[] getArguments() { return arguments; }
        
        public String getMethodKey() {
            return version + interfaceClass.getName() + "." + method.getName();
        }
    }
    
    /**
     * RPC调用处理器接口
     */
    public interface RpcInvocationHandler extends InvocationHandler {
        Object handleRpcCall(MethodInfo methodInfo) throws Throwable;
    }
} 