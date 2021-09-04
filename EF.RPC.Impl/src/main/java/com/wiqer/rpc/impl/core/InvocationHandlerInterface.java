package com.wiqer.rpc.impl.core;

import sun.management.MethodInfo;

public interface InvocationHandlerInterface {
    Object invoke(Object proxy, MethodInfo method, Object[] args) throws Throwable;
}
