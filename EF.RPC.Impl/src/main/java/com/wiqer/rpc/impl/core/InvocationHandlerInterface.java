package com.wiqer.rpc.impl.core;

import java.lang.reflect.Method;

/**
 * 调用处理器接口 - 统一Java和.NET版本的调用处理机制
 */
public interface InvocationHandlerInterface {
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
}
