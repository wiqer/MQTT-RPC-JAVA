package com.wiqer.rpc.impl.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态代理工厂 - 生成和缓存动态代理
 * 参考.NET版本的DynamicProxyFactory实现
 */
@Slf4j
@Component
public class DynamicProxyFactory {
    
    // 代理缓存 - 一级缓存
    private final Map<Class<?>, Object> proxyCache = new ConcurrentHashMap<>();
    
    // 方法信息缓存 - 二级缓存
    private final Map<String, MethodInfo> methodInfoCache = new ConcurrentHashMap<>();
    
    /**
     * 创建代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> interfaceClass, InvocationHandler handler) {
        // 检查缓存
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
        
        // 缓存代理
        proxyCache.put(interfaceClass, proxy);
        log.info("创建并缓存代理: {}", interfaceClass.getSimpleName());
        
        return (T) proxy;
    }
    
    /**
     * 创建RPC代理
     */
    public <T> T createRpcProxy(Class<T> interfaceClass, String version, RpcHandler rpcHandler) {
        return createProxy(interfaceClass, new RpcInvocationHandler(interfaceClass, version, rpcHandler));
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
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        proxyCache.clear();
        methodInfoCache.clear();
        log.info("清除代理缓存");
    }
    
    /**
     * RPC调用处理器
     */
    public static class RpcInvocationHandler implements InvocationHandler {
        
        private final Class<?> interfaceClass;
        private final String version;
        private final RpcHandler rpcHandler;
        
        public RpcInvocationHandler(Class<?> interfaceClass, String version, RpcHandler rpcHandler) {
            this.interfaceClass = interfaceClass;
            this.version = version;
            this.rpcHandler = rpcHandler;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 生成方法键
            String methodKey = generateMethodKey(interfaceClass, method, version);
            
            // 创建方法信息
            MethodInfo methodInfo = new MethodInfo(interfaceClass, method, version, args);
            
            // 调用RPC处理器
            return rpcHandler.handleRpcCall(methodInfo);
        }
        
        private String generateMethodKey(Class<?> interfaceClass, Method method, String version) {
            return version + interfaceClass.getName() + "." + method.getName();
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
        
        public Class<?> getInterfaceClass() {
            return interfaceClass;
        }
        
        public Method getMethod() {
            return method;
        }
        
        public String getVersion() {
            return version;
        }
        
        public Object[] getArguments() {
            return arguments;
        }
        
        public String getMethodKey() {
            return version + interfaceClass.getName() + "." + method.getName();
        }
    }
    
    /**
     * RPC调用处理器接口
     */
    public interface RpcHandler {
        Object handleRpcCall(MethodInfo methodInfo) throws Throwable;
    }
} 