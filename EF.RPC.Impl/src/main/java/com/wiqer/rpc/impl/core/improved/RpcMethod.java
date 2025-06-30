package com.wiqer.rpc.impl.core.improved;

import java.lang.reflect.Method;

/**
 * RPC方法信息
 * 改进设计：封装方法信息，提供更好的类型安全
 */
public class RpcMethod {
    
    private final String methodName;
    private final Method method;
    private final Class<?>[] parameterTypes;
    private final Class<?> returnType;
    private final String methodKey;
    private final String version;
    
    public RpcMethod(String methodName, Method method, String version) {
        this.methodName = methodName;
        this.method = method;
        this.parameterTypes = method.getParameterTypes();
        this.returnType = method.getReturnType();
        this.version = version;
        this.methodKey = generateMethodKey();
    }
    
    /**
     * 生成方法键
     */
    private String generateMethodKey() {
        StringBuilder sb = new StringBuilder();
        sb.append(version).append(".").append(methodName);
        if (parameterTypes.length > 0) {
            sb.append("(");
            for (int i = 0; i < parameterTypes.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(parameterTypes[i].getSimpleName());
            }
            sb.append(")");
        }
        return sb.toString();
    }
    
    // Getters
    public String getMethodName() {
        return methodName;
    }
    
    public Method getMethod() {
        return method;
    }
    
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }
    
    public Class<?> getReturnType() {
        return returnType;
    }
    
    public String getMethodKey() {
        return methodKey;
    }
    
    public String getVersion() {
        return version;
    }
    
    @Override
    public String toString() {
        return "RpcMethod{" +
                "methodName='" + methodName + '\'' +
                ", methodKey='" + methodKey + '\'' +
                ", version='" + version + '\'' +
                ", returnType=" + returnType.getSimpleName() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcMethod rpcMethod = (RpcMethod) o;
        return methodKey.equals(rpcMethod.methodKey);
    }
    
    @Override
    public int hashCode() {
        return methodKey.hashCode();
    }
} 