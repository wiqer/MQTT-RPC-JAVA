package com.wiqer.rpc.impl.core;

import com.wiqer.rpc.serialize.SuperMsgMulti;

import java.io.IOException;
import java.lang.reflect.Method;

public interface ServerHandler {
     String handle(String request, Object serviceBean, Method method) throws Throwable;

}
