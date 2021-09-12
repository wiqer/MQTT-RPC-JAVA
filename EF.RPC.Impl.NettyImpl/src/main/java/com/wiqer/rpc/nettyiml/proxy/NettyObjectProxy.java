package com.wiqer.rpc.nettyiml.proxy;

import com.wiqer.rpc.impl.proxy.ObjectProxy;
import com.wiqer.rpc.serialize.SuperMsgMulti;

import java.io.IOException;
import java.lang.reflect.Method;

public class NettyObjectProxy extends ObjectProxy {

    public NettyObjectProxy(Class clazz, String version) {
        super(clazz, version);
    }

    @Override
    protected boolean sendMsg(Object proxy, Method method, SuperMsgMulti superMsgMulti, String markName) throws IOException {
        return false;
    }
}
